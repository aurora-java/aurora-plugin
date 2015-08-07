package aurora.plugin.redis.sc;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import aurora.plugin.redis.IRedisConnectionFactory;
import redis.clients.jedis.Jedis;
import uncertain.core.ConfigurationError;
import uncertain.core.ILifeCycle;
import uncertain.data.DataChangeType;
import uncertain.data.IDataDistributor;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.ocm.AbstractLocatableObject;

public class SecondaryCacheManager extends AbstractLocatableObject implements ILifeCycle {

	IDataDistributor dataDistributor;
	IRedisConnectionFactory connFactory;
	Map<String, ISecondaryCache> cacheConfigMap;
	ThreadGroup workerThreadGroup;
	ObjectMapper mapper;

	String serverName;

	/**
	 * Set for all PKs -> cache:<table_name>:pk Key for single record -> cache:
	 * <table_name>:r:<pk_value>
	 */
	String keySeparator = ":";
	String keyPrefix = "cache";
	String recordKeyPostfix = "r";
	String notifyChannelName = "cache:notify:all";
	
	CacheManagerMode	mode = CacheManagerMode.distributor;
	
	public class FullLoadWorker implements Runnable {

		Jedis conn;
		ISecondaryCache cache;
		Exception exception;

		public FullLoadWorker(Jedis conn, ISecondaryCache cache) {
			this.conn = conn;
			this.cache = cache;
		}

		public void fullLoad(Jedis conn, ISecondaryCache cache) throws Exception {
			Set<String> pk_set;
			cache.start();
			pk_set = conn.smembers(getPkSetKey(cache.getName()));
			if (pk_set == null || pk_set.size() == 0)
				return;
			for (String pk : pk_set) {
				// String key = getRecordKey(cache.getName(), pk);
				// Map<String, String> record_map = conn.hgetAll(key);
				Object data = loadData(conn, cache, pk);
				cache.insert(pk, data);
			}
			dataDistributor.setData(cache.getName(), cache.getProcessedData());
		}

		public void run() {
			try {
				fullLoad(conn, cache);
			} catch (Exception ex) {
				fullLoadFail(this, ex);
			} finally {
				if (conn != null && conn.isConnected())
					conn.close();
			}
		}
	};

	protected SecondaryCacheManager() {
		cacheConfigMap = new HashMap<String, ISecondaryCache>();
		workerThreadGroup = new ThreadGroup("SecondaryCacheManager");
		mapper = new ObjectMapper();
	}

	public SecondaryCacheManager(IDataDistributor dataDistributor, IRedisConnectionFactory connFactory) {
		this();
		this.dataDistributor = dataDistributor;
		this.connFactory = connFactory;
	}

	public ISecondaryCache getCache(String name) {
		ISecondaryCache cache = cacheConfigMap.get(name);
		if (cache == null)
			throw new IllegalArgumentException("Cache not found:" + name);
		return cache;
	}

	protected Jedis getConnection() {
		if (serverName == null) {
			throw BuiltinExceptionFactory.createAttributeMissing(this, "serverName");
		}
		Jedis jedis = connFactory.getConnection(serverName);
		if(jedis==null)
			throw BuiltinExceptionFactory.createResourceNotFoundException(this, "redis server", serverName);
		return jedis;
	}

	public void dataChanged(String name, DataChangeType type, String primary_key) {
		ISecondaryCache cache = getCache(name);
		if (cache == null)
			return;
		Jedis jedis = this.connFactory.getConnection(serverName);
		switch (type) {
		case insert:
		case update:
			try {
				cache.refresh(primary_key, loadData(jedis, cache, primary_key));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			break;
		case delete:
			cache.delete(primary_key);
			break;
		}
		dataDistributor.setData(cache.getName(), cache.getProcessedData());
	}

	public void addCaches(Collection<ISecondaryCache> cache_list) {
		for (ISecondaryCache cache : cache_list) {
			cacheConfigMap.put(cache.getName(), cache);
		}
	}

	protected Object loadData(Jedis conn, ISecondaryCache cache, String primary_key) throws Exception {
		String name = cache.getName();
		String sf = cache.getSerializeFormat();
		Class type = cache.getRecordType();
		if ("hash".equalsIgnoreCase(sf))
			return loadDataAsMap(conn, name, primary_key);
		else if ("json".equalsIgnoreCase(sf))
			return loadDataFromJSON(conn, name, primary_key, type);
		else
			throw new ConfigurationError("Unknown serializeFormat:" + type);
	}

	protected Map<String, String> loadDataAsMap(Jedis conn, String name, String primary_key) {
		String key = getRecordKey(name, primary_key);
		Map<String, String> record_map = conn.hgetAll(key);
		return record_map;
	}

	protected Object loadDataFromJSON(Jedis conn, String name, String primary_key, Class type)
			throws JsonParseException, IOException {
		String key = getRecordKey(name, primary_key);
		String content = conn.get(key);
		if (content == null || content.length() == 0)
			return null;
		Object obj = mapper.readValue(content, type);
		return obj;
	}

	public void fullLoad() {
		if(mode!=CacheManagerMode.distributor)
			throw new IllegalStateException("Server is not in data distribution mode");
		for (ISecondaryCache cache : cacheConfigMap.values()) {
			Jedis jedis = getConnection();
			FullLoadWorker worker = new FullLoadWorker(jedis, cache);
			Thread thread = new Thread(workerThreadGroup, worker, "InitialLoad-" + cache.getName());
			thread.start();
		}
	}

	protected void saveData(Jedis conn, ISecondaryCache cache, String primary_key, Object data) 
		throws Exception
	{
		String name = cache.getName();
		String sf = cache.getSerializeFormat();
		if ("hash".equalsIgnoreCase(sf))
			saveDataAsMap(conn,name, primary_key, data);
		else if("json".equalsIgnoreCase(sf))
			saveDataAsJSON(conn,name, primary_key, data);
		else
			throw new ConfigurationError("Unknown serializeFormat:" + sf);
	}

	protected void saveDataAsMap(Jedis conn, String name, String primary_key, Object data) {
		String key = getRecordKey(name, primary_key);
		if (data instanceof Map) {
			conn.hmset(key, (Map) data);
		} else {
			Map result = (Map) mapper.convertValue(data, Map.class);
			conn.hmset(key, result);
			// ocManager.getReflectionMapper().toContainer(data, map);
		}
	}

	protected void saveDataAsJSON(Jedis conn, String name, String primary_key, Object obj)
			throws JsonParseException, IOException {
		String key = getRecordKey(name, primary_key);
		String content = mapper.writeValueAsString(obj);
		conn.set(key, content);
	}
	
	protected void notifyDataChange(Jedis conn, String name, DataChangeType type, String primary_key) {
		conn.publish(notifyChannelName, type.toString()+":"+name+":"+primary_key);
	}

	public void insert(String data_name, String primary_key, Object inserted_data) {
		ISecondaryCache cache = getCache(data_name);
		String key = getPkSetKey(data_name);
		Jedis conn = getConnection();
		conn.sadd(key, primary_key);
		try{
			saveData(conn,cache,primary_key, inserted_data);
		}catch(Exception ex){
			throw new RuntimeException("Error when inserting data",ex);
		}
		notifyDataChange(conn, data_name, DataChangeType.insert, primary_key);
	}

	public void update(String data_name, String primary_key, Object updated_data) {
		ISecondaryCache cache = getCache(data_name);
		Jedis conn = getConnection();
		try{
			saveData(conn,cache,primary_key, updated_data);
		}catch(Exception ex){
			throw new RuntimeException("Error when inserting data",ex);
		}
		notifyDataChange(conn, data_name, DataChangeType.update, primary_key);
	}

	public void delete(String data_name, String primary_key, Object deleted_data) {
		Jedis conn = getConnection();
		String pk_key = getPkSetKey(data_name);
		String record_key = getRecordKey(data_name, primary_key);
		conn.srem(pk_key,primary_key);
		conn.del(record_key);
		notifyDataChange(conn, data_name, DataChangeType.delete, primary_key);
	}

	/**
	 * @Todo log exception and retry
	 */
	protected void fullLoadFail(FullLoadWorker failed_worker, Exception ex) {
		ex.printStackTrace();
	}

	public boolean startup() {
		if(mode==CacheManagerMode.distributor){
			//subscribe to notify channel
		}
		return true;
	}

	public void shutdown() {
		workerThreadGroup.interrupt();
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getKeySeparator() {
		return keySeparator;
	}

	public void setKeySeparator(String keySeparator) {
		this.keySeparator = keySeparator;
	}

	public String getKeyPrefix() {
		return keyPrefix;
	}

	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

	public String getRecordKeyPostfix() {
		return recordKeyPostfix;
	}

	public void setRecordKeyPostfix(String recordKeyPostfix) {
		this.recordKeyPostfix = recordKeyPostfix;
	}

	public String getPkSetKey(String data_key) {
		StringBuffer buf = new StringBuffer(keyPrefix);
		buf.append(keySeparator).append(data_key).append(keySeparator).append("pk");
		return buf.toString();
	}

	public String getRecordKey(String data_key, String pk) {
		StringBuffer buf = new StringBuffer(keyPrefix);
		buf.append(keySeparator).append(data_key).append(keySeparator).append(recordKeyPostfix).append(keySeparator)
				.append(pk);
		return buf.toString();
	}

	public String getNotifyChannelName() {
		return notifyChannelName;
	}

	public void setNotifyChannelName(String notifyChannelName) {
		this.notifyChannelName = notifyChannelName;
	}

	public CacheManagerMode getMode() {
		return mode;
	}

	public void setMode(CacheManagerMode mode) {
		this.mode = mode;
	}

}

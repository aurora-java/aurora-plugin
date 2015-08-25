package aurora.plugin.redis.sc;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import aurora.plugin.redis.IRedisConnectionFactory;
import aurora.plugin.redis.ISubscriber;
import aurora.plugin.redis.RedisUtil;
import aurora.plugin.redis.job.RedisJobManager;
import redis.clients.jedis.Jedis;
import uncertain.core.ConfigurationError;
import uncertain.core.ILifeCycle;
import uncertain.data.DataChangeType;
import uncertain.data.IDataDistributor;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.ocm.AbstractLocatableObject;

public class SecondaryCacheManager extends AbstractLocatableObject
		implements ILifeCycle, ISubscriber, ISecondaryCacheManager {

	static final Class[] DEFAULT_ARG_TYPES = { Jedis.class, String.class, String.class, Object.class };
	static final Class[] DATACHANGE_ARG_TYPES = { Jedis.class, String.class, DataChangeType.class, String.class };
	static final Method ASYNC_INSERT;
	static final Method ASYNC_UPDATE;
	static final Method ASYNC_DELETE;
	static final Method ASYNC_DATACHANGED;

	static {
		try {
			ASYNC_INSERT = SecondaryCacheManager.class.getMethod("asyncInsert", DEFAULT_ARG_TYPES);
			ASYNC_UPDATE = SecondaryCacheManager.class.getMethod("asyncUpdate", DEFAULT_ARG_TYPES);
			ASYNC_DELETE = SecondaryCacheManager.class.getMethod("asyncDelete", DEFAULT_ARG_TYPES);
			ASYNC_DATACHANGED = SecondaryCacheManager.class.getMethod("asyncDataChanged", DATACHANGE_ARG_TYPES);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static final String KEY_WRITE = "write";
	public static final String KEY_READ = "read";
	IDataDistributor dataDistributor;
	IRedisConnectionFactory connFactory;
	Map<String, ISecondaryCache> cacheConfigMap;
	ThreadGroup workerThreadGroup;
	ObjectMapper mapper;
	RedisJobManager jobManager;

	String serverName;
	String subId;

	/**
	 * Set for all PKs -> cache:<table_name>:pk Key for single record -> cache:
	 * <table_name>:r:<pk_value>
	 */
	String keySeparator = ":";
	String keyPrefix = "cache";
	String recordKeyPostfix = "r";
	String notifyChannelName = "cache:notify:all";
	int readThreadCount = 5;
	int writeThreadCount = 2;

	CacheManagerMode mode = CacheManagerMode.distributor;
	String modeProperty;

	Thread subscribeThread;

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
				if (data == null)
					continue;
				// System.out.println("Loaded data:"+data.getClass()+"
				// "+data.toString());
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
		jobManager = new RedisJobManager("SecondaryCacheManage", connFactory);
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
		if (jedis == null)
			throw BuiltinExceptionFactory.createResourceNotFoundException(this, "redis server", serverName);
		return jedis;
	}

	protected String getQueueName(String cache_name, String type) {
		return type;
	}

	public void onMessage(String channel, String message) {
		DataChangeEvent evt = decodeChangeEvent(message);
		dataChanged(evt.getName(), evt.getEventType(), evt.getPrimaryKey());
	};

	public void dataChanged(String name, DataChangeType type, String primary_key) {
		String queue = this.getQueueName(name, KEY_READ);
		jobManager.submit(queue, this, ASYNC_DATACHANGED, new Object[] { name, type, primary_key });
	}

	public void asyncDataChanged(Jedis jedis, String name, DataChangeType type, String primary_key) throws Exception {
		ISecondaryCache cache = getCache(name);
		if (cache == null)
			return;
		switch (type) {

		case insert:
			cache.insert(primary_key, loadData(jedis, cache, primary_key));
			break;
		case update:
			cache.refresh(primary_key, loadData(jedis, cache, primary_key));
			break;
		case delete:
			cache.delete(primary_key);
			break;
		}
		dataDistributor.setData(cache.getName(), cache.getProcessedData());
	}

	public void addCache(ISecondaryCache cache) {
		cacheConfigMap.put(cache.getName(), cache);
	}

	public void addCaches(Collection<ISecondaryCache> cache_list) {
		for (ISecondaryCache cache : cache_list) {
			addCache(cache);
			// cacheConfigMap.put(cache.getName(), cache);
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
			throw new ConfigurationError("Unknown serializeFormat:" + sf);
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
		if (mode != CacheManagerMode.distributor)
			throw new IllegalStateException("Server is not in data distribution mode");
		for (ISecondaryCache cache : cacheConfigMap.values()) {
			Jedis jedis = getConnection();
			FullLoadWorker worker = new FullLoadWorker(jedis, cache);
			Thread thread = new Thread(workerThreadGroup, worker, "InitialLoad-" + cache.getName());
			thread.start();
		}
	}

	protected void saveData(Jedis conn, ISecondaryCache cache, String primary_key, Object data) throws Exception {
		String name = cache.getName();
		String sf = cache.getSerializeFormat();
		if ("hash".equalsIgnoreCase(sf))
			saveDataAsMap(conn, name, primary_key, data);
		else if ("json".equalsIgnoreCase(sf))
			saveDataAsJSON(conn, name, primary_key, data);
		else
			throw new ConfigurationError("Unknown serializeFormat:" + sf);
	}

	protected void saveDataAsMap(Jedis conn, String name, String primary_key, Object data) {
		String key = getRecordKey(name, primary_key);
		RedisUtil.getInstance().save(conn, key, data);
	}

	protected void saveDataAsJSON(Jedis conn, String name, String primary_key, Object obj)
			throws JsonParseException, IOException {
		String key = getRecordKey(name, primary_key);
		String content = mapper.writeValueAsString(obj);
		conn.set(key, content);
	}

	protected String encodeChangeEvent(String name, DataChangeType type, String primary_key) {
		try {
			DataChangeEvent evt = new DataChangeEvent(type, name, primary_key);
			return mapper.writeValueAsString(evt);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	protected DataChangeEvent decodeChangeEvent(String event) {
		try {
			DataChangeEvent de = (DataChangeEvent) mapper.readValue(event, DataChangeEvent.class);
			return de;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	protected void notifyDataChange(Jedis conn, String name, DataChangeType type, String primary_key) {
		conn.publish(notifyChannelName, encodeChangeEvent(name, type, primary_key));
	}

	public void insert(String data_name, String primary_key, Object inserted_data) {
		String queue = this.getQueueName(data_name, KEY_WRITE);
		jobManager.submit(queue, this, ASYNC_INSERT, new Object[] { data_name, primary_key, inserted_data });
	}

	public void asyncInsert(Jedis conn, String data_name, String primary_key, Object inserted_data) throws Exception {
		// Jedis conn = getConnection();

		ISecondaryCache cache = getCache(data_name);
		String key = getPkSetKey(data_name);

		conn.sadd(key, primary_key);
		saveData(conn, cache, primary_key, inserted_data);
		notifyDataChange(conn, data_name, DataChangeType.insert, primary_key);
	}

	public void update(String data_name, String primary_key, Object updated_data) {
		String queue = this.getQueueName(data_name, KEY_WRITE);
		jobManager.submit(queue, this, ASYNC_UPDATE, new Object[] { data_name, primary_key, updated_data });
	}

	public void asyncUpdate(Jedis conn, String data_name, String primary_key, Object updated_data) throws Exception {
		ISecondaryCache cache = getCache(data_name);
		saveData(conn, cache, primary_key, updated_data);
		notifyDataChange(conn, data_name, DataChangeType.update, primary_key);
	}

	public void delete(String data_name, String primary_key, Object deleted_data) {
		String queue = this.getQueueName(data_name, KEY_WRITE);
		jobManager.submit(queue, this, ASYNC_DELETE, new Object[] { data_name, primary_key, deleted_data });
	}

	public void asyncDelete(Jedis conn, String data_name, String primary_key, Object deleted_data) {
		// Jedis conn = getConnection();
		String pk_key = getPkSetKey(data_name);
		String record_key = getRecordKey(data_name, primary_key);
		conn.srem(pk_key, primary_key);
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
		if (serverName == null)
			throw BuiltinExceptionFactory.createAttributeMissing(this, "serverName");
		if (modeProperty != null) {
			String value = System.getProperty(modeProperty);
			if (value == null)
				throw new IllegalStateException("can't get mode from system property " + modeProperty);
			setMode(value);
		}
		if (mode == CacheManagerMode.distributor) {
			subscribeThread = new Thread() {
				public void run() {
					subId = connFactory.getSubscribeManager().addSubscriber(serverName, notifyChannelName,
							SecondaryCacheManager.this);
				}

			};
			subscribeThread.start();
		}
		jobManager.createQueue(KEY_READ, this.readThreadCount, serverName);
		jobManager.createQueue(KEY_WRITE, this.writeThreadCount, serverName);
		return true;
	}

	public void shutdown() {
		if (subId != null)
			connFactory.getSubscribeManager().removeSubscriber(subId);
		workerThreadGroup.interrupt();
		if (subscribeThread != null)
			subscribeThread.interrupt();
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

	public String getMode() {
		return mode.toString();
	}

	public void setMode(String mode) {
		this.mode = CacheManagerMode.valueOf(mode);
	}

	public void onSubscribe(String channel, int subscribedChannels) {
		fullLoad();
	};

	public void onUnsubscribe(String channel, int subscribedChannels) {
		return;
	}

	public String getModeProperty() {
		return modeProperty;
	}

	public void setModeProperty(String modeProperty) {
		this.modeProperty = modeProperty;
	}

	public int getReadThreadCount() {
		return readThreadCount;
	}

	public void setReadThreadCount(int readThreadCount) {
		this.readThreadCount = readThreadCount;
	}

	public int getWriteThreadCount() {
		return writeThreadCount;
	}

	public void setWriteThreadCount(int writeThreadCount) {
		this.writeThreadCount = writeThreadCount;
	};

}

package aurora.plugin.redis.sc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import aurora.plugin.redis.IRedisConnectionFactory;
import redis.clients.jedis.Jedis;
import uncertain.core.ILifeCycle;
import uncertain.data.DataChangeType;
import uncertain.data.IDataDistributor;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.ocm.AbstractLocatableObject;

public class SecondaryCacheManager extends AbstractLocatableObject implements ILifeCycle {
	
	IDataDistributor dataDistributor;
	IRedisConnectionFactory	connFactory;
	Map<String,ISecondaryCache>	cacheConfigMap;
    ThreadGroup workerThreadGroup;
	
	/**
	 * Set for all PKs -> cache:<table_name>:pk
	 * Key for single record -> cache:<table_name>:r:<pk_value>
	 */
	String	keySeparator = ":";
	String	keyPrefix = "cache";
	String  recordKeyPostfix = "r";
	String	serverName;
	

	
	public class FullLoadWorker implements Runnable {
		
		Jedis conn;
		ISecondaryCache cache;

		public FullLoadWorker(Jedis conn, ISecondaryCache cache) {
			this.conn = conn;
			this.cache = cache;
		}

		public void fullLoad( Jedis conn, ISecondaryCache cache ){
			Set<String> pk_set;
			pk_set = conn.smembers(getPkSetKey(cache.getName()));
			if(pk_set==null || pk_set.size()==0)
				return;
			for(String pk : pk_set){
				String key = getRecordKey(cache.getName(), pk);
				Map<String,String> record_map = conn.hgetAll(key);
				cache.insert(pk, record_map);
			}
			dataDistributor.setData(cache.getName(), cache.getProcessedData());
		}		

		public void run() {
			try{
				fullLoad(conn,cache);
			}finally{
				if(conn!=null && conn.isConnected())
					conn.close();
			}
		}		
	};
	
	protected SecondaryCacheManager(){
		cacheConfigMap = new HashMap<String,ISecondaryCache>();
		workerThreadGroup = new ThreadGroup("SecondaryCacheManager");
	}
	
	public SecondaryCacheManager(IDataDistributor dataDistributor, IRedisConnectionFactory connFactory) {
		this();
		this.dataDistributor = dataDistributor;
		this.connFactory = connFactory;
	}
	
	public String getPkSetKey(String data_key){
		StringBuffer buf = new StringBuffer(keyPrefix);
		buf.append(keySeparator).append(data_key).append(keySeparator).append("pk");
		return buf.toString();
	}
	
	public String getRecordKey(String data_key, String pk){
		StringBuffer buf = new StringBuffer(keyPrefix);
		buf.append(keySeparator).append(data_key).append(keySeparator).append(recordKeyPostfix).append(keySeparator).append(pk);
		return buf.toString();
	}
	
	public ISecondaryCache getCache(String name){
		return cacheConfigMap.get(name);
	}


	public void dataChanged( String name, DataChangeType type, String primary_key ){
		ISecondaryCache data = getCache(name);
		if(data==null)
			return;
		Jedis jedis = this.connFactory.getConnection(serverName);
		switch(type){
		case insert:
		case update:
			data.refresh(primary_key, loadData(jedis, name, primary_key));
			break;
		case delete:
			data.delete(primary_key);
			break;
		}
		dataDistributor.setData(data.getName(), data.getProcessedData());
	}	
	
	public void addCaches( Collection<ISecondaryCache> cache_list ){
		for(ISecondaryCache cache: cache_list ){
			cacheConfigMap.put(cache.getName(), cache);
		}
	}

	public Map<String,String> loadData(Jedis conn, String name, String primary_key){
		String key = getRecordKey(name, primary_key);
		Map<String,String> record_map = conn.hgetAll(key);
		return record_map;
	}
	
	public void fullLoad(){
		if(serverName==null)
			throw BuiltinExceptionFactory.createAttributeMissing(this, "serverName");
		for(ISecondaryCache cache :cacheConfigMap.values()){
			Jedis jedis = this.connFactory.getConnection(serverName);
			if(jedis==null)
				throw new RuntimeException("Server not found:"+serverName);
			FullLoadWorker worker = new FullLoadWorker(jedis, cache);
			Thread thread = new Thread(workerThreadGroup, worker, "InitialLoad-"+cache.getName());
			thread.start();
		}
	}
	
	public boolean startup() {
		return true;
	}


	public void shutdown() {
		workerThreadGroup.interrupt();
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

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

}

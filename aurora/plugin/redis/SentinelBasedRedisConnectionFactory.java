package aurora.plugin.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.Pool;
import uncertain.core.ILifeCycle;

import java.util.*;

/**
 * Created by jessen on 15/8/24.
 */
public class SentinelBasedRedisConnectionFactory implements IRedisConnectionFactory, ILifeCycle {
	List<SentinelPool> connList;
	Map<String, JedisSentinelPool> poolMap = new HashMap<String, JedisSentinelPool>();

	ISubscribeManager subscribeManager;

	public SentinelBasedRedisConnectionFactory() {
		connList = new LinkedList<SentinelPool>();
		this.subscribeManager = new SubscribeManager(this);
	}

	public void addConnections(List<SentinelPool> conns) {
		connList.addAll(conns);
	}

	public List<SentinelPool> getConnections() {
		return connList;
	}


	public Jedis getConnection(String name) {
		Pool<Jedis> pool = getJedisPool(name);
		while (true) {
			try {
				return pool.getResource();
			} catch (JedisConnectionException e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
				}
			}
		}
	}

	public ISubscribeManager getSubscribeManager(){
		return subscribeManager;
	}

	public Pool<Jedis> getJedisPool(String name) {
		Pool<Jedis> pool = poolMap.get(name);
		if (pool == null)
			throw new RuntimeException("JedisSentinelPool named [" + name + "] not found.");
		return pool;
	}


	@Override
	public boolean startup() {
		for (SentinelPool pool : connList) {
			Set<String> sentinels = new HashSet<String>();
			for (SentinelServer ss : pool.getServers()) {
				sentinels.add(ss.getHost() + ":" + ss.getPort());
			}
			GenericObjectPoolConfig config = new GenericObjectPoolConfig();
			JedisSentinelPool sentinelPool = new JedisSentinelPool(pool.getMaster(), sentinels, config, (int) pool.getConnectionTimeout());
			poolMap.put(pool.getName(), sentinelPool);

		}
		return true;
	}

	@Override
	public void shutdown() {
		for (JedisSentinelPool pool : poolMap.values()) {
			pool.close();
		}
	}
}

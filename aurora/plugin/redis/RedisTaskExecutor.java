package aurora.plugin.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.util.Pool;

/**
 * Created by jessen on 15/8/5.
 */
public class RedisTaskExecutor {
	public static <T> T  execute(Pool<Jedis> pool, RedisTask task) {

		while (true) {
			Jedis redis = getJedisConnection(pool);
			try {
				Object obj = task.runWithRedis(redis);
				return (T)obj;
			} catch (JedisConnectionException e) {
				if (!task.retryOnException()) {
					throw e;
				}
			} catch (JedisDataException e) {
				if (!task.retryOnException()) {
					throw e;
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				try {
					redis.close();
				} catch (Exception e) {
				}
			}
		}

	}

	private static Jedis getJedisConnection(Pool<Jedis> pool) {
		while (true) {
			try {
				return pool.getResource();
			} catch (JedisConnectionException e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param pool      pool to acquire jedis connection(only if the init jedis is useless and task.tryOnException() is true)
	 * @param initJedis array contains init jedis(length 1),if the init jedis is useless,new jedis connection will be acquire from pool,and set into array
	 * @param task
	 * @return
	 */
	public static <T> T execute(Pool<Jedis> pool, Jedis[] initJedis, RedisTask task) {
		while (true) {
			Jedis redis = initJedis[0];
			try {
				if (redis == null) {
					redis = initJedis[0] = getJedisConnection(pool);
				}
				Object obj = task.runWithRedis(redis);
				return (T)obj;
			} catch (JedisConnectionException e) {
				if (task.retryOnException()) {
					initJedis[0] = getJedisConnection(pool);
				} else {
					throw e;
				}
			} catch (JedisDataException e) {
				if (task.retryOnException()) {
					initJedis[0] = getJedisConnection(pool);
				} else {
					throw e;
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}

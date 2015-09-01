package aurora.plugin.redis;

import redis.clients.jedis.Jedis;

/**
 * Created by jessen on 15/8/27.
 */
public abstract class VoidRedisTask extends RedisTask {
	@Override
	public Object runWithRedis(Jedis redis) throws Exception {
		run(redis);
		return null;
	}

	public abstract void run(Jedis redis) throws  Exception;
}

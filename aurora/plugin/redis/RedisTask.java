package aurora.plugin.redis;

import aurora.database.actions.Transaction;
import redis.clients.jedis.Jedis;

/**
 * Created by jessen on 15/8/5.
 */
public abstract class RedisTask {
	public abstract Object runWithRedis(Jedis redis) throws Exception;

	protected boolean retryOnException(){
		return true;
	}
}

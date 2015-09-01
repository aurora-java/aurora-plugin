package aurora.plugin.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

/**
 * Created by jessen on 15/8/5.
 */
public abstract class RedisTaskWithTransaction extends RedisTask {
	public abstract void runWithTransaction(Transaction trans) throws Exception;

	/**
	 * @param redis
	 * @return the last result (if exists)
	 * @throws Exception
	 */
	public Object runWithRedis(Jedis redis) throws Exception {
		Transaction trans = redis.multi();
		try {
			runWithTransaction(trans);
			List<Object> results = trans.exec();
			if (results.size() == 0)
				return null;
			return results.get(results.size() - 1);
		} catch (Exception e) {
			trans.discard();
			throw e;
		}
	}
}

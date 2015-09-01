/*
 * Created on 2015年6月25日 下午10:28:10
 * $Id$
 */
package aurora.plugin.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ShardedJedis;
import redis.clients.util.Pool;

public interface IRedisConnectionFactory {
    
    public Jedis getConnection(String name);
    
    public ISubscribeManager getSubscribeManager();

	public Pool<Jedis> getJedisPool(String poolName);

}

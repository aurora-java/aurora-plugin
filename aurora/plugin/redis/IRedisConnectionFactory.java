/*
 * Created on 2015年6月25日 下午10:28:10
 * $Id$
 */
package aurora.plugin.redis;

import redis.clients.jedis.Jedis;

public interface IRedisConnectionFactory {
    
    //public Jedis getConnection();
    
    public Jedis getConnection(String name);

}

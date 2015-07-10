/*
 * Created on 2015年6月25日 下午10:31:13
 * $Id$
 */
package aurora.plugin.redis;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;

public class RedisConnectionFactoryImpl implements IRedisConnectionFactory {
    
    Map<String,JedisShardInfo>   connMap;
    List<RedisServer>         connList;

    public RedisConnectionFactoryImpl() {
        connMap = new HashMap<String,JedisShardInfo>();
    }
    
    public void addConnections(List<RedisServer> conns){
        connList = new LinkedList<RedisServer>();
        connList.addAll(conns);
        for(RedisServer server: conns){
            JedisShardInfo info = new JedisShardInfo(server.getHost(), server.getName(), server.getPort(), server.getConnectionTimeout(), server.getWeight());
            String name = server.getName();
            if(name!=null)
                connMap.put(name, info);
        }
    }
    
    public List<RedisServer> getConnections(){
        return connList;
    }
    

    
    public Jedis getConnection(String name){
        JedisShardInfo info = connMap.get(name);
        return info==null?null: new Jedis(info);
        //return new Jedis();
    }
/*
    //JedisShardInfo               defaultShardInfo;
    //String                       defaultConn;
 
    public String getDefaultConn() {
        return defaultConn;
    }

    public void setDefaultConn(String defaultConn) {
        this.defaultConn = defaultConn;
    };
    
    public Jedis getConnection(){
        return new Jedis(defaultShardInfo);
    };  
*/    
      
    

}

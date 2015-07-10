/*
 * Created on 2015年6月26日 下午9:26:46
 * $Id$
 */
package aurora.plugin.redis;

import java.net.URI;

import redis.clients.jedis.JedisShardInfo;

public class RedisServer  {
    
    JedisShardInfo  shardInfo;
    String          host = "127.0.0.1";
    int             port = 6379;
    int             db = 0;
    String          name;
    String          password;
    int             connectionTimeout = 2000;
    int             soTimeout = 2000;
    int             weight;
    
    public RedisServer(){
    }

    public String getHost() {
        return host;
    }
    
    public void setHost(String host){
        this.host = host;
    }

    public int getPort() {
        return port;
    }
    
    public void setPort(int port){
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String auth) {
        this.password = auth;
    }
    
    public void setName(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public void setDb( int db ){
        this.db = db;
    }

    public int getDb() {
        return db;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSoTimeout() {
        return soTimeout;
    }
    
    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }


    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
    

}

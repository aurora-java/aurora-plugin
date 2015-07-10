/*
 * Created on 2015年6月24日 下午7:30:36
 * $Id$
 */
package aurora.plugin.redis.pipe;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import aurora.plugin.redis.IRedisConnectionFactory;
import redis.clients.jedis.Jedis;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.ocm.IObjectRegistry;
import uncertain.pipe.base.PipeDirection;
import uncertain.pipe.impl.AdaptivePipe;

public class RedisPipe extends AdaptivePipe  {
    IObjectRegistry ios;
    IRedisConnectionFactory connFactory;
    String serverName;
    //Jedis jedisForPop, jedisForPush;
    PipeDirection direction = PipeDirection.FIFO;
    String dataType;
    String serializeFormat = "json";
    Class dataTypeClass;
    ObjectMapper jsonMapper;
    
    Jedis jedisForPop, jedisForPush;
    
    static ThreadLocal  conns = new ThreadLocal();
    
    public RedisPipe(IObjectRegistry ios) {
        jsonMapper = new ObjectMapper();
        this.ios = ios;
    }

    /*
     * public RedisPipe() { super(); jsonMapper = new ObjectMapper(); }
     */
/*
    public RedisPipe(IRedisConnectionFactory fact) {
        jsonMapper = new ObjectMapper();
        this.connFactory = fact;
    }
*/    

    protected Object toObject(String source) {
        if (dataTypeClass != null) {
            try {
                Object value = jsonMapper.readValue(source, dataTypeClass);
                return value;
            } catch (Exception ex) {
                throw new RuntimeException("Error reading JSON string", ex);
            }
        } else {
            return source;
        }
    }

    protected String toString(Object value) {
        if (dataTypeClass != null) {
            if (dataTypeClass.isAssignableFrom(value.getClass())) {
                try {
                    String json = jsonMapper.writeValueAsString(value);
                    return json;
                } catch (Exception ex) {
                    throw new RuntimeException("Error generating JSON string",
                            ex);
                }
            }
        }
        return value.toString();
    }

    @Override
    public void addData(Object data) {
        String str = toString(data);
        try {
            jedisForPush.lpush(getId(), str);
        } catch (Throwable thr) {
            thr.printStackTrace();
        }
    }
    
    @Override
    public Object take() throws InterruptedException {
        try {
            Jedis jedis = jedisForPop;
            List<String> value = direction == PipeDirection.FIFO ? jedis.brpop(
                    0, getId()) : jedis.blpop(0, getId());
            if (value.size() != 2)
                throw new RuntimeException("blpop return format error:" + value);
            String str = value.get(1);
            return toObject(str);
        } catch (Throwable thr) {
            if(jedisForPop.isConnected())
                thr.printStackTrace();
            return null;
        }
    }

    @Override
    public int size() {
        return getQueueSize();
    }

    public void start() {
        connFactory = (IRedisConnectionFactory)ios.getInstanceOfType(IRedisConnectionFactory.class);
        if(connFactory==null)
            BuiltinExceptionFactory.createInstanceDependencyNotMeetException(this, "IRedisConnectionFactory");
        if (serverName == null)
            BuiltinExceptionFactory.createAttributeMissing(this, "serverName");
        jedisForPush = connFactory.getConnection(serverName);
        jedisForPop = connFactory.getConnection(serverName);
        if(jedisForPush==null || jedisForPop==null)
            throw new RuntimeException(String.format("server %s not found",
                    serverName));
        super.start();
    }
    
    @Override
    public void shutdown() {
        super.shutdown();
        jedisForPush.close();
        jedisForPop.close();
    }

    @Override
    public int getQueueSize() {
        return 0;
        // return jedis.llen(getId()).intValue();
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) throws ClassNotFoundException {
        this.dataType = dataType;
        this.dataTypeClass = Class.forName(dataType);
    }

    public String getSerializeFormat() {
        return serializeFormat;
    }

    public void setSerializeFormat(String serializeFormat) {
        this.serializeFormat = serializeFormat;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

/*    
    @Override
    public void onThreadStart(Thread thread) {
        Jedis jpush = connFactory.getConnection(getServerName()), jpop = connFactory.getConnection(getServerName());
        if (jpop == null || jpush == null )
            throw new RuntimeException(String.format("server %s not found",
                    serverName));
        //this.jedisForPop.put(thread, jpop);
        //this.jedisForPush.put(thread, jpush);
        //System.out.println(jedisForPush);
    }
*/

}

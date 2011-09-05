/*
 * Created on 2011-6-6 ����11:51:45
 * $Id$
 */
package aurora.plugin.memcached;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.apache.commons.codec.digest.DigestUtils;

import net.spy.memcached.MemcachedClient;
import uncertain.cache.ICache;
import uncertain.logging.DummyLogger;
import uncertain.logging.ILogger;

public class MemcachedClientWrapper implements ICache {
    
    String                  mName;
    MemcachedClient         mClient;
    long                    mTimeout = 100;
    ILogger                 mLogger = DummyLogger.getInstance();
    
    /**
     * @param mClient
     */
    public MemcachedClientWrapper(String name, MemcachedClient client) {
        this.mClient = client;
        this.mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public long getTimeout() {
        return mTimeout;
    }

    public void setTimeout(long timeout) {
        this.mTimeout = timeout;
    }

    public ILogger getLogger() {
        return mLogger;
    }

    public void setLogger(ILogger logger) {
        this.mLogger = logger;
    }

    public Object getValue(Object key) {
        checkKey(key);
        key = DigestUtils.md5Hex(key.toString());
        return mClient.get(key.toString());
    }

    public boolean setValue(Object key, Object value) {
        return setValue(key,0,value);
    }
    
    private Boolean checkResult( Future<Boolean> result, String message ){
        try{
            return result.get(mTimeout, TimeUnit.MILLISECONDS);
        }catch(Exception ex){
            mLogger.log(Level.WARNING, message, ex);
            return null;
        }
    }
    
    private void checkKey( Object key ){
        if(key==null)
            throw new IllegalArgumentException("key can't be null");
    }

    public boolean setValue(Object key, int timeout, Object value) {
        checkKey(key);
        key = DigestUtils.md5Hex(key.toString());
        Future<Boolean> b = mClient.set(key.toString(),timeout,value);
        Boolean result = checkResult(b, "Error when trying to set value to memcached server");
        return result==null?false:result.booleanValue();

    }

    public void remove(Object key) {
        checkKey(key); 
        key = DigestUtils.md5Hex(key.toString());
        Future<Boolean> f = mClient.delete(key.toString());
        checkResult(f,"Error when removing key "+key+" from memcached server");

    }

    public void clear() {
        mClient.flush();
    }
    
    
    

}

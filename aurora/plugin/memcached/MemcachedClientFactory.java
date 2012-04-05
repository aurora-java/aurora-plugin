/*
 * Created on 2011-6-6 ����11:50:14
 * $Id$
 */
package aurora.plugin.memcached;

import java.io.IOException;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;
import uncertain.cache.ICache;
import uncertain.cache.ICacheReader;
import uncertain.cache.ICacheWriter;
import uncertain.cache.INamedCacheFactory;
import uncertain.core.ILifeCycle;
import uncertain.ocm.IConfigureListener;

public class MemcachedClientFactory implements INamedCacheFactory, IConfigureListener, ILifeCycle {
    
    MemcachedClient         mClient;
    String                  mServerList;
    MemcachedClientWrapper  mDefaultWrapper;
    String                  mName;
    
    int                    operationTimeout;
    boolean                addNameToKey;
    
    public MemcachedClientFactory(){


    }
    
    private MemcachedClientWrapper createWrapper(String name){
        MemcachedClientWrapper wrapper = new MemcachedClientWrapper(name, mClient);
        return wrapper;
    }

    public String getServerList() {
        return mServerList;
    }

    public void setServerList(String serverList) {
        this.mServerList = serverList;
    }

    public ICacheReader getCacheReader() {
        return mDefaultWrapper;
    }

    public ICacheWriter getCacheWriter() {
        return mDefaultWrapper;
    }

    public ICache getCache() {
        return mDefaultWrapper;
    }

    public String getName() {
        return mName;
    }
    
    public void setName(String name){
        mName = name;
    }

    public boolean isCacheEnabled(String name) {
        return true;
    }

    public ICache getNamedCache(String name) {
        return createWrapper(name);
    }

    public void endConfigure() {
        startup();
    }
    
    public boolean startup(){
        try{
            mClient = new MemcachedClient(
                    new BinaryConnectionFactory(),
                    AddrUtil.getAddresses(mServerList));
            mDefaultWrapper = createWrapper(null);
        }catch(IOException ex){
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        return true;
    }
    
    public void shutdown(){
        if(mClient!=null)
            mClient.shutdown();
        mClient = null;
        mDefaultWrapper = null;
    }

	public void setNamedCache(String name, ICache cache) {
		// not support this action
	}

}

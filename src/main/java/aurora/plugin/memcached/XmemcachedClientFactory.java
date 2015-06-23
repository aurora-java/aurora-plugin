package aurora.plugin.memcached;

import java.io.IOException;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.utils.AddrUtil;
import uncertain.cache.ICache;
import uncertain.cache.ICacheReader;
import uncertain.cache.ICacheWriter;
import uncertain.cache.INamedCacheFactory;
import uncertain.core.ILifeCycle;
import uncertain.ocm.IConfigureListener;

/**
 * Create on 2012-08-09, to support xmemcached
 * 
 * @author jessen
 * 
 */
public class XmemcachedClientFactory implements INamedCacheFactory,
		IConfigureListener, ILifeCycle {

	MemcachedClient mClient;
	String mServerList;
	MemcachedClientWrapper mDefaultWrapper;
	String mName;

	int operationTimeout;
	boolean addNameToKey;

	public XmemcachedClientFactory() {

	}

	private MemcachedClientWrapper createWrapper(String name) {
		MemcachedClientWrapper wrapper = new XmemcachedClientWrapper(name,
				mClient);
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

	public void setName(String name) {
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

	public boolean startup() {
		try {
			XMemcachedClientBuilder builder = new XMemcachedClientBuilder(
					AddrUtil.getAddresses(mServerList));
			mClient = builder.build();
			mDefaultWrapper = createWrapper(null);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		return true;
	}

	public void shutdown() {
		if (mClient != null)
			try {
				mClient.shutdown();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				mClient = null;
				mDefaultWrapper = null;
			}
	}

	public void setNamedCache(String name, ICache cache) {
		// not support this action
	}

}

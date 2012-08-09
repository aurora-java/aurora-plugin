package aurora.plugin.memcached;

import java.util.logging.Level;

import net.rubyeye.xmemcached.MemcachedClient;

import org.apache.commons.codec.digest.DigestUtils;

import uncertain.logging.DummyLogger;
import uncertain.logging.ILogger;

/**
 * Create on 2012-08-09, to support xmemcached
 * 
 * @author jessen
 * 
 */
public class XmemcachedClientWrapper extends MemcachedClientWrapper {

	String mName;
	MemcachedClient mClient;
	long mTimeout = 100;
	ILogger mLogger = DummyLogger.getInstance();

	/**
	 * @param mClient
	 */
	private XmemcachedClientWrapper(String name,
			net.spy.memcached.MemcachedClient client) {
		super(name, client);
	}

	public XmemcachedClientWrapper(String name, MemcachedClient client) {
		super(null, null);
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
		try {
			return mClient.get(key.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean setValue(Object key, Object value) {
		return setValue(key, 0, value);
	}

	private void checkKey(Object key) {
		if (key == null)
			throw new IllegalArgumentException("key can't be null");
	}

	public boolean setValue(Object key, int timeout, Object value) {
		checkKey(key);
		key = DigestUtils.md5Hex(key.toString());
		try {
			return mClient.set(key.toString(), timeout, value);
		} catch (Exception e) {
			mLogger.log(Level.WARNING,
					"Error when trying to set value to memcached server", e);
		}
		return false;
	}

	public void remove(Object key) {
		checkKey(key);
		key = DigestUtils.md5Hex(key.toString());
		try {
			mClient.delete(key.toString());
		} catch (Exception e) {
			mLogger.log(Level.WARNING, "Error when removing key " + key
					+ " from memcached server", e);
		}
	}

	public void clear() {
		try {
			mClient.flushAll();
		} catch (Exception e) {
			mLogger.log(Level.WARNING,
					"Error when do flushAll on memcached server", e);
		}
	}

}

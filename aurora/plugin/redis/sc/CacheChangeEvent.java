package aurora.plugin.redis.sc;

import uncertain.data.DataChangeType;

import java.io.Serializable;

/**
 * Created by jessen on 15/8/11.
 */
public class CacheChangeEvent implements Serializable {
	DataChangeType type;
	String cache;
	String key;

	public DataChangeType getType() {
		return type;
	}

	public void setType(DataChangeType type) {
		this.type = type;
	}

	public String getCache() {
		return cache;
	}

	public void setCache(String cache) {
		this.cache = cache;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}

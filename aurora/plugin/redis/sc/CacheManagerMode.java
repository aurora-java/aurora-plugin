package aurora.plugin.redis.sc;

public enum CacheManagerMode {
	
	/** save data to redis and notify change */
	publisher, 
	
	/** monitor data change and load data from redis, send to java classes */
	distributor

}

package aurora.plugin.redis.sc;

public interface IDataProcessor {
	
	public void start();
	
	public Object insert( String primary_key, Object data);

	public Object refresh( String primary_key, Object data );
	
	public void delete( String primary_key );
	
	public Object get(String primary_key);
	
	public Object getProcessedData();
	
}

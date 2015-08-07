package aurora.plugin.redis.sc;

public interface IDataProcessor {
	
	public void start();
	
	public void insert( String primary_key, Object data);

	public void refresh( String primary_key, Object data );
	
	public void delete( String primary_key );
	
	public Object getProcessedData();
	
}

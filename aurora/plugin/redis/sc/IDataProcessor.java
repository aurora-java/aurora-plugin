package aurora.plugin.redis.sc;

import java.util.Map;

public interface IDataProcessor {
	
	public void insert( String primary_key, Map<String,String> data);

	public void refresh( String primary_key, Map<String,String> data );
	
	public void delete( String primary_key );
	
	public Object getProcessedData();
	
}

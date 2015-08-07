package aurora.plugin.redis.sc;

public interface ISecondaryCache extends IDataProcessor {

	public String getName();
	
	public String getSerializeFormat();
	
	public Class getRecordType();

}
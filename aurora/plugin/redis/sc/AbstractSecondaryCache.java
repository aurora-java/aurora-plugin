package aurora.plugin.redis.sc;

import java.util.Map;

import uncertain.composite.CompositeMap;
import uncertain.ocm.AbstractLocatableObject;
import uncertain.ocm.OCManager;

public abstract class AbstractSecondaryCache extends AbstractLocatableObject implements ISecondaryCache {
	
	String	name;
	Class   recordType;
	
	OCManager mapper;

	protected AbstractSecondaryCache(OCManager mapper) {
		this.mapper = mapper;
	}	

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Class getRecordType() {
		return recordType;
	}
	
	public void setRecordType(Class recordType) {
		this.recordType = recordType;
	}
	
	protected Object convert(Map<String, String> input) {
		if (recordType == null) {
			return input;
		}
		try {
			Object obj = recordType.newInstance();
			CompositeMap map = new CompositeMap();
			map.putAll(input);
			mapper.populateObject(map, obj);
			return obj;
		} catch (Exception ex) {
			throw new RuntimeException("Can't create instance of " + recordType, ex);
		}
	}
	

}

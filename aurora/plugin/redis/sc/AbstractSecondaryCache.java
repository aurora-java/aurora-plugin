package aurora.plugin.redis.sc;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import uncertain.composite.CompositeMap;
import uncertain.ocm.AbstractLocatableObject;

public abstract class AbstractSecondaryCache extends AbstractLocatableObject implements ISecondaryCache {
	
	String	name;
	Class	collectionType;
	Class   recordType;
	String	serializeFormat = "hash";
	
	ObjectMapper	objMapper = new ObjectMapper();
	
	//OCManager mapper;

	/*
	protected AbstractSecondaryCache(OCManager mapper) {
		this.mapper = mapper;
	}
	*/	

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setRecordType(Class recordType) {
		this.recordType = recordType;
	}
	
	protected Object convert(Object input) {
		if (recordType == null || !(input instanceof Map)) {
			return input;
		}
		try {
			Map mdata = (Map)input;
			Object obj= objMapper.convertValue(mdata, recordType);
			return obj;
			/*
			Object obj = recordType.newInstance();
			CompositeMap map = new CompositeMap();
			map.putAll(mdata);
			mapper.populateObject(map, obj);
			return obj;
			*/
		} catch (Exception ex) {
			throw new RuntimeException("Can't create instance of " + recordType, ex);
		}
	}

	public String getSerializeFormat() {
		return serializeFormat;
	}
	
	public Class getRecordType() {
		return recordType;
	}

	public void setSerializeFormat(String serialize_format) {
		this.serializeFormat = serialize_format;
	}
	
	public abstract Class getBaseDataType();
	
	public Class getCollectionType() {
		return collectionType;
	}

	public void setCollectionType(Class type) {
		Class cls = getBaseDataType();
		if( !cls.isAssignableFrom(type))
			throw new IllegalArgumentException(type+" is incompatible with "+cls.getName());
		this.collectionType = type;
	}
	

}

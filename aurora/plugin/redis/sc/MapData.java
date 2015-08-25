package aurora.plugin.redis.sc;

import java.util.HashMap;
import java.util.Map;

import uncertain.ocm.OCManager;

public class MapData extends AbstractSecondaryCache {
	
	protected Map<String, Object> dataMap;

	/*
	public MapData(OCManager mapper) throws Exception {
		super(mapper);
		collectionType = HashMap.class;
	}
	*/
	
	public MapData(){
		super();
	}

	public Object insert(String primary_key, Object record) {
		if(dataMap.containsKey(primary_key))
			return null;
		Object value = convert(record);
		dataMap.put(primary_key, value);
		return value;
	}

	public Object refresh(String primary_key, Object record) {
		Object existing = dataMap.get(primary_key);
		if(existing!=null && existing instanceof Map){
			((Map)existing).clear();
		}
		Object value = convert(record);
		dataMap.put(primary_key, value);
		return value;
	}

	public void delete(String primary_key) {
		dataMap.remove(primary_key);
	}
	
	public Object get(String primary_key){
		return dataMap.get(primary_key);
	}

	public Object getProcessedData() {
		return dataMap;
	}
	
	@Override
	public Class getBaseDataType(){
		return Map.class;
	};


	public void start() {
		try{
			dataMap = new HashMap<String, Object>();
		}catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}


}

package aurora.plugin.redis.sc;

import java.util.HashMap;
import java.util.Map;

import uncertain.ocm.OCManager;

public class MapData extends AbstractSecondaryCache {

	Map<String, Object> dataMap;

	public MapData(OCManager mapper) {
		super(mapper);
		dataMap = new HashMap<String, Object>();
	}

	public void insert(String primary_key, Map<String, String> record) {
		if(dataMap.containsKey(primary_key))
			return;
		Object value = convert(record);
		dataMap.put(primary_key, value);
	}

	public void refresh(String primary_key, Map<String, String> record) {
		Object existing = dataMap.get(primary_key);
		if(existing!=null && existing instanceof Map){
			((Map)existing).clear();
		}
		dataMap.put(primary_key, record);
	}

	public void delete(String primary_key) {
		dataMap.remove(primary_key);
	}

	public Object getProcessedData() {
		return dataMap;
	}

}

package aurora.plugin.redis.sc;

import uncertain.data.DataChangeType;

public class DataChangeEvent {
	
	DataChangeType	eventType;
	String			name;
	String			primaryKey;
	
	public DataChangeEvent(){
		
	}
	
	public DataChangeEvent(DataChangeType eventType, String name, String primaryKey) {
		super();
		this.eventType = eventType;
		this.name = name;
		this.primaryKey = primaryKey;
	}
	
	public DataChangeType getEventType() {
		return eventType;
	}
	
	public void setEventType(DataChangeType eventType) {
		this.eventType = eventType;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPrimaryKey() {
		return primaryKey;
	}
	
	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

}

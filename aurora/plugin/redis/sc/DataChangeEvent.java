package aurora.plugin.redis.sc;

import uncertain.data.DataChangeType;

public class DataChangeEvent {
	
	DataChangeType	eventType;
	
	Object			changedData;

	public DataChangeType getEventType() {
		return eventType;
	}

	public void setEventType(DataChangeType eventType) {
		this.eventType = eventType;
	}

	public Object getChangedData() {
		return changedData;
	}

	public void setChangedData(Object changedData) {
		this.changedData = changedData;
	}

}

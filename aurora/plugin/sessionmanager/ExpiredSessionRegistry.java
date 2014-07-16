package aurora.plugin.sessionmanager;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;

import uncertain.core.ILifeCycle;
import uncertain.ocm.AbstractLocatableObject;

public class ExpiredSessionRegistry extends AbstractLocatableObject implements ILifeCycle {

	private String topic;
	private String message;
	private Long timeOut = Long.valueOf(1000*60*60*3);//3 hours

	private ConcurrentHashMap<String,Long> expiredSessionHashMap = new ConcurrentHashMap<String,Long>();
	ScheduledExecutorService service;

	public void addExpiredSession(String sessionId) {
		if (sessionId != null && !"".equals(sessionId))
			expiredSessionHashMap.put(sessionId,System.currentTimeMillis());
	}

	public void removeExpiredSession(String sessionId) {
		if (sessionId != null && !"".equals(sessionId))
			expiredSessionHashMap.remove(sessionId);
	}

	public boolean isExpiredSession(HttpSession session) {
		String sessionId = session.getId();
		return expiredSessionHashMap.keySet().contains(sessionId);
	}

	public boolean isExpiredSession(String sessionId) {
		return expiredSessionHashMap.keySet().contains(sessionId);
	}

	@Override
	public boolean startup() {
		addScheduledService();
		return true;
		
	}
	
	private void addScheduledService(){
		service = Executors.newScheduledThreadPool(1);
		service.scheduleWithFixedDelay(new RemoveSession(), 1, timeOut, TimeUnit.MILLISECONDS);
	}

	@Override
	public void shutdown() {
		expiredSessionHashMap.clear();
		service.shutdown();

	}
	
	class RemoveSession implements Runnable {
		public void run() {
			Long currentTime = System.currentTimeMillis();
			Iterator it = expiredSessionHashMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				Long putTime = (Long)entry.getValue();
				if(currentTime>putTime+timeOut){
					it.remove();
				}
			}
		}
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Long getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(Long timeOut) {
		this.timeOut = timeOut;
	}
	

}

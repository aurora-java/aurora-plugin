package aurora.plugin.redis;

import java.lang.reflect.Method;

public interface ISubscribeManager {

	String addSubscriber(String server_name, String channel, ISubscriber subscriber);

	String addSubscriber(String server_name, String channel, Object subscriber, Method method);

	boolean removeSubscriber(String id);

}
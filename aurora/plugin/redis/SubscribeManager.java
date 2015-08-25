package aurora.plugin.redis;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class SubscribeManager implements ISubscribeManager {

	IRedisConnectionFactory factory;

	int counter = 1000;
	Map<String, SubscriberInstance> subscriberMap = new ConcurrentHashMap<String, SubscriberInstance>();

	protected synchronized String getNextId() {
		counter++;
		return Integer.toString(counter);
	}

	public SubscribeManager(IRedisConnectionFactory factory) {
		super();
		this.factory = factory;
	}

	public SubscriberInstance createSubscriber(Jedis jedis, Object instance, Method method) {
		SubscriberInstance inst = new SubscriberInstance(instance, method);
		inst.id = getNextId();
		inst.connection = jedis;
		return inst;
	}

	public SubscriberInstance createSubscriber(Jedis jedis, ISubscriber subscriber) {
		SubscriberInstance inst = new SubscriberInstance(subscriber);
		inst.id = getNextId();
		inst.connection = jedis;
		return inst;
	}

	public class SubscriberInstance extends JedisPubSub {

		String id;
		Object instance;
		Method method;
		ISubscriber subscriber;
		Jedis connection;

		protected SubscriberInstance(ISubscriber subscriber) {
			this.subscriber = subscriber;
		}

		protected SubscriberInstance(Object instance, Method method) {
			this.instance = instance;
			this.method = method;
			Class[] argtypes = method.getParameterTypes();
			boolean valid = false;
			if (argtypes.length == 2) {
				if (String.class.isAssignableFrom(argtypes[0]) && String.class.isAssignableFrom(argtypes[1]))
					valid = true;
			}
			if (!valid)
				throw new IllegalArgumentException("Invalid method:" + method.getName()
						+ " subscribe handle method must be taking arguments of (String,String)");

		}

		public void stop() {
			System.out.println(this + " stop called");
			super.unsubscribe();
			connection.close();
		}

		@Override
		public void onMessage(String channel, String message) {
			try {
				if (subscriber != null)
					subscriber.onMessage(channel, message);
				else if (method != null && instance != null)
					try {
						method.invoke(instance, new Object[] { channel, message });
					} catch (Exception ex) {
						throw new RuntimeException(ex.getCause());
					}
				else
					throw new IllegalStateException();
			} catch (Throwable ex) {
				ex.printStackTrace(System.err);
			}

		}

		@Override
		public void onSubscribe(String channel, int subscribedChannels) {
			if (subscriber != null)
				subscriber.onSubscribe(channel, subscribedChannels);
		}

		@Override
		public void onUnsubscribe(String channel, int subscribedChannels) {
			if (subscriber != null)
				subscriber.onUnsubscribe(channel, subscribedChannels);
		}

	}

	protected Jedis getConnection(String name) {
		Jedis jedis = factory.getConnection(name);
		return jedis;
	}

	public String addSubscriber(String server_name, String channel, ISubscriber subscriber) {
		Jedis jedis = getConnection(server_name);
		SubscriberInstance inst = this.createSubscriber(jedis, subscriber);
		jedis.subscribe(inst, channel);
		return inst.id;
	}

	public String addSubscriber(String server_name, String channel, Object subscriber, Method method) {
		Jedis jedis = getConnection(server_name);
		SubscriberInstance inst = this.createSubscriber(jedis, subscriber, method);
		jedis.subscribe(inst, channel);
		return inst.id;
	}

	public boolean removeSubscriber(String id) {
		System.out.println(this + " remove called");
		SubscriberInstance inst = subscriberMap.get(id);
		if (inst == null)
			return false;
		inst.stop();
		subscriberMap.remove(id);
		return true;
	}

	public void shutdown() {
		for (SubscriberInstance inst : subscriberMap.values()) {
			inst.stop();
		}
	}

}

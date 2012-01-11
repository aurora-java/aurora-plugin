package aurora.plugin.jms;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IConfigurable;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;

public class JmsMessageDispatch extends AbstractEntry implements IConfigurable {
	public static final String MESSAGE_ATTR = "message";
	public static final String TOPIC_ATTR = "topic";
	public static String key;
	private String message;
	private String topic;
	private IObjectRegistry registry;
	private Map<String, String> property;

	static {
		key = getRandomString(32);
	}

	public JmsMessageDispatch(IObjectRegistry registry) {
		this.registry = registry;
	}

	/*
	 * John 添加 加载cofig 到全局
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void beginConfigure(CompositeMap config) {
		CompositeMap parameters = config.getChild("parameters");
		if (parameters != null) {
			List<CompositeMap> params = parameters.getChilds();
			for (CompositeMap param : params) {
				config.put(param.get("key"), param.get("value"));
			}
		}
		this.property = config;
		config.put("key", key);

		super.beginConfigure(config);
	}

	/**
	 * 参数随机数key值
	 * 
	 * @param length
	 * @return
	 */
	public static String getRandomString(int length) {
		StringBuffer buffer = new StringBuffer(
				"0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
		StringBuffer sb = new StringBuffer();
		Random r = new Random();
		int range = buffer.length();
		for (int i = 0; i < length; i++) {
			sb.append(buffer.charAt(r.nextInt(range)));
		}
		return sb.toString();
	}

	@Override
	public void run(ProcedureRunner runner) throws Exception {
		if (message == null)
			BuiltinExceptionFactory.createAttributeMissing(this, MESSAGE_ATTR);
		if (topic == null)
			BuiltinExceptionFactory.createAttributeMissing(this, TOPIC_ATTR);
		ILogger logger = LoggingContext.getLogger(runner.getContext(),
				JMSUtil.PLUGIN);
		ConnectionFactory connectionFactory = (ConnectionFactory) registry
				.getInstanceOfType(ConnectionFactory.class);
		if (connectionFactory == null) {
			throw BuiltinExceptionFactory.createInstanceNotFoundException(this,
					ConnectionFactory.class);
		}
		Connection connection = null;
		Session session = null;
		MessageProducer messageProducer = null;
		try {
			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			logger.log(Level.CONFIG, "create createTopic {0}",
					new Object[] { topic });
			Topic jmsTopic = session.createTopic(topic);
			messageProducer = session.createProducer(jmsTopic);
			logger.log(Level.CONFIG, "start producer connection");
			connection.start();
			logger.log(Level.CONFIG, "start producer successfull!");
			String real_message = TextParser
					.parse(message, runner.getContext());
			Message message = session.createTextMessage(real_message);

			/*
			 * John 添加 将全员信息放入到 message中去
			 */
			Set<Entry<String, String>> set1 = property.entrySet();

			for (Entry<String, String> en : set1) {
				message.setStringProperty(en.getKey(), TextParser.parse(en
						.getValue(), runner.getContext()));
			}
			/*
			 * end 添加
			 */

			messageProducer.send(message);
			logger.log(Level.CONFIG, "Message:{0} sent",
					new Object[] { real_message });
		} finally {
			JMSUtil.freeMessageProducer(messageProducer);
			JMSUtil.freeJMSSession(session);
			JMSUtil.freeJMSConnection(connection);
		}
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String event) {
		this.message = event;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Map<String, String> getProperty() {
		return property;
	}

	public void setProperty(Map<String, String> property) {
		this.property = property;
	}

}

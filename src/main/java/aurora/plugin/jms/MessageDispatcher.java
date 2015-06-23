package aurora.plugin.jms;

import java.util.Map.Entry;
import java.util.Set;
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
import uncertain.ocm.AbstractLocatableObject;
import uncertain.ocm.IObjectRegistry;
import aurora.application.features.msg.IMessage;
import aurora.application.features.msg.IMessageDispatcher;
import aurora.application.features.msg.IMessageStub;

public class MessageDispatcher extends AbstractLocatableObject implements IMessageDispatcher {

	private IObjectRegistry mRegistry;

	public MessageDispatcher(IObjectRegistry registry) {
		this.mRegistry = registry;
	}

	public void send(String topic, IMessage message, CompositeMap context) throws Exception {
		if (topic == null)
			BuiltinExceptionFactory.createAttributeMissing(this, "topic");
		ILogger logger = LoggingContext.getLogger(context, JMSUtil.PLUGIN);
		IMessageStub messageStub = (IMessageStub) mRegistry.getInstanceOfType(IMessageStub.class);
		if (messageStub == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(null, IMessageStub.class, this.getClass().getCanonicalName());
		if (!messageStub.isStarted()) {
			throw new IllegalStateException("Message Provider is not started, please check the configuration.");
		}
		ConnectionFactory connectionFactory = (ConnectionFactory) mRegistry.getInstanceOfType(ConnectionFactory.class);
		if (connectionFactory == null) {
			throw BuiltinExceptionFactory.createInstanceNotFoundException(this, ConnectionFactory.class);
		}
		Connection connection = null;
		Session session = null;
		MessageProducer messageProducer = null;
		try {
			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			logger.log(Level.CONFIG, "create createTopic {0}", new Object[] { topic });
			Topic jmsTopic = session.createTopic(topic);
			messageProducer = session.createProducer(jmsTopic);
			logger.log(Level.CONFIG, "start producer connection");
			connection.start();
			logger.log(Level.CONFIG, "start producer successfull!");
			String parsedText = TextParser.parse(message.getText(), context);
			Message textMessage = session.createTextMessage(parsedText);

			CompositeMap properties = message.getProperties();
			if (properties != null && !properties.isEmpty()) {
				Set<Entry<Object, Object>> set = properties.entrySet();
				for (Entry<Object, Object> en : set) {
					textMessage.setObjectProperty(en.getKey().toString(), TextParser.parse(en.getValue().toString(), context));
				}
			}
			messageProducer.send(textMessage);
			logger.log(Level.CONFIG, "Message:{0} sent", new Object[] { parsedText });
		} finally {
			JMSUtil.freeMessageProducer(messageProducer);
			JMSUtil.freeJMSSession(session);
			JMSUtil.freeJMSConnection(connection);
		}

	}

}

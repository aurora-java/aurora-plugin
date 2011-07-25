package aurora.plugin.amq;

import java.util.logging.Level;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import uncertain.composite.TextParser;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IConfigurable;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;

public class JmsMessageDispatch extends AbstractEntry implements IConfigurable{
	public static final String EVENT_ATTR = "event";
	public static final String TOPIC_ATTR = "topic";
	private String event;
	private String topic;
	private IObjectRegistry registry;
	public JmsMessageDispatch(IObjectRegistry registry) {
		this.registry = registry;
	}
	
	@Override
	public void run(ProcedureRunner runner) throws Exception {
		event = TextParser.parse(event, runner.getContext());
	    if(event==null)
	        BuiltinExceptionFactory.createAttributeMissing(this, EVENT_ATTR);
	    topic = TextParser.parse(topic, runner.getContext());
	    if(topic==null)
	        BuiltinExceptionFactory.createAttributeMissing(this, TOPIC_ATTR);
	    ILogger logger = LoggingContext.getLogger(runner.getContext(), AMQClientInstance.PLUGIN);
	    ActiveMQConnectionFactory connectionFactory = (ActiveMQConnectionFactory)registry.getInstanceOfType(ActiveMQConnectionFactory.class);
	    Connection connection = null;
	    Session session = null;
	    MessageProducer messageProducer = null;
	    try{
		    connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			logger.log(Level.CONFIG,"create createTopic {0}", new Object[]{topic});
			Topic jsmTopic = session.createTopic(topic);
			messageProducer = session.createProducer(jsmTopic);
			logger.log(Level.CONFIG,"start producer connection");
			connection.start();
			logger.log(Level.CONFIG,"start producer successfull!");
			String message = TextParser.parse(event, runner.getContext());
			messageProducer.send(session.createTextMessage(message));	
	        logger.log(Level.CONFIG, "Message:{0} sent", new Object[]{message});
	    }finally{
	       JMSUtil.freeMessageProducer(messageProducer);
	       JMSUtil.freeJMSSession(session);
	       JMSUtil.freeJMSConnection(connection);
	    }
	}
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
}

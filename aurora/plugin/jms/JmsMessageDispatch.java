package aurora.plugin.jms;

import java.util.logging.Level;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import uncertain.composite.TextParser;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IConfigurable;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;

public class JmsMessageDispatch extends AbstractEntry implements IConfigurable{
	public static final String MESSAGE_ATTR = "message";
	public static final String TOPIC_ATTR = "topic";
	private String message;
	private String topic;
	private IObjectRegistry registry;
	public JmsMessageDispatch(IObjectRegistry registry) {
		this.registry = registry;
	}
	
	@Override
	public void run(ProcedureRunner runner) throws Exception {
	    if(message==null )
	        BuiltinExceptionFactory.createAttributeMissing(this, MESSAGE_ATTR);
	    if(topic==null)
	        BuiltinExceptionFactory.createAttributeMissing(this, TOPIC_ATTR);
	    ILogger logger = LoggingContext.getLogger(runner.getContext(), JMSUtil.PLUGIN);
	    ConnectionFactory connectionFactory = (ConnectionFactory)registry.getInstanceOfType(ConnectionFactory.class);
	    if(connectionFactory==null){
	        throw BuiltinExceptionFactory.createInstanceNotFoundException(this, ConnectionFactory.class);
	    }
	    Connection connection = null;
	    Session session = null;
	    MessageProducer messageProducer = null;
	    try{
		    connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			logger.log(Level.CONFIG,"create createTopic {0}", new Object[]{topic});
			Topic jmsTopic = session.createTopic(topic);
			messageProducer = session.createProducer(jmsTopic);
			logger.log(Level.CONFIG,"start producer connection");
			connection.start();
			logger.log(Level.CONFIG,"start producer successfull!");
			String real_message = TextParser.parse(message, runner.getContext());
			messageProducer.send(session.createTextMessage(real_message));	
	        logger.log(Level.CONFIG, "Message:{0} sent", new Object[]{real_message});
	    }finally{
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
}

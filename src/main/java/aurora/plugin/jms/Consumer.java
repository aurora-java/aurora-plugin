package aurora.plugin.jms;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import uncertain.exception.BuiltinExceptionFactory;
import uncertain.exception.ConfigurationFileException;
import uncertain.exception.GeneralException;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.AbstractLocatableObject;
import uncertain.ocm.IObjectRegistry;
import aurora.application.features.msg.Event;
import aurora.application.features.msg.IConsumer;
import aurora.application.features.msg.IMessage;
import aurora.application.features.msg.IMessageHandler;
import aurora.application.features.msg.IMessageStub;
import aurora.application.features.msg.MessageCodes;

public class Consumer extends AbstractLocatableObject implements MessageListener,ExceptionListener,IConsumer {
	
	private IObjectRegistry registry;
	
	private String topic;
	private String client;
	private Event[] events;
	
	private Map<String,String> eventMap = new HashMap<String,String>(); 
	private ILogger logger;
	private Session session;
	private Connection connection;
	private MessageConsumer messageConsumer;	
	private JMSStub jmsStub;
	
    public Consumer(IObjectRegistry registry) {
        this.registry = registry;
    }
	public void init(IMessageStub stub) throws Exception {
		if(!(stub instanceof JMSStub)){
			throw new IllegalArgumentException("The IMessageStub is not IJMSMessageStub!");
		}
		jmsStub =(JMSStub)stub;
		
		if(topic ==null){
			throw BuiltinExceptionFactory.createAttributeMissing(this, "topic");
		}
		logger = LoggingContext.getLogger(JMSUtil.PLUGIN, registry);
		logger.log(Level.CONFIG,"init Consumer");
		connection = jmsStub.createConnection();
		connection.setExceptionListener(this);
		if(client == null){
			client = getAutoClient(topic);
		}
		connection.setClientID(client);
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Topic jmsTopic = session.createTopic(topic);
		logger.log(Level.CONFIG,"create Topic:{0}",new Object[]{topic});
		messageConsumer = session.createDurableSubscriber(jmsTopic, topic);
		messageConsumer.setMessageListener(this);
		connection.start();
		logger.log(Level.CONFIG,"start Consumer successfull!");
	}
	public void onShutdown(){
		JMSUtil.freeMessageConsumer(messageConsumer);
		JMSUtil.freeJMSSession(session);
		JMSUtil.freeJMSConnection(connection);
	}
	public void onMessage(Message message) {
		if(!(message instanceof TextMessage)){
			throw new GeneralException(MessageCodes.MESSAGE_TYPE_ERROR, new Object[]{TextMessage.class.getName(),message.getClass().getCanonicalName()}, this);
		}
		TextMessage textMessage = (TextMessage)message;
		String messageText = null;
		try {
			messageText = (textMessage).getText();
		} catch (JMSException e) {
			throw new GeneralException(MessageCodes.JMSEXCEPTION_ERROR, new Object[]{e.getMessage()}, e);
		}
		String handlerName = (String)eventMap.get(messageText);
		if(handlerName != null){
			IMessageHandler handler = (IMessageHandler)jmsStub.getMessageHandler(handlerName);
			if(handler == null){
				ConfigurationFileException ex = new ConfigurationFileException(MessageCodes.HANDLER_NOT_FOUND_ERROR, new Object[]{handlerName}, this);
				logger.log(Level.SEVERE,"Error when handle jsm message", ex);
				throw ex;
				
			}
			handler.onMessage(new JMSMessage(textMessage));
		}
		
	}
	public void onMessage(IMessage msg){
		throw new IllegalArgumentException("This method will never be called is this class!");
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public void setSession(Session session) {
		this.session = session;
	}
	public Session getSession() {
		return this.session;
	}
	public void setEvents(Event[] events) {
		this.events = events;
		if(events != null){
			for (int i = 0; i < events.length; i++) {
				Event event = events[i];
				if(event.getHandler() != null)
					eventMap.put(event.getMessage(), event.getHandler());
			}
		}
	}
	public Event[] getEvents() {
		return events;
	}
	public String getClient() {
		return client;
	}
	public void setClient(String client) {
		this.client = client;
	}
	public String getAutoClient(String topic){
		return Calendar.getInstance().getTimeInMillis()+(topic!= null?topic:"");
	}

	public void onException(JMSException paramJMSException) {
		paramJMSException.printStackTrace();
		logger.log(Level.SEVERE,"JMSException:",paramJMSException);
	}
}

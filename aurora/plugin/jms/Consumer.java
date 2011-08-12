package aurora.plugin.jms;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import uncertain.composite.CompositeMap;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.exception.ConfigurationFileException;
import uncertain.exception.GeneralException;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IConfigurable;
import uncertain.ocm.IObjectRegistry;
import uncertain.util.resource.ILocatable;

public class Consumer implements IConfigurable, MessageListener,ExceptionListener {
	private String topic;
	private String client;
	private CompositeMap config;
	private Session session;
	private Connection connection;
	private MessageConsumer messageConsumer;
	private Event[] events;
	private Map eventMap = new HashMap(); 
	private ILogger logger;
	private IObjectRegistry registry;
	private Map mHandlersMap;
    public Consumer(IObjectRegistry registry) {
        this.registry = registry;
    }

	
	public void init(ConnectionFactory factory,Map handlersMap) throws Exception {
		if(factory == null){
			throw new IllegalArgumentException("ConnectionFactory can not be null");
		}
		if(handlersMap == null)
			handlersMap = new HashMap();
		this.mHandlersMap = handlersMap;
		if(topic ==null){
			throw BuiltinExceptionFactory.createAttributeMissing(config.asLocatable(), "topic");
		}
		logger = LoggingContext.getLogger(JMSUtil.PLUGIN, registry);
		logger.log(Level.CONFIG,"init Consumer");
		connection = factory.createConnection();
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
		for (int i = 0; i < events.length; i++) {
			Event event = events[i];
			if(event.getHandler() != null)
				eventMap.put(event.getMessage(), event.getHandler());
		}
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
			ILocatable locatable = null;
			throw new GeneralException(MessageCodes.MESSAGE_TYPE_ERROR, new Object[]{TextMessage.class.getName(),message.getClass().getCanonicalName()}, locatable);
		}
		String messageText = null;
		try {
			messageText = ((TextMessage)message).getText();
		} catch (JMSException e) {
			throw new GeneralException(MessageCodes.JMSEXCEPTION_ERROR, new Object[]{e.getMessage()}, e);
		}
		String handlerName = (String)eventMap.get(messageText);
		if(handlerName != null){
			IMessageHandler handler = (IMessageHandler)mHandlersMap.get(handlerName);
			if(handler == null){
				ConfigurationFileException ex = new ConfigurationFileException(MessageCodes.HANDLER_NOT_FOUND_ERROR, new Object[]{handlerName}, config.asLocatable());
				logger.log(Level.SEVERE,"Error when handle jsm message", ex);
				throw ex;
				
			}
			handler.onMessage(message);
		}
		
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
	public void beginConfigure(CompositeMap config) {
		this.config = config;
	}
	public void endConfigure() {
	}


	public void onException(JMSException paramJMSException) {
		paramJMSException.printStackTrace();
		logger.log(Level.SEVERE,"JMSException:",paramJMSException);
		
	}
}

package aurora.plugin.jms;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

import aurora.application.features.msg.IMessage;
import aurora.application.features.msg.IMessageListener;
import aurora.application.features.msg.IMessageStub;
import aurora.application.features.msg.INoticerConsumer;
import aurora.application.features.msg.MessageCodes;

import uncertain.exception.BuiltinExceptionFactory;
import uncertain.exception.GeneralException;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.AbstractLocatableObject;
import uncertain.ocm.IObjectRegistry;
import uncertain.util.resource.ILocatable;

public class DefaultNoticeConsumer extends AbstractLocatableObject implements INoticerConsumer,MessageListener,ExceptionListener {
	private String topic;
	private String client;
	private Session session;
	private Connection connection;
	private MessageConsumer messageConsumer;
	private Map<String,List<IMessageListener>> messageListeners = new HashMap<String,List<IMessageListener>>(); 
	private ILogger logger;
	private IObjectRegistry registry;
    public DefaultNoticeConsumer(IObjectRegistry registry) {
        this.registry = registry;
    }

	
	public void init(IMessageStub stub) throws Exception {
		if(!(stub instanceof JMSStub)){
			throw new IllegalArgumentException("The IMessageStub is not IJMSMessageStub!");
		}
		JMSStub jmsStub =(JMSStub)stub;
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
			ILocatable locatable = null;
			throw new GeneralException(MessageCodes.MESSAGE_TYPE_ERROR, new Object[]{TextMessage.class.getName(),message.getClass().getCanonicalName()}, locatable);
		}
		String messageText = null;
		try {
			messageText = ((TextMessage)message).getText();
		} catch (JMSException e) {
			throw new GeneralException(MessageCodes.JMSEXCEPTION_ERROR, new Object[]{e.getMessage()}, e);
		}
		List<IMessageListener> listeners = messageListeners.get(messageText);
		if(listeners != null){
			for(IMessageListener l:listeners){
				try {
					l.onMessage(new JMSMessage((TextMessage)message));
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Listener:"+l.toString()+" occur exception.", e);
					throw new RuntimeException("Listener:"+l.toString()+" occur exception.",e);
				}
			}
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


	public void addListener(String message, IMessageListener listener) {
		List<IMessageListener> listeners = messageListeners.get(message);
		if(listeners == null){
			listeners = new LinkedList<IMessageListener>();
			messageListeners.put(message, listeners);
		}
		if(!listeners.contains(listener))
			listeners.add(listener);
	}


	public void removeListener(String message, IMessageListener listener) {
		List<IMessageListener> listeners = messageListeners.get(message);
		if(listeners == null){
			return;
		}
		listeners.remove(listener);
		
	}
}

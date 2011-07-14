package aurora.plugin.amq;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import uncertain.composite.CompositeMap;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.exception.GeneralException;
import uncertain.ocm.IConfigurable;
import uncertain.util.resource.ILocatable;

public class Consumer implements IConfigurable, MessageListener {
	private String Url;
	private String Topic;
	private String Client;
	private CompositeMap config;
	private Session session;
	private MessageConsumer messageConsumer;
	private Event[] events;
	private MessageHandler[] messageHandlers;
	private Map handlersMap = new HashMap(); 
	private Map eventMap = new HashMap(); 
	private AMQClientInstance amqClient;

	public void init(AMQClientInstance amqClient) throws Exception {
		amqClient.getILogger().log("init Consumer");
		this.amqClient = amqClient;
		if(Url ==null){
			throw BuiltinExceptionFactory.createAttributeMissing(config.asLocatable(), "url");
		}
		amqClient.getILogger().log("create ConnectionFactory with Url "+Url);
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(Url);
		Connection connection = factory.createConnection();
		connection.setClientID(Client);
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		if(Topic ==null){
			throw BuiltinExceptionFactory.createAttributeMissing(config.asLocatable(), "topic");
		}
		amqClient.getILogger().log("create Topic "+Topic);
		Topic topic = session.createTopic(Topic);
		if(Client ==null){
			throw BuiltinExceptionFactory.createAttributeMissing(config.asLocatable(), "client");
		}
		messageConsumer = session.createDurableSubscriber(topic, Topic);
		messageConsumer.setMessageListener(this);
		for (int i = 0; i < events.length; i++) {
			Event event = events[i];
			Object messageHandler = handlersMap.get(event.getHandler());
			if(messageHandler == null){
				throw new RuntimeException("test");
			}
			eventMap.put(event.getMessage(), messageHandler);
		}
		amqClient.getILogger().log("start Consumer connection");
		connection.start();
		amqClient.getILogger().log("start Consumer successfull!");
	}
	public void onMessage(Message message) {
		if(!(message instanceof TextMessage)){
			ILocatable locatable = null;
			throw new GeneralException(MessageCodes.MESSAGE_TYPE_ERROR, new Object[]{TextMessage.class.getName(),message.getClass().getName()}, locatable);
		}
		String messageText = null;
		try {
			messageText = ((TextMessage)message).getText();
		} catch (JMSException e) {
			throw new GeneralException(MessageCodes.JMSEXCEPTION_ERROR, new Object[]{e.getMessage()}, e);
		}
		MessageHandler handler = (MessageHandler)eventMap.get(messageText);
		if(handler == null){
			ILocatable locatable = null;
			throw new GeneralException(MessageCodes.MESSAGE_TYPE_ERROR, new Object[]{messageText}, locatable);
		}
		handler.onMessage(amqClient,message);
	}
	public String getUrl() {
		return Url;
	}
	public void setUrl(String url) {
		Url = url;
	}
	public String getTopic() {
		return Topic;
	}
	public void setTopic(String topic) {
		Topic = topic;
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
		return Client;
	}
	public void setClient(String client) {
		Client = client;
	}
	public void setMessageConsumer(MessageConsumer messageProducer) {
		this.messageConsumer = messageProducer;;
	}
	public MessageConsumer getMessageConsumer() {
		return messageConsumer;
	}
	public MessageHandler[] getMessageHandlers() {
		return messageHandlers;
	}
	public void setMessageHandlers(MessageHandler[] messageHandlers) {
		this.messageHandlers = messageHandlers;
		for(int i= 0;i<messageHandlers.length;i++){
			handlersMap.put(messageHandlers[i].getName(), messageHandlers[i]);
		}
	}
	public void beginConfigure(CompositeMap config) {
		this.config = config;
	}
	public void endConfigure() {
	}
}

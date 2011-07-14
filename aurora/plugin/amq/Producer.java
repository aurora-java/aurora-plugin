package aurora.plugin.amq;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import uncertain.composite.CompositeMap;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.ocm.IConfigurable;

public class Producer implements IConfigurable {
	private String Url;
	private String Topic;
	private CompositeMap config;
	private Session session;
	private MessageProducer messageProducer;
	private AMQClientInstance amqClient;

	public void init(AMQClientInstance amqClient) throws Exception {
		amqClient.getILogger().log("init Producer");
		this.amqClient = amqClient;
		if(Url ==null){
			throw BuiltinExceptionFactory.createAttributeMissing(config.asLocatable(), "url");
		}
		amqClient.getILogger().log("create ConnectionFactory with Url " + Url);
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(Url);
		Connection connection = factory.createConnection();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		if(Topic ==null){
			throw BuiltinExceptionFactory.createAttributeMissing(config.asLocatable(), "topic");
		}
		amqClient.getILogger().log("create createTopic " + Topic);
		Topic topic = session.createTopic(Topic);
		messageProducer = session.createProducer(topic);
		amqClient.getILogger().log("start producer connection");
		connection.start();
		amqClient.getILogger().log("start producer successfull!");
	}
	public void sendTextMessage(String message) throws JMSException {
		messageProducer.send(session.createTextMessage(message));
	}
	public void sendMessage(Message message) throws JMSException {
		messageProducer.send(message);
	}
	public void setSession(Session session) {
		this.session = session;
	}
	public Session getSession() {
		return this.session;
	}
	public void setMessageProducer(MessageProducer messageProducer) {
		this.messageProducer = messageProducer;;
	}
	public MessageProducer getMessageProducer() {
		return messageProducer;
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
	public void beginConfigure(CompositeMap config) {
		this.config = config;
	}
	public void endConfigure() {
	}

}

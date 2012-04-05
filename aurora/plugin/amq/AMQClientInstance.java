package aurora.plugin.amq;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.activemq.ActiveMQConnectionFactory;

import uncertain.composite.CompositeMap;
import uncertain.core.ILifeCycle;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.exception.MessageFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.AbstractLocatableObject;
import uncertain.ocm.IObjectRegistry;
import aurora.application.features.msg.IConsumer;
import aurora.application.features.msg.IMessage;
import aurora.application.features.msg.IMessageDispatcher;
import aurora.application.features.msg.IMessageHandler;
import aurora.application.features.msg.IMessageStub;
import aurora.plugin.jms.JMSStub;

public class AMQClientInstance extends AbstractLocatableObject implements ILifeCycle,JMSStub {
	/**
	 * 配置样本
	<?xml version="1.0" encoding="UTF-8"?>
	<amq:AMQ-client-instance xmlns:amq="aurora.plugin.amq" xmlns:jms="aurora.plugin.jms" url="failover:tcp://localhost:61616">
	    <jms:messageHandlers>
	        <jms:defaultMessageHandler name="handler1" procedure="init.load_priviledge_check_data"/>
	    </jms:messageHandlers>
	    <jms:consumers >
	        <amq:consumer topic="test1">
	            <amq:events>
	                <amq:event handler="handler1" message="resource_update"/>
	            </amq:events>
	        </amq:consumer>
	    </amq:consumers>
	</amq:AMQ-client-instance>
	 * 
	 */
	public static final String PLUGIN = "aurora.plugin.amq";
	private IMessageDispatcher[] mMessageDispatchers;
	private IMessageHandler[] mMessageHandlers;
	private IConsumer[] consumers;
	private String url;
	
	private IObjectRegistry registry;
	public ILogger logger;
	private Map<String,IMessageHandler> handlersMap = new HashMap<String,IMessageHandler>();
	private Map<String,IMessageDispatcher> dispatchersMap = new HashMap<String,IMessageDispatcher>();
	private ActiveMQConnectionFactory factory;
	private Map<String,IConsumer> consumerMap;
	private boolean inited = false;
	
	public AMQClientInstance(IObjectRegistry registry) {
		this.registry = registry;
	}
	
	public boolean startup() {
		if(inited)
			return true;
		logger = LoggingContext.getLogger(PLUGIN, registry);
		MessageFactory.loadResource("resources.aurora_plugin_amq");
		if(url == null){
			BuiltinExceptionFactory.createOneAttributeMissing(this, "url");
		}
		factory = new ActiveMQConnectionFactory(url);
		// javax.jms.QueueConnectionFactory, javax.jms.TopicConnectionFactory
		registry.registerInstance(ConnectionFactory.class, factory);
		consumerMap = new HashMap();
		//init consumer config
		if(consumers != null){
			for(int i= 0;i<consumers.length;i++){
				consumerMap.put(consumers[i].getTopic(), consumers[i]);
			}
		}
		(new Thread(){
			public void run(){
				if(consumers != null){
					for(int i= 0;i<consumers.length;i++){
						try {
							consumers[i].init(AMQClientInstance.this);
						} catch (Exception e) {
							logger.log(Level.SEVERE,"init jms consumers failed!",e);
						}
					}
				}
			}
		}).start();
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				try {
					onShutdown();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		registry.registerInstance(IMessageStub.class, this);
		inited = true;
		return true;
	}
	public void onShutdown() throws Exception{
		if(consumers != null){
			for(int i= 0;i<consumers.length;i++){
				consumers[i].onShutdown();
			}
		}
	}
	public IMessageHandler getMessageHandler(String name){
		return (IMessageHandler)handlersMap.get(name);
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public IMessageHandler[] getMessageHandlers() {
		return mMessageHandlers;
	}
	public void setMessageHandlers(IMessageHandler[] messageHandlers) {
		this.mMessageHandlers = messageHandlers;
		for(int i= 0;i<messageHandlers.length;i++){
			handlersMap.put(messageHandlers[i].getName(), messageHandlers[i]);
		}
	}
	public IMessageDispatcher[] getMessageDispatchers() {
		return mMessageDispatchers;
	}
	public void setMessageDispatchers(IMessageDispatcher[] messageDispatchers) {
		this.mMessageDispatchers = messageDispatchers;
		for(int i= 0;i<messageDispatchers.length;i++){
			dispatchersMap.put(messageDispatchers[i].getTopic(), messageDispatchers[i]);
		}
	}
	public IConsumer[] getConsumers() {
		return consumers;
	}
	public void setConsumers(IConsumer[] consumers) {
		this.consumers = consumers;
	}
	public ILogger getLogger() {
		return logger;
	}
	public void setLogger(ILogger logger) {
		this.logger = logger;
	}
	public ActiveMQConnectionFactory getFactory() {
		return factory;
	}
	public void setFactory(ActiveMQConnectionFactory factory) {
		this.factory = factory;
	}

	public IConsumer getConsumer(String topic) {
		return consumerMap.get(topic);
	}
	
	public void shutdown() {
		try {
			onShutdown();
		} catch (Exception e) {
			logger.log(Level.SEVERE,"shutdown jms instance failed!",e);
		}
	}

	public IMessageDispatcher getDispatcher(String topic) {
		return dispatchersMap.get(topic);
	}

	public void send(String topic,IMessage message, CompositeMap context) throws Exception {
		IMessageDispatcher sender = getDispatcher(topic);
		if(sender == null)
			throw new IllegalArgumentException("Don't not define the MessageDispatcher for topic:"+topic);
		sender.send(message, context);
	}

	public Connection createConnection() throws JMSException {
		if(factory == null)
			throw new IllegalStateException("ConnectionFactory is not initialiaze!");
		return factory.createConnection();
	}
}

package aurora.plugin.amq;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;

import uncertain.core.ILifeCycle;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.AbstractLocatableObject;
import uncertain.ocm.IObjectRegistry;
import aurora.application.features.msg.IConsumer;
import aurora.application.features.msg.IMessageDispatcher;
import aurora.application.features.msg.IMessageHandler;
import aurora.application.features.msg.IMessageStub;
import aurora.plugin.jms.JMSStub;
import aurora.plugin.jms.MessageDispatcher;

public class AMQClientInstance extends AbstractLocatableObject implements ILifeCycle,JMSStub {
	/**
	 * 配置样本
	<amq:AMQ-client-instance xmlns:msg="aurora.application.features.msg" xmlns:jms="aurora.plugin.jms" xmlns:amq="aurora.plugin.amq" url="failover:(tcp://127.0.0.1:61616)">
	    <messageHandlers>
	        <msg:DefaultMessageHandler name="refreshPriviledge" procedure="init.load_priviledge_check_data"/>
	        <msg:DefaultMessageHandler name="refreshService" procedure="init.load_system_service"/>
	    </messageHandlers>
		
	    <consumers>
	        <jms:consumer topic="application_foundation">
	            <events>
	                <msg:event handler="refreshPriviledge" message="priviledge_setting_change"/>
	                <msg:event handler="refreshService" message="service_config_change"/>
	            </events>
	        </jms:consumer>
			<jms:DefaultNoticeConsumer topic="dml_event"/>
	    </consumers>
		
	</amq:AMQ-client-instance>
	 * 
	 */
	public static final String PLUGIN = "aurora.plugin.amq";
	private IMessageHandler[] mMessageHandlers;
	private IConsumer[] consumers;
	private String url;
	
	private IObjectRegistry registry;
	private Logger logger;
	private Map<String,IMessageHandler> handlersMap = new HashMap<String,IMessageHandler>();
	private IMessageDispatcher messageDispatcher;
	private ActiveMQConnectionFactory factory;
	private Map<String,IConsumer> consumerMap;
	private int status = STOP_STATUS;
	private boolean shutdown = false;
	private Thread moniteStartThread;
	private Thread initConsumersThread;
	public AMQClientInstance(IObjectRegistry registry) {
		this.registry = registry;
		messageDispatcher = new MessageDispatcher(registry);
	}
	
	public boolean startup() {
		if(status == STARTING_STATUS || status == STARTED_STATUS)
			return true;
		status = STARTING_STATUS;
		logger = Logger.getLogger(PLUGIN);
		if(url == null){
			BuiltinExceptionFactory.createOneAttributeMissing(this, "url");
		}
		factory = new ActiveMQConnectionFactory(url);
		// javax.jms.QueueConnectionFactory, javax.jms.TopicConnectionFactory
		registry.registerInstance(ConnectionFactory.class, factory);
		consumerMap = new HashMap<String,IConsumer>();
		//init consumer config
		if(consumers != null){
			for(int i= 0;i<consumers.length;i++){
				consumerMap.put(consumers[i].getTopic(), consumers[i]);
			}
		}
		initConsumersThread = new Thread(){
			public void run(){
				if(consumers != null){
					for(int i= 0;i<consumers.length;i++){
						try {
							consumers[i].init(AMQClientInstance.this);
						} catch (Exception e) {
							logger.log(Level.SEVERE,"init jms consumers failed!",e);
//							throw new RuntimeException(e);
						}
					}
				}
				status = STARTED_STATUS;
				LoggingContext.getLogger(PLUGIN, registry).log(Level.INFO,"start jms client successful!");
			}
		};
		initConsumersThread.start();
		moniteStart();
//		Runtime.getRuntime().addShutdownHook(new Thread(){
//			public void run(){
//				try {
//					onShutdown();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
		registry.registerInstance(IMessageStub.class, this);
		return true;
	}
	public void onShutdown() throws Exception{
		shutdown = true;
		if(moniteStartThread != null)
			moniteStartThread.interrupt();
		if(initConsumersThread != null)
			initConsumersThread.interrupt();
		if(consumers != null){
			for(int i= 0;i<consumers.length;i++){
				consumers[i].onShutdown();
			}
		}
		
	}
	private void moniteStart(){
		final ILogger logger = LoggingContext.getLogger(PLUGIN, registry);
		moniteStartThread = new Thread(){
			public void run(){
				while(!shutdown&&status!=STARTED_STATUS){
					try {
						Thread.sleep(600000);
					} catch (InterruptedException e) {
//						logger.log(Level.SEVERE,"",e);
					}
					logger.log(Level.INFO,"Trying to Connect to "+url);
				}
			}
		};
		moniteStartThread.start();
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
	public IConsumer[] getConsumers() {
		return consumers;
	}
	public void setConsumers(IConsumer[] consumers) {
		this.consumers = consumers;
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

	
	public IMessageDispatcher getDispatcher() {
		return messageDispatcher;
	}

	public Connection createConnection() throws JMSException {
		if(factory == null)
			throw new IllegalStateException("ConnectionFactory is not initialiaze!");
		return factory.createConnection();
	}

	public boolean isStarted() {
		return status == STARTED_STATUS;
	}
}

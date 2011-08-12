package aurora.plugin.amq;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;

import aurora.plugin.jms.Consumer;
import aurora.plugin.jms.IMessageHandler;

import uncertain.composite.CompositeMap;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.exception.MessageFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IConfigurable;
import uncertain.ocm.IObjectRegistry;

public class AMQClientInstance implements IConfigurable{
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
	private IMessageHandler[] mMessageHandlers;
	private Consumer[] consumers;
	private CompositeMap config;
	private String url;
	private IObjectRegistry registry;
	public ILogger logger;
	private Map handlersMap = new HashMap();
	private ActiveMQConnectionFactory factory;
	public AMQClientInstance(IObjectRegistry registry) {
		this.registry = registry;
	}
	
	// Framework function
	public void onInitialize() throws Exception {
		logger = LoggingContext.getLogger(PLUGIN, registry);
		MessageFactory.loadResource("resources.aurora_plugin_amq");
		if(url == null){
			BuiltinExceptionFactory.createOneAttributeMissing(config.asLocatable(), "url");
		}
		factory = new ActiveMQConnectionFactory(url);
		registry.registerInstance(ConnectionFactory.class, factory);
		// javax.jms.QueueConnectionFactory, javax.jms.TopicConnectionFactory
		(new Thread(){
			public void run(){
				if(consumers != null){
					for(int i= 0;i<consumers.length;i++){
						try {
							consumers[i].init(factory,handlersMap);
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
	public void beginConfigure(CompositeMap config) {
		this.config = config;
	}
	public void endConfigure() {
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
	public Consumer[] getConsumers() {
		return consumers;
	}
	public void setConsumers(Consumer[] consumers) {
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
}

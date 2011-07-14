package aurora.plugin.amq;

import uncertain.composite.CompositeMap;
import uncertain.core.IGlobalInstance;
import uncertain.exception.MessageFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IConfigurable;
import uncertain.ocm.IObjectRegistry;

public class AMQClientInstance implements IConfigurable, IGlobalInstance {
	/**
	 * 配置样本
	<amq:AMQ-client-instance xmlns:amq="aurora.plugin.amq" >
		<amq:producer url="tcp://0.0.0.0:61616" topic="test"/>
		<amq:consumer url="tcp://0.0.0.0:61616" topic="test">
			<amq:messageHandlers>
				<amq:defaultMessageHandler name="handler1" />
			</amq:messageHandlers>
			<amq:events>
				<amq:event message="resource_update" handler="handler1"/>
			</amq:events>
		</amq:consumer>
	</amq:AMQ-client-instance>
	 * 
	 */
	public static final String PLUGIN = "aurora.plugin.amq";
	private Consumer consumer;
	private Producer producer;
	private CompositeMap config;
	private IObjectRegistry registry;
	public ILogger logger;
	public AMQClientInstance(IObjectRegistry registry) {
		this.registry = registry;
	}
	// Framework function
	public void onInitialize() throws Exception {
		logger = LoggingContext.getLogger(PLUGIN, registry);
		MessageFactory.loadResource("resources.aurora_plugin_amq");
		if (consumer != null)
			consumer.init(this);
		if (producer != null)
			producer.init(this);
	}
	public void beginConfigure(CompositeMap config) {
		this.config = config;
	}
	public Consumer getConsumer() {
		return consumer;
	}
	public void addConsumer(Consumer consumer) {
		this.consumer = consumer;
	}
	public Producer getProducer() {
		return producer;
	}
	public void addProducer(Producer producer) {
		this.producer = producer;
	}
	public void endConfigure() {
	}
	public ILogger getILogger() {
		return logger;
	}

}

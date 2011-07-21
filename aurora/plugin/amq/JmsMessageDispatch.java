package aurora.plugin.amq;

import java.util.logging.Level;

import uncertain.composite.TextParser;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IConfigurable;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import uncertain.util.resource.ILocatable;

public class JmsMessageDispatch extends AbstractEntry implements IConfigurable{
	public static final String EVENT_ATTR = "event";
	private String event;
	private IObjectRegistry registry;
	public JmsMessageDispatch(IObjectRegistry registry) {
		this.registry = registry;
	}
	
	@Override
	public void run(ProcedureRunner runner) throws Exception {
	    if(event==null)
	        BuiltinExceptionFactory.createAttributeMissing(this, EVENT_ATTR);
	    
	    ILogger logger = LoggingContext.getLogger(runner.getContext(), AMQClientInstance.PLUGIN);
	    
		AMQClientInstance amqClient = (AMQClientInstance)registry.getInstanceOfType(AMQClientInstance.class);
		if(amqClient == null){
			//throw new GeneralException(MessageCodes.INSTANCE_NOT_FOUND_ERROR, new Object[]{AMQClientInstance.class.getCanonicalName()}, locatable);
			throw BuiltinExceptionFactory.createInstanceNotFoundException(this, AMQClientInstance.class );
		}
		String message = TextParser.parse(event, runner.getContext());
		amqClient.getProducer().sendTextMessage(message);	
        logger.log(Level.CONFIG, "Message:{0} sent", new Object[]{message});

	}


	
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	
}

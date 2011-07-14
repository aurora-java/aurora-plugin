package aurora.plugin.amq;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.exception.GeneralException;
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
		AMQClientInstance amqClient = (AMQClientInstance)registry.getInstanceOfType(AMQClientInstance.class);
		if(amqClient == null){
			ILocatable locatable = null;
			throw new GeneralException(MessageCodes.INSTANCE_NOT_FOUND_ERROR, new Object[]{AMQClientInstance.class.getCanonicalName()}, locatable);
		}
		String message = TextParser.parse(event, runner.getContext());
		amqClient.getILogger().log("send message:"+message);
		amqClient.getProducer().sendTextMessage(message);	
	}
    public void beginConfigure(CompositeMap config){
    	if(config.get(EVENT_ATTR) ==null){
    		throw BuiltinExceptionFactory.createAttributeMissing(config.asLocatable(), EVENT_ATTR);
    	}
       super.beginConfigure(config);
    }

	
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	
}

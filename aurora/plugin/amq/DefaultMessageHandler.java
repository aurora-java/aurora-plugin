package aurora.plugin.amq;

import java.io.IOException;
import java.util.logging.Level;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.xml.sax.SAXException;

import uncertain.composite.CompositeMap;
import uncertain.core.UncertainEngine;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.exception.GeneralException;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IConfigurable;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.IProcedureManager;
import uncertain.proc.Procedure;
import uncertain.proc.ProcedureRunner;
import uncertain.util.resource.ILocatable;

public class DefaultMessageHandler implements MessageHandler,IConfigurable {

	private String name;
	private String procedure;
	private CompositeMap config;
	private IObjectRegistry registry;
	public DefaultMessageHandler(IObjectRegistry registry) {
		this.registry = registry;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
		
	}
	public void onMessage(Message message) {
    	if(config.get("procedure") ==null){
    		throw BuiltinExceptionFactory.createAttributeMissing(config.asLocatable(), "procedure");
    	}
		ILogger logger = LoggingContext.getLogger(AMQClientInstance.PLUGIN,registry);
		if(message == null){
			logger.log(Level.WARNING, "message is null");
			return ;
		}
		if(message instanceof TextMessage){
			try {
				logger.log(Level.CONFIG,"receive message text:{0}",new Object[]{((TextMessage)message).getText()});
				logger.log(Level.CONFIG,"load procedure:{0}",new Object[]{procedure});
				UncertainEngine ue = (UncertainEngine)registry.getInstanceOfType(UncertainEngine.class);
				ILocatable location = null;
				if(ue == null){
					BuiltinExceptionFactory.createInstanceNotFoundException(config.asLocatable(), UncertainEngine.class);
				}
				IProcedureManager pm = ue.getProcedureManager();
				if(ue == null){
					BuiltinExceptionFactory.createInstanceNotFoundException(config.asLocatable(), IProcedureManager.class);
				}
				ProcedureRunner runner = pm.createProcedureRunner();
		        Procedure proc = pm.loadProcedure(procedure);
		        if(proc==null)
		            throw new IllegalArgumentException("Can't load procedure "+procedure);
		        runner.call(proc);
			} catch (JMSException e) {
				throw new GeneralException(MessageCodes.JMSEXCEPTION_ERROR, new Object[]{e.getMessage()}, e); 
			} catch (IOException e) {
				BuiltinExceptionFactory.createResourceLoadException(config.asLocatable(), procedure, e);
			} catch (SAXException e) {
				throw new GeneralException(MessageCodes.SAX_ERRORR, new Object[]{procedure}, e); 
			}		
		}
		else
			logger.log(Level.CONFIG,"This is not TextMessage.This is "+message.getClass().getCanonicalName());
		
	}
    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }
	public void beginConfigure(CompositeMap config) {
    	this.config = config;
	}
	public void endConfigure() {
	}

}

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
import uncertain.ocm.IConfigurable;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.IProcedureManager;
import uncertain.proc.Procedure;
import uncertain.proc.ProcedureRunner;
import uncertain.util.resource.ILocatable;

public class DefaultMessageHandler implements MessageHandler,IConfigurable {

	private String name;
	private String procedure;
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
	public void onMessage(AMQClientInstance client,Message message) {
		if(message == null){
			client.getILogger().log(Level.WARNING, "message is null");
		}
		if(message instanceof TextMessage){
			try {
				client.getILogger().log("receive message text:"+((TextMessage)message).getText());
				client.getILogger().log("load procedure:"+procedure);
				UncertainEngine ue = (UncertainEngine)registry.getInstanceOfType(UncertainEngine.class);
				ILocatable location = null;
				if(ue == null){
					throw new GeneralException(MessageCodes.INSTANCE_NOT_FOUND_ERROR,new Object[]{UncertainEngine.class.getCanonicalName()},location);
				}
				IProcedureManager pm = ue.getProcedureManager();
				if(ue == null){
					throw new GeneralException(MessageCodes.INSTANCE_NOT_FOUND_ERROR,new Object[]{IProcedureManager.class.getCanonicalName()},location);
				}
				ProcedureRunner runner = pm.createProcedureRunner();
		        Procedure proc = pm.loadProcedure(procedure);
		        if(proc==null)
		            throw new IllegalArgumentException("Can't load procedure "+procedure);
		        runner.call(proc);
			} catch (JMSException e) {
				throw new GeneralException(MessageCodes.JMSEXCEPTION_ERROR, new Object[]{e.getMessage()}, e); 
			} catch (IOException e) {
				throw new GeneralException(MessageCodes.IO_ERROR, new Object[]{procedure}, e);  
			} catch (SAXException e) {
				throw new GeneralException(MessageCodes.SAX_ERRORR, new Object[]{procedure}, e); 
			}		
		}
		else
			client.getILogger().log("This is not TextMessage.This is "+message.getClass().getName());
		
	}
    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }
	public void beginConfigure(CompositeMap config) {
    	if(config.get("procedure") ==null){
    		throw BuiltinExceptionFactory.createAttributeMissing(config.asLocatable(), "procedure");
    	}
	}
	public void endConfigure() {
	}

}

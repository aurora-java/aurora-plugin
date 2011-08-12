package aurora.plugin.jms;

import java.io.IOException;
import java.util.logging.Level;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.xml.sax.SAXException;

import uncertain.composite.CompositeMap;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.exception.GeneralException;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.AbstractLocatableObject;
import uncertain.ocm.IConfigurable;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.IProcedureManager;
import uncertain.proc.Procedure;
import aurora.service.IServiceFactory;
import aurora.service.ServiceInvoker;

public class DefaultMessageHandler extends AbstractLocatableObject implements IMessageHandler {

    private String name;
    private String procedure;
    //private CompositeMap config;
    private IObjectRegistry registry;
    private IProcedureManager procedureManager;
    private IServiceFactory serviceFactory;

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
        if(procedureManager==null){
            this.procedureManager = (IProcedureManager) registry
                    .getInstanceOfType(IProcedureManager.class);
            if (procedureManager == null)
                throw BuiltinExceptionFactory.createInstanceNotFoundException(
                        this, IProcedureManager.class, this
                                .getClass().getName());
        
        this.serviceFactory = (IServiceFactory)registry.getInstanceOfType(IServiceFactory.class);
        if(serviceFactory==null)
            throw BuiltinExceptionFactory.createInstanceNotFoundException(
                    this, IServiceFactory.class, this
                            .getClass().getName());
        }
        
    	if(procedure==null){
    		throw BuiltinExceptionFactory.createAttributeMissing( this, "procedure");
    	}
		ILogger logger = LoggingContext.getLogger(JMSUtil.PLUGIN,registry);
		if(message == null){
			logger.log(Level.WARNING, "message is null");
			return ;
		}
		if(message instanceof TextMessage){
            CompositeMap context = new CompositeMap();

		    try {
				logger.log(Level.CONFIG,"receive message text:{0}",new Object[]{((TextMessage)message).getText()});
				logger.log(Level.CONFIG,"load procedure:{0}",new Object[]{procedure});
				Procedure proc = null;
				try{
				    proc = procedureManager.loadProcedure(procedure);
				}catch(Exception ex){
                    throw BuiltinExceptionFactory.createResourceLoadException( this, procedure, ex);
				}
				String name = "JMS."+procedure;
				ServiceInvoker.invokeProcedureWithTransaction(name, proc, serviceFactory, context);

			}catch(Exception ex){ 
			    logger.log(Level.SEVERE, "Error when invoking procedure "+procedure, ex);
			}finally {
			    context.clear();
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


}

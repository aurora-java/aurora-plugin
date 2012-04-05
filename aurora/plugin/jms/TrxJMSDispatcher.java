package aurora.plugin.jms;

import uncertain.ocm.IObjectRegistry;
import aurora.application.features.msg.IMessageDispatcher;
import aurora.application.features.msg.TrxMessageDispatcher;

public class TrxJMSDispatcher extends TrxMessageDispatcher{
	
	public TrxJMSDispatcher(IObjectRegistry registry) {
		super(registry);
	}
	protected IMessageDispatcher createMessageDispatcher(){
		return new MessageDispatcher(mRegistry);
	}
	

}

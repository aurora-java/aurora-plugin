package aurora.plugin.amq;

import javax.jms.Message;
import javax.jms.MessageListener;

public interface MessageHandler extends MessageListener{
	public String getName();
	public void setName(String name) ;
}

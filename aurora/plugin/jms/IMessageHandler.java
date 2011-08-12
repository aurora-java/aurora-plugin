package aurora.plugin.jms;

import javax.jms.MessageListener;

public interface IMessageHandler extends MessageListener{
	public String getName();
	public void setName(String name) ;
}

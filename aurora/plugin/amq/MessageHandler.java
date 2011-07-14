package aurora.plugin.amq;

import javax.jms.Message;

public interface MessageHandler {
	public String getName();
	public void setName(String name) ;
	public void onMessage(AMQClientInstance client,Message message);
}

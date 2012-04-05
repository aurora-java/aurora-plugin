package aurora.plugin.jms;

import javax.jms.Connection;
import javax.jms.JMSException;

import aurora.application.features.msg.IMessageStub;

public interface JMSStub extends IMessageStub{

	public Connection createConnection() throws JMSException;

}

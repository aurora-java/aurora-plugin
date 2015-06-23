package aurora.plugin.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

public class JMSUtil {
	public static final String PLUGIN = "aurora.plugin.jms";
	
	public static void freeMessageProducer(MessageProducer messageProducer) {
		if (messageProducer != null) {
			try {
				messageProducer.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
	public static void freeMessageConsumer(MessageConsumer messageConsumer) {
		if (messageConsumer != null) {
			try {
				messageConsumer.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
	

	public static void freeJMSSession(Session session) {
		if (session != null) {
			try {
				session.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}

	public static void freeJMSConnection(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
}

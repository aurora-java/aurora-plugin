package aurora.plugin.jms;

import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import uncertain.composite.CompositeMap;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.AbstractLocatableObject;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.IProcedureManager;
import uncertain.proc.Procedure;
import aurora.service.IServiceFactory;
import aurora.service.ServiceInvoker;

public class DefaultMessageHandler extends AbstractLocatableObject implements
		IMessageHandler {

	private String name;
	private String procedure;
	private String javaClass;

	// private CompositeMap config;
	private IObjectRegistry registry;
	private IProcedureManager procedureManager;
	private IServiceFactory serviceFactory;

	// private ProcedureRunner runner;

	public DefaultMessageHandler(IObjectRegistry registry) {
		this.registry = registry;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;

	}

	@SuppressWarnings("unchecked")
	public void onMessage(Message message) {

		ILogger logger = LoggingContext.getLogger(JMSUtil.PLUGIN, registry);

		logger.log(Level.INFO, "accepted a new message！");
		System.err.println(new Date()+": accepted a new message!");
		/*
		 * john添加 判断是否响应该次消息
		 */
		String key = "";
		String singletion = "";
		try {
			key = message.getStringProperty("key");
			singletion = message.getStringProperty("singleton");
		} catch (JMSException e1) {
			e1.printStackTrace();
		}
		if ("true".equals(singletion)) {
			if (!JmsMessageDispatch.key.equals(key)) {
				logger.log(Level.INFO, ":不是本服务器发出的请求，不作出响应。发送方：" + key);
				return;
			}
		}

		if (procedureManager == null) {
			this.procedureManager = (IProcedureManager) registry
					.getInstanceOfType(IProcedureManager.class);
			if (procedureManager == null)
				throw BuiltinExceptionFactory.createInstanceNotFoundException(
						this, IProcedureManager.class, this.getClass()
								.getName());

			this.serviceFactory = (IServiceFactory) registry
					.getInstanceOfType(IServiceFactory.class);
			if (serviceFactory == null)
				throw BuiltinExceptionFactory.createInstanceNotFoundException(
						this, IServiceFactory.class, this.getClass().getName());
		}

		if (procedure == null) {
			throw BuiltinExceptionFactory.createAttributeMissing(this,
					"procedure");
		}

		if (message == null) {
			logger.log(Level.WARNING, "message is null");
			return;
		}
		if (message instanceof TextMessage) {
			CompositeMap context = new CompositeMap();

			/*
			 * john添加 将message中的信息放到service的数据容器
			 */
			try {
				Enumeration en = message.getPropertyNames();

				while (en.hasMoreElements()) {
					String messageName = (String) en.nextElement();
					String parsed_content = message
							.getStringProperty(messageName);
					// 将解析后的部分放入service的数据容器
					context.putObject("/parameter/message/@" + messageName,
							parsed_content, true);
				}
			} catch (JMSException e1) {
				e1.printStackTrace();
			}
			

			try {
				logger.log(Level.CONFIG, "receive message text:{0}",
						new Object[] { ((TextMessage) message).getText() });
				logger.log(Level.CONFIG, "load procedure:{0}",
						new Object[] { procedure });
				Procedure proc = null;
				try {
					proc = procedureManager.loadProcedure(procedure);
				} catch (Exception ex) {
					throw BuiltinExceptionFactory.createResourceLoadException(
							this, procedure, ex);
				}
				String name = "JMS." + procedure;
				ServiceInvoker.invokeProcedureWithTransaction(name, proc,
						serviceFactory, context);

			} catch (Exception ex) {
				logger.log(Level.SEVERE, "Error when invoking procedure "
						+ procedure, ex);
			} finally {
				context.clear();
			}
		} else
			logger.log(Level.CONFIG, "This is not TextMessage.This is "
					+ message.getClass().getCanonicalName());
	}

	public String getProcedure() {
		return procedure;
	}

	public void setProcedure(String procedure) {
		this.procedure = procedure;
	}

	public String getJavaClass() {
		return javaClass;
	}

	public void setJavaClass(String javaClass) {
		this.javaClass = javaClass;
	}
}

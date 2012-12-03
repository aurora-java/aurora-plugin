package aurora.plugin.mail;

import java.util.logging.Level;

import javax.mail.MessagingException;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IConfigurable;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.database.service.SqlServiceContext;

public class AutoSendMail extends AbstractEntry implements IConfigurable {

	private IObjectRegistry registry;
	private String title;
	private String content;
	private String smtpServer;
	private String password;
	private String userName;
	private String tto;
	private String cto;
	private String from;
	private String port;
	private Boolean auth = null;

	private Attachment[] attachments;

	public AutoSendMail(IObjectRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void run(ProcedureRunner runner) throws Exception {

		IMailServerConfig mailConfig = (IMailServerConfig) registry.getInstanceOfType(IMailServerConfig.class);
		if (mailConfig != null) {
			smtpServer = smtpServer != null ? smtpServer : mailConfig.getSmtpServer();
			userName = userName != null ? userName : mailConfig.getUserName();
			password = password != null ? password : mailConfig.getPassword();
			from = from != null ? from : mailConfig.getFrom();
			port = port != null ? port : mailConfig.getPort();
			auth = auth != null ? auth : mailConfig.getAuth();
		}
		if(port == null)
			port = "25";
		if(auth == null)
			auth = false;
		ILogger logger = LoggingContext.getLogger("aurora.plugin.mail", registry);
		logger.log(Level.CONFIG, "Accept to E-mail message, began sendind mail operation");

		CompositeMap map = runner.getContext();
		SqlServiceContext svcContext = SqlServiceContext.createSqlServiceContext(map);
		CompositeMap current_param = svcContext.getCurrentParameter();

		SendMail sendMail = new SendMail();

		sendMail.setCto(TextParser.parse(cto, current_param));
		sendMail.setPassword(TextParser.parse(password, current_param));
		sendMail.setSmtpServer(TextParser.parse(smtpServer, current_param));
		sendMail.setTcontent(TextParser.parse(content, current_param));
		sendMail.setTfrom(TextParser.parse(from, current_param));
		sendMail.setTtitle(TextParser.parse(title, current_param));
		sendMail.setTto(TextParser.parse(tto, current_param));
		sendMail.setPort(port);
		sendMail.setUserName(TextParser.parse(userName, current_param));
		sendMail.setAuth(auth);

		convertAttach(runner.getContext());
		sendMail.setAttachments(getAttachments());

		try {
			sendMail.check();
			sendMail.sendMail();
			current_param.put("status", "SUCCESS");
			logger.log(Level.INFO, "Mail send successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			current_param.put("status", "FAILED");
			current_param.put("message", e.getMessage());
			logger.log(Level.WARNING, e.getMessage());
		}
	}

	protected void convertAttach(CompositeMap context) throws MessagingException {
		if (attachments != null) {
			for (int i = 0; i < attachments.length; i++) {
				attachments[i].setPath(TextParser.parse(attachments[i].getPath(), context));
				attachments[i].setName(TextParser.parse(attachments[i].getName(), context));
			}
		}
	}

	public IObjectRegistry getRegistry() {
		return registry;
	}

	public void setRegistry(IObjectRegistry registry) {
		this.registry = registry;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSmtpServer() {
		return smtpServer;
	}

	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTto() {
		return tto;
	}

	public void setTto(String tto) {
		this.tto = tto;
	}

	public String getCto() {
		return cto;
	}

	public void setCto(String cto) {
		this.cto = cto;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Attachment[] getAttachments() {
		return attachments;
	}

	public void setAttachments(Attachment[] attaches) {
		this.attachments = attaches;
	}

	public boolean isAuth() {
		return auth;
	}

	public void setAuth(boolean auth) {
		this.auth = auth;
	}

}

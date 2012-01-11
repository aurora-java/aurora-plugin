package aurora.plugin.mail;

import java.util.logging.Level;

import aurora.database.service.SqlServiceContext;
import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IConfigurable;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;

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
	private String port="25";

	public AutoSendMail(IObjectRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void run(ProcedureRunner runner) throws Exception {

		ILogger logger = LoggingContext.getLogger("aurora.plugin.mail", registry);
		logger.log(Level.INFO, "Accept to E-mail message, began sendind mail operation");
		
		CompositeMap map = runner.getContext();
		SqlServiceContext svcContext = SqlServiceContext
				.createSqlServiceContext(map);
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
		
        try {
        	sendMail.check();
        	sendMail.sendMail();
        	current_param.put("status", "SUCCESS");
        	logger.log(Level.INFO, "Mail send successfully!");
		} catch (Exception e) {
			current_param.put("status", "FAILED");
			current_param.put("message", e.getMessage());
			logger.log(Level.WARNING, e.getMessage());
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
  
}

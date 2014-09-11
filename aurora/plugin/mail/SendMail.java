package aurora.plugin.mail;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IConfigurable;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.database.service.SqlServiceContext;

public class SendMail extends AbstractEntry implements IConfigurable {

	private IObjectRegistry registry;
	private String title;
	private String content;
	private String smtpServer;
	private String password;
	private String userName;
	private String to;
	private String cc;
	private String from;
	private String port;
	private Boolean auth = null;

	private Attachment[] attachments;

	public SendMail(IObjectRegistry registry) {
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
		if (port == null)
			port = "25";
		if (auth == null)
			auth = false;
		ILogger logger = LoggingContext.getLogger(runner.getContext(), this.getClass().getCanonicalName());
		logger.config("Accept to E-mail message, began sendind mail operation");
		CompositeMap map = runner.getContext();
		SqlServiceContext svcContext = SqlServiceContext.createSqlServiceContext(map);
		CompositeMap current_param = svcContext.getCurrentParameter();

		password = TextParser.parse(password, current_param);
		smtpServer = TextParser.parse(smtpServer, current_param);
		content = TextParser.parse(content, current_param);
		from = TextParser.parse(from, current_param);
		title = TextParser.parse(title, current_param);
		to = TextParser.parse(to, current_param);
		port = TextParser.parse(port, current_param);
		userName = TextParser.parse(userName, current_param);
		cc = TextParser.parse(cc, current_param);

		if (smtpServer == null || "".equals(smtpServer)) {
			throw BuiltinExceptionFactory.createAttributeMissing(this, "smtpServer");
		}
		if (from == null || "".equals(from)) {
			throw BuiltinExceptionFactory.createAttributeMissing(this, "from");
		}
		if (password == null || "".equals(password)) {
			throw BuiltinExceptionFactory.createAttributeMissing(this, "password");
		}
		if (to == null || "".equals(to)) {
			throw BuiltinExceptionFactory.createAttributeMissing(this, "to");
		}
		if (content == null || "".equals(content)) {
			throw BuiltinExceptionFactory.createAttributeMissing(this, "content");
		}

		parseAttachParameters(runner.getContext());
		sendMail();
		logger.config("Mail send successfully!");
	}

	public void sendMail() throws Exception {

		Properties props = new Properties();
		props.put("mail.smtp.host", smtpServer);// 存储发送邮件服务器的信息
		props.put("mail.smtp.auth", "true");// 同时通过验证
		props.put("mail.smtp.port", port);

		Session s = null;
		if (auth) { // 服务器需要身份认证
			props.put("mail.smtp.auth", "true");
			SmtpAuth smtpAuth = new SmtpAuth(userName, password);
			s = Session.getDefaultInstance(props, smtpAuth);
		} else {
			props.put("mail.smtp.auth", "false");
			s = Session.getDefaultInstance(props, null);
		}

		Message message = new MimeMessage(s);// 由邮件会话新建一个消息对象
		Address fromAddress = new InternetAddress(from);// 发件人的邮件地址
		message.setFrom(fromAddress);// 设置发件人

		message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to));// 设置收件人,并设置其接收类型为TO,还有3种预定义类型如下：

		if (cc != null && !"".equals(cc)) {
			message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));// 设置抄送
		}
		message.setSubject(title);// 设置主题
		message.setSentDate(new Date());// 设置发信时间

		Multipart mp = new MimeMultipart();
		MimeBodyPart mbp = new MimeBodyPart();
		mbp.setContent(content, "text/html;charset=utf-8");
		mp.addBodyPart(mbp);
		addAttachment(mp);
		message.setContent(mp);
		Transport.send(message, message.getAllRecipients());

	}

	class SmtpAuth extends Authenticator {
		private String username, password;

		public SmtpAuth(String username, String password) {
			this.username = username;
			this.password = password;
		}

		protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
			return new javax.mail.PasswordAuthentication(username, password);
		}
	}

	protected void addAttachment(Multipart mp) throws MessagingException, UnsupportedEncodingException {
		if (attachments != null) {
			MimeBodyPart mbp;
			FileDataSource fds;
			String fileName;
			for (int i = 0; i < attachments.length; i++) {
				mbp = new MimeBodyPart();
				fileName = attachments[i].getPath(); // 选择出每一个附件名
				fds = new FileDataSource(fileName); // 得到数据源
				mbp.setDataHandler(new DataHandler(fds)); // 得到附件本身并至入BodyPart
				// MimeUtility.encodeText(filename)
				String encodeFileName = MimeUtility.encodeText(attachments[i].getName());
				mbp.setFileName(encodeFileName); // 得到文件名同样至入BodyPart
				mp.addBodyPart(mbp);
			}
		}
	}

	protected void parseAttachParameters(CompositeMap context) throws MessagingException {
		if (attachments != null) {
			for (int i = 0; i < attachments.length; i++) {
				attachments[i].setPath(TextParser.parse(attachments[i].getPath(), context));
				attachments[i].setName(TextParser.parse(attachments[i].getName(), context));
			}
		}
	}

	public void check() {

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

	@Deprecated
	public String getTto() {
		return to;
	}

	@Deprecated
	public void setTto(String tto) {
		this.to = tto;
	}

	@Deprecated
	public String getCto() {
		return cc;
	}

	@Deprecated
	public void setCto(String cto) {
		this.cc = cto;
	}
	@Deprecated
	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}
	
	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
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

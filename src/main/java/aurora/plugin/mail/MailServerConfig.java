package aurora.plugin.mail;

public class MailServerConfig implements IMailServerConfig{

	private String smtpServer;
	private String password;
	private String userName;
	private String from;
	private String port="25";
	private boolean auth = false;
	
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
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
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
	
	public boolean getAuth() {
		return auth;
	}

	public void setAuth(boolean auth) {
		this.auth = auth;
	}
}

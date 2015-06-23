package aurora.plugin.mail;

public interface IMailServerConfig {
	
	public String getSmtpServer();
	
	public String getPassword();
	
	public String getUserName();
	
	public String getFrom();
	
	public String getPort();
	
	public boolean getAuth();
	
}

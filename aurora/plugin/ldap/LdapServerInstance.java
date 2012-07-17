package aurora.plugin.ldap;

public class LdapServerInstance {
	String name;
	String host;
	String port;
	String domain;
	String securityAuthentication = "simple";
	String initialContextFactory = "com.sun.jndi.ldap.LdapCtxFactory";
	boolean isSSLEnabled=false;	
	
	public Boolean getSSLEnabled() {
		return isSSLEnabled;
	}

	public void setSSLEnabled(Boolean SSLEnabled) {
		this.isSSLEnabled = SSLEnabled;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}	

	public String getSecurityAuthentication() {
		return securityAuthentication;
	}

	public void setSecurityAuthentication(String securityAuthentication) {
		this.securityAuthentication = securityAuthentication;
	}

	public String getInitialContextFactory() {
		return initialContextFactory;
	}

	public void setInitialContextFactory(String initialContextFactory) {
		this.initialContextFactory = initialContextFactory;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

}

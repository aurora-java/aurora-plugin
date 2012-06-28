package aurora.plugin.ldap;

public class LdapServerInstance {
	String name;
	String host;
	String port;
	String keystore;
	String securityProtocol;
	String domain;
	String securityAuthentication = "simple";
	String initialContextFactory = "com.sun.jndi.ldap.LdapCtxFactory";

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

	public String getKeystore() {
		return keystore;
	}

	public void setKeystore(String keystore) {
		this.keystore = keystore;
	}

	public String getSecurityProtocol() {
		return securityProtocol;
	}

	public void setSecurityProtocol(String securityProtocol) {
		this.securityProtocol = securityProtocol;
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

package aurora.plugin.spnego;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;

import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;

public class SpnegoConfig {
	IObjectRegistry mObjectRegistry;
	ILogger mLogger;
	private transient SpnegoAuthenticator authenticator = null;	
	public SpnegoConfig(IObjectRegistry registry) {
		mObjectRegistry=registry;
		mLogger = LoggingContext.getLogger("aurora.plugin.spnego.SpnegoConfig",mObjectRegistry);
	}
	public void onInitialize() {		
		try {
			System.setProperty("java.security.krb5.conf", createKrb5File());
			System.setProperty("java.security.auth.login.config", createLoinFile());
			authenticator = new SpnegoAuthenticator(this);
			mObjectRegistry.registerInstance(SpnegoConfig.class, this);
		} catch (Exception e) {
			mLogger.log(Level.SEVERE, e.getMessage());
			throw new RuntimeException(e);
		}
	}	

	String createKrb5File() throws IOException {
		File krb5 = null;
		PrintWriter out = null;
		try {
			krb5 = File.createTempFile("krb5", ".conf");
			krb5.deleteOnExit();
			out = new PrintWriter(krb5);
			out.println("[libdefaults]");
			out.println("	default_realm=" + getDomain());
			out.println("	default_tkt_enctypes = aes128-cts rc4-hmac des3-cbc-sha1 des-cbc-md5 des-cbc-crc");
			out.println("	default_tgs_enctypes = aes128-cts rc4-hmac des3-cbc-sha1 des-cbc-md5 des-cbc-crc");
			out.println("	permitted_enctypes   = aes128-cts rc4-hmac des3-cbc-sha1 des-cbc-md5 des-cbc-crc");
			out.println("[realms]");
			out.println("	"+getDomain() + "  = {");
			out.println("		kdc =" + getHost());
			out.println("		default_domain = " + getDomain());
			out.println("}");
			out.println("[domain_realm]");
			out.println("	." + getDomain() + " = " + getDomain());
		} finally {
			if (out != null)
				out.close();
		}
		if (krb5 == null)
			return null;
		return krb5.getPath();
	}

	String createLoinFile() throws Exception {
		File loginFile = null;
		PrintWriter out = null;
		try {
			loginFile = File.createTempFile("login", ".conf");
			loginFile.deleteOnExit();
			out = new PrintWriter(loginFile);
			out.println("spnego-client {com.sun.security.auth.module.Krb5LoginModule required;};");
			out.println("spnego-server {");
			out.println("com.sun.security.auth.module.Krb5LoginModule required");
			out.println("storeKey=true");
			out.println("isInitiator=false;};");
		} finally {
			if (out != null)
				out.close();
		}
		if (loginFile == null)
			return null;
		return loginFile.getPath();
	}

	public void onShutdown() {
		if (null != this.authenticator) {
			this.authenticator.dispose();
			this.authenticator = null;
		}
	}

	String username;
	String password;
	String domain;
	String host;
	String procedure;
	String loginchekpath;
	boolean allowBasic = true;
	boolean allowDelegation = false;
	boolean allowLocalhost = false;
	boolean allowUnsecure = true;
	boolean promptIfNtlm = true;
	String clientModuleName = "spnego-client";
	String serverModuleName = "spnego-server";	
	
	public String getLoginchekpath() {
		return loginchekpath;
	}
	public void setLoginchekpath(String loginchekpath) {
		this.loginchekpath = loginchekpath;
	}
	public String getProcedure() {
		return procedure;
	}
	public void setProcedure(String procedure) {
		this.procedure = procedure;
	}
	public String getClientModuleName() {
		return clientModuleName;
	}

	public void setClientModuleName(String clientModuleName) {
		this.clientModuleName = clientModuleName;
	}

	public String getServerModuleName() {
		return serverModuleName;
	}

	public void setServerModuleName(String serverModuleName) {
		this.serverModuleName = serverModuleName;
	}

	public boolean getAllowBasic() {
		return allowBasic;
	}

	public void setAllowBasic(boolean allowBasic) {
		this.allowBasic = allowBasic;
	}

	public boolean getAllowDelegation() {
		return allowDelegation;
	}

	public void setAllowDelegation(boolean allowDelegation) {
		this.allowDelegation = allowDelegation;
	}

	public boolean getAllowLocalhost() {
		return allowLocalhost;
	}

	public void setAllowLocalhost(boolean allowLocalhost) {
		this.allowLocalhost = allowLocalhost;
	}

	public boolean getAllowUnsecure() {
		return allowUnsecure;
	}

	public void setAllowUnsecure(boolean allowUnsecure) {
		this.allowUnsecure = allowUnsecure;
	}

	public boolean getPromptIfNtlm() {
		return promptIfNtlm;
	}

	public void setPromptIfNtlm(boolean promptIfNtlm) {
		this.promptIfNtlm = promptIfNtlm;
	}

	public String getUsername() {
		if (username == null)
			throw new IllegalArgumentException("Must specify a username");
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		if (password == null)
			throw new IllegalArgumentException("Must specify the password");
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDomain() {
		if (domain == null)
			throw new IllegalArgumentException("Must specify the domain");
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getHost() {
		if (host == null)
			throw new IllegalArgumentException("Must specify the host");
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public SpnegoAuthenticator getSpnegoAuthenticator() {
		return this.authenticator;
	}

	public static final class Constants {

		private Constants() {
			// default private
		}

		public static final String ALLOW_BASIC = "spnego.allow.basic";

		public static final String ALLOW_DELEGATION = "spnego.allow.delegation";

		public static final String ALLOW_LOCALHOST = "spnego.allow.localhost";

		public static final String ALLOW_UNSEC_BASIC = "spnego.allow.unsecure.basic";

		public static final String AUTHN_HEADER = "WWW-Authenticate";

		public static final String AUTHZ_HEADER = "Authorization";

		public static final String BASIC_HEADER = "Basic";

		public static final String CLIENT_MODULE = "spnego.login.client.module";

		public static final String KRB5_CONF = "spnego.krb5.conf";

		static final String LOGGER_LEVEL = "spnego.logger.level";

		static final String LOGGER_NAME = "SpnegoHttpFilter";

		public static final String LOGIN_CONF = "spnego.login.conf";

		public static final String NEGOTIATE_HEADER = "Negotiate";

		static final String NTLM_PROLOG = "TlRMTVNT";

		public static final String PREAUTH_PASSWORD = "spnego.preauth.password";

		public static final String PREAUTH_USERNAME = "spnego.preauth.username";

		public static final String PROMPT_NTLM = "spnego.prompt.ntlm";

		public static final String SERVER_MODULE = "spnego.login.server.module";
	}
}

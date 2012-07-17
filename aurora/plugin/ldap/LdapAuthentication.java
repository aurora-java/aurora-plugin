package aurora.plugin.ldap;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import aurora.application.util.LanguageUtil;
import aurora.service.ServiceContext;
import aurora.service.validation.ErrorMessage;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;

public class LdapAuthentication extends AbstractEntry{
	LdapConfig ldapMap;
	String serverName;
	String username;
	String password;
	String errorMessage;
	boolean is_ssl=false;
	IObjectRegistry mObjectRegistry;

	public LdapAuthentication(LdapConfig ldapMap,IObjectRegistry reg) {
		this.ldapMap = ldapMap;
		this.mObjectRegistry = reg;
	}

	public void run(ProcedureRunner runner) throws Exception{
		CompositeMap context=runner.getContext();
		validateParameter(context);
		LdapServerInstance ldapServer=this.ldapMap.getSapInstance(this.serverName);
		if("ssl".equalsIgnoreCase(ldapServer.getSecurityProtocol()))
			is_ssl=true;
		String user = this.getUsername().indexOf(ldapServer.getDomain()) > 0 ? this.getUsername() : this.getUsername()
				+ ldapServer.getDomain();
		String url="ldap://" + ldapServer.getHost() + ":" + ldapServer.getPort();
		if(is_ssl)
			url="ldaps://" + ldapServer.getHost() + ":" + ldapServer.getPort();			
		
		Hashtable<String,String> env = new Hashtable<String,String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY,ldapServer.getInitialContextFactory());
		env.put(Context.PROVIDER_URL, url);
		env.put(Context.SECURITY_AUTHENTICATION, ldapServer.getSecurityAuthentication());
		env.put(Context.SECURITY_PRINCIPAL, user);
		env.put(Context.SECURITY_CREDENTIALS, this.getPassword());
		if(is_ssl){
			env.put(Context.SECURITY_PROTOCOL, ldapServer.getSecurityProtocol());
			if(ldapServer.getKeystore()==null)
				throw new IllegalStateException("javax.net.ssl.trustStore is null"); 
			System.setProperty("javax.net.ssl.trustStore", ldapServer.getKeystore());
			env.put("java.naming.ldap.factory.socket","aurora.plugin.ldap.SSLSocketFactoryWrap");
		}
		LdapContext ctx = null;
		try {
			ctx = new InitialLdapContext(env, null);	
			ctx.close();
		} catch (NamingException e) {
			 if(e.getMessage().startsWith("[LDAP: error code 49")){
				 String error_message=this.getErrorMessage();			 
				 error_message = LanguageUtil.getTranslatedMessage(mObjectRegistry, error_message, context);
				 ErrorMessage msg = new ErrorMessage(null,error_message, null);			
				 ServiceContext  sc = ServiceContext.createServiceContext(context);
				 sc.setError(msg.getObjectContext());
				 sc.put("success", false);
		         runner.getCaller().locateTo("CreateResponse");
		         runner.stop();
			 }else
				 throw e;
		}
	}
	
	void validateParameter(CompositeMap context){
		this.username=TextParser.parse(this.username, context);
		if(this.username==null)
			throw new IllegalStateException("parameter \"username\" is null");
		this.password=TextParser.parse(this.password, context);
		if(this.password==null)
			throw new IllegalStateException("parameter \"password\" is null");
		this.serverName=TextParser.parse(this.serverName, context);
		if(this.serverName==null)
			throw new IllegalStateException("parameter \"serverName\" is null");
		this.errorMessage=TextParser.parse(this.errorMessage, context);
		if(this.errorMessage==null)
			throw new IllegalStateException("parameter \"errorMessage\" is null");
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
}

package aurora.plugin.ldap;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

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
	List<LdapServerInstance> ldapServerList=null;
	String serverName;
	String username;
	String password;
	Boolean terminate=true;
	String errorMessage;
	IObjectRegistry mObjectRegistry;

	public LdapAuthentication(LdapConfig ldapMap,IObjectRegistry reg) {
		this.ldapMap = ldapMap;
		this.mObjectRegistry = reg;
		ldapServerList=this.ldapMap.getInstanceList();		
	}

	public void run(ProcedureRunner runner) throws Exception{
		CompositeMap context=runner.getContext();
		validateParameter(context);		
		Iterator<LdapServerInstance> iterator=ldapServerList.iterator();
		Exception exception=null;
		while(iterator.hasNext()){			
			LdapServerInstance ldapServer=iterator.next();			
			if(!this.serverName.equals(ldapServer.getName()))
				continue;
			exception=null;
			String user = this.getUsername().indexOf(ldapServer.getDomain()) > 0 ? this.getUsername() : this.getUsername()
					+ ldapServer.getDomain();		
			String url="ldap://" + ldapServer.getHost() + ":" + ldapServer.getPort();			
			Hashtable<String,String> env = new Hashtable<String,String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY,ldapServer.getInitialContextFactory());		
			env.put(Context.SECURITY_AUTHENTICATION, ldapServer.getSecurityAuthentication());
			env.put(Context.SECURITY_PRINCIPAL, user);
			env.put(Context.SECURITY_CREDENTIALS, this.getPassword());
			if(ldapServer.getSSLEnabled()){		
				url="ldaps://" + ldapServer.getHost() + ":" + ldapServer.getPort();	
				env.remove(Context.SECURITY_AUTHENTICATION);			
				env.put(Context.SECURITY_PROTOCOL, "ssl");
				env.put("java.naming.ldap.factory.socket","aurora.plugin.ldap.SSLSocketFactoryWrap");
			}
			env.put(Context.PROVIDER_URL, url);			
			LdapContext ctx = null;
			try {
				ctx = new InitialLdapContext(env, null);					
				return;
			} catch (NamingException e) {
				exception=e;	
			}finally{
				try {
					if(ctx!=null)
						ctx.close();
				} catch (NamingException e) {					
				}
			}		
		}
		if(exception!=null){
			if(exception.getMessage().startsWith("[LDAP: error code 49")){
				 String error_message=this.getErrorMessage();			 
				 error_message = LanguageUtil.getTranslatedMessage(mObjectRegistry, error_message, context);
				 ErrorMessage msg = new ErrorMessage(null,error_message, null);			
				 ServiceContext  sc = ServiceContext.createServiceContext(context);
				 sc.setError(msg.getObjectContext());
				 sc.put("success", false);
				 if(terminate){
					 runner.getCaller().locateTo("CreateResponse");
					 runner.stop();
				 }
			 }
			 else
				 throw exception;
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

	public Boolean getTerminate() {
		return terminate;
	}

	public void setTerminate(Boolean terminate) {
		this.terminate = terminate;
	}
	
}

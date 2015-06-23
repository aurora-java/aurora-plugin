package aurora.plugin.ntlm;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uncertain.composite.CompositeMap;

import jcifs.Config;
import jcifs.UniAddress;
import jcifs.http.NtlmSsp;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbSession;
import jcifs.util.Base64;

public class NtlmAuthenticator {
	private String defaultDomain;
	private String domainController;
	private String realm;
	private boolean offerBasic;
	NtlmConfig ntlmConfig;

	public NtlmAuthenticator(NtlmConfig ntlmConfig) {
		this.ntlmConfig = ntlmConfig;
		offerBasic=ntlmConfig.getEnableBasic();
	}

	public NtlmPasswordAuthentication authenticate(HttpServletRequest req,
			HttpServletResponse resp) throws IOException, ServletException {
		UniAddress dc;
		String msg;
		NtlmPasswordAuthentication ntlm = null;
		msg = req.getHeader("Authorization");
		if (msg != null && msg.startsWith("NTLM ")) {
			Type1Message type1=null;
			Type3Message type3=null;
			byte[] token = Base64.decode(msg.substring(5));
			if (token[8] == 1) {
				type1 = new Type1Message(token);
				defaultDomain = type1.getSuppliedDomain();					
			} else if (token[8] == 3) {
				type3 = new Type3Message(token);
				defaultDomain = type3.getDomain();
			}
			DomainInstance domainInstance;
			if(defaultDomain==null){
				domainInstance = (DomainInstance) this.ntlmConfig.getDefaultDomainInstance();
			}else	{
				domainInstance = (DomainInstance) this.ntlmConfig.getDomainInstance(defaultDomain.toUpperCase());
			}			
			if (domainInstance == null){							
				throw new NtlmException("DomainInstance is null;defaultDomain:"+defaultDomain+";type1:"+type1 +";type3:"+type3);
			}
			domainController=domainInstance.getDomainController();
			
			Config.setProperty("jcifs.smb.client.domain", domainInstance.getDomain());
			Config.setProperty("jcifs.smb.client.username", domainInstance.getUserName());
			Config.setProperty("jcifs.smb.client.password", domainInstance.getPassword());
			
			byte[] challenge;
			dc = UniAddress.getByName(domainController, true);

			challenge = SmbSession.getChallenge(dc);
			
			if ((ntlm = NtlmSsp.authenticate(req, resp, challenge)) == null) {
				return null;
			}

			SmbSession.logon(dc, ntlm);
			return ntlm;
		}else if(msg != null && msg.startsWith("Basic ") && offerBasic){
			String auth = new String(Base64.decode(msg.substring(6)),
            "US-ASCII");
		    int index = auth.indexOf(':');
		    String user = (index != -1) ? auth.substring(0, index) : auth;
		    String password = (index != -1) ? auth.substring(index + 1) :
		            "";
		    index = user.indexOf('\\');
		    if (index == -1) index = user.indexOf('/');		   
		    user = (index != -1) ? user.substring(index + 1) : user;
		    CompositeMap domianMap=this.ntlmConfig.getDomainInstances();
		    Set keySet=domianMap.keySet();
		    Iterator iterator =keySet.iterator();
		    while(iterator.hasNext()){
		    	DomainInstance instance=(DomainInstance) domianMap.get(iterator.next());
		    	Config.setProperty("jcifs.smb.client.domain", instance.getDomain());
				Config.setProperty("jcifs.smb.client.username", instance.getUserName());
				Config.setProperty("jcifs.smb.client.password", instance.getPassword());
		    	defaultDomain=instance.getDomain();
		    	domainController=instance.getDomainController();
		    	String domain = (index != -1) ? user.substring(0, index) :
			            defaultDomain;
				ntlm = new NtlmPasswordAuthentication(domain, user, password);
				dc = UniAddress.getByName( domainController, true);
				try{
					SmbSession.logon(dc, ntlm);
					return ntlm;
				}catch(SmbAuthException e){	
					e.printStackTrace();
					continue;
				}				
		    }				
		} 
		resp.setHeader("WWW-Authenticate", "NTLM");
		if(offerBasic){
			realm="Basic Authenticate Logon failure: unknown user name or bad password";
			resp.addHeader( "WWW-Authenticate", "Basic realm=\"" +
                    realm + "\"");
		}
		resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		resp.setContentLength(0);
		resp.flushBuffer();
		return null;		
	}	
}

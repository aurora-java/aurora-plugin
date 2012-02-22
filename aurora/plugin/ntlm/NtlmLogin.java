package aurora.plugin.ntlm;

import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jcifs.smb.NtlmPasswordAuthentication;

import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;
import uncertain.composite.CompositeMap;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.IProcedureManager;
import uncertain.proc.ProcedureRunner;

public class NtlmLogin extends AbstractEntry {
	IProcedureManager procedureManager;
	IObjectRegistry mObjectRegistry;
	NtlmConfig ntlmConfig;
	ILogger mLogger;

	public NtlmLogin(NtlmConfig config, IProcedureManager procedureManager,
			IObjectRegistry registry) {
		ntlmConfig = config;
		this.procedureManager = procedureManager;
		mObjectRegistry = registry;
	}

	public void run(ProcedureRunner runner) throws Exception {
		ILogger mLogger = LoggingContext.getLogger("aurora.plugin.ntlm",mObjectRegistry);
		CompositeMap context=runner.getContext();		
		HttpServiceInstance svc = (HttpServiceInstance) ServiceInstance.getInstance(context);
		
		HttpServletRequest httpRequest = svc.getRequest();
		String msg=httpRequest.getHeader("Authorization");
		
		if (httpRequest.getSession().getAttribute("user_id") == null) {
			if(context.getObject("/cookie/@JSID/@value")!=null&&!"Y".equals(context.getObject("/cookie/@IS_NTLM/@value"))){
				//如果超时，且没有域登陆过，跳过验证
				return;
			}			
			//如果不需要验证登录，则跳过验证
			mLogger.info("httpRequest Authorization:{"+msg+"}");
			if(msg==null||!msg.startsWith("NTLM")){
				context.putObject("/request/@service_name", svc.getName(),true);
				runner.call(procedureManager.loadProcedure(ntlmConfig.getProcedure()));
				Object result = context.getObject(ntlmConfig.getReturnPath());
				if (result == null) {
					mLogger.log(Level.SEVERE, ntlmConfig.getReturnPath()
							+ " is null");
					return;
				}				
				
				if (((CompositeMap) result).getChilds() != null) {
					mLogger.info(svc.getName() + " is not login required");					
					return;
				}
			}			
			
			NtlmPasswordAuthentication ntlm=authenticate(runner);
			if(ntlm==null){
				return;
			}
			String locale=httpRequest.getLocale().toString();
			String username = ntlm.getUsername().toUpperCase();			
			mLogger.info("username:" + username);
			context.putObject("/spnego/@user_name", username,true);
			context.putObject("/spnego/@status_code", "Y",true);			
			context.putObject("/spnego/@locale", locale,true);
			runner.call(procedureManager.loadProcedure(ntlmConfig.getProcedure()));		
			mLogger.info("excute procedure " + ntlmConfig.getProcedure());
		} else {			
			if ("POST".equals(httpRequest.getMethod().toUpperCase())) {
				if(msg!=null&&msg.startsWith("NTLM"))
					authenticate(runner);
			}
		}
	}

	NtlmPasswordAuthentication authenticate(ProcedureRunner runner) throws IOException {
		ILogger mLogger = LoggingContext.getLogger("aurora.plugin.ntlm",mObjectRegistry);	
		NtlmPasswordAuthentication ntlm;
		HttpServiceInstance svc = (HttpServiceInstance) ServiceInstance
				.getInstance(runner.getContext());
		HttpServletRequest httpRequest = svc.getRequest();
		HttpServletResponse httpResponse = svc.getResponse();
		String realm;
		try {
			if ((ntlm = new NtlmAuthenticator(ntlmConfig).authenticate(
					httpRequest, httpResponse)) == null) {
				mLogger.log(Level.INFO,"runner is stop;ServiceName:"+svc.getName());
				runner.stop();
				return null;
			}
		}catch(NtlmException ntlmException){
			mLogger.log(Level.WARNING,"NTLM authenticate fail;ServiceName:"+svc.getName(),ntlmException);			
			if(ntlmConfig.getEnableBasic()){
				realm="Ntlm Auth failure,Please use the basic authentication";
				httpResponse.addHeader( "WWW-Authenticate", "Basic realm=\"" +
	                    realm + "\"");
				httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				httpResponse.setContentLength(0);
				httpResponse.flushBuffer();
				runner.stop();
			}			
			return null;
		}catch (Exception e) {
			// 域验证不通过，跳入普通处理方式
			mLogger.log(Level.WARNING,"NTLM authenticate fail;ServiceName:"+svc.getName(),e);
			if(ntlmConfig.getEnableBasic()){
				realm=e.getMessage();
				httpResponse.addHeader( "WWW-Authenticate", "Basic realm=\"" +
	                    realm + "\"");
				httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				httpResponse.setContentLength(0);
				httpResponse.flushBuffer();
				runner.stop();
			}			
		    return null;
		}
		mLogger.log(Level.INFO, "NTLM authenticate domain:"+ntlm.getDomain()+";Username:"+ntlm.getUsername()+";name:"+ntlm.getName()+";IP:"+httpRequest.getRemoteHost()+"ServiceName:"+svc.getName());
		return ntlm;
	}
}

package aurora.plugin.ntlm;

import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jcifs.smb.NtlmPasswordAuthentication;

import aurora.service.ServiceContext;
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
		ServiceContext context = ServiceContext.createServiceContext(runner.getContext());
		HttpServiceInstance svc = (HttpServiceInstance) ServiceInstance
				.getInstance(context.getObjectContext());
		
		HttpServletRequest httpRequest = svc.getRequest();
		HttpServletResponse httpResponse =svc.getResponse();
			
		mLogger.info("user_id:"+httpRequest.getSession().getAttribute("user_id"));		
		
		if (httpRequest.getSession().getAttribute("user_id") == null) {
			String serviceName = context.getObjectContext().getString("service_name");
			context.getParameter().putString("service_name", serviceName);
			mLogger.info("excute procedure "+ntlmConfig.getProcedure());
			runner.call(procedureManager.loadProcedure(ntlmConfig.getProcedure()));
			Object result=context.getObjectContext().getObject(ntlmConfig.getReturnPath());
			if(result==null){
				mLogger.log(Level.SEVERE, ntlmConfig.getReturnPath()+" is null");
				return;
			}
			if(((CompositeMap)result).getChilds()!=null){
				mLogger.info(serviceName+" is not login required");
				return;
			}
			
			NtlmPasswordAuthentication ntlm = null;
			try {
				if ((ntlm = new NtlmAuthenticator(ntlmConfig).authenticate(httpRequest,
						httpResponse)) == null) {
					runner.stop();
					return;
				}
			} catch (Exception e) {
				//域验证不通过，跳入普通处理方式
				return;
			}
			String username=ntlm.getUsername();
			context.getParameter().put("user_name", username.toUpperCase());
			mLogger.info("username:"+username);
			context.getParameter().put("status_code", "Y");
			runner.call(procedureManager.loadProcedure(ntlmConfig.getProcedure()));
			mLogger.info("doLogin context:"+svc.getContextMap().toXML());			
		}
	}
}

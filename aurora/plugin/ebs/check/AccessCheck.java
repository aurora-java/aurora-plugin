package aurora.plugin.ebs.check;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



import oracle.apps.fnd.common.WebAppsContext;
import oracle.apps.fnd.common.WebRequestUtil;
import aurora.service.ServiceInstance;
import aurora.service.ServiceThreadLocal;
import aurora.service.http.AutoCrudServiceContext;
import aurora.service.http.HttpServiceInstance;
import uncertain.composite.CompositeMap;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.AbstractLocatableObject;
import uncertain.proc.ProcedureRunner;

public class AccessCheck extends AbstractLocatableObject{
	
	public static void run() throws Exception{
		CompositeMap context = ServiceThreadLocal.getCurrentThreadContext();
		if(context == null){
			context = new CompositeMap("context");
		}
		ILogger logger = LoggingContext.getLogger(context, AccessCheck.class.getCanonicalName());
		HttpServiceInstance svc = (HttpServiceInstance) ServiceInstance.getInstance(context);
		HttpServletRequest request = svc.getRequest();
		logger.log("AccessCheck..");
		logger.log("context:"+context.toXML());
		
		String requestBase = "http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
		logger.log("requestBase:"+requestBase);
		String referer = request.getHeader("referer");
		logger.log("referer:"+referer);
		if(referer != null && referer.startsWith(requestBase)){
			return;
		}
		String reqTraxId = (String)request.getAttribute("ICX_TRANSACTION_ID");
		logger.log("reqTraxId:"+reqTraxId);
		if(reqTraxId == null){
			context.putObject("/access-check/@status_code", "unauthorized", true);
		}else{
			String paraTraxId = request.getParameter("transactionid");
			logger.log("paraTraxId:"+paraTraxId);
			if(!reqTraxId.equals(paraTraxId)){
				context.putObject("/access-check/@status_code", "unauthorized", true);
			}
		}
	}
	
	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		HttpServiceInstance svc = (HttpServiceInstance) ServiceInstance.getInstance(context);
		HttpServletRequest request = svc.getRequest();
		HttpServletResponse response = svc.getResponse();
		WebAppsContext webAppcontext = WebRequestUtil.validateContext(request, response);
		int validId = WebRequestUtil.verifyTransaction(request, response, webAppcontext);
		if(validId == -1){
			context.putObject("/access-check/@status_code", "unauthorized", true);
			//String errorUrl = "http://syfdemo.ml.com:8000/OA_HTML/jsp/fnd/fnderror.jsp?text=此功能不是当前用户的有效责任。请联系您的系统管理员。";
		}
	}

}

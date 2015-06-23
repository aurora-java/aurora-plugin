package aurora.plugin.sso;

import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uncertain.composite.CompositeMap;
import uncertain.core.IContainer;
import uncertain.event.Configuration;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.IProcedureManager;
import uncertain.proc.Procedure;
import aurora.application.action.HttpSessionCopy;
import aurora.application.features.HttpRequestTransfer;
import aurora.service.IServiceFactory;
import aurora.service.ServiceInvoker;
import aurora.service.ServiceThreadLocal;
import aurora.service.http.HttpServiceInstance;
import aurora.service.http.WebContextInit;

public class SSOLogin {
	public final static String PLUGIN = SSOLogin.class.getCanonicalName();
	public final static String DEFAULT_LOGIN_PROC = "init.auto_login";
	private String autoLoginProc;
	ServletContext context;
	IObjectRegistry objectRegistry;
	ILogger logger;

	public SSOLogin(ServletContext context) {
		this.context = context;
		setObjectRegistry();
		setLogger();
	}

	public String getAutoLoginProc() {
		if(autoLoginProc==null||"".equals(autoLoginProc))
			autoLoginProc=DEFAULT_LOGIN_PROC;
		return autoLoginProc;
	}

	public void setAutoLoginProc(String autoLoginProc) {
		this.autoLoginProc = autoLoginProc;
	}

	public IObjectRegistry getObjectRegistry() {
		return objectRegistry;
	}

	public void setObjectRegistry() {
		objectRegistry = WebContextInit.getUncertainEngine(context)
				.getObjectRegistry();
	}

	public ILogger getLogger() {
		return logger;
	}

	public void setLogger() {
		logger = LoggingContext.getLogger(PLUGIN, getObjectRegistry());
	}

	public void doLogin(HttpServletRequest request,
			HttpServletResponse response, String loginName) throws IllegalAccessException {
		IProcedureManager procedureManager = (IProcedureManager) getObjectRegistry()
				.getInstanceOfType(IProcedureManager.class);
		IServiceFactory serviceFactory = (IServiceFactory) getObjectRegistry()
				.getInstanceOfType(IServiceFactory.class);
		Procedure proc = procedureManager.loadProcedure(getAutoLoginProc());
		CompositeMap auroraContext = new CompositeMap("sso_conext");
		auroraContext.createChild("parameter").put("user_name", loginName);

		HttpServiceInstance svc = createHttpService(autoLoginProc, request,
				response, procedureManager, auroraContext);

		ServiceThreadLocal.setCurrentThreadContext(auroraContext);
		
		try {
			ServiceInvoker.invokeProcedureWithTransaction(autoLoginProc, proc,
					serviceFactory, svc, auroraContext);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "***loginName:"+loginName+"***"+e.getMessage(), e);
			throw new IllegalAccessException(e.getMessage());
		}

		ServiceThreadLocal.setCurrentThreadContext(auroraContext);
		HttpRequestTransfer.copyRequest(svc);
		HttpSessionCopy.copySession(auroraContext, request.getSession(false));
	}

	public HttpServiceInstance createHttpService(String service_name,
			HttpServletRequest request, HttpServletResponse response,
			IProcedureManager procedureManager, CompositeMap context) {
		HttpServiceInstance svc = new HttpServiceInstance(service_name,
				procedureManager);
		svc.setRequest(request);
		svc.setResponse(response);
		svc.setContextMap(context);
		svc.setName(service_name);
		HttpRequestTransfer.copyRequest(svc);
		HttpSessionCopy.copySession(svc.getContextMap(),
				request.getSession(false));
		IContainer container = (IContainer) getObjectRegistry()
				.getInstanceOfType(IContainer.class);
		Configuration config = (Configuration) container.getEventDispatcher();
		if (config != null)
			svc.setRootConfig(config);
		return svc;
	}
}

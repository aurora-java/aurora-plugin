package aurora.plugin.sso.cas;

import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.cas.client.util.AssertionHolder;

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

public class AutoSetUserFilter implements Filter {

	public final static String PLUGIN = AutoSetUserFilter.class.getCanonicalName();
	public final static String AURORA_USER_LOGIN = "_aurora_user_login_";
	public final static String DEFAULT_LOGIN_PROC = "init.auto_login";

	private String afterLoginRedirectUrl;
	private String autoLoginProc;

	IObjectRegistry objectRegistry;
	ILogger logger;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		afterLoginRedirectUrl = getPropertyFromInitParams(filterConfig, "afterLoginRedirectUrl", null);

		autoLoginProc = getPropertyFromInitParams(filterConfig, "autoLoginProc", null);
		if(autoLoginProc == null)
			autoLoginProc = DEFAULT_LOGIN_PROC;
		
		objectRegistry = WebContextInit.getUncertainEngine(filterConfig.getServletContext()).getObjectRegistry();
		logger = LoggingContext.getLogger(PLUGIN, objectRegistry);

	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest)servletRequest;
		HttpServletResponse response = (HttpServletResponse)servletResponse;
		
		HttpSession session = request.getSession();
		Object user_login = session.getAttribute(AURORA_USER_LOGIN);
		if (user_login != null){
			filterChain.doFilter(request, response);
			return;
		}
		String loginName = AssertionHolder.getAssertion().getPrincipal().getName();
		try {
			executeLoginProc(request,response,loginName);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "executeLoginProc error:", e);
			return;
		}
		session.setAttribute(AURORA_USER_LOGIN, Boolean.TRUE);
		if(afterLoginRedirectUrl != null)
			response.sendRedirect(afterLoginRedirectUrl);
	}

	public void executeLoginProc(HttpServletRequest request,HttpServletResponse response,String loginName) throws Exception {
		IProcedureManager procedureManager = (IProcedureManager) objectRegistry.getInstanceOfType(IProcedureManager.class);
		IServiceFactory serviceFactory = (IServiceFactory) objectRegistry.getInstanceOfType(IServiceFactory.class);
		Procedure proc = procedureManager.loadProcedure(autoLoginProc);
		CompositeMap auroraContext = new CompositeMap("sso_conext");
		auroraContext.createChild("parameter").put("user_name", loginName);
		
		HttpServiceInstance svc = createHttpService(autoLoginProc,request,response,procedureManager,auroraContext);
		
		ServiceThreadLocal.setCurrentThreadContext(auroraContext);
		ServiceInvoker.invokeProcedureWithTransaction(autoLoginProc, proc, serviceFactory,svc,auroraContext);
		
		ServiceThreadLocal.setCurrentThreadContext(auroraContext);
		HttpRequestTransfer.copyRequest(svc);
        HttpSessionCopy.copySession(auroraContext, request.getSession(false));
	}
	public HttpServiceInstance createHttpService(String service_name,HttpServletRequest request, HttpServletResponse response,IProcedureManager procedureManager,CompositeMap context){
    	HttpServiceInstance svc = new HttpServiceInstance(service_name,procedureManager);
    	svc.setRequest(request);
        svc.setResponse(response);
        svc.setContextMap(context);
        svc.setName(service_name);
        HttpRequestTransfer.copyRequest(svc);
        HttpSessionCopy.copySession(svc.getContextMap(), request.getSession(false));
        IContainer container = (IContainer) objectRegistry.getInstanceOfType(IContainer.class);
        Configuration config = (Configuration)container.getEventDispatcher();
        if(config!=null)
        	svc.setRootConfig(config);
        return svc;
	}

	@Override
	public void destroy() {

	}

	protected final String getPropertyFromInitParams(FilterConfig filterConfig, String propertyName, String defaultValue) {
		String value = filterConfig.getInitParameter(propertyName);

		if (value != null)
			return value;

		value = filterConfig.getServletContext().getInitParameter(propertyName);

		if (value != null)
			return value;

		return defaultValue;
	}

}

package aurora.plugin.spnego;

import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ietf.jgss.GSSException;

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

public class SpnegoLogin extends AbstractEntry {
	SpnegoConfig config;
	IProcedureManager procedureManager;
	IObjectRegistry mObjectRegistry;
	public SpnegoLogin(SpnegoConfig config, IProcedureManager procedureManager,IObjectRegistry registry) {
		this.config = config;
		this.procedureManager = procedureManager;
		mObjectRegistry=registry;
	}

	public void run(ProcedureRunner runner) throws Exception {
		ILogger mLogger = LoggingContext.getLogger("aurora.plugin.spnego",mObjectRegistry);
		ServiceContext context = ServiceContext.createServiceContext(runner
				.getContext());
		HttpServiceInstance svc = (HttpServiceInstance) ServiceInstance
				.getInstance(context.getObjectContext());
		HttpServletRequest httpRequest = svc.getRequest();
		String username;
		mLogger.info("user_id:"+httpRequest.getSession().getAttribute("user_id"));
		if (httpRequest.getSession().getAttribute("user_id") == null) {
			String serviceName = httpRequest.getRequestURI().substring(
					httpRequest.getContextPath().length() + 1);

			context.getParameter().putString("service_name", serviceName);
			mLogger.info("excute procedure "+config.getProcedure());
			doLogin(runner);
			Object result=svc.getContextMap().getObject(config.getLoginchekpath());
			if(result==null){
				mLogger.log(Level.SEVERE, config.getLoginchekpath()+" is null");
				return;
			}
			List<CompositeMap> list = ((CompositeMap)result).getChilds();
			mLogger.info("context:"+svc.getContextMap().toXML());
			if (list!=null){
				mLogger.info(serviceName+" is not login required");
				return;
			}

			SpnegoHttpServletResponse spnegoResponse = new SpnegoHttpServletResponse(
					(HttpServletResponse) svc.getResponse());
			// client/caller principal

			final SpnegoPrincipal principal;
			try {
				principal = config.getSpnegoAuthenticator().authenticate(
						httpRequest, spnegoResponse);

			} catch (GSSException gsse) {
				// LOGGER.severe("HTTP Authorization Header="
				// + httpRequest.getHeader(Constants.AUTHZ_HEADER));
				throw new Exception(gsse);
			}

			// context/auth loop not yet complete
			if (spnegoResponse.isStatusSet()) {
				runner.stop();
				return;
			}

			// assert
			if (null == principal) {
				spnegoResponse.setStatus(
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR, true);
				runner.stop();
				return;
			}
			httpRequest = new SpnegoHttpServletRequest(httpRequest, principal);
			username = httpRequest.getRemoteUser();			
			context.getParameter().put("user_name", username.toUpperCase());
			mLogger.info("username:"+username);
			context.getParameter().put("status_code", "Y");
			doLogin(runner);
			mLogger.info("doLogin context:"+svc.getContextMap().toXML());
		}
	}

	void doLogin(ProcedureRunner runner) throws Exception {
		String procedure = config.getProcedure();
		runner.call(procedureManager.loadProcedure(procedure));
	}
}

package aurora.plugin.spnego;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ietf.jgss.GSSException;

import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;
import uncertain.composite.CompositeMap;
import uncertain.proc.AbstractEntry;
import uncertain.proc.IProcedureManager;
import uncertain.proc.ProcedureRunner;

public class SpnegoLogin extends AbstractEntry {
	SpnegoConfig config;
	IProcedureManager procedureManager;

	public SpnegoLogin(SpnegoConfig config, IProcedureManager procedureManager) {
		this.config = config;
		this.procedureManager = procedureManager;
	}

	public void run(ProcedureRunner runner) throws Exception {
		ServiceContext context = ServiceContext.createServiceContext(runner
				.getContext());
		HttpServiceInstance svc = (HttpServiceInstance) ServiceInstance
				.getInstance(context.getObjectContext());
		HttpServletRequest httpRequest = svc.getRequest();
		String username;
		if (httpRequest.getSession().getAttribute("user_id") == null) {
			String serviceNmae = httpRequest.getRequestURI().substring(
					httpRequest.getContextPath().length() + 1);

			context.getParameter().putString("service_name", serviceNmae);
			doLogin(runner);
			List<CompositeMap> list = svc.getContextMap().getChild("spnego")
					.getChilds();
			if (list!=null)
				return;

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
			context.getParameter().put("status_code", "Y");
			doLogin(runner);
		}
	}

	void doLogin(ProcedureRunner runner) throws Exception {
		String procedure = config.getProcedure();
		runner.call(procedureManager.loadProcedure(procedure));
	}
}

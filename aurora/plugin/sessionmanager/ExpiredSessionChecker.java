package aurora.plugin.sessionmanager;

import javax.servlet.http.HttpSession;

import aurora.service.IServiceFactory;
import aurora.service.ServiceInstance;
import aurora.service.ServiceInvoker;
import aurora.service.http.HttpServiceInstance;
import uncertain.composite.CompositeMap;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.IProcedureManager;
import uncertain.proc.Procedure;
import uncertain.proc.ProcedureConfigManager;
import uncertain.proc.ProcedureRunner;

public class ExpiredSessionChecker extends AbstractEntry {

	private IObjectRegistry objectRegistry;
	private ExpiredSessionRegistry esRegistry;

	private CompositeMap postChecker;

	String resultPath = "/parameter/@expired_aurora_session_id";

	public ExpiredSessionChecker(IObjectRegistry objectRegistry, ExpiredSessionRegistry esRegistry) {
		this.objectRegistry = objectRegistry;
		this.esRegistry = esRegistry;
	}

	@Override
	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		HttpServiceInstance svc = (HttpServiceInstance) ServiceInstance.getInstance(context);
		HttpSession session = svc.getRequest().getSession(false);
		if (session == null)
			return;
		Long sessionIdLong = (Long)session.getAttribute("aurora_session_id");
		if (sessionIdLong == null)
			return;
		String sessionId = sessionIdLong.toString();
		boolean isExpiredSession = esRegistry.isExpiredSession(sessionId);
		if (!isExpiredSession)
			return;
		session.invalidate();
		esRegistry.removeExpiredSession(sessionId);
		if (postChecker == null)
			return;
		context.putObject("/oneUserOneSession/@is_expired", "Y",true);
		executeProc(context, sessionId);
	}

	public String getResultPath() {
		return resultPath;
	}

	public void setResultPath(String resultPath) {
		this.resultPath = resultPath;
	}

	public CompositeMap getPostChecker() {
		return postChecker;
	}

	public void setPostChecker(CompositeMap postChecker) {
		this.postChecker = postChecker;
	}

	private void executeProc(CompositeMap context, String sessionId) throws Exception {
		IProcedureManager procedureManager = (IProcedureManager) objectRegistry.getInstanceOfType(IProcedureManager.class);
		IServiceFactory serviceFactory = (IServiceFactory) objectRegistry.getInstanceOfType(IServiceFactory.class);
		CompositeMap proc_config = ProcedureConfigManager.createConfigNode("procedure");
		proc_config.addChilds(postChecker.getChilds());
		Procedure proc = procedureManager.createProcedure(proc_config);
		CompositeMap fakeContext = (CompositeMap) context.clone();
		fakeContext.putObject(resultPath, sessionId, true);

		// ServiceThreadLocal.setCurrentThreadContext(auroraContext);
		ServiceInvoker.invokeProcedureWithTransaction("postChecker", proc, serviceFactory, fakeContext);
	}

}

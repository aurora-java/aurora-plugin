package aurora.plugin.quartz;

import org.quartz.JobExecutionContext;

import aurora.service.IServiceFactory;
import aurora.service.ServiceInvoker;
import aurora.service.ServiceThreadLocal;

import uncertain.composite.CompositeMap;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.IProcedureManager;
import uncertain.proc.Procedure;

public class ProcedureInvoke {
	IObjectRegistry registry;
	String jobName;
	String serviceName;

	public ProcedureInvoke() {
		registry = SchedulerConfig.getObjectRegistry();
	}

	public void run(JobExecutionContext context) throws Exception {
		AuroraJobDetail detail = (AuroraJobDetail) context.getJobDetail();
		CompositeMap config = detail.getConfig();
		jobName = config.getString("name");
		serviceName = config.getString("procedure");
		IProcedureManager procedureManager = (IProcedureManager) registry
				.getInstanceOfType(IProcedureManager.class);

		IServiceFactory serviceFactory = (IServiceFactory) registry
				.getInstanceOfType(IServiceFactory.class);		
		Procedure proc = procedureManager.loadProcedure(serviceName);
		CompositeMap auroraContext = new CompositeMap();
		auroraContext.createChild("parameter").put("job_name", jobName);
		ServiceThreadLocal.setCurrentThreadContext(auroraContext);
		ServiceInvoker.invokeProcedureWithTransaction(serviceName, proc,
				serviceFactory, auroraContext);
	}
}

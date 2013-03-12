package aurora.plugin.export.task;

import uncertain.composite.CompositeMap;
import uncertain.event.Configuration;
import uncertain.event.IParticipantManager;
import uncertain.ocm.IObjectRegistry;
import uncertain.ocm.OCManager;
import uncertain.proc.AbstractEntry;
import uncertain.proc.IProcedureManager;
import uncertain.proc.Procedure;
import uncertain.proc.ProcedureRunner;
import aurora.database.service.IDatabaseServiceFactory;
import aurora.service.IConfigurableService;
import aurora.service.ServiceInstance;
import aurora.service.controller.ControllerProcedures;
import aurora.service.http.AbstractFacadeServlet;


//模拟执行SVC
public class ModelExportTask extends AbstractEntry {

	// It need define export category in service-listener.config.
	public final static String PARTICIPANT_LIST_EXPORT_CATEGORY = "export";
	public final static String KEY_COLUMN_CONFIG = "_column_config_";
	public final static String KEY_FILE_NAME = "_file_name_";
	public final static String KEY_CHARSET = "GBK";
	public final static String KEY_PROMPT = "prompt";
	public final static String KEY_DATA_INDEX = "name";
	public final static String KEY_COLUMN = "column";
	public final static String KEY_WIDTH = "width";
	public final static String KEY_GENERATE_STATE_TASK = "_generate_state_task";
	public final static String KEY_GENERATE_STATE = "_generate_state";
	public final static String KEY_FORMAT = "_format";
	public final static String KEY_ENABLETASK = "enableTask";

	public final String KEY_URL = "${/parameter/@url}";

	private IObjectRegistry mObjectRegistry;
	private CompositeMap context;

	public ModelExportTask(IObjectRegistry registry, OCManager ocManager, IDatabaseServiceFactory databaseServiceFactory) {
		this.mObjectRegistry = registry;
	}

	@Override
	public void run(ProcedureRunner runner) throws Exception {
		context = runner.getContext();
		CompositeMap parameter = context.getChild("parameter");
		if (parameter != null) {
			parameter.remove(KEY_GENERATE_STATE_TASK);
			parameter.put(ModelExportTask.KEY_GENERATE_STATE, true);
			parameter.put(ModelExportTask.KEY_ENABLETASK, true);
		}
		CompositeMap parsedSvcConfigNode = context.getChild("parsedSvcConfig");
		if (parsedSvcConfigNode != null && parsedSvcConfigNode.getChilds() != null) {
			CompositeMap reportProcedure = (CompositeMap) parsedSvcConfigNode.getChilds().get(0);
			ServiceInstance svc = ServiceInstance.getInstance(context);
			svc.setServiceConfigData(reportProcedure);
			try {
				service();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// copy from AbstractFacadeServlet
	protected void service() throws Exception {
		boolean is_success = true;
		IProcedureManager mProcManager = (IProcedureManager) mObjectRegistry.getInstanceOfType(IProcedureManager.class);
		ServiceInstance svc = ServiceInstance.getInstance(context);
		if (is_success) {
			svc.getController().setProcedureName(ControllerProcedures.INVOKE_SERVICE);
			IParticipantManager mParticipantManager = (IParticipantManager) mObjectRegistry.getInstanceOfType(IParticipantManager.class);
			Configuration mServiceParentConfig = null;
			if (mParticipantManager != null) {
				mServiceParentConfig = mParticipantManager.getParticipantsAsConfig(PARTICIPANT_LIST_EXPORT_CATEGORY);
			}
			svc.setRootConfig(mServiceParentConfig);
			Procedure proc = AbstractFacadeServlet.getProcedureToRun(mProcManager, svc);
			if (proc != null) {
				if (svc instanceof IConfigurableService) {
					IConfigurableService cfsvc = (IConfigurableService) svc;
					if (!cfsvc.isConfigParsed())
						cfsvc.parseConfig();
				}
				// ControllerProcedures.INVOKE_SERVICE will check @success
				// value;
				context.put("success", true);
				svc.invoke(proc);
			}
		}
	}
}

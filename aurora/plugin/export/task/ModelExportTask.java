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
import aurora.plugin.export.ModelOutput;
import aurora.service.IConfigurableService;
import aurora.service.ServiceInstance;
import aurora.service.controller.ControllerProcedures;
import aurora.service.http.AbstractFacadeServlet;

public class ModelExportTask extends AbstractEntry {
	
	public final String PARTICIPANT_LIST_EXPORT_CATEGORY = "export";//It need define export category in service-listener.config.
	public final String KEY_COLUMN_CONFIG = "_column_config_";
	public final String KEY_FILE_NAME = "_file_name_";
	public final String KEY_CHARSET = "GBK";
	public final String KEY_PROMPT = "prompt";
	public final String KEY_DATA_INDEX = "name";
	public final String KEY_COLUMN = "column";
	public final String KEY_WIDTH = "width";
	public final String KEY_GENERATE_STATE = "_generate_state_task";
	public final String KEY_FORMAT = "_format";
	public final String KEY_EXCEL = "xls";

	public final String KEY_URL = "${/parameter/@url}";

	private IObjectRegistry mObjectRegistry;
	/*private File excelDir;
	private OCManager mOcManager;
	private IDatabaseServiceFactory mDatabaseServiceFactory;
	private String object_name;*/
	private CompositeMap context;

	public ModelExportTask(IObjectRegistry registry, OCManager ocManager, IDatabaseServiceFactory databaseServiceFactory) {
		this.mObjectRegistry = registry;
//		this.mOcManager = ocManager;
//		this.mDatabaseServiceFactory = databaseServiceFactory;
	}

	@Override
	public void run(ProcedureRunner runner) throws Exception {
		context = runner.getContext();
		CompositeMap parameter = context.getChild("parameter");
		if (parameter != null) {
			parameter.remove(KEY_GENERATE_STATE);
			parameter.put(ModelOutput.KEY_GENERATE_STATE, true);
			parameter.put(ModelOutput.KEY_ENABLETASK, true);
		}
		CompositeMap parsedSvcConfigNode = context.getChild("parsedSvcConfig");
		if (parsedSvcConfigNode != null && parsedSvcConfigNode.getChilds() != null) {
			CompositeMap excelProcedure = (CompositeMap) parsedSvcConfigNode.getChilds().get(0);
			ServiceInstance svc = ServiceInstance.getInstance(context);
			svc.setServiceConfigData(excelProcedure);
			try{
			service();
			}catch(Exception e){
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
				//ControllerProcedures.INVOKE_SERVICE will check @success value;
				context.put("success", true);
				svc.invoke(proc);
			}
		}
	}

/*	public void fetchData(ProcedureRunner parent_runner) throws Exception {
		String uri = TextParser.parse(KEY_URL, context);
		if (uri == null)
			throw new IllegalArgumentException("Can not find url in parameter!");
		String[] args = uri.split("/");
		if (args.length < 4) {
			throw new IllegalArgumentException("Invalid request format");
		}
		int start_index = 0;
		for (int i = 0; i < args.length; i++) {
			String tmp = args[i];
			if ("autocrud".equals(tmp)) {
				start_index = i;
				break;
			}
		}
		object_name = args[(start_index + 1)];
		String operation_expression = args[(start_index + 2)];
		int parameter = operation_expression.indexOf("?");
		if (parameter != -1) {
			operation_expression = operation_expression.substring(0, parameter);
		}
		CompositeMap config = createAction(object_name, operation_expression, context);
		runProc(config, parent_runner);
	}

	public void preInvokeService(ServiceContext context) throws Exception {
		if (excelDir == null) {
			IExcelTask task = (IExcelTask) mObjectRegistry.getInstanceOfType(IExcelTask.class);
			if (task == null)
				throw BuiltinExceptionFactory.createInstanceNotFoundException(null, IExcelTask.class, this.getClass().getCanonicalName());
			File dir = new File(task.getDir());
			if (!dir.exists())
				throw new IllegalArgumentException("File " + task.getDir() + " is not exits!");
			if (!dir.isDirectory())
				throw new IllegalArgumentException("File " + task.getDir() + " is not directory!");
			excelDir = dir;
		}
	}

	private CompositeMap createAction(String model, String modelaction, CompositeMap context) throws Exception {
		BusinessModel bm = mDatabaseServiceFactory.getModelFactory().getModelForRead(model);
		if (bm == null)
			throw new ServletException("Can't load model:" + model);
		CompositeMap action_config = null;
		if ("query".equals(modelaction)) {
			ModelQueryConfig mq = ActionConfigManager.createModelQuery(model);
			mq.setParameters(context.getChild("parameter"));
			mq.setFetchAll(true);
			// if(KEY_EXCEL.equals(context.getChild("parameter").getString(KEY_FORMAT)))
			// mq.getObjectContext().createChildByTag("consumer").createChildByTag("output-excel").setNameSpaceURI("http://www.aurora-framework.org/application");
			action_config = mq.getObjectContext();
		} else if ("update".equals(modelaction)) {
			action_config = ActionConfigManager.createModelUpdate(model);
		} else if ("insert".equals(modelaction)) {
			action_config = ActionConfigManager.createModelInsert(model);
		} else if ("delete".equals(modelaction)) {
			action_config = ActionConfigManager.createModelDelete(model);
		} else if ("batch_update".equals(modelaction)) {
			action_config = ActionConfigManager.createModelBatchUpdate(model);
		} else if ("execute".equals(modelaction)) {
			action_config = ActionConfigManager.createModelAction("model-execute", model);
		} else
			throw new ServletException("Unknown command:" + modelaction);
		return action_config;
	}

	private void runProc(CompositeMap config, ProcedureRunner parent_runner) throws Exception {
		CompositeMap proc_config = ProcedureConfigManager.createConfigNode("procedure");
		proc_config.addChild(config);
		proc_config.setSourceFile(config.getSourceFile());
		Procedure proc = (Procedure) mOcManager.createObject(proc_config);
		ProcedureRunner runner = parent_runner.spawn(proc);
		runner.run();
		runner.checkAndThrow();
	}

	public void preCreateSuccessResponse(ServiceContext context) throws Exception {
		CompositeMap parameter = context.getParameter();
		// get ILocalizedMessageProvider
		IMessageProvider msgProvider = (IMessageProvider) mObjectRegistry.getInstanceOfType(IMessageProvider.class);
		String langString = context.getSession().getString("lang", "ZHS");
		ILocalizedMessageProvider localMsgProvider = msgProvider.getLocalizedMessageProvider(langString);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String date = dateFormat.format(new Date());
		String fileName = parameter.getString(KEY_FILE_NAME, "excel_") + date + "_" + System.currentTimeMillis() + ".xls";
		ExcelExportImpl excelFactory = new ExcelExportImpl(localMsgProvider);
		File excel = new File(excelDir, fileName);
		if (excel.createNewFile()) {
			OutputStream os = new FileOutputStream(excel);
			try {
				excelFactory.createExcel(getExportData(context), getColumnConfig(context), os, null);
			} finally {
				if (os != null) {
					os.flush();
					os.close();
				}
			}
		}
		parameter.put("file_path", excel.getCanonicalPath());
	}

	CompositeMap getExportData(ServiceContext context) throws ServletException {
		ServiceInstance svc = ServiceInstance.getInstance(context.getObjectContext());
		CompositeMap configData = svc.getServiceConfigData();
		String return_path = null;
		if (configData != null) {
			return_path = (String) configData.getObject(ServiceOutputConfig.KEY_SERVICE_OUTPUT + "/@" + ServiceOutputConfig.KEY_OUTPUT);
		}
		if (return_path == null)
			return_path = object_name;
		CompositeMap exportData = (CompositeMap) context.getModel().getObject(return_path);
		return exportData;
	}

	CompositeMap getColumnConfig(ServiceContext context) throws ServletException {
		ILogger mLogger = LoggingContext.getLogger("aurora.plugin.export.task", mObjectRegistry);
		CompositeMap column_config = (CompositeMap) context.getParameter().getObject(KEY_COLUMN_CONFIG + "/" + KEY_COLUMN);
		if (column_config == null) {
			mLogger.log(Level.SEVERE, "_column_config_ must be defined");
			throw new ServletException("_column_config_ must be defined");
		}
		return column_config;
	}
*/
}

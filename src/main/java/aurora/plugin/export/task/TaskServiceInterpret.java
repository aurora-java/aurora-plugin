package aurora.plugin.export.task;

import uncertain.composite.CompositeMap;
import uncertain.event.EventModel;
import uncertain.event.RuntimeContext;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.ocm.IObjectRegistry;
import uncertain.ocm.OCManager;
import aurora.application.features.msg.IMessageDispatcher;
import aurora.application.features.msg.Message;
import aurora.application.task.AsyncTask;
import aurora.application.task.TaskHandler;
import aurora.database.service.IDatabaseServiceFactory;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;


//适用于Grid数据的导出：添加一个后台任务
public class TaskServiceInterpret {

	public final String KEY_GENERATE_STATE = "_generate_state_task";
	private IObjectRegistry mObjectRegistry;
	private OCManager mOcManager;

	public TaskServiceInterpret(IObjectRegistry registry, OCManager ocManager) {
		mObjectRegistry = registry;
		this.mOcManager = ocManager;
	}

	public int preInvokeService(ServiceContext servicContext) throws Exception {
		CompositeMap context = servicContext.getObjectContext();
		CompositeMap parameter = servicContext.getParameter();
		if (!parameter.getBoolean(KEY_GENERATE_STATE, false))
			return EventModel.HANDLE_NORMAL;
		IReportTask reportTask = (IReportTask) mObjectRegistry.getInstanceOfType(IReportTask.class);
		if (reportTask == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(null, IReportTask.class, this.getClass().getCanonicalName());
		IDatabaseServiceFactory databaseServiceFactory = (IDatabaseServiceFactory) mObjectRegistry
				.getInstanceOfType(IDatabaseServiceFactory.class);
		if (databaseServiceFactory == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(null, IDatabaseServiceFactory.class, this.getClass()
					.getCanonicalName());

		ServiceInstance svc = ServiceInstance.getInstance(servicContext.getObjectContext());
		CompositeMap parsedSvcConfig = svc.getServiceConfigData();
		CompositeMap child = context.createChild("parsedSvcConfig");
		if (parsedSvcConfig != null)
			child.addChild(parsedSvcConfig);

		// create new task and record on database
		CompositeMap reportTaskTemplate = reportTask.getReportTaskTemplate();
		AsyncTask newReportTask = (AsyncTask) mOcManager.createObject(reportTaskTemplate);
		newReportTask.execute(context);

		// send transaction Type message
		IMessageDispatcher messageDispatcher = (IMessageDispatcher) RuntimeContext.getInstance(context).getInstanceOfType(
				IMessageDispatcher.class);
		if (messageDispatcher == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(null, IMessageDispatcher.class, this.getClass()
					.getCanonicalName());
		Message msg = new Message(TaskHandler.DEFAULT_MESSAGE, null);
		messageDispatcher.send(TaskHandler.DEFAULT_TOPIC, msg, context);
		return EventModel.HANDLE_NORMAL;
	}

	public int preCreateSuccessResponse(ServiceContext context) throws Exception {
		CompositeMap parameter = context.getParameter();
		if (!parameter.getBoolean(KEY_GENERATE_STATE, false))
			return EventModel.HANDLE_NORMAL;
		return EventModel.HANDLE_STOP;
	}
}

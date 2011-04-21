package aurora.plugin.export;

import java.util.Iterator;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import aurora.application.config.BaseServiceConfig;
import aurora.i18n.ILocalizedMessageProvider;
import aurora.i18n.IMessageProvider;
import aurora.plugin.export.xlsx.poi.ExcelFactoryImpl;
import aurora.presentation.component.std.config.DataSetConfig;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.ServiceOutputConfig;
import aurora.service.http.HttpServiceInstance;
import uncertain.composite.CompositeMap;
import uncertain.event.EventModel;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;

public class ModelExport {
	public final String KEY_COLUMN_CONFIG = "_column_config_";
	public final String KEY_FILE_NAME = "_file_name_";
	public final String KEY_CHARSET = "GBK";
	public final String KEY_PROMPT = "prompt";
	public final String KEY_DATA_INDEX = "name";
	public final String KEY_COLUMN = "column";
	public final String KEY_WIDTH = "width";
	public final String KEY_GENERATE_STATE = "_generate_state";
	public final String KEY_FORMAT = "_format";

	IObjectRegistry mObjectRegistry;

	public ModelExport(IObjectRegistry registry) {
		mObjectRegistry = registry;
	}

	public int preInvokeService(ServiceContext context) throws Exception {
		if (!context.getParameter().getBoolean(KEY_GENERATE_STATE, false))
			return EventModel.HANDLE_NORMAL;
		ILogger mLogger = LoggingContext.getLogger("aurora.plugin.export",mObjectRegistry);
		ServiceInstance svc = ServiceInstance.getInstance(context.getObjectContext());

		// 修改fetchall为ture
		CompositeMap config = svc.getServiceConfigData().getChild(
				BaseServiceConfig.KEY_INIT_PROCEDURE);
		if (config == null) {
			mLogger.log(Level.SEVERE, "init-procedure tag must be defined");
			throw new ServletException("init-procedure tag must be defined");
		}
		Iterator iterator = config.getChildIterator();
		CompositeMap modelQueryMap;
		if (iterator != null) {
			while (iterator.hasNext()) {
				modelQueryMap = (CompositeMap) iterator.next();
				if ("model-query".equals(modelQueryMap.getName()))
					modelQueryMap.putBoolean(DataSetConfig.PROPERTITY_FETCHALL,true);
			}
		}

		return EventModel.HANDLE_NORMAL;
	}

	public int preCreateSuccessResponse(ServiceContext context)
			throws Exception {
		CompositeMap parameter = context.getParameter();
		if (!parameter.getBoolean(KEY_GENERATE_STATE, false))
			return EventModel.HANDLE_NORMAL;
		
		//get ILocalizedMessageProvider
		IMessageProvider msgProvider = (IMessageProvider) mObjectRegistry
				.getInstanceOfType(IMessageProvider.class);
		String langString = context.getSession().getString("lang");
		ILocalizedMessageProvider localMsgProvider = msgProvider.getLocalizedMessageProvider(langString);

		ServiceInstance svc = ServiceInstance.getInstance(context.getObjectContext());		
		HttpServletResponse response = ((HttpServiceInstance) svc).getResponse();

		String fileName = parameter.getString(KEY_FILE_NAME, "excel");
		response.setContentType("application/vnd.ms-excel");
		response.setCharacterEncoding(KEY_CHARSET);
		response.setHeader("Content-Disposition", "attachment; filename=\""+ fileName + ".xls\"");
		ExcelFactoryImpl excelFactory = new ExcelFactoryImpl(localMsgProvider);
		excelFactory.createExcel(getExportData(context), getColumnConfig(context),
				response.getOutputStream());

		return EventModel.HANDLE_STOP;
	}
	
	CompositeMap getExportData(ServiceContext context) throws ServletException{
		ILogger mLogger = LoggingContext.getLogger("aurora.plugin.export",mObjectRegistry);
		ServiceInstance svc = ServiceInstance.getInstance(context.getObjectContext());
		String return_path = (String) svc.getServiceConfigData().getObject(
				ServiceOutputConfig.KEY_SERVICE_OUTPUT + "/@"
						+ ServiceOutputConfig.KEY_OUTPUT);		
		if (return_path == null) {
			mLogger.log(Level.SEVERE, "_column_config_ must be defined");
			throw new ServletException("_column_config_ must be defined");
		}
		CompositeMap exportData = (CompositeMap) context.getModel().getObject(return_path);
		return exportData;
	}
	
	CompositeMap getColumnConfig(ServiceContext context) throws ServletException{
		ILogger mLogger = LoggingContext.getLogger("aurora.plugin.export",mObjectRegistry);
		CompositeMap column_config = (CompositeMap) context.getParameter()
		.getObject(KEY_COLUMN_CONFIG + "/" + KEY_COLUMN);
		if (column_config == null) {
			mLogger.log(Level.SEVERE,
					"service-output tag and output attibute must be defined");
			throw new ServletException(
					"service-output tag and output attibute must be defined");
		}
		return column_config;
	}	
	
}

package aurora.plugin.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import aurora.application.config.BaseServiceConfig;
import aurora.i18n.ILocalizedMessageProvider;
import aurora.i18n.IMessageProvider;
import aurora.plugin.export.task.IReportTask;
import aurora.plugin.poi.ExcelExportImpl;
import aurora.presentation.component.std.config.DataSetConfig;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.ServiceOutputConfig;
import aurora.service.http.HttpServiceInstance;
import uncertain.composite.CompositeMap;
import uncertain.event.EventModel;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;

public class ModelOutput {
	public final static String KEY_COLUMN_CONFIG = "_column_config_";
	public final static String KEY_FILE_NAME = "_file_name_";
	public final static String KEY_CHARSET = "GBK";
	public final static String KEY_PROMPT = "prompt";
	public final static String KEY_DATA_INDEX = "name";
	public final static String KEY_MERGE_COLUMN = "_merge_column_";
	public final static String KEY_COLUMN = "column";
	public final static String KEY_WIDTH = "width";
	public final static String KEY_GENERATE_STATE = "_generate_state";
	public final static String KEY_FORMAT = "_format";
	public final static String KEY_SEPARATOR = "separator";
	
	public final static String KEY_ENABLETASK = "enableTask";
	private File excelDir;
	
	public final static String KEY_XLSX = "xlsx";
	public final static String KEY_TXT = "txt";
	public final static String KEY_XLS = "xls";
	public final static String KEY_XLS_MEMORY = "xls_memory";


	IObjectRegistry mObjectRegistry;

	int modelQueryTagNum;

	public ModelOutput(IObjectRegistry registry) {
		mObjectRegistry = registry;
	}

	public int preInvokeService(ServiceContext context) throws Exception {
		CompositeMap parameters = context.getParameter();
		if (!parameters.getBoolean(KEY_GENERATE_STATE, false))
			return EventModel.HANDLE_NORMAL;
		ILogger mLogger = LoggingContext.getLogger("aurora.plugin.export",
				mObjectRegistry);
		ServiceInstance svc = ServiceInstance.getInstance(context
				.getObjectContext());

		// 修改fetchall为ture
		CompositeMap config = svc.getServiceConfigData().getChild(
				BaseServiceConfig.KEY_INIT_PROCEDURE);
		if (config == null) {
			mLogger.log(Level.SEVERE, "init-procedure tag must be defined");
			throw new ServletException("init-procedure tag must be defined");
		}

		String return_path = (String) svc.getServiceConfigData().getObject(
				ServiceOutputConfig.KEY_SERVICE_OUTPUT + "/@"
						+ ServiceOutputConfig.KEY_OUTPUT);
		if (return_path == null) {
			mLogger.log(Level.SEVERE, "service-output tag must be defined");
			throw new ServletException("service-output tag must be defined");
		} else {
			if (!return_path.startsWith("/"))
				return_path = "/" + return_path;
		}
		modelQueryTagNum = 0;
		if (createConsumerTag(config, return_path, parameters)) {
			if (modelQueryTagNum == 0) {
				mLogger.log(Level.SEVERE, "The path '" + return_path
						+ "' can't find model-query tag");
				throw new ServletException("The path '" + return_path
						+ "' can't find model-query tag");
			}
		}
		boolean enableTask = isEnableTask(parameters);
		if (enableTask){
			if (excelDir == null) {
				IReportTask excelTask = (IReportTask) mObjectRegistry.getInstanceOfType(IReportTask.class);
				if (excelTask == null)
					throw BuiltinExceptionFactory.createInstanceNotFoundException(null, IReportTask.class, this.getClass().getCanonicalName());
				File excelDirectory = new File(excelTask.getReportDir());
				if (!excelDirectory.exists())
					throw new IllegalArgumentException("File " + excelTask.getReportDir() + " is not exits!");
				if (!excelDirectory.isDirectory())
					throw new IllegalArgumentException("File " + excelTask.getReportDir() + " is not directory!");
				excelDir = excelDirectory;
			}
		}
		return EventModel.HANDLE_NORMAL;
	}

	boolean createConsumerTag(CompositeMap actionConfig, String return_path,
			CompositeMap parameters) {
		if (modelQueryTagNum != 0)
			return true;
		Iterator iterator = actionConfig.getChildIterator();
		if (iterator != null) {
			while (iterator.hasNext()) {
				if (createConsumerTag((CompositeMap) iterator.next(),
						return_path, parameters)) {
					break;
				}
			}
		} else {
			String rootpath = actionConfig.getString("rootpath");
			if (rootpath != null) {
				if (!rootpath.startsWith("/"))
					rootpath = "/model/" + rootpath;
				if ("model-query".equals(actionConfig.getName())
						&& return_path.equalsIgnoreCase(rootpath)) {
					actionConfig.putBoolean(DataSetConfig.PROPERTITY_FETCHALL,
							true);
					String format = parameters.getString(KEY_FORMAT);
					if (KEY_XLSX.equals(format)||KEY_XLS.equals(format)) {
						actionConfig
								.createChildByTag("consumer")
								.createChildByTag("output-excel")
								.setNameSpaceURI(
										"http://www.aurora-framework.org/application");
						modelQueryTagNum++;
						return true;
					}
					if (KEY_TXT.equals(format)) {
						actionConfig
								.createChildByTag("consumer")
								.createChildByTag("output-txt")
								.setNameSpaceURI(
										"http://www.aurora-framework.org/application");
						modelQueryTagNum++;
						return true;
					}					
				}
			}
		}
		return false;
	}

	public int preCreateSuccessResponse(ServiceContext context)
			throws Exception {
		CompositeMap parameter = context.getParameter();
		if (!parameter.getBoolean(KEY_GENERATE_STATE, false))
			return EventModel.HANDLE_NORMAL;
		if (!KEY_XLS_MEMORY.equals(parameter.getString(KEY_FORMAT)))
			return EventModel.HANDLE_STOP;
		// get ILocalizedMessageProvider
		IMessageProvider msgProvider = (IMessageProvider) mObjectRegistry
				.getInstanceOfType(IMessageProvider.class);
		String langString = context.getSession().getString("lang", "ZHS");
		ILocalizedMessageProvider localMsgProvider = msgProvider
				.getLocalizedMessageProvider(langString);

		ServiceInstance svc = ServiceInstance.getInstance(context
				.getObjectContext());
		ExcelExportImpl excelFactory = new ExcelExportImpl(localMsgProvider);
		if (!isEnableTask(parameter)) {
			HttpServletResponse response = ((HttpServiceInstance) svc).getResponse();

			String fileName = parameter.getString(KEY_FILE_NAME, "excel");
			response.setContentType("application/vnd.ms-excel");
			response.setCharacterEncoding(KEY_CHARSET);
			String userAgent = ((HttpServiceInstance) svc).getRequest().getHeader("User-Agent");
			if (userAgent != null) {
				userAgent = userAgent.toLowerCase();
				if (userAgent.indexOf("msie") != -1) {
					fileName=new String(fileName.getBytes("GBK"),"ISO-8859-1");
				}else{
					fileName=new String(fileName.getBytes("UTF-8"),"ISO-8859-1");
				}
			}
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".xls\"");
			response.setHeader("cache-control", "must-revalidate");
			response.setHeader("pragma", "public");	
			excelFactory.createExcel(getExportData(context), getColumnConfig(context), response.getOutputStream(), (CompositeMap) context
					.getParameter().getChild(this.KEY_MERGE_COLUMN));
		} else {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
			String date = dateFormat.format(new Date());
			String fileName = parameter.getString(KEY_FILE_NAME, "excel_") + date + "_" + System.currentTimeMillis() + ".xls";
			File excel = new File(excelDir, fileName);
			if (excel.createNewFile()) {
				OutputStream os = new FileOutputStream(excel);
				try {
					excelFactory.createExcel(getExportData(context), getColumnConfig(context), os, (CompositeMap) context
							.getParameter().getChild(this.KEY_MERGE_COLUMN));
				} finally {
					if (os != null) {
						os.flush();
						os.close();
					}
				}
			}
			parameter.put("file_path", excel.getCanonicalPath());
		}

		return EventModel.HANDLE_STOP;
	}

	CompositeMap getExportData(ServiceContext context) throws ServletException {
		ILogger mLogger = LoggingContext.getLogger("aurora.plugin.export",
				mObjectRegistry);
		ServiceInstance svc = ServiceInstance.getInstance(context
				.getObjectContext());
		String return_path = (String) svc.getServiceConfigData().getObject(
				ServiceOutputConfig.KEY_SERVICE_OUTPUT + "/@"
						+ ServiceOutputConfig.KEY_OUTPUT);
		if (return_path == null) {
			mLogger.log(Level.SEVERE, "service-output must be defined");
			throw new ServletException("service-output must be defined");
		}
		CompositeMap exportData = (CompositeMap) context.getObjectContext()
				.getObject(return_path);
		return exportData;
	}

	CompositeMap getColumnConfig(ServiceContext context)
			throws ServletException {
		ILogger mLogger = LoggingContext.getLogger("aurora.plugin.export",
				mObjectRegistry);
		CompositeMap column_config = (CompositeMap) context.getParameter()
				.getObject(KEY_COLUMN_CONFIG + "/" + KEY_COLUMN);
		if (column_config == null) {
			mLogger.log(Level.SEVERE,
					"_column_config_ tag and column attibute must be defined");
			throw new ServletException(
					"_column_config_ tag and column attibute must be defined");
		}
		CompositeMap contextMap = context.getObjectContext();
		CompositeMap datatype = (CompositeMap) contextMap
				.getObject("/_export_datatype");
		if (datatype != null) {
			Iterator it = datatype.getChildIterator();
			if (it != null) {
				while (it.hasNext()) {
					CompositeMap record = (CompositeMap) it.next();
					String name = record.getString("field");
					CompositeMap columnRecord = column_config.getChildByAttrib(
							"record", "name", name);
					columnRecord.put(ExcelExportImpl.KEY_DATA_TYPE, record
							.getString(ExcelExportImpl.KEY_DATA_TYPE
									.toLowerCase()));
				}
			}
		}
		return column_config;
	}
	private boolean isEnableTask(CompositeMap parameter){
		if(parameter == null)
			return false;
		boolean enableTask = parameter.getBoolean(KEY_ENABLETASK, false);
		return enableTask;
	}
}

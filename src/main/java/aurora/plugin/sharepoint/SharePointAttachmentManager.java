package aurora.plugin.sharepoint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import uncertain.composite.CompositeMap;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.database.DBUtil;
import aurora.database.FetchDescriptor;
import aurora.database.service.BusinessModelService;
import aurora.database.service.DatabaseServiceFactory;
import aurora.database.service.SqlServiceContext;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility;

public class SharePointAttachmentManager extends AbstractEntry {

	public static final String PROPERTITY_ACTION_TYPE = "actiontype";
	public static final String PROPERTITY_SAVE_TYPE = "savetype";
	public static final String PROPERTITY_URL = "url";

	public int Buffer_size = 500 * 1024;

	private static final String FND_UPLOAD_FILE_TYPE = "fnd.fnd_upload_file_type";
	private static final String ATM_URL_QUERY = "fnd.fnd_atm_attachment_query";

	private int maxSize = 0;
	private String fileType = null;
	private String actionType;
	private String useSubFolder = null;
	private String dataSourcename = null;

	private IObjectRegistry registry;
	private SharePointConfig spConfig;
	private DatabaseServiceFactory databasefactory;
	private ILogger logger;

	public SharePointAttachmentManager(IObjectRegistry registry) {
		this.registry = registry;
		databasefactory = (DatabaseServiceFactory) registry.getInstanceOfType(DatabaseServiceFactory.class);
		this.spConfig = (SharePointConfig) registry.getInstanceOfType(SharePointConfig.class);
		if (spConfig == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(this, SharePointConfig.class);
	}

	public Connection getContextConnection(CompositeMap context) throws SQLException {
		if (context == null)
			throw new IllegalStateException("Can not get context from ServiceThreadLocal!");
		SqlServiceContext sqlServiceContext = SqlServiceContext.createSqlServiceContext(context);
		Connection conn = sqlServiceContext.getNamedConnection(null);
		if (conn == null) {
			sqlServiceContext.initConnection(registry, null);
			conn = sqlServiceContext.getNamedConnection(null);
		}
		return conn;
	}

	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		logger = LoggingContext.getLogger(context, this.getClass().getCanonicalName());
		String actionType = getActionType();
		if ("upload".equalsIgnoreCase(actionType)) {
			doUpload(context);
			// runner.stop();
			ProcedureRunner preRunner = runner;
			while (preRunner.getCaller() != null) {
				preRunner = preRunner.getCaller();
				preRunner.stop();
			}
		} else if ("update".equalsIgnoreCase(actionType)) {
			doUpdate(context);
			ProcedureRunner preRunner = runner;
			while (preRunner.getCaller() != null) {
				preRunner = preRunner.getCaller();
				preRunner.stop();
			}
		} else if ("delete".equalsIgnoreCase(actionType)) {
			doDelete(context);
		} else if ("download".equalsIgnoreCase(actionType)) {
			doDownload(context);
			// runner.stop();
			ProcedureRunner preRunner = runner;
			while (preRunner.getCaller() != null) {
				preRunner = preRunner.getCaller();
				preRunner.stop();
			}
		}
	}

	private void doDownload(CompositeMap context) throws Exception {
		ServiceContext service = ServiceContext.createServiceContext(context);
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance.getInstance(context);
		CompositeMap params = service.getParameter();
		Object attachment_id = (Object) params.getObject("@attachment_id");
		if (attachment_id != null) {
			Connection conn = getContextConnection(context);
			PreparedStatement pst = null;
			ResultSet rs = null;
			InputStream is = null;
			OutputStream os = null;
			try {
				pst = conn.prepareStatement("select file_name,file_size,mime_type, file_path, content from fnd_atm_attachment t where t.attachment_id = ?");
				pst.setObject(1, attachment_id);
				rs = pst.executeQuery();
				if (!rs.next())
					throw new IllegalArgumentException("attachment_id not set");
				String fileFullPath = rs.getString(4);
				String fileName = rs.getString(1);
				int fileSize = rs.getInt(2);
				String mimeType = rs.getString(3);
				HttpServletResponse response = serviceInstance.getResponse();
				response.setHeader("cache-control", "must-revalidate");
				response.setHeader("pragma", "public");
				response.setHeader("Content-Type", mimeType);
				response.setHeader("Content-disposition", "attachment;" + processFileName(serviceInstance.getRequest(), fileName));

				try {
					Class.forName("org.apache.catalina.startup.Bootstrap");
					if (fileSize > 0)
						response.setContentLength(fileSize);
				} catch (ClassNotFoundException e) {
				}
				Download download = new Download(spConfig, fileFullPath);
				byte[] fileBytes = download.execute();
				os = response.getOutputStream();
				os.write(fileBytes);
				os.flush();
				response.setHeader("Connection", "close");
			} finally {
				DBUtil.closeResultSet(rs);
				DBUtil.closeStatement(pst);
				SharePointConfig.close(is);
				SharePointConfig.close(os);
			}
		}
	}

	private void doDelete(CompositeMap context) throws Exception {
		ServiceContext service = ServiceContext.createServiceContext(context);
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance.getInstance(context);

//		CompositeMap params = service.getParameter();
		CompositeMap currentParameter = service.getCurrentParameter();
		Object attachment_id = serviceInstance.getRequest().getAttribute("attachment_id");
		if (attachment_id == null)
			attachment_id = (Object) currentParameter.getObject("@attachment_id");
		if (attachment_id != null && !"".equals(attachment_id)) {
			Connection conn = getContextConnection(context);
			PreparedStatement pst = null;
			ResultSet rs = null;
			try {
				pst = conn.prepareStatement("select file_name,file_path from fnd_atm_attachment t where t.attachment_id = ?");
				pst.setObject(1,attachment_id);
				rs = pst.executeQuery(); 
				if (!rs.next()) throw new IllegalArgumentException("attachment_id not set");

				String fileFullPath = rs.getString(2);
				SharePointFile spFile = new SharePointFile(spConfig, fileFullPath);
				Delete delete = new Delete(spConfig, spFile);
				delete.execute();
				pst.close();
				
				pst = conn.prepareStatement("delete from fnd_atm_attachment at where at.attachment_id = ?");
				pst.setObject(1,attachment_id);
				pst.executeUpdate();
				pst.close();
				
				pst = conn.prepareStatement("delete from fnd_atm_attachment_multi atm where atm.attachment_id = ?");
				pst.setObject(1,attachment_id);
				pst.executeUpdate();
			} finally {
				DBUtil.closeResultSet(rs);
				DBUtil.closeStatement(pst);
			}
		}
	}

	private void doUpdate(CompositeMap context) throws Exception {
		//not use
		ServiceContext service = ServiceContext.createServiceContext(context);
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance.getInstance(context);
		CompositeMap params = service.getParameter();
		Object attachment_id = (Object) params.getObject("@attachment_id");

		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload up = new ServletFileUpload(factory);
		List<FileItem> items = up.parseRequest(serviceInstance.getRequest());
		FileItem fileItem = null;
		Iterator<FileItem> i = items.iterator();
		while (i.hasNext()) {
			FileItem fi = (FileItem) i.next();
			if (!fi.isFormField()) {
				fileItem = fi;
			}
		}

		if (attachment_id != null && !"".equals(attachment_id)) {
			Connection conn = getContextConnection(context);
			PreparedStatement pst = null;
			ResultSet rs = null;
			String path = null;
			try {
				pst = conn.prepareStatement("select file_path from fnd_atm_attachment t where t.attachment_id = ?");
				pst.setObject(1,attachment_id);
				rs = pst.executeQuery(); 
				if (!rs.next()) throw new IllegalArgumentException("attachment_id not set");
				path = rs.getString(1);
			} finally {
				DBUtil.closeResultSet(rs);
				DBUtil.closeStatement(pst);
			}
			File delFile = new File(path);
			if (delFile.exists()) {
				delFile.delete();
			}
			if (fileItem != null) {
				long size = 0;
				int b;
				FileOutputStream fos = null;
				InputStream ins = null;
				Statement stmt = null;
				try {
					fos = new FileOutputStream(delFile);
					ins = fileItem.getInputStream();
					while ((b = ins.read()) >= 0) {
						fos.write(b);
						size++;
					}
					stmt = conn.createStatement();
					pst = conn.prepareStatement("update fnd_atm_attachment a set a.file_size = ? where a.attachment_id = ?");
					pst.setLong(1, size);
					pst.setObject(2, attachment_id);
					pst.executeUpdate();
				} finally {
					if (ins != null)
						ins.close();
					if (fos != null)
						fos.close();
					DBUtil.closeStatement(stmt);
				}
			}
		}
	}
	
	private String fixDestUrl(String source){
		String result = source;
		if (!result.endsWith("/")) {
			result = result + "/";
		}
		if(!result.startsWith("http")){
			if(!result.startsWith("/")){
				result = "/"+result;
			}
			result = spConfig.getAppLocation()+result;
		}
		return result;
	}

	private void doUpload(CompositeMap context) throws Exception {
		ServiceContext service = ServiceContext.createServiceContext(context);
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance.getInstance(context);

		CompositeMap params = service.getParameter();
		String user_name = (String) params.getObject("@user_name");
		String destUrl = (String) params.getObject("@destUrl");
		logger.config("user_name:"+user_name);
		logger.config("source destUrl:"+destUrl);
		
		destUrl = fixDestUrl(destUrl);
		logger.config("result destUrl:"+destUrl);

		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload up = new ServletFileUpload(factory);
		int ms = getFileSize();
		if (ms > 0)
			up.setSizeMax(ms);
		List<FileItem> items = null;
		List<FileItem> files = new ArrayList<FileItem>();
		Connection conn = null;
		String url = null;
		try {
			items = up.parseRequest(serviceInstance.getRequest());
			Iterator<FileItem> i = items.iterator();
			while (i.hasNext()) {
				FileItem fileItem = (FileItem) i.next();
				if (fileItem.isFormField()) {
					String name = fileItem.getFieldName();
					String value = fileItem.getString("UTF-8");
					if (PROPERTITY_URL.equalsIgnoreCase(name)) {
						url = value;
					} else if (PROPERTITY_ACTION_TYPE.equalsIgnoreCase(name)) {
						actionType = value;
					} else {
						params.put(name, value);
						if ("attachment_id".equalsIgnoreCase(name))
							serviceInstance.getRequest().setAttribute("attachment_id", value);
					}
				} else {
					String fts = getFileType();
					if (fts != null) {
						String name = fileItem.getName().toLowerCase();
						String ft = name.substring(name.lastIndexOf(".") + 1, name.length());
						if (fts.indexOf(ft) != -1) {
							files.add(fileItem);
						} else {
							throw new Exception("文件类型不匹配!只允许 " + fts);
						}
					} else {
						files.add(fileItem);
					}
				}
			}
			Map<String, String> file_paths = null;
			Iterator<FileItem> it = files.iterator();
			while (it.hasNext()) {
				FileItem fileItem = (FileItem) it.next();
				File file = new File(fileItem.getName());
				String file_name = file.getName();
				if ("".equals(file_name))
					continue;
				params.put("file_name", file_name);
				params.put("file_size", new Long(fileItem.getSize()));
				if (file_paths == null)
					file_paths = getExistsAttachments(context);

				BusinessModelService modelService = databasefactory.getModelService(FND_UPLOAD_FILE_TYPE, context);
				modelService.execute(null);
				Object aid = service.getModel().getObject("/parameter/@attachment_id");
				conn = getContextConnection(context);
				InputStream in = fileItem.getInputStream();
				String attach_id = aid.toString();

				String sourceSystem = spConfig.getSourceSystem();
				byte[] fileContent = spConfig.inputStreamToBytes(in);
				String fileFullPath = destUrl + file_name;
				String attachment_id = file_paths.get(fileFullPath);
				if (attachment_id != null) {
					deleteAttachement(conn, attachment_id);
				}
				SharePointFile spFile = new SharePointFile(spConfig, fileFullPath);
				uploadToSharePoint(conn, attach_id, spFile, fileContent, sourceSystem, user_name);

				fileItem.delete();
				params.put("success", "true");

				if (url == null) {
					PrintWriter out = serviceInstance.getResponse().getWriter();
					out.write(aid.toString());
					out.close();
				}
			}
			if (url != null) {
				serviceInstance.getResponse().sendRedirect(url);
			}

		} catch (Exception ex) {
			LoggingContext.getLogger(context, SharePointAttachmentManager.class.getCanonicalName()).log(ex.getMessage());
			throw ex;
		}
	}

	private Map<String, String> getExistsAttachments(CompositeMap context) throws Exception {
		Map<String, String> file_paths = new HashMap<String, String>();
		CompositeMap fakeContext = (CompositeMap) context.clone();
		// CompositeMap parameters = new CompositeMap("parameters");
		String table_name = (String) fakeContext.getObject("/parameter/@source_type");
		String table_pk_value = (String) fakeContext.getObject("/parameter/@pkvalue");
		fakeContext.putObject("/parameter/@table_name", table_name, true);
		fakeContext.putObject("/parameter/@table_pk_value", table_pk_value, true);
		fakeContext.putString("table_name", table_name);
		fakeContext.putString("table_pk_value", table_pk_value);
		CompositeMap result = queryBM(ATM_URL_QUERY, fakeContext, fakeContext);
		if (result == null)
			return file_paths;
		List<CompositeMap> childs = result.getChilds();
		if (childs == null)
			return file_paths;
		String appLocation = spConfig.getAppLocation();
		for (CompositeMap record : childs) {
			String fileFullPath = record.getString("file_path");
			String attachment_id = record.getString("attachment_id");
			if(fileFullPath == null || !fileFullPath.startsWith(appLocation))
				continue;
			SharePointFile spf = new SharePointFile(spConfig, fileFullPath);
			String folder = spf.getFolderPath(); // getFolderPath(fileFullPath);
			if (folder != null)
				spConfig.addFolder(folder);
			file_paths.put(fileFullPath, attachment_id);
		}
		return file_paths;
	}

	private void deleteAttachement(Connection conn, String attachment_id) throws Exception {
		if (attachment_id != null && !"".equals(attachment_id)) {
			int atm_id = Integer.valueOf(attachment_id);
			PreparedStatement pst = null;
			ResultSet rs = null;
			try {
				pst = conn.prepareStatement("delete from fnd_atm_attachment at where at.attachment_id = ?");
				pst.setInt(1, atm_id);
				pst.executeUpdate();
				pst.close();
				
				pst = conn.prepareStatement("delete from fnd_atm_attachment_multi atm where atm.attachment_id = ?");
				pst.setInt(1, atm_id);
				pst.executeUpdate();
				pst.close();
			} finally {
				DBUtil.closeResultSet(rs);
				DBUtil.closeStatement(pst);
			}
		}
	}

	private CompositeMap queryBM(String bm_name, CompositeMap context, CompositeMap queryMap) throws Exception {
		if (context == null)
			context = new CompositeMap("context");
		BusinessModelService service = databasefactory.getModelService(bm_name, context);
		CompositeMap resultMap = service.queryAsMap(queryMap, FetchDescriptor.fetchAll());
		return resultMap;
	}

	private void uploadToSharePoint(Connection conn, String aid, SharePointFile spFile, byte[] fileContent, String sourceSystem, String sourceSystemUser)
			throws Exception {
		Upload upload = new Upload(spConfig, spFile, fileContent, sourceSystem, sourceSystemUser);
		upload.execute();
		PreparedStatement pst = null;
		try {
			// Update attachment record
			String url = spFile.getFileFullPath();
			pst = conn.prepareStatement("update fnd_atm_attachment a set a.file_path = ? where a.attachment_id = ?");
			pst.setString(1, url);
			pst.setObject(2, aid);
			pst.executeUpdate();
			// conn.commit();
		} finally {
			DBUtil.closeStatement(pst);
		}
	}


	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public int getFileSize() {
		return maxSize;
	}

	public void setFileSize(int fileSize) {
		this.maxSize = fileSize;
	}

	public String getActionType() {
		return actionType == null ? "upload" : actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	private String processFileName(HttpServletRequest request, String filename) throws UnsupportedEncodingException {
		String userAgent = request.getHeader("User-Agent");
		String new_filename = URLEncoder.encode(filename, "UTF8");
		// 如果没有UA，则默认使用IE的方式进行编码，因为毕竟IE还是占多数的
		String rtn = "filename=\"" + new_filename + "\"";
		if (userAgent != null) {
			userAgent = userAgent.toLowerCase();
			// IE浏览器，只能采用URLEncoder编码
			if (userAgent.indexOf("msie") != -1) {
				rtn = "filename=\"" + new String(filename.getBytes("gb2312"), "iso-8859-1") + "\"";
			}
			// if (userAgent.indexOf("msie 6") != -1 ||
			// userAgent.indexOf("msie 7") != -1) {
			// rtn = "filename=\"" + new
			// String(filename.getBytes("gb2312"),"iso-8859-1") + "\"";
			// }
			// else if (userAgent.indexOf("msie") != -1) {
			// rtn = "filename=\"" + new_filename + "\"";
			// }
			// Opera浏览器只能采用filename*
			else if (userAgent.indexOf("opera") != -1) {
				rtn = "filename*=UTF-8''" + new_filename;
			}
			// Safari浏览器，只能采用ISO编码的中文输出
			else if (userAgent.indexOf("safari") != -1) {
				rtn = "filename=\"" + new String(filename.getBytes("UTF-8"), "ISO8859-1") + "\"";
			}
			// Chrome浏览器，只能采用MimeUtility编码或ISO编码的中文输出
			else if (userAgent.indexOf("applewebkit") != -1) {
				new_filename = MimeUtility.encodeText(filename, "UTF8", "B");
				rtn = "filename=\"" + new_filename + "\"";
			}
			// FireFox浏览器，可以使用MimeUtility或filename*或ISO编码的中文输出
			else if (userAgent.indexOf("mozilla") != -1) {
				rtn = "filename*=UTF-8''" + new_filename;
			}
		}
		return rtn;
	}

	public String getUseSubFolder() {
		return useSubFolder == null ? "true" : useSubFolder;
	}

	public void setUseSubFolder(String useSubFolder) {
		this.useSubFolder = useSubFolder;
	}

	public String getDataSourceName() {
		return dataSourcename;
	}

	public void setDataSourceName(String name) {
		this.dataSourcename = name;
	}

}

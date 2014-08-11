package aurora.plugin.export;

import java.io.IOException;

import java.io.PrintWriter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import uncertain.composite.CompositeMap;
import uncertain.composite.DynamicObject;
import uncertain.event.IContextAcceptable;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;
import aurora.database.IResultSetConsumer;
import aurora.database.service.SqlServiceContext;

import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

public class TxtOutput implements IResultSetConsumer, IContextAcceptable {
	String separator;
	ServiceContext context;
	PrintWriter pw = null;
	String fileName;
	
	Map<String, Object> rowMap;
	List<String> headList = new LinkedList<String>();

	@Override
	public void setContext(CompositeMap context) {
		this.context = (SqlServiceContext) DynamicObject.cast(context,
				SqlServiceContext.class);
		CompositeMap parameter = this.context.getParameter();
		this.fileName = parameter
				.getString(ModelOutput.KEY_FILE_NAME, "export");
		CompositeMap column_config = (CompositeMap) parameter
				.getObject(ModelOutput.KEY_COLUMN_CONFIG + "/"
						+ ModelOutput.KEY_COLUMN);
		Iterator it = column_config.getChildIterator();
		CompositeMap record;
		if (it == null)
			return;
		while (it.hasNext()) {
			record = (CompositeMap) it.next();
			headList.add(record.getString("name"));
		}
		this.separator = parameter.getString(ModelOutput.KEY_SEPARATOR, ",");
	}

	@Override
	public void begin(String root_name) {
		try {
			ServiceInstance svc = ServiceInstance.getInstance(this.context
					.getObjectContext());
			HttpServletResponse response = ((HttpServiceInstance) svc)
					.getResponse();
			String userAgent = ((HttpServiceInstance) svc).getRequest().getHeader("User-Agent");
			if (userAgent != null) {
				userAgent = userAgent.toLowerCase();
				if (userAgent.indexOf("msie") != -1) {
					fileName=new String(fileName.getBytes("GBK"),"ISO-8859-1");
					response.setCharacterEncoding("GBK");
				}else{
					fileName=new String(fileName.getBytes("UTF-8"),"ISO-8859-1");
					response.setCharacterEncoding("GBK");
				}
			}
			response.setContentType("text/plain");
			response.setHeader("Content-Disposition", "attachment; filename=\""
					+ fileName + ".txt\"");
			pw = response.getWriter();
		} catch (IOException e) {
			if (pw != null)
				pw.close();
			throw new RuntimeException(e);
		}
	}

	@Override
	public void newRow(String row_name) {
		rowMap = new HashMap<String, Object>();
	}

	@Override
	public void loadField(String name, Object value) {
		rowMap.put(name, value);
	}

	@Override
	public void endRow() {
		Iterator<String> it = headList.iterator();
		StringBuffer sb = new StringBuffer();
		String content = null;
		boolean is_first=true;
		while (it.hasNext()) {
			if(!is_first)
				sb.append(this.separator);
			Object value=rowMap.get(it.next());
			if(value!=null)
				content = value.toString();				
			else
				content = "";
			sb.append(content);
			is_first=false;
		}
		pw.println(sb);
	}

	@Override
	public void end() {		
		pw.close();
	}

	@Override
	public void setRecordCount(long count) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getResult() {
		// TODO Auto-generated method stub
		return null;
	}

}

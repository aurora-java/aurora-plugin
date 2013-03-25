package aurora.plugin.export.word;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uncertain.composite.CompositeMap;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@SuppressWarnings("unchecked")
public class WordExport extends AbstractEntry {
	
	private static final String DEFAULT_TEMPLATE_DIR = "aurora.plugin.export.word";
	private static final String TEMPLATE_TABLE = "table.ftl";
	private static final String DEFAULT_WORD_NAME = "default.doc";
	
	protected Table[] tables;
	protected Replace[] replaces;
	private String template = null;
	private String name = DEFAULT_WORD_NAME;
	private WordTemplateProvider provider;
	
	public WordExport(IObjectRegistry registry) {
		provider = (WordTemplateProvider) registry.getInstanceOfType(WordTemplateProvider.class);
	}

	
	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		ServiceContext service = ServiceContext.createServiceContext(context);
		CompositeMap model = service.getModel();
		
		Map dataMap = new HashMap();
		if(replaces != null)
		for(Replace replace:replaces){
			CompositeMap data = (CompositeMap)model.getObject(replace.getPath());
			Map map = transformHashMap(data);
			dataMap.put(replace.getName(),map);
		}
		
		Configuration configuration = provider.getFreeMarkerConfiguration();
		getData(dataMap);
		Template t = null;
		Writer out = null;
		try {
			String templateName =  getTemplate();
			if(templateName == null) throw new IllegalArgumentException("template can not be null!");
			t = configuration.getTemplate(templateName);	
			Template tbl = configuration.getTemplate(DEFAULT_TEMPLATE_DIR + File.separatorChar + TEMPLATE_TABLE);
			
			if(tables != null)
			for(Table table:getTables()){
				String tb1 = createTable(table,tbl,model);
				dataMap.put(table.getId(), tb1);		
			}	
			
			HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance.getInstance(context);
			HttpServletResponse response = serviceInstance.getResponse();
			response.setHeader("cache-control", "must-revalidate");
			response.setHeader("pragma", "public");	
			response.setHeader("Content-Type", "application/msword");
			response.setHeader("Content-disposition", "attachment;" + processFileName(serviceInstance.getRequest(),getName()));
			response.setCharacterEncoding("utf-8");
			out = response.getWriter();
			t.process(dataMap, out);
		} finally {
			if(out!=null)out.close();
		}
	}
	
	public String getTemplate() {
		return template;
	}

	public void setTemplate(String t) {
		this.template = t;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String n) {
		this.name = n;
	}
	
	public Table[] getTables() {
		return tables;
	}

	public void setTables(Table[] tables) {
		this.tables = tables;
	}
	
	public Replace[] getReplaces() {
		return replaces;
	}

	public void setReplaces(Replace[] rps) {
		this.replaces = rps;
	}
	
	
	
	private String processFileName(HttpServletRequest request, String filename) throws UnsupportedEncodingException {
		String userAgent = request.getHeader("User-Agent");
		String new_filename = URLEncoder.encode(filename, "UTF8");
		String rtn = "filename=\"" + new_filename + "\"";
		if (userAgent != null) {
			userAgent = userAgent.toLowerCase();
			if (userAgent.indexOf("msie") != -1) {
				rtn = "filename=\"" + new String(filename.getBytes("gb2312"),"iso-8859-1") + "\"";
			} else if (userAgent.indexOf("opera") != -1) {
				rtn = "filename*=UTF-8''" + new_filename;
			}else if (userAgent.indexOf("safari") != -1) {
				rtn = "filename=\"" + new String(filename.getBytes("UTF-8"), "ISO8859-1") + "\"";
			}else if (userAgent.indexOf("applewebkit") != -1) {
				new_filename = MimeUtility.encodeText(filename, "UTF8", "B");
				rtn = "filename=\"" + new_filename + "\"";
			}else if (userAgent.indexOf("mozilla") != -1) {
				rtn = "filename*=UTF-8''" + new_filename;
			}
		}
		return rtn;
	}
	
	private Map transformHashMap(CompositeMap data){
		Map map = new HashMap();
		Set ks = data.keySet();
		Iterator kit = ks.iterator();
		while(kit.hasNext()){
			Object key = kit.next();
			map.put(key, data.get(key));
		}
		return map;
	}
	
	
	private String createTable(Table table, Template template,CompositeMap model) throws TemplateException, IOException{
		StringWriter out = new StringWriter();
		Map map = new HashMap();
		List columns = new ArrayList();
		map.put("table", table);
		
		for(Column col:table.getColumns()){
			columns.add(col);
		}
		map.put("columns", columns);
		
		CompositeMap data = (CompositeMap)model.getObject(table.getModel());
		map.put("records", data.getChilds());
		
		template.process(map, out);
		out.flush();
		return out.toString();
	}

	/**
	 * 注意dataMap里存放的数据Key值要与模板中的参数相对应
	 * 
	 * @param dataMap
	 */
	private void getData(Map dataMap) {
		for(int i=0;i<200;i++){
			dataMap.put("act"+i, Math.random()*2000);			
		}
		
	}

}

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
	
	protected SectList[] sectLists;
	protected Table[] tables;
	protected Replace[] replaces;
	private String template = null;
	private String name = DEFAULT_WORD_NAME;
	private WordTemplateProvider provider;
	
	private int baseId;
	
	private StringBuffer listDefSb = new StringBuffer();
	private StringBuffer listMapSb = new StringBuffer();
	
	public WordExport(IObjectRegistry registry) {
		provider = (WordTemplateProvider) registry.getInstanceOfType(WordTemplateProvider.class);
		baseId = 1000;
	}

	
	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		ServiceContext service = ServiceContext.createServiceContext(context);
		CompositeMap model = service.getModel();
		
		Map dataMap = new HashMap();
		if(replaces != null){
			for(Replace replace:replaces){
				CompositeMap data = (CompositeMap)model.getObject(replace.getPath());
				dataMap.put(replace.getName(),data);
			}
		}
		
		if(sectLists != null) {
			listDefSb.append(createTopListDef());
			listMapSb.append(createListDefMap());
			for(SectList list:sectLists){
				CompositeMap data = (CompositeMap)model.getObject(list.getModel());
				CompositeMap top = buildTree(data);
				List children = top.getChilds();
				if(children!=null){
					StringBuffer psb = new StringBuffer();
					createSection(psb,children,0);
					dataMap.put(list.getId(),psb.toString());
				}
			}
			listDefSb.append(listMapSb);
			dataMap.put("listdef", listDefSb.toString());
		}
		
		
		
		
		Configuration configuration = provider.getFreeMarkerConfiguration();
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
	
	public SectList[] getSectLists() {
		return sectLists;
	}

	public void setSectLists(SectList[] lists) {
		this.sectLists = lists;
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
		map.put("records", data.getChildsNotNull());
		
		template.process(map, out);
		out.flush();
		return out.toString();
	}
	
	
	
	private CompositeMap buildTree(CompositeMap data){
		CompositeMap top = new CompositeMap();
		Map map = new HashMap();
		if(data!=null && data.getChilds()!=null){
			List children = data.getChilds();
			Iterator it = children.iterator();
			while(it.hasNext()){
				CompositeMap item = (CompositeMap)it.next();
				String id = item.getString("id");
				String pid = item.getString("pid");
				map.put(id, item);
				if(pid == null || "".equals(pid)){
					top.addChild(item);					
				}
			}
			it = children.iterator();
			while(it.hasNext()){
				CompositeMap item = (CompositeMap)it.next();
				String pid = item.getString("pid");
				if(pid != null && !"".equals(pid)){
					CompositeMap parent = (CompositeMap)map.get(pid);
					if(parent != null){
						parent.addChild(item);						
					}
				}
			}
		}
		return top;
	}
	
	
	
	private String createListDefMap(){
		StringBuffer sb = new StringBuffer();
		sb.append("<w:list w:ilfo='"+baseId+"'>");		
		sb.append("<w:ilst w:val='"+baseId+"'/>");
		sb.append("</w:list>");
		return sb.toString();
	}
	
	
	private void createSection(StringBuffer psb,List children,int level){
		if(children == null) return;
		Iterator it = children.iterator();
		while(it.hasNext()){
			CompositeMap item = (CompositeMap)it.next();
			String pid = item.getString("pid");
			String text = item.getString("text");
			boolean isBold = (pid == null || "".equals(pid));
			boolean isTop = level ==0;
			if(isTop){
				baseId ++;
				listDefSb.append(createListDef(baseId-1000));
				listMapSb.append(createListDefMap());
			}
			psb.append(createP(level==0, isBold,level,baseId,text));
			List childs = item.getChilds();
			if(childs!=null){
				createSection(psb,childs,level+1);
			}
			
			if(isTop){
				psb.append("<w:p>");
				psb.append("    <w:pPr>");
				psb.append("        <w:widowControl/>");
				psb.append("        <w:spacing w:line='400' w:line-rule='exact'/>");
				psb.append("        <w:rPr>");
				psb.append("            <w:rFonts w:ascii='宋体' w:h-ansi='宋体'/>");
				psb.append("            <wx:font wx:val='宋体'/>");
				psb.append("            <w:sz w:val='24'/>");
				psb.append("            <w:sz-cs w:val='24'/>");
				psb.append("        </w:rPr>");
				psb.append("    </w:pPr>");
				psb.append("</w:p>");
			}
		}
		
	}
	
	
	private String createP(boolean isOutLine,boolean isBold,int level,int listDefId,String text){
		StringBuffer sb = new StringBuffer();
		sb.append("<w:p>");
		sb.append("    <w:pPr>");
		if(isOutLine) sb.append("        <w:outlineLvl w:val='0'/>");
		sb.append("        <w:listPr>");
		sb.append("            <w:ilvl w:val='"+level+"'/>");
		sb.append("            <w:ilfo w:val='"+(isOutLine ? 1000 : listDefId)+"'/>");
		sb.append("        </w:listPr>");
		sb.append("        <w:rPr>");
		sb.append("            <w:rFonts w:ascii='宋体' w:h-ansi='宋体'/>");
		sb.append("            <wx:font wx:val='宋体'/>");
		sb.append("            <w:sz w:val='24'/>");
		sb.append("            <w:sz-cs w:val='24'/>");
		if(isBold)sb.append("            <w:b/>");
		sb.append("        </w:rPr>");
		sb.append("    </w:pPr>");
		sb.append("    <w:r>");
		sb.append("        <w:rPr>");
		sb.append("            <w:rFonts w:ascii='宋体' w:h-ansi='宋体'/>");
		sb.append("            <wx:font wx:val='宋体'/>");
		sb.append("            <w:sz w:val='24'/>");
		sb.append("            <w:sz-cs w:val='24'/>");
		if(isBold)sb.append("            <w:b/>");
		sb.append("        </w:rPr>");
		sb.append("        <w:t>"+text+"</w:t>");
		sb.append("    </w:r>");
		sb.append("</w:p>");
		return sb.toString();
	}
	
	
	private String createTopListDef(){
		StringBuffer sb = new StringBuffer();
		sb.append("<w:listDef w:listDefId='"+baseId+"'>");
		sb.append("  <w:plt w:val='Multilevel'/>");
		sb.append("  <w:lvl w:ilvl='0'>");
		sb.append("    <w:start w:val='1'/>");
		sb.append("    <w:lvlText w:val='%1'/>");
		sb.append("    <w:lvlJc w:val='left'/>");
		sb.append("    <w:pPr>");
		sb.append("      <w:ind w:left='420' w:hanging='420'/>");
		sb.append("      <w:spacing w:line='300' w:line-rule='auto' />");
		sb.append("    </w:pPr>");
		sb.append("    <w:rPr>");
		sb.append("      <w:rFonts w:hint='default'/>");
		sb.append("    </w:rPr>");
		sb.append("  </w:lvl>");
		sb.append("</w:listDef>");
		return sb.toString();	
	}
	
	
	
	
	private String createListDef(int level){
		StringBuffer sb = new StringBuffer();
		sb.append("<w:listDef w:listDefId='"+baseId+"'>");
		sb.append("  <w:plt w:val='Multilevel'/>");
		sb.append("  <w:lvl w:ilvl='0'>");
		sb.append("    <w:start w:val='1'/>");
		sb.append("    <w:lvlText w:val='%1'/>");
		sb.append("    <w:lvlJc w:val='left'/>");
		sb.append("    <w:rPr>");
		sb.append("      <w:rFonts w:hint='default'/>");
		sb.append("    </w:rPr>");
		sb.append("  </w:lvl>");
		sb.append("  <w:lvl w:ilvl='1'>");
		sb.append("    <w:start w:val='1'/>");
		sb.append("    <w:lvlText w:val='"+level+".%2'/>");
		sb.append("    <w:lvlJc w:val='left'/>");
		sb.append("    <w:pPr>");
		sb.append("      <w:ind w:left='420' w:hanging='420'/>");
		sb.append("      <w:spacing w:line='300' w:line-rule='auto' />");
		sb.append("    </w:pPr>");
		sb.append("    <w:rPr>");
		sb.append("      <w:rFonts w:hint='default'/>");
		sb.append("    </w:rPr>");
		sb.append("  </w:lvl>");
		sb.append("  <w:lvl w:ilvl='2'>");
		sb.append("    <w:start w:val='1'/>");
		sb.append("    <w:lvlText w:val='(%3)'/>");
		sb.append("    <w:lvlJc w:val='left'/>");
		sb.append("    <w:pPr>");
		sb.append("      <w:ind w:left='840' w:hanging='420'/>");
		sb.append("      <w:spacing w:line='300' w:line-rule='auto' />");
		sb.append("    </w:pPr>");
		sb.append("    <w:rPr>");
		sb.append("      <w:rFonts w:hint='default'/>");
		sb.append("    </w:rPr>");
		sb.append("  </w:lvl>");
		sb.append("  <w:lvl w:ilvl='3'>");
		sb.append("    <w:start w:val='1'/>");
		sb.append("    <w:nfc w:val='4'/>");
		sb.append("    <w:lvlText w:val='%4)'/>");
		sb.append("    <w:lvlJc w:val='left'/>");
		sb.append("    <w:pPr>");
		sb.append("      <w:ind w:left='1260' w:hanging='420'/>");
		sb.append("      <w:spacing w:line='300' w:line-rule='auto' />");
		sb.append("    </w:pPr>");
		sb.append("  </w:lvl>");
		sb.append("</w:listDef>");
		return sb.toString();		
	}

}

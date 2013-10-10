package aurora.plugin.export.word;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
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

import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;
import uncertain.composite.XMLOutputter;
import uncertain.core.UncertainEngine;
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
	
	private static final String LISTS_START = "<w:lists xmlns:w='http://schemas.microsoft.com/office/word/2003/wordml'>";
	private static final String LISTS_END = "</w:lists>";
	
	protected SectList[] sectLists;
	protected Table[] tables;
	protected Replace[] replaces;
	private String template = null;
	private String name = DEFAULT_WORD_NAME;
	private WordTemplateProvider provider;
	private UncertainEngine uncertainEngine;
	
	
	private int baseId;
	
	private StringBuffer listDefSb = new StringBuffer();
	private StringBuffer listMapSb = new StringBuffer();
	
	public WordExport(IObjectRegistry registry) {
		provider = (WordTemplateProvider) registry.getInstanceOfType(WordTemplateProvider.class);
		baseId = 1000;
		uncertainEngine = (UncertainEngine) registry.getInstanceOfType(UncertainEngine.class);
	}

	
	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		ServiceContext service = ServiceContext.createServiceContext(context);
		CompositeMap model = service.getModel();
		String templateName =  getTemplate();
		if(templateName !=null) templateName = uncertain.composite.TextParser.parse(templateName, model);
		
		Map dataMap = new HashMap();
		if(replaces != null){
			for(Replace replace:replaces){
				String path = replace.getPath();
				if(path!=null) {
					Object data = model.getObject(path);
					if(data instanceof String){
						List rs = createWordR((String)data);
						if(rs.size() > 1) {
							StringBuffer psb = new StringBuffer("<w:p>");
							Iterator rit = rs.iterator();
							while(rit.hasNext()){
								WordR r = (WordR)rit.next();
								psb.append(r.toXML());
							}
							psb.append("</w:p>"); 
							dataMap.put(replace.getName(),psb);
						} else {
							dataMap.put(replace.getName(),data);
							
						}
					}else {
						dataMap.put(replace.getName(),data);
					}
				}
			}
		}
		
		CompositeLoader loader = new CompositeLoader();
		loader.setSaveNamespaceMapping(true);
		File f = new File(uncertainEngine.getConfigDirectory(),templateName);
		CompositeMap file = loader.loadByFullFilePath(f.getCanonicalPath());
		
		if(sectLists != null) {
			listDefSb.append(createTopListDef());
			listMapSb.append(createListDefMap());
			for(SectList list:sectLists){
				CompositeMap data = (CompositeMap)model.getObject(list.getModel());
				CompositeMap top = buildTree(list,data);
				List children = top.getChilds();
				if(children!=null){
					StringBuffer psb = new StringBuffer();
					createSection(list,psb,children,0);
					dataMap.put(list.getId(),psb.toString());
				}
			}
//			listDefSb.append(listMapSb);
//			dataMap.put("listDefSb", listDefSb);
//			dataMap.put("listMapSb", listMapSb);
			
			
			List childList = file.getChilds();
			int index = 0;
			Iterator it = childList.iterator();
			while(it.hasNext()){
				CompositeMap c = (CompositeMap)it.next();
				if("fonts".equals(c.getName())){
					break;
				}
				index ++;
			}
			CompositeMap lists = file.getChild("lists");
			if(lists == null) {
				lists = new CompositeMap("w","http://schemas.microsoft.com/office/word/2003/wordml", "lists");
				childList.add(index, lists);
			}
			String listDefStr =  LISTS_START + listDefSb + LISTS_END;
			String listMapStr =  LISTS_START + listMapSb + LISTS_END;
			CompositeMap listDefObj = loader.loadFromString(listDefStr);
			CompositeMap listMapObj = loader.loadFromString(listMapStr);
			
			List listDef = new ArrayList();
			List listMap = new ArrayList();
			List child = lists.getChilds();
			if(child!= null){
				Iterator lit = child.iterator();
				while(lit.hasNext()) {
					CompositeMap litem = (CompositeMap)lit.next();
					if("listDef".equals(litem.getName())){
						listDef.add(litem);
					}else if("list".equals(litem.getName())){
						listMap.add(litem);
					}
				}
				listDef.addAll(listDefObj.getChilds());
				listMap.addAll(listMapObj.getChilds());
				child.clear();
				child.addAll(listDef);
				child.addAll(listMap);
			}else {
				lists.addChilds(listDefObj.getChilds());
				lists.addChilds(listMapObj.getChilds());
			}
		}
		
		//CompositeLoader 会丢失命名空间
		file.put("xmlns:aml", "http://schemas.microsoft.com/aml/2001/core");
		file.put("xmlns:dt", "uuid:C2F41010-65B3-11d1-A29F-00AA00C14882");
		file.put("xmlns:ve", "http://schemas.openxmlformats.org/markup-compatibility/2006");
		file.put("xmlns:o", "urn:schemas-microsoft-com:office:office");
		file.put("xmlns:v", "urn:schemas-microsoft-com:vml");
		file.put("xmlns:w10", "urn:schemas-microsoft-com:office:word");
//		file.put("xmlns:w", "http://schemas.microsoft.com/office/word/2003/wordml");
		file.put("xmlns:wx", "http://schemas.microsoft.com/office/word/2003/auxHint");
		file.put("xmlns:wsp", "http://schemas.microsoft.com/office/word/2003/wordml/sp2");
		file.put("xmlns:sl", "http://schemas.microsoft.com/schemaLibrary/2003/core");
		file.put("w:macrosPresent", "no");
		file.put("w:embeddedObjPresent", "no");
		file.put("w:ocxPresent", "no");
		file.put("xml:space", "preserve");
		
		
		XMLOutputter putter = XMLOutputter.defaultInstance();
		putter.setGenerateCdata(false);
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><?mso-application progid=\"Word.Document\"?>" + putter.toXML(file);
		
		Configuration configuration = provider.getFreeMarkerConfiguration();
		Template t = null;
		Writer out = null;
		try {
			
			if(templateName == null) throw new IllegalArgumentException("template can not be null!");
//			t = configuration.getTemplate(templateName);
			Reader reader = new BufferedReader(new StringReader(xml));
			try {
				t = new Template("word_template", reader ,configuration,"UTF-8");
			}finally{
				reader.close();
			}
			
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
	
	
	private String createTable(Table table, Template template,CompositeMap model) throws TemplateException, IOException {
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
	
	
	private CompositeMap buildTree(SectList list,CompositeMap data){
		CompositeMap top = new CompositeMap();
		Map map = new HashMap();
		if(data!=null && data.getChilds()!=null){
			List children = data.getChilds();
			Iterator it = children.iterator();
			while(it.hasNext()){
				CompositeMap item = (CompositeMap)it.next();
				String id = item.getString(list.getIdField());
				String pid = item.getString(list.getParentIdField());
				map.put(id, item);
				if(pid == null || "".equals(pid)){
					top.addChild(item);					
				}
			}
			it = children.iterator();
			while(it.hasNext()){
				CompositeMap item = (CompositeMap)it.next();
				String pid = item.getString(list.getParentIdField());
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
		sb.append("	 <w:ilst w:val='"+baseId+"'/>");
		sb.append("</w:list>");
		return sb.toString();
	}
	
	
	private void createSection(SectList list,StringBuffer psb,List children,int level) throws IOException {
		if(children == null) return;
		Iterator it = children.iterator();
		while(it.hasNext()){
			CompositeMap item = (CompositeMap)it.next();
			String pid = item.getString(list.getParentIdField());
			String text = item.getString(list.getTextField());
			boolean isBold = (pid == null || "".equals(pid));
			boolean isTop = level ==0;
			if(isTop){
				baseId ++;
				listDefSb.append(createListDef(baseId-1000));
				listMapSb.append(createListDefMap());
			}
			
			psb.append("<w:p>");
			WordPPr pPr = new WordPPr();
			pPr.setOutlineLvl(level);
			pPr.createListPr();
			pPr.getRPr().setBold(isBold);
			pPr.getListPr().setIlvl(level);
			pPr.getListPr().setIlfo((level==0 ? 1000 : baseId));
			psb.append(pPr.toXML());
			
			List rs = createWordR(text);
			Iterator rit = rs.iterator();
			while(rit.hasNext()){
				WordR r = (WordR)rit.next();
				if(level==0)r.getRPr().setBold(true);
				psb.append(r.toXML());
			}
			
			psb.append("</w:p>");
			List childs = item.getChilds();
			if(childs!=null){
				createSection(list,psb,childs,level+1);
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
	
	
	private List createWordR(String text) throws IOException{
		List result = new ArrayList();
		BBCodeParser parser = new BBCodeParser();
		List list = parser.parse(text);
		boolean isBold = false;
		boolean isUnderLine = false;
		if(list != null){
			Iterator it = list.iterator();
			while(it.hasNext()){
				WordR wr = new WordR();
				String item = (String)it.next();
				if(!parser.isTag(item)){
					wr.getRPr().setBold(isBold);
					wr.getRPr().setUnderLine(isUnderLine);
					wr.setText(item);
					result.add(wr);
				}else if(BBCodeParser.TAG_BR.equals(item)){
					wr.getRPr().setBold(isBold);
					wr.getRPr().setUnderLine(isUnderLine);
					wr.setBr();
					result.add(wr);
				}else if(BBCodeParser.TAG_B_S.equals(item)){
					isBold = true;
				}else if(BBCodeParser.TAG_B_E.equals(item)){
					isBold = false;
				}else if(BBCodeParser.TAG_U_S.equals(item)){
					isUnderLine = true;
				}else if(BBCodeParser.TAG_U_E.equals(item)){
					isUnderLine = false;
				}
			}
		}
		return result;
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
		sb.append("<w:listDef xmlns:w='http://schemas.microsoft.com/office/word/2003/wordml' w:listDefId='"+baseId+"'>");
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

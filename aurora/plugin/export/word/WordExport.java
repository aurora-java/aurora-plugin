package aurora.plugin.export.word;

import java.io.File;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ObjectFactory;

import uncertain.composite.CompositeMap;
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

@SuppressWarnings("unchecked")
public class WordExport extends AbstractEntry {
	
	private static final String DEFAULT_WORD_NAME = "untitle.docx";
	private static final String TYPE_WORD = "word";
	private static final String TYPE_PDF = "pdf";
	
	protected Replace[] replaces;
	protected SectList[] sectLists;
	private String template = null;
	private String name = DEFAULT_WORD_NAME;
	private String type = TYPE_WORD;
	private String savePath = null;
	private UncertainEngine uncertainEngine;
	private WordTemplateProvider provider;
	
	
	public WordExport(IObjectRegistry registry) {
		provider = (WordTemplateProvider) registry.getInstanceOfType(WordTemplateProvider.class);
		uncertainEngine = (UncertainEngine) registry.getInstanceOfType(UncertainEngine.class);
	}


	@SuppressWarnings("unchecked")
	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		ServiceContext service = ServiceContext.createServiceContext(context);
		CompositeMap model = service.getModel();
		String templateName =  getTemplate();
		if(templateName !=null) templateName = uncertain.composite.TextParser.parse(templateName, model);
		if(templateName == null) throw new IllegalArgumentException("template can not be null!");		
		File templateFile = new File(uncertainEngine.getConfigDirectory(),templateName);		
		
		Map<String,Object> dataMap = new HashMap<String,Object>();
		dataMap.put("model", model);
		if(replaces != null){
			for(Replace replace:replaces){
				String path = replace.getPath();
				if(path!=null) {
					Object data = model.getObject(path);
					dataMap.put(replace.getName(),data);
				}
			}
		}
		if(sectLists != null) {			
			StringBuffer sb = new StringBuffer();
			ObjectFactory factory = Context.getWmlObjectFactory();
			for(SectList list:sectLists){
				CompositeMap data = (CompositeMap)model.getObject(list.getModel());
				List<CompositeMap> children = data.getChilds();
				for(CompositeMap item:children){
					sb.append(createSection(factory,list, item));					
				}
				dataMap.put(list.getId(),sb.toString());
			}
		}
		
		
		Configuration configuration = provider.getFreeMarkerConfiguration();
		StringWriter out = null;
		String xml = "";
		try {
			Template t = configuration.getTemplate(templateName);
			out = new StringWriter();
			t.process(dataMap, out);
			out.flush();
			xml = out.toString();
		} finally {
			if(out!=null)out.close();
		}		
		
		WordprocessingMLPackage wordMLPackage = WordUtils.createWord(xml,templateFile);
		
		String name = uncertain.composite.TextParser.parse(getName(), model);
		
		String savePath = getSavePath();
		if(savePath!=null){
			File destPdf = new File(savePath,name);
			if(TYPE_WORD.equals(getType())) {
				wordMLPackage.save(destPdf);
			}else if(TYPE_PDF.equals(getType())){
				FOSettings foSettings = Docx4J.createFOSettings();
				foSettings.setWmlPackage(wordMLPackage);
				OutputStream os = new java.io.FileOutputStream(destPdf);
				Docx4J.toFO(foSettings, os, Docx4J.FLAG_NONE);
			}
		}else {
			HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance.getInstance(context);
			HttpServletResponse response = serviceInstance.getResponse();
			response.setHeader("cache-control", "must-revalidate");
			response.setHeader("pragma", "public");	
			response.setHeader("Content-disposition", "attachment;" + processFileName(serviceInstance.getRequest(),name));
			response.setCharacterEncoding("utf-8");
			if(TYPE_WORD.equals(getType())) {
				response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
				OutputStream res_out = null;
				try {
					res_out = response.getOutputStream();
					wordMLPackage.save(res_out);
//					SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
//					saver.save(res_out);
				} finally {
					if(res_out!=null)res_out.close();
				}
			}else if(TYPE_PDF.equals(getType())){
				response.setContentType("application/pdf");
				OutputStream res_out = null;
				try {
					res_out = response.getOutputStream();
					FOSettings foSettings = Docx4J.createFOSettings();
					foSettings.setWmlPackage(wordMLPackage);
					Docx4J.toFO(foSettings, res_out, Docx4J.FLAG_NONE);
				} finally {
					if(res_out!=null)res_out.close();
				}
				
			}
			ProcedureRunner preRunner=runner;
			while(preRunner.getCaller()!=null){
				preRunner=preRunner.getCaller();
				preRunner.stop();
			}
		}
		
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
	
	private String createSection(ObjectFactory factory,SectList list, CompositeMap item) throws JAXBException{
		
		String type = item.getString(list.getType());
		if("table".equals(type)){
			return item.getString(list.getTextField());
		}else {
			StringBuffer sb = new StringBuffer();
			sb.append("<p");
			String align = item.getString(list.getAlignField());
			if(align!=null) sb.append(" align=\"").append(align).append("\"");
			Boolean toc = item.getBoolean(list.getTocField(),false);
			if(toc) sb.append(" toc=\"").append(toc).append("\"");
			String indLeft = item.getString(list.getIndLeftField());
			if(indLeft!=null) sb.append(" indLeft=\"").append(indLeft).append("\"");
			String indFirstLine = item.getString(list.getIndFirstLineField());
			if(indFirstLine!=null) sb.append(" indFirstLine=\"").append(indFirstLine).append("\"");
			String ilvl = item.getString(list.getIlvlField());
			String numId = list.getNumId();
			if(ilvl!=null) {
				sb.append(" numId=\""+numId+"\" ilvl=\"").append(ilvl).append("\"");
			}
			
			sb.append(">");
			sb.append(item.getString(list.getTextField()));
			sb.append("</p>");
			return "0".equals(ilvl) ? "<p/>"+sb.toString() : sb.toString();
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
	
	public Replace[] getReplaces() {
		return replaces;
	}

	public void setReplaces(Replace[] rps) {
		this.replaces = rps;
	}
	

	public SectList[] getSectLists() {
		return sectLists;
	}

	public void setSectLists(SectList[] lists) {
		this.sectLists = lists;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getSavePath() {
		return savePath;
	}


	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

}

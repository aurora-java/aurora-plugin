package aurora.plugin.poi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import aurora.database.IResultSetConsumer;
import aurora.database.service.SqlServiceContext;
import aurora.i18n.ILocalizedMessageProvider;
import aurora.i18n.IMessageProvider;
import aurora.plugin.export.MergedHeader;
import aurora.plugin.export.ModelOutput;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

import uncertain.composite.CompositeMap;
import uncertain.composite.DynamicObject;
import uncertain.composite.TextParser;
import uncertain.event.IContextAcceptable;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;

public class Excel2007Output implements IResultSetConsumer,IContextAcceptable{

	private static final String XML_ENCODING = "UTF-8";
	/*
	 * 导出类型配置默认路径是
	 * 	<context>
	 * 		<_export_datatype>
	 * 			<record field="code" datatype="Number"/>
	 * 			<record field="name" datatype="String"/>
	 * 		</_export_datatype>
	 * </context>
	 * */
	public static final String KEY_DATA_TYPE = "datatype";
	public static final String KEY_DATA_TYPE_NUMBER = "Number";
	public static final String KEY_DATA_TYPE_STRING = "String";
	public static final String KEY_DATA_FORMAT = "dataFormat";
	
	 
	private static int CELL_CHAR_LIMIT = (int) Math.pow(2, 15) - 1; // 32,767

	private static String TRUNCATE_WARNING = "DATA TRUNCATED";
	
	Map<Integer, CompositeMap> columnMap=new TreeMap<Integer, CompositeMap>();
	Map <Integer,Map<Integer,String>> headMap=new TreeMap<Integer,Map<Integer,String>>();
	Map<String ,Object> rowMap;
	
	int headLevel;
	Excel2007Bean excel2007=new Excel2007Bean();
	ExcelCellStyles styles;
	File rawSheet;
	File templateFile;
	SpreadsheetWriter sw;		
	String sheetName ;
	
	ServiceContext context;
	ILogger mLogger ;
	IObjectRegistry mObjectRegistry;
	ILocalizedMessageProvider localMsgProvider;

	
	public Excel2007Output(IObjectRegistry registry){		
		this.mObjectRegistry=registry;
		mLogger = LoggingContext.getLogger("aurora.plugin.export",mObjectRegistry);			
	}
	
	public void setContext(CompositeMap contextMap) {
		this.context=(SqlServiceContext)DynamicObject.cast(contextMap, SqlServiceContext.class);
		IMessageProvider msgProvider = (IMessageProvider) mObjectRegistry.getInstanceOfType(IMessageProvider.class);
		String langString = this.context.getSession().getString("lang","ZHS");
		localMsgProvider = msgProvider.getLocalizedMessageProvider(langString);		
		Writer rawWriter =null;	
		try {					
			rawSheet = File.createTempFile("Excel2007TempSheet", ".xml");
			rawWriter = new OutputStreamWriter(new FileOutputStream(rawSheet), XML_ENCODING);
			sw = new SpreadsheetWriter(rawWriter, XML_ENCODING);
			createExcelHeader(getHeaderConfig());			
		} catch (Exception e) {
			if(rawWriter!=null)
				try {
					rawWriter.close();
				} catch (IOException e1) {
					mLogger.log(Level.SEVERE,e1.getMessage());
				}
			mLogger.log(Level.SEVERE,e.getMessage());
			throw new RuntimeException(e);
		}
	}
	
	CompositeMap getHeaderConfig() throws ServletException{		
		CompositeMap columnConfig = (CompositeMap) context.getParameter()
		.getObject(ModelOutput.KEY_COLUMN_CONFIG + "/" + ModelOutput.KEY_COLUMN);
		if (columnConfig == null) {			
			throw new ServletException(
					"service-output tag and output attibute must be defined");
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
					CompositeMap columnRecord = columnConfig.getChildByAttrib(
							"record", "name", name);
					columnRecord.put(ExcelExportImpl.KEY_DATA_TYPE, record
							.getString(ExcelExportImpl.KEY_DATA_TYPE
									.toLowerCase()));
				}
			}
		}
		return (new MergedHeader(columnConfig)).conifg;
	}	
	
	void createExcelHeader(CompositeMap headerConfig) throws Exception{
		OutputStream os=null;
		
		Workbook wb=excel2007.getNewWorkbook();
		styles = new ExcelCellStyles(wb);	
		Sheet sheet = wb.createSheet();
		sheetName = ((XSSFSheet) sheet).getPackagePart().getPartName().getName();
		Row header=sheet.createRow(0);
		generatExcelHead(headerConfig,sheet,header,-1);
		try {
			templateFile = File.createTempFile("Excel2007TempTemplate", ".xlsx");
			os=new FileOutputStream(templateFile);
			wb.write(os);
		} catch (Exception e) {
			throw e;
		}finally{
			if(os!=null)
				try {
					os.close();
				} catch (IOException e1) {					
					throw e1;
				}
		}		
	}
	
   /** 
 	*    合并单元格 
	*    第一个参数：第一个单元格的行数（从0开始） 
	*    第二个参数：第二个单元格的行数（从0开始） 
	*    第三个参数：第一个单元格的列数（从0开始） 
	*    第四个参数：第二个单元格的列数（从0开始） 
    */
	int generatExcelHead(CompositeMap columnConfigs,Sheet sheet,Row header,int col) {
		CompositeMap record;			
		Long span;
		int level;
		String title;
		int rownum=header.getRowNum();		
		Iterator iterator=columnConfigs.getChildIterator();
		if(iterator!=null){
			while (iterator.hasNext()) {
				col++;			
				record = (CompositeMap) iterator.next();
				title=getPrompt(record.getString("prompt"));
				title=TextParser.parse(title, context.getObjectContext());
				Map<Integer, String> map=headMap.get(Integer.valueOf(rownum));
				if(map!=null)					
					map.put(Integer.valueOf(col), title);
				else {
					map =new TreeMap<Integer, String>();
					map.put(Integer.valueOf(col), title);
					headMap.put(Integer.valueOf(rownum), map);
				}

				level=record.getInt("_level",0);
				if(this.headLevel==0)
					this.headLevel=level;
				
				Iterator it=record.getChildIterator();				
				if(it!=null){
					span=(Long)record.getObject("column/@_count");					
					CellRangeAddress range = new CellRangeAddress(rownum,rownum,col,col+span.intValue()-1);
					sw.addMergedRegion(range);				
					while (it.hasNext()) {
						Row nextRow=sheet.getRow(rownum+1);
						if(nextRow==null)						
							nextRow=sheet.createRow(rownum+1);
						CompositeMap object = (CompositeMap) it.next();
						col=generatExcelHead(object,sheet,nextRow,col-1);
					}					
				}else{					
					columnMap.put(col, record);
					int width=record.getInt("width", 100);
					sw.setCellWidth(col+1, (short)(width/6));
					if(level!=0){
						CellRangeAddress range = new CellRangeAddress(rownum,rownum+level,col,col);
						sw.addMergedRegion(range);
					}
				}							
			}
		}
		return col;
	}
	
	public void begin(String root_name) {	
		int headerStyleIndex = styles.getHeaderStyle().getIndex();
		try {			
		    sw.beginSheet();		
		    Set<Integer> keySet=headMap.keySet();
		    Iterator<Integer> iterator=keySet.iterator();
		    Map<Integer, String> map;
		    while(iterator.hasNext()){
		    	Integer row=(Integer)iterator.next();
		    	sw.insertRow(row);
		    	map=headMap.get(row);
		    	Set<Integer> colSet=map.keySet();
		    	Iterator<Integer> it=colSet.iterator();
		    	while(it.hasNext()){
		    		Integer col=(Integer)it.next();		    		
		    		sw.createCell(col, map.get(col),headerStyleIndex);
		    	}
		    	sw.endRow();
		    }
		} catch (IOException e) {
			try {
				sw.close();
			} catch (IOException e1) {
				mLogger.log(Level.SEVERE,e1.getMessage());
				throw new RuntimeException(e1);
			}
			mLogger.log(Level.SEVERE,e.getMessage());
			throw new RuntimeException(e);
		}

	}
	public void newRow(String row_name) {
		this.headLevel++;
		if(headLevel>excel2007.getRowLimit())return;
		try {
			sw.insertRow(this.headLevel);
			rowMap=new HashMap<String, Object>();
		} catch (IOException e) {			
			try {
				sw.close();
			} catch (IOException e1) {
				mLogger.log(Level.SEVERE,e1.getMessage());
				throw new RuntimeException(e1);
			}
			mLogger.log(Level.SEVERE,e.getMessage());
			throw new RuntimeException(e);
		}			
	}
	public void loadField(String name, Object value) {		
		rowMap.put(name, value);
	}
	public void endRow() {	
		Set<Integer> keySet=columnMap.keySet();
		Iterator<Integer> iterator =keySet.iterator();
		try {
			while(iterator.hasNext()){
				Integer key=(Integer)iterator.next();	
				CompositeMap record=columnMap.get(key);
				Object att=rowMap.get(record.getString("name"));
				int col=Integer.valueOf(key);
				if(col>excel2007.getColLimit())break;	
				if (att != null) {
					if (record.getString(KEY_DATA_TYPE) != null) {
						if (KEY_DATA_TYPE_STRING.equalsIgnoreCase(record
								.getString(KEY_DATA_TYPE)))							
							sw.createCell(col,att.toString());
						else {
							try {
								sw.createCell(col,Double.parseDouble(att
										.toString()));								
							} catch (Exception e) {
								sw.createCell(col,att.toString());
							}
						}						
					} else{
	                if (att instanceof Number) {
	                    sw.createCell(col, ((Number) att).doubleValue());
	                } else if (att instanceof Calendar) {
	                    sw.createCell(col, (Calendar) att, styles.getDateStyle()
	                            .getIndex());
	                } else if (att instanceof Boolean) {
	                    sw.createCell(col, (Boolean) att);
	                } else {
	                    // ok, it seems we have no better way than dump it as a string
	                    String stringVal = att.toString();
	
	                    // if string length > excel cell limit, truncate it and warn the
	                    // user, otherwise excel workbook will be corrupted
	                    if (stringVal.length() > CELL_CHAR_LIMIT) {
	                        stringVal = TRUNCATE_WARNING
	                                + " "
	                                + stringVal.substring(0, CELL_CHAR_LIMIT
	                                        - TRUNCATE_WARNING.length() - 1);
	                        sw.createCell(col, stringVal, styles.getWarningStyle()
	                                .getIndex());
	                    } else {
	                        sw.createCell(col, stringVal);
	                    }
	                }
					}
	            }
				
			}
			sw.endRow();
		} catch (IOException e) {			
			try {
				sw.close();
			} catch (IOException e1) {
				mLogger.log(Level.SEVERE,e1.getMessage());
				throw new RuntimeException(e1);
			}
			mLogger.log(Level.SEVERE,e.getMessage());
			throw new RuntimeException(e);
		}

	}
	public void end() {
		ServiceInstance svc = ServiceInstance.getInstance(this.context.getObjectContext());		
		HttpServletResponse response = ((HttpServiceInstance) svc).getResponse();			
		setResponseHeader(response);
		OutputStream out=null;
		try {
			sw.endSheet();			
		} catch (IOException e) {			
			mLogger.log(Level.SEVERE,e.getMessage());
			throw new RuntimeException(e);
		}finally{
			try {
				sw.close();
			} catch (IOException e1) {
				mLogger.log(Level.SEVERE,e1.getMessage());
				throw new RuntimeException(e1);
			}
		}
		try {
			out=response.getOutputStream();
			BigGridUtil.substitute(templateFile, rawSheet, sheetName.substring(1), out);
		} catch (IOException e) {
			mLogger.log(Level.SEVERE,e.getMessage());
			throw new RuntimeException(e);
		}	
				
		templateFile.delete();
		rawSheet.delete();
		
	}
	public void setRecordCount(long count) {
		
	}
	public Object getResult() {		
		return null;
	}
	
	String getPrompt(String key){
		String promptString=this.localMsgProvider.getMessage(key);
		promptString=promptString==null?key:promptString;
		return promptString;
	}
	
	void setResponseHeader(HttpServletResponse response){
		String fileName = "excel";
		response.setContentType(excel2007.getMimeType());
		response.setCharacterEncoding(XML_ENCODING);
		response.setHeader("cache-control", "must-revalidate");
		response.setHeader("pragma", "public");	
		response.setHeader("Content-Disposition", "attachment; filename=\""+ fileName + excel2007.getFileExtension()+"\"");
	}
}

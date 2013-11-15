package aurora.plugin.poi;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import uncertain.composite.CompositeMap;
import uncertain.composite.DynamicObject;
import uncertain.event.IContextAcceptable;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;
import aurora.database.IResultSetConsumer;
import aurora.database.service.SqlServiceContext;
import aurora.i18n.ILocalizedMessageProvider;
import aurora.i18n.IMessageProvider;
import aurora.plugin.export.MergedHeader;
import aurora.plugin.export.ModelOutput;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

public class ExcelOutput implements IResultSetConsumer, IContextAcceptable {
	CellStyle headstyle;
	CellStyle bodystyle;	
	
	Map<Integer,CellStyle> styleMap=new HashMap<Integer,CellStyle>();
	
	Map<Integer, CompositeMap> columnMap = new TreeMap<Integer, CompositeMap>();
	Map<Integer, Map<Integer, String>> headMap = new TreeMap<Integer, Map<Integer, String>>();
	Map<String, Object> rowMap;
	int headLevel;

	ServiceContext ServiceContext;
	ILogger mLogger;
	IObjectRegistry mObjectRegistry;
	ILocalizedMessageProvider localMsgProvider;
	
	IExcelBean excelBean;

	Workbook wb;
	Sheet sheet;
	CreationHelper creationHelper;
	CellStyle bodyStyle;
	String fileName;
	String fileType;

	public final String XML_ENCODING = "UTF-8";
	public static final String KEY_DATA_TYPE = "datatype";
	public static final String KEY_DATA_TYPE_NUMBER = "Number";
	public static final String KEY_DATA_TYPE_STRING = "String";
	public static final String KEY_DATA_FORMAT = "dataFormat";

	public ExcelOutput(IObjectRegistry registry) {
		this.mObjectRegistry = registry;
		mLogger = LoggingContext
				.getLogger("aurora.plugin.poi", mObjectRegistry);
	}

	void initialization() {
		IMessageProvider msgProvider = (IMessageProvider) mObjectRegistry
				.getInstanceOfType(IMessageProvider.class);
		String langString = this.ServiceContext.getSession().getString("lang",
				"ZHS");
		localMsgProvider = msgProvider.getLocalizedMessageProvider(langString);
		CompositeMap parameter = this.ServiceContext.getParameter();
		fileName = parameter.getString(ModelOutput.KEY_FILE_NAME, "excel");
		fileType = parameter.getString(ModelOutput.KEY_FORMAT);
	}

	@Override
	public void setContext(CompositeMap context) {
		this.ServiceContext = (SqlServiceContext) DynamicObject.cast(context,
				SqlServiceContext.class);	
		initialization();
		if (Excel2007Bean.KEY_XLSX.equals(fileType)) {
			excelBean=new Excel2007Bean();
		} else if (Excel2003Bean.KEY_XLS.equals(fileType)) {
			excelBean=new Excel2003Bean();
		}
		wb=excelBean.getNewWorkbook();
		setCellStyle();
		creationHelper = wb.getCreationHelper();
		sheet = wb.createSheet();
		try {
			createExcelHeader(createHeaderConfig(), sheet, sheet.createRow(0), -1);
			sheet.createFreezePane(0, this.headLevel+1);// 冻结
		} catch (ServletException e) {
			mLogger.log(Level.SEVERE, e.getMessage());
			throw new RuntimeException(e);
		}
	}
	
	CompositeMap createHeaderConfig() throws ServletException {
		CompositeMap columnConfig = (CompositeMap) this.ServiceContext.getParameter()
				.getObject(
						ModelOutput.KEY_COLUMN_CONFIG + "/"
								+ ModelOutput.KEY_COLUMN);
		if (columnConfig == null) {
			throw new ServletException(
					"service-output tag and output attibute must be defined");
		}
		CompositeMap contextMap = this.ServiceContext.getObjectContext();
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
					columnRecord.put(KEY_DATA_TYPE, record
							.getString(KEY_DATA_TYPE
									.toLowerCase()));
				}
			}
		}
		return (new MergedHeader(columnConfig)).conifg;
	}
	/**
	 *     合并单元格      第一个参数：第一个单元格的行数（从0开始）      第二个参数：第二个单元格的行数（从0开始） 
	 *     第三个参数：第一个单元格的列数（从0开始）      第四个参数：第二个单元格的列数（从0开始）     
	 */
	int createExcelHeader(CompositeMap columnConfigs, Sheet sheet, Row header,
			int col){
		CompositeMap record;
		Long span;
		int level;
		String title;
		int rownum = header.getRowNum();
		Iterator iterator = columnConfigs.getChildIterator();
		if (iterator != null) {
			while (iterator.hasNext()) {
				col++;
				record = (CompositeMap) iterator.next();
				title = promptParse(record.getString("prompt"));
				Map<Integer, String> map = headMap.get(Integer.valueOf(rownum));
				if (map != null)
					map.put(Integer.valueOf(col), title);
				else {
					map = new TreeMap<Integer, String>();
					map.put(Integer.valueOf(col), title);
					headMap.put(Integer.valueOf(rownum), map);
				}

				level = record.getInt("_level", 0);
				if (this.headLevel == 0)
					this.headLevel = level;

				Iterator it = record.getChildIterator();
				if (it != null) {
					span = (Long) record.getObject("column/@_count");
					CellRangeAddress range = new CellRangeAddress(rownum,
							rownum, col, col + span.intValue() - 1);
					sheet.addMergedRegion(range);
					while (it.hasNext()) {
						Row nextRow = sheet.getRow(rownum + 1);
						if (nextRow == null)
							nextRow = sheet.createRow(rownum + 1);
						CompositeMap object = (CompositeMap) it.next();
						col = createExcelHeader(object, sheet, nextRow, col - 1);
					}
				} else {
					columnMap.put(col, record);					
					if (level != 0) {
						CellRangeAddress range = new CellRangeAddress(rownum,
								rownum + level, col, col);
						sheet.addMergedRegion(range);
					}
				}
			}
		}
		return col;
	}
	
	String promptParse(String key){
		String promptString = this.localMsgProvider.getMessage(key);
		promptString = promptString == null ? key : promptString;
		return promptString;
	}

	@Override
	public void begin(String root_name) {
		Iterator<Entry<Integer, Map<Integer,String>>> iterator= headMap.entrySet().iterator();
		Entry<Integer, Map<Integer,String>> entry;
		Map<Integer,String> map;
		Iterator<Entry<Integer,String>> colIterator;
		Entry<Integer,String> colEntry;
		while(iterator.hasNext()){
			entry=iterator.next();
			Integer rowIndex =entry.getKey();
			Row row = sheet.createRow(rowIndex);
			map= headMap.get(rowIndex);
			colIterator= map.entrySet().iterator();
			while(colIterator.hasNext()){
				colEntry=colIterator.next();
				int col = Integer.valueOf(colEntry.getKey());
				if (col+1 > excelBean.getColLimit())
					break;
				Cell cell = row.createCell(col);
				cell.setCellValue(creationHelper.createRichTextString(colEntry.getValue()));
				cell.setCellStyle(this.headstyle);
			}
		}
		createBodyStyle();		
	}
	
	void createBodyStyle(){
		Iterator<Entry<Integer, CompositeMap>> iterator= columnMap.entrySet().iterator();
		Entry<Integer, CompositeMap> entry;
		CellStyle style;
		CompositeMap record;
		while(iterator.hasNext()){
			entry=iterator.next();
			int col = Integer.valueOf(entry.getKey());
			if (col+1 > excelBean.getColLimit())
				break;
			record=entry.getValue();
			style=wb.createCellStyle();
			style.cloneStyleFrom(bodystyle);
			style.setAlignment(getExcelAlign(record.getString("align")));
			styleMap.put(col, style);
			int width = record.getInt("width", 100);
			sheet.setColumnWidth(col, (short) (width * 42));
		}			
	}

	@Override
	public void newRow(String row_name) {
		this.headLevel++;	
		if (headLevel+1 > excelBean.getRowLimit()){
			String errorMsg="The number of rows exceed the limit";
			mLogger.log(Level.SEVERE, errorMsg);
			throw new RuntimeException(errorMsg);
		}		
		rowMap = new HashMap<String, Object>();
	}

	@Override
	public void loadField(String name, Object value) {
		rowMap.put(name, value);
	}

	@Override
	public void endRow() {
		Row row = sheet.createRow(this.headLevel);
		Iterator<Entry<Integer, CompositeMap>> iterator= columnMap.entrySet().iterator();
		Entry<Integer, CompositeMap> entry;
		while(iterator.hasNext()){
			entry=iterator.next();
			int col = Integer.valueOf(entry.getKey());
			if (col+1 > excelBean.getColLimit())
				break;
			createCell(row.createCell(col),entry.getValue());	
		}		
	}
	
	void createCell(Cell cell,CompositeMap record){
		CellStyle style=styleMap.get(cell.getColumnIndex());
		cell.setCellStyle(style);		
		Object value = rowMap.get(record.getString("name"));		
		if (value != null) {
			if (record.getString(KEY_DATA_TYPE) != null) {
				if (KEY_DATA_TYPE_STRING.equalsIgnoreCase(record
						.getString(KEY_DATA_TYPE)))
					cell.setCellValue(creationHelper.createRichTextString(value
							.toString()));						
				else {
					try {
						cell.setCellValue(Double.parseDouble(value
								.toString()));
					} catch (Exception e) {
						cell.setCellValue(creationHelper.createRichTextString(value
								.toString()));
					}
				}						
			} else {
				if (value instanceof String) {
					cell.setCellValue(creationHelper.createRichTextString(value
							.toString()));
				}else if (value instanceof java.lang.Number) {
					cell.setCellValue(Double.parseDouble(value
							.toString()));
				}else{
					if(value!=null)
						cell.setCellValue(creationHelper.createRichTextString(value
							.toString()));
				}						
			}
		}
	}

	@Override
	public void end() {
		ServiceInstance svc = ServiceInstance.getInstance(this.ServiceContext
				.getObjectContext());	
		HttpServletResponse response = ((HttpServiceInstance) svc)
				.getResponse();		
		setResponseHeader(((HttpServiceInstance) svc).getRequest(),response);
		OutputStream out = null;
		try {
			this.ServiceContext.putBoolean("responseWrite", true);
			out =response.getOutputStream();
			wb.write(out);
		} catch (Exception e) {
			mLogger.log(Level.SEVERE, e.getMessage());
			throw new RuntimeException(e);
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (Exception e) {

				}
		}


	}

	@Override
	public void setRecordCount(long count) {

	}

	@Override
	public Object getResult() {
		return null;
	}
	void setResponseHeader(HttpServletRequest request,HttpServletResponse response){
		response.setContentType(excelBean.getMimeType());
		response.setCharacterEncoding(XML_ENCODING);
		response.setHeader("cache-control", "must-revalidate");
		response.setHeader("pragma", "public");
		try {
			String userAgent = request.getHeader("User-Agent");
			if (userAgent != null) {
				userAgent = userAgent.toLowerCase();
				if (userAgent.indexOf("msie") != -1) {
					fileName=new String(fileName.getBytes("GBK"),"ISO-8859-1");
				}else{
					fileName=new String(fileName.getBytes("UTF-8"),"ISO-8859-1");
				}
			}
			response.setHeader("Content-Disposition", "attachment; filename=\""
					+ fileName +excelBean.getFileExtension()+"\"");
		} catch (UnsupportedEncodingException e) {
			mLogger.log(Level.SEVERE, e.getMessage());
			throw new RuntimeException(e);
		}			
	}
	void setCellStyle() {
		headstyle = wb.createCellStyle();
		Font headfont =  wb.createFont();
		headfont.setFontName("宋体");
		headfont.setBoldweight(Font.BOLDWEIGHT_BOLD);// 加粗
		headfont.setFontHeightInPoints((short) 12);// 字体大小
		headstyle.setFont(headfont);
		headstyle.setAlignment(CellStyle.ALIGN_CENTER);// 左右居中
		headstyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);// 上下居中		
		
		bodystyle = wb.createCellStyle();
		bodystyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);// 上下居中
		Font bodyfont = wb.createFont();
		bodyfont.setFontName("宋体");
		bodyfont.setFontHeightInPoints((short) 12);// 字体大小
		bodystyle.setFont(bodyfont);
	}

	short getExcelAlign(String align) {
		short excelAlign = 0;
		if (align == null || "left".equalsIgnoreCase(align))
			excelAlign = CellStyle.ALIGN_LEFT;
		else if ("right".equalsIgnoreCase(align))
			excelAlign = CellStyle.ALIGN_RIGHT;
		else if ("center".equalsIgnoreCase(align))
			excelAlign = CellStyle.ALIGN_CENTER;
		return excelAlign;
	}
}

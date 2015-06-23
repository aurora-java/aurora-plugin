package aurora.plugin.poi;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import aurora.i18n.ILocalizedMessageProvider;
import aurora.plugin.export.MergedHeader;
import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.composite.transform.GroupConfig;
import uncertain.composite.transform.GroupTransformer;

public class ExcelExportImpl {
	public static final String KEY_DATA_TYPE = "datatype";
	public static final String KEY_DATA_TYPE_NUMBER = "Number";
	public static final String KEY_DATA_TYPE_STRING = "String";
	public final String KEY_DATA_FORMAT = "dataFormat";
	ILocalizedMessageProvider localMsgProvider;
	Workbook wb;

	CompositeMap dataModel;
	CompositeMap context;
	CompositeMap mergeColumn;
	CompositeMap headerConfig;
	List<CompositeMap> headerList;
	final int numberLimit = 65535;
	int headLevel;
	CellStyle headstyle;
	CellStyle bodystyle;	
	Map<Integer,CellStyle> styleMap=new HashMap<Integer,CellStyle>();
	Sheet sheet;
	CreationHelper creationHelper;


	public ExcelExportImpl(ILocalizedMessageProvider localMsgProvider) {
		this.localMsgProvider = localMsgProvider;
	}

	public void createExcel(CompositeMap dataModel, CompositeMap column_config,
			OutputStream os, CompositeMap merge_column) throws Exception {
		context=dataModel.getRoot();
		if (merge_column != null) {
			CompositeMap groupConfig = null;
			CompositeMap record;
			Iterator it = merge_column.getChildIterator();
			if (it != null) {
				groupConfig = new CompositeMap();
				while (it.hasNext()) {
					record = (CompositeMap) it.next();
					CompositeMap configRecord = new CompositeMap();
					configRecord.put(GroupConfig.KEY_GROUP_KEY_FIELDS,
							record.getString("name"));
					configRecord.put(GroupConfig.KEY_RECORD_NAME,
							record.getString("name"));
					groupConfig.addChild(configRecord);
				}
			}
			this.mergeColumn = groupConfig;
			this.dataModel = GroupTransformer.transformByConfig(dataModel,
					this.mergeColumn);
		} else {
			this.dataModel = dataModel;
		}

		headerConfig = (new MergedHeader(column_config)).conifg;
		wb = new HSSFWorkbook();	
		creationHelper=wb.getCreationHelper();
		setCellStyle(wb);// 设置列style
		createExcel();
		try {
			wb.write(os);
			os.flush();
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				os.close();
			} catch (Exception e) {
				throw e;
			}
		}
	}

	void setCellStyle(Workbook wb) {
		headstyle =  wb.createCellStyle();
		Font headfont = wb.createFont();
		headfont.setFontName("宋体");
		headfont.setBoldweight(Font.BOLDWEIGHT_BOLD);// 加粗
		headfont.setFontHeightInPoints((short) 12);// 字体大小
		headstyle.setFont(headfont);
		headstyle.setAlignment(CellStyle.ALIGN_CENTER);// 左右居中
		headstyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);// 上下居中
		bodystyle = (CellStyle) wb.createCellStyle();
		bodystyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);// 上下居中
		Font bodyfont = wb.createFont();
		bodyfont.setFontName("宋体");
//		bodyfont.setBoldweight(Font.BOLDWEIGHT_NORMAL);// 加粗
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

	void createExcel() {
		sheet = null;
		Iterator iterator = this.dataModel.getChildIterator();
		this.headLevel = 0;
		sheet = wb.createSheet();
		createExcelHeader(sheet);
		sheet.createFreezePane(0, ++this.headLevel);// 冻结
		if (iterator != null) {
			if (this.mergeColumn != null)
				createExcelTableMerge(sheet, this.dataModel);
			else
				createExcelTable(sheet, iterator);
		}
	}

	void createExcelHeader(Sheet sheet) {
		Row header = sheet.createRow(0);
		headerList = new LinkedList<CompositeMap>();
		generatExcelHead(headerConfig, sheet, header, -1);
		createBodyStyle();
	}
	
	void createBodyStyle(){		
		CompositeMap record;
		CellStyle style;
		for(int i=0,length=headerList.size();i<length;i++){
			record=headerList.get(i);
			style=wb.createCellStyle();
			style.cloneStyleFrom(bodystyle);
			style.setAlignment(getExcelAlign(record.getString("align")));
			if(record.getString(this.KEY_DATA_FORMAT)!=null){	
				style.setDataFormat(wb.createDataFormat().getFormat(record.getString(this.KEY_DATA_FORMAT)));
			}
			styleMap.put(i, style);
			int width = record.getInt("width", 100);
			sheet.setColumnWidth(i, (short) (width * 42));
		}	
	}

	void createExcelTableMerge(Sheet sheet, CompositeMap record) {
		int mergeCount = 0;
		String colName;
		int col = 0;
		CompositeMap head;
		Iterator it;
		Iterator iterator = record.getChildIterator();
		while (iterator.hasNext()) {
			CompositeMap childRecord = (CompositeMap) iterator.next();
			it = childRecord.getChildIterator();
			if (it != null) {
				createExcelTableMerge(sheet, childRecord);
				mergeCount = childRecord.getInt("_count");
				colName = childRecord.getName();
				for (int i = 0, l = this.headerList.size(); i < l; i++) {
					head = this.headerList.get(i);
					if (colName.equals(head.get("name"))) {
						col = i;
						break;
					}
				}
				CellRangeAddress range = new CellRangeAddress(this.headLevel
						- mergeCount, this.headLevel - 1, col, col);
				sheet.addMergedRegion(range);
				int count = record.getInt("_count", 0);
				record.put("_count", count + mergeCount);
			} else {
				createExcelTable(sheet, record.getChildIterator());
				record.put("_count", record.getChilds().size());
				break;
			}
		}
	}

	void createExcelTable(Sheet sheet, Iterator iterator) {
		int col = 0;
		Cell cell;
		while (iterator.hasNext()) {
			if (this.headLevel == numberLimit) {
				break;
			}		
			CompositeMap object = (CompositeMap) iterator.next();
			if (!"record".equals(object.getName()))
				continue;
			Row row = sheet.getRow(this.headLevel);
			if (row == null) {
				row = sheet.createRow(this.headLevel);
			}
			Iterator it = this.headerList.iterator();
			while (it.hasNext()) {
				CompositeMap record = (CompositeMap) it.next();
				CellStyle style=styleMap.get(col);			
				cell = row.createCell(col);
				cell.setCellStyle(style);				
				setCellValue(cell,object,record);		
				col++;
			}
			col = 0;
			this.headLevel++;
		}

	}
	
	void setCellValue(Cell cell,CompositeMap record,CompositeMap config){
		Object value = record.get(config.getString("name"));	
		if(value==null)return;
		if (config.getString(KEY_DATA_TYPE) != null) {
			if (KEY_DATA_TYPE_STRING.equalsIgnoreCase(config
					.getString(KEY_DATA_TYPE)))
				cell.setCellValue(creationHelper.createRichTextString(value.toString()));						
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

	String getPrompt(String key) {
		String promptString = localMsgProvider.getMessage(key);
		promptString = promptString == null ? key : promptString;
		promptString = TextParser.parse(promptString, context);
		return promptString;
	}

	/**
	 *     合并单元格      第一个参数：第一个单元格的行数（从0开始）      第二个参数：第二个单元格的行数（从0开始） 
	 *     第三个参数：第一个单元格的列数（从0开始）      第四个参数：第二个单元格的列数（从0开始）     
	 */
	int generatExcelHead(CompositeMap columnConfigs, Sheet sheet, Row header,
			int col) {
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
				title = getPrompt(record.getString("prompt"));
				Cell cell = header.createCell(col);
				cell.setCellValue(creationHelper.createRichTextString(title));
				cell.setCellStyle(this.headstyle);
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
						col = generatExcelHead(object, sheet, nextRow, col - 1);
					}
				} else {
					this.headerList.add(record);
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
}

package aurora.plugin.poi;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.*;

import aurora.i18n.ILocalizedMessageProvider;
import aurora.plugin.export.MergedHeader;
import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.composite.transform.GroupConfig;
import uncertain.composite.transform.GroupTransformer;

public class ExcelExportImpl {
	public static final String KEY_DATA_TYPE = "dataType";
	public static final String KEY_DATA_TYPE_NUMBER = "Number";
	public static final String KEY_DATA_TYPE_STRING = "String";
	public static final String KEY_DATA_FORMAT = "dataFormat";
	ILocalizedMessageProvider localMsgProvider;
	Workbook wb;

	CompositeMap dataModel;
	CompositeMap context;
	CompositeMap mergeColumn;
	CompositeMap headerConfig;
	List<CompositeMap> headerList;
	final int numberLimit = 65535;
	int headLevel;
	HSSFCellStyle headstyle;
	HSSFCellStyle bodystyle;	

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
		headstyle = (HSSFCellStyle) wb.createCellStyle();
		HSSFFont headfont = (HSSFFont) wb.createFont();
		headfont.setFontName("宋体");
		headfont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 加粗
		headfont.setFontHeightInPoints((short) 12);// 字体大小
		headstyle.setFont(headfont);
		headstyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 左右居中
		headstyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 上下居中
		bodystyle = (HSSFCellStyle) wb.createCellStyle();
		bodystyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 上下居中
		HSSFFont bodyfont = (HSSFFont) wb.createFont();
		bodyfont.setFontName("宋体");
		bodyfont.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);// 加粗
		bodyfont.setFontHeightInPoints((short) 12);// 字体大小
		bodystyle.setFont(bodyfont);
	}

	short getExcelAlign(String align) {
		short excelAlign = 0;
		if (align == null || "left".equalsIgnoreCase(align))
			excelAlign = HSSFCellStyle.ALIGN_LEFT;
		else if ("right".equalsIgnoreCase(align))
			excelAlign = HSSFCellStyle.ALIGN_RIGHT;
		else if ("center".equalsIgnoreCase(align))
			excelAlign = HSSFCellStyle.ALIGN_CENTER;
		return excelAlign;
	}

	void createExcel() {
		Sheet sheet = null;
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
		boolean is_setwidth = false;
		int col = 0;
		Cell cell;
		String columnName;

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
				cell = row.createCell(col);
				CompositeMap record = (CompositeMap) it.next();
				CellStyle dateStyle=wb.createCellStyle();
				if(record.getString(this.KEY_DATA_FORMAT)!=null){	
					dateStyle.setDataFormat(wb.createDataFormat().getFormat(record.getString(this.KEY_DATA_FORMAT)));
				}
				columnName = record.getString("name");
				Object value = object.get(columnName);
				bodystyle
						.setAlignment(getExcelAlign(record.getString("align")));
				cell.setCellType(Cell.CELL_TYPE_STRING);
				cell.setCellStyle(bodystyle);
				if (value != null) {
					if (record.getString(KEY_DATA_TYPE) != null) {
						if (KEY_DATA_TYPE_STRING.equalsIgnoreCase(record
								.getString(KEY_DATA_TYPE)))
							cell.setCellValue(new HSSFRichTextString(value
									.toString()));						
						else {
							try {
								cell.setCellValue(Double.parseDouble(value
										.toString()));
							} catch (Exception e) {
								cell.setCellValue(new HSSFRichTextString(value
										.toString()));
							}
						}						
					} else {
						if (value instanceof String) {
							cell.setCellValue(new HSSFRichTextString(value
									.toString()));
						}else if (value instanceof java.lang.Number) {
							cell.setCellValue(Double.parseDouble(value
									.toString()));
						}else{
							if(value!=null)
								cell.setCellValue(new HSSFRichTextString(value
									.toString()));
						}						
					}
					cell.setCellStyle(dateStyle);
				}

				if (!is_setwidth) {
					int width = record.getInt("width", 100);
					sheet.setColumnWidth(col, (short) (width * 35.7));
				}
				col++;
			}
			is_setwidth = true;
			col = 0;
			this.headLevel++;
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
				cell.setCellValue(new HSSFRichTextString(title));
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

package aurora.plugin.excelreport;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.composite.transform.GroupConfig;
import uncertain.composite.transform.GroupTransformer;

public class SheetWrap {
	String name;
	DynamicContent dynamicContent;
	CellData[] staticContent;

	private int currentRownum = 0;
	private CreationHelper createHelper;
	private Sheet sheet;
	ExcelFactory excelFactory;

	public DynamicContent getDynamicContent() {
		return dynamicContent;
	}

	public void addDynamicContent(DynamicContent dynamicContent) {
		this.dynamicContent = dynamicContent;
	}

	public CellData[] getStaticContent() {
		return staticContent;
	}

	public void setStaticContent(CellData[] staticContent) {
		this.staticContent = staticContent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void createSheet(Workbook wb, CompositeMap context,
			ExcelFactory excelFactory) {
		this.sheet = wb.createSheet(this.getName());
		this.createHelper = wb.getCreationHelper();
		this.excelFactory = excelFactory;
		createDynamicContent(context);
		createStaticContent(context);
	}

	void createStaticContent(CompositeMap context) {
		int rownum;
		int colnum;
		if(this.getStaticContent()==null)return;
		for (CellData cellDataObj : this.getStaticContent()) {
			colnum = CellReference.convertColStringToIndex(cellDataObj
					.getCell());
			if (cellDataObj.getOffset()) {
				rownum = this.currentRownum + cellDataObj.getRow();
			} else {
				rownum = cellDataObj.getRow();
			}
			Row row = ExcelFactory.createRow(sheet, rownum);
			Cell cell = row.createCell(colnum);
			if (cellDataObj.getStyleName() != null
					&& !"".equals(cellDataObj.getStyleName()))
				cell.setCellStyle(excelFactory.styles.get(cellDataObj
						.getStyleName()));
			if (cellDataObj.getRange() != null) {
				sheet.addMergedRegion(CellRangeAddress.valueOf(cellDataObj
						.getRange()));
			}
			if (CellData.KEY_FORMULA.equals(cellDataObj.getType())) {
				cell.setCellFormula(cellDataObj.getValue());
			} else {
				String value = cellDataObj.getValue();
				value = TextParser.parse(value, context);
				if (value == null || "".equals(value))
					continue;
				String dataType = cellDataObj.getDataType();
				if (CellData.KEY_DATA_TYPE_STRING.equals(dataType))
					cell.setCellValue(createHelper.createRichTextString(value));
				if (CellData.KEY_DATA_TYPE_NUMBER.equals(dataType))
					cell.setCellValue(Double.valueOf(value).doubleValue());
				else
					cell.setCellValue(createHelper.createRichTextString(value));
			}
		}
	}

	void createTableTitle(CompositeMap context, DynamicContent dynamicContent,
			Sheet sheet) {
		List<TableColumn> list = dynamicContent.getTableColumnList();
		if (list == null)
			return;
		currentRownum = dynamicContent.getRow();
		Row row = ExcelFactory.createRow(sheet, currentRownum);
		int cellAddress = CellReference.convertColStringToIndex(dynamicContent
				.getCell());
		Iterator<TableColumn> it = list.iterator();
		TableColumn column;
		while (it.hasNext()) {
			column = it.next();
			Cell cell = row.createCell(cellAddress++);
			cell.setCellValue(createHelper.createRichTextString(column
					.getTitle()));
			if (column.getTitleStyle() != null
					&& !"".equals(column.getTitleStyle()))
				cell.setCellStyle(excelFactory.styles.get(column
						.getTitleStyle()));
		}
	}

	List<Integer> createTableGroup(CompositeMap dataModel,
			DynamicContent dynamicContent, Sheet sheet) {
		Iterator it = dataModel.getChildIterator();
		TableColumn column;
		List<Integer> rowList = new LinkedList<Integer>();
		if (it != null) {
			while (it.hasNext()) {
				CompositeMap childMap = (CompositeMap) it.next();
				if (childMap.getChildIterator() != null) {
					rowList.addAll(createTableGroup(childMap, dynamicContent,
							sheet));
				} else {
					rowList.add(createRecord(childMap, dynamicContent, sheet));
				}
			}
			if (rowList.size() != 0) {
				List<TableColumn> list = dynamicContent.getTableColumnList();
				Iterator<TableColumn> colIt = list.iterator();
				int cellAddress = CellReference
						.convertColStringToIndex(dynamicContent.getCell());
				Row row = ExcelFactory.createRow(sheet, ++currentRownum);
				while (colIt.hasNext()) {
					column = colIt.next();
					if (column.getGroupFormula() != null) {
						Cell cell = row.createCell(cellAddress);
						StringBuffer colBuffer = new StringBuffer();
						colBuffer.append(column.getGroupFormula());
						colBuffer.append("(");
						Iterator iterator = rowList.iterator();
						boolean is_first = true;
						while (iterator.hasNext()) {
							if (!is_first)
								colBuffer.append(",");

							colBuffer.append(CellReference
									.convertNumToColString(cell
											.getColumnIndex()));
							colBuffer.append(iterator.next());
							is_first = false;
						}
						colBuffer.append(")");
						cell.setCellFormula(colBuffer.toString());
					} else if (column.getGroupDesc() != null
							&& (dataModel.get(column.getField()) != null)) {
						Cell cell = row.createCell(cellAddress);
						cell.setCellValue(createHelper
								.createRichTextString(column.getGroupDesc()));
					}
					cellAddress++;
				}
				if(!dataModel.getName().startsWith("level")){
					Cell cell = row.createCell(CellReference.convertColStringToIndex(dynamicContent.getCell()));
					cell.setCellValue(createHelper
							.createRichTextString(dynamicContent.getGroupDesc()));
				}
			}
		}
		return rowList;
	}

	int createRecord(CompositeMap record, DynamicContent dynamicContent,
			Sheet sheet) {
		List<TableColumn> list = dynamicContent.getTableColumnList();
		TableColumn column;
		Row row = ExcelFactory.createRow(sheet, ++currentRownum);
		int cellAddress = CellReference.convertColStringToIndex(dynamicContent
				.getCell());
		Iterator<TableColumn> it = list.iterator();
		while (it.hasNext()) {
			column = it.next();
			Cell cell = row.createCell(cellAddress++);
			Object value = record.get(column.getField());
			setCellValue(cell, value);
			if (column.getCellStyleName() != null
					&& !"".equals(column.getCellStyleName()))
				cell.setCellStyle(excelFactory.styles.get(column
						.getCellStyleName()));
		}
		return currentRownum;
	}

	void setCellValue(Cell cell, Object value) {
		if (value == null)
			return;
		if (value instanceof String) {
			cell.setCellValue(createHelper.createRichTextString((String) value));
			return;
		}
		if (value instanceof Number) {
			cell.setCellValue(Double.parseDouble(value.toString()));
			return;
		}
		if (value instanceof Date) {
			cell.setCellValue((Date) value);
			return;
		} else {
			cell.setCellValue(value.toString());
			return;
		}
	}

	void createDynamicContent(CompositeMap context) {
		if(this.getDynamicContent()==null)return;
		createTableTitle(context, this.getDynamicContent(), sheet);
		CompositeMap data = (CompositeMap) context.getObject(this
				.getDynamicContent().getDataModel());
		if (data == null)
			return;

		GroupConfig[] groupConfig = createGroupConfig(data,
				this.getDynamicContent());
		if (groupConfig != null) {
			CompositeMap target = GroupTransformer.transform(data, groupConfig);
			createTableGroup(target, this.getDynamicContent(), sheet);
		} else {
			Iterator rowIt = data.getChildIterator();
			if (rowIt == null)
				return;
			while (rowIt.hasNext()) {
				CompositeMap record = (CompositeMap) rowIt.next();
				createRecord(record, this.getDynamicContent(), sheet);
			}
		}
	}

	GroupConfig[] createGroupConfig(CompositeMap dataModel,
			DynamicContent dynamicContent) {
		CompositeMap groupConfig = new CompositeMap();
		List<TableColumn> list = dynamicContent.getTableColumnList();
		TableColumn column;
		Iterator<TableColumn> it = list.iterator();
		CompositeMap configRecord = null;
		StringBuffer buffer = new StringBuffer();
		int level = 0;
		while (it.hasNext()) {
			column = it.next();
			if (level != column.getGroupLevel()) {
				level = column.getGroupLevel();
				if (buffer.length() != 0) {
					configRecord.put(GroupConfig.KEY_GROUP_KEY_FIELDS,
							buffer.toString());
					configRecord.put(GroupConfig.KEY_RECORD_NAME, "level"
							+ (level + 1));
					groupConfig.addChild(configRecord);
				}
				buffer = new StringBuffer();
				configRecord = new CompositeMap();
			}
			if (level != 0) {
				if (buffer.length() != 0)
					buffer.append(",");
				buffer.append(column.getField());
			}

		}
		if (groupConfig.getChildIterator() != null) {
			GroupConfig[] configs = GroupConfig.createGroupConfigs(groupConfig);		
			for (GroupConfig config : configs) {
				config.setExtendParentAttributes(false);
			}
			return configs;
		} else
			return null;
	}
}

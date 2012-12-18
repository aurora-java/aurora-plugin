package aurora.plugin.excelreport;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;

public class SheetWrap {
	String name;
	DynamicContent dynamicContent;
	CellData[] staticContent;

	private int offsetRowIndex = 0;

	private Sheet excelSheet;
	ExcelFactory excelFactory;
	int totalCount = -1;

	public void createSheet(Workbook wb, CompositeMap context,
			ExcelFactory excelFactory) {
		this.excelSheet = wb.createSheet(this.getName());
		this.excelFactory = excelFactory;
		if (this.getDynamicContent() != null)
			this.offsetRowIndex = this.getDynamicContent().createContent(
					context, excelFactory, this.excelSheet);
		if (this.getStaticContent() != null)
			createStaticContent(context);
	}

	void createStaticContent(CompositeMap context) {
		int rowIndex;
		int colIndex;

		Row row;
		Cell cell;
		CellStyle cellStyle;

		if (this.getStaticContent() == null)
			return;
		for (CellData cellConfig : this.getStaticContent()) {
			if (cellConfig.getOffset()) {
				rowIndex = this.offsetRowIndex + cellConfig.getRow();
			} else {
				rowIndex = cellConfig.getRow();
			}

			row = ExcelFactory.createRow(this.excelSheet, rowIndex);
			colIndex = CellReference.convertColStringToIndex(cellConfig
					.getCell());
			cell = row.createCell(colIndex);
			cellStyle = this.excelFactory.getStyle(cellConfig.getStyleName());

			if (ExcelFactory.isNotNull(cellStyle)) {
				cell.setCellStyle(cellStyle);
			}
			if (cellConfig.getRange() != null) {
				this.excelSheet.addMergedRegion(CellRangeAddress
						.valueOf(cellConfig.getRange()));
			}
			if (CellData.KEY_FORMULA.equals(cellConfig.getType())) {
				cell.setCellFormula(cellConfig.getValue());
			} else {
				String value = cellConfig.getValue();
				this.excelFactory.setCellValue(cell,
						TextParser.parse(value, context),
						cellConfig.getDataType());
			}
		}
	}

	// List<CompositeMap> createTableGroup(CompositeMap dataModel,
	// DynamicContent dynamicContent) {
	// Iterator it = dataModel.getChildIterator();
	// List<CompositeMap> rowList = new LinkedList<CompositeMap>();
	// if (it != null) {
	// while (it.hasNext()) {
	// CompositeMap childMap = (CompositeMap) it.next();
	// if (childMap.getChildIterator() != null) {
	// rowList.addAll(createTableGroup(childMap, dynamicContent
	// ));
	// } else {
	// rowList.add(createRecord(childMap, dynamicContent));
	// }
	// }
	// if (rowList.size() != 0) {
	// Map<String, List<TableColumn>> groupMap=dynamicContent.getGroupMap();
	// List<TableColumn> list=groupMap.get(dataModel.getName());
	// if(list!=null){
	// aaa(list,rowList,false);
	// }
	// if (!dataModel.getName().startsWith("level")) {
	// CompositeMap m=null;
	// Iterator<String> iterator=new
	// TreeSet<String>(groupMap.keySet()).iterator();
	// while(iterator.hasNext()){
	// String levelName=iterator.next();
	// List<TableColumn> list1=groupMap.get(levelName);
	// m=aaa(list1,rowList,true);
	// }
	// int firstRownum=m.getInt("first");
	// int endRownum=m.getInt("end");
	// this.excelSheet.groupRow(firstRownum-1, endRownum+totalCount);
	// }
	// }
	// }
	// return rowList;
	// }

	// CompositeMap aaa(List<TableColumn> groupList,List<CompositeMap>
	// rowList,boolean is_total){
	// Map<String, TableColumn> columnMap = dynamicContent
	// .getColumnsMap();
	// Iterator<TableColumn> colIt = groupList.iterator();
	// String curGroupFormula=null;
	// Row row =null;
	// TableColumn column;
	// int firstRownum=0;
	// int endRownum=0;
	// boolean is_flag=false;
	// boolean is_group=false;
	// while (colIt.hasNext()) {
	// column = colIt.next();
	// if(!column.getGroupFormula().equals(curGroupFormula)){
	// curGroupFormula=column.getGroupFormula();
	// row = ExcelFactory.createRow(this.excelSheet, ++this.curRowIndex);
	// is_flag=true;
	// is_group=true;
	// if(is_total)
	// totalCount++;
	// }
	// if (column.getGroupFormula() != null) {
	// Cell cell = row.createCell(column.getIndex());
	// StringBuffer colBuffer = new StringBuffer("SUBTOTAL(");
	// colBuffer.append(column.getGroupFormula());
	// colBuffer.append(",");
	// String ref1 = null;
	// String ref2 = null;
	// Iterator<CompositeMap> iterator = rowList.iterator();
	// String value=null;
	// boolean is_first = true;
	// while (iterator.hasNext()) {
	// CompositeMap map1=iterator.next();
	// value=((CompositeMap)map1.get("record")).getString(column.getGroupField());
	// if (is_first) {
	// firstRownum=map1.getInt("rownum");
	// ref1 = CellReference.convertNumToColString(cell
	// .getColumnIndex()) + firstRownum;
	// colBuffer.append(ref1);
	// colBuffer.append(":");
	// is_first = false;
	// } else {
	// endRownum=map1.getInt("rownum");
	// ref2 = CellReference.convertNumToColString(cell
	// .getColumnIndex()) + endRownum;
	// }
	// }
	// if (ref2 == null){
	// ref2 = ref1;
	// endRownum=firstRownum;
	// }
	// colBuffer.append(ref2);
	// colBuffer.append(")");
	// cell.setCellFormula(colBuffer.toString());
	// if(is_flag){
	// String field = column.getGroupField();
	// TableColumn groupColumn = columnMap.get(field);
	// Cell groupCell = row.createCell(groupColumn.getIndex());
	// if(is_total)
	// groupCell.setCellValue(value+" "+column.getTotalDesc());
	// else
	// groupCell.setCellValue(value+" "+column.getGroupDesc());
	// groupCell.setCellStyle(excelFactory.styles.get("group"));
	// }
	//
	// }
	// if(is_group){
	// if(!is_total){
	// this.excelSheet.groupRow(firstRownum-1, (this.curRowIndex-2));
	// }
	// is_group=false;
	// }
	// }
	// if(is_total){
	// CompositeMap map=new CompositeMap();
	// map.put("first", firstRownum);
	// map.put("end", endRownum);
	// return map;
	// }else{
	// return null;
	// }
	// }

	// void createTableTitle(CompositeMap context, DynamicContent
	// dynamicContent) {
	// List<TableColumn> list = dynamicContent.getTableColumnList();
	// if (list == null)return;
	//
	// this.curRowIndex = dynamicContent.getRow();
	// Row row = ExcelFactory.createRow(this.excelSheet, this.curRowIndex);
	//
	// CellStyle style;
	// TableColumn column;
	//
	// int cellnum = CellReference.convertColStringToIndex(dynamicContent
	// .getCell());
	// Iterator<TableColumn> it = list.iterator();
	// while (it.hasNext()) {
	// column = it.next();
	// Cell cell = row.createCell(cellnum++);
	// setCellValue(cell,column.getTitle());
	// style=this.excelFactory.getStyle(column.getTitleStyle());
	// if(ExcelFactory.isNotNull(style)){
	// cell.setCellStyle(style);
	// }
	// }
	// this.excelSheet.createFreezePane(0, this.curRowIndex);// 冻结
	// }

	// CompositeMap createRecord(CompositeMap record, DynamicContent
	// dynamicContent,boolean isHead) {
	// List<TableColumn> list = dynamicContent.getTableColumnList();
	// if (list == null)return null;
	//
	// TableColumn column;
	// Row row = ExcelFactory.createRow(this.excelSheet, ++this.curRowIndex);
	// int cellnum = CellReference.convertColStringToIndex(dynamicContent
	// .getCell());
	// Iterator<TableColumn> it = list.iterator();
	// while (it.hasNext()) {
	// column = it.next();
	// Cell cell = row.createCell(cellnum++);
	// Object value = record.get(column.getField());
	// setCellValue(cell, value);
	//
	// if (column.getCellStyle() != null
	// && !"".equals(column.getCellStyle()))
	// cell.setCellStyle(excelFactory.styles.get(column
	// .getCellStyle()));
	// }
	// CompositeMap map=new CompositeMap();
	// map.put("rownum", this.curRowIndex);
	// map.put("record", record);
	// return map;
	// }
	//
	// CompositeMap createRecord(CompositeMap record, DynamicContent
	// dynamicContent) {
	// List<TableColumn> list = dynamicContent.getTableColumnList();
	// if (list == null)return null;
	//
	// TableColumn column;
	// Row row = ExcelFactory.createRow(this.excelSheet, ++this.curRowIndex);
	// int cellnum = CellReference.convertColStringToIndex(dynamicContent
	// .getCell());
	// Iterator<TableColumn> it = list.iterator();
	// while (it.hasNext()) {
	// column = it.next();
	// Cell cell = row.createCell(cellnum++);
	// Object value = record.get(column.getField());
	// setCellValue(cell, value);
	//
	// if (column.getCellStyle() != null
	// && !"".equals(column.getCellStyle()))
	// cell.setCellStyle(excelFactory.styles.get(column
	// .getCellStyle()));
	// }
	// CompositeMap map=new CompositeMap();
	// map.put("rownum", this.curRowIndex);
	// map.put("record", record);
	// return map;
	// }

	// void createDynamicContent(CompositeMap context) {
	// if (this.getDynamicContent() == null)
	// return;
	// createTableTitle(context, this.getDynamicContent());
	// CompositeMap data = (CompositeMap) context.getObject(this
	// .getDynamicContent().getDataModel());
	// if (data == null)
	// return;
	//
	// GroupConfig[] groupConfig = createGroupConfig(data,
	// this.getDynamicContent());
	// if (groupConfig != null) {
	// CompositeMap target = GroupTransformer.transform(data, groupConfig);
	// createTableGroup(target, this.getDynamicContent());
	// } else {
	// Iterator rowIt = data.getChildIterator();
	// if (rowIt == null)
	// return;
	// while (rowIt.hasNext()) {
	// CompositeMap record = (CompositeMap) rowIt.next();
	// createRecord(record, this.getDynamicContent());
	// }
	// }
	// }

	// GroupConfig[] createGroupConfig(CompositeMap dataModel,
	// DynamicContent dynamicContent) {
	// dynamicContent.createGroupMap();
	// CompositeMap groupConfig = new CompositeMap();
	// List<TableColumn> list = dynamicContent.getTableColumnList();
	// TableColumn column;
	// Iterator<TableColumn> it = list.iterator();
	// CompositeMap configRecord = null;
	// StringBuffer buffer = new StringBuffer();
	// int level = 0;
	// int index = CellReference.convertColStringToIndex(dynamicContent
	// .getCell());
	// while (it.hasNext()) {
	// column = it.next();
	// column.setIndex(index);
	// dynamicContent.putColumnsMap(column.getField(), column);
	// if (level != column.getGroupLevel()) {
	// level = column.getGroupLevel();
	// if (buffer.length() != 0) {
	// configRecord.put(GroupConfig.KEY_GROUP_KEY_FIELDS,
	// buffer.toString());
	// configRecord.put(GroupConfig.KEY_RECORD_NAME, "level"
	// + (level + 1));
	// groupConfig.addChild(configRecord);
	// }
	// buffer = new StringBuffer();
	// configRecord = new CompositeMap();
	// }
	// if (level != 0) {
	// if (buffer.length() != 0)
	// buffer.append(",");
	// buffer.append(column.getField());
	// }
	// index++;
	// }
	// if (groupConfig.getChildIterator() != null) {
	// // System.out.println(groupConfig.toXML());
	// GroupConfig[] configs = GroupConfig.createGroupConfigs(groupConfig);
	// for (GroupConfig config : configs) {
	// config.setExtendParentAttributes(false);
	// }
	// return configs;
	// } else
	// return null;
	// }

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
}

package aurora.plugin.excelreport;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
	Set<String> groupLevelSet=new HashSet<String>();
	int totalCount=-1;
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
		if (this.getStaticContent() == null)
			return;
		for (CellData cellConfig : this.getStaticContent()) {
			colnum = CellReference.convertColStringToIndex(cellConfig.getCell());
			if (cellConfig.getOffset()) {
				rownum = this.currentRownum + cellConfig.getRow();
			} else {
				rownum = cellConfig.getRow();
			}
			Row row = ExcelFactory.createRow(sheet, rownum);
			Cell cell = row.createCell(colnum);
			if (cellConfig.getStyleName() != null
					&& !"".equals(cellConfig.getStyleName()))
				cell.setCellStyle(excelFactory.styles.get(cellConfig
						.getStyleName()));
			if (cellConfig.getRange() != null) {
				sheet.addMergedRegion(CellRangeAddress.valueOf(cellConfig
						.getRange()));
			}
			if (CellData.KEY_FORMULA.equals(cellConfig.getType())) {
				cell.setCellFormula(cellConfig.getValue());
			} else {
				String value = cellConfig.getValue();
				value = TextParser.parse(value, context);
				if (value == null || "".equals(value))
					continue;
				String dataType = cellConfig.getDataType();
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
		this.sheet.createFreezePane(0, currentRownum);// 冻结		
	}
	
	List<CompositeMap> createTableGroup(CompositeMap dataModel,
			DynamicContent dynamicContent, Sheet sheet) {
		Iterator it = dataModel.getChildIterator();		
		List<CompositeMap> rowList = new LinkedList<CompositeMap>();
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
				Map<String, List<TableColumn>> groupMap=dynamicContent.getGroupMap();
				List<TableColumn> list=groupMap.get(dataModel.getName());
				if(list!=null){				
					aaa(list,rowList,false);				
				}				
				if (!dataModel.getName().startsWith("level")) {		
					CompositeMap m=null;
					Iterator<String> iterator=new TreeSet<String>(groupMap.keySet()).iterator();
					while(iterator.hasNext()){
						String levelName=iterator.next();
						List<TableColumn> list1=groupMap.get(levelName);
						m=aaa(list1,rowList,true);						
					}
					int firstRownum=m.getInt("first");
					int endRownum=m.getInt("end");
					this.sheet.groupRow(firstRownum-1, endRownum+totalCount);
//					System.out.println("total this.sheet.groupRow("+(firstRownum-1)+","+(endRownum+totalCount)+")");
						
				}
			}
		}
		return rowList;
	}
	
	CompositeMap aaa(List<TableColumn> groupList,List<CompositeMap> rowList,boolean is_total){
		Map<String, TableColumn> columnMap = dynamicContent
				.getColumnsMap();		
		Iterator<TableColumn> colIt = groupList.iterator();				
		String curGroupFormula=null;
		Row row =null;
		TableColumn column;
		int firstRownum=0;
		int endRownum=0;
		boolean is_flag=false;
		boolean is_group=false;		
		while (colIt.hasNext()) {
			column = colIt.next();					
			if(!column.getGroupFormula().equals(curGroupFormula)){
				curGroupFormula=column.getGroupFormula();
				row = ExcelFactory.createRow(sheet, ++currentRownum);			
				is_flag=true;
				is_group=true;	
				if(is_total)
					totalCount++;
			}
			if (column.getGroupFormula() != null) {										
				Cell cell = row.createCell(column.getIndex());
				StringBuffer colBuffer = new StringBuffer("SUBTOTAL(");
				colBuffer.append(column.getGroupFormula());
				colBuffer.append(",");
				String ref1 = null;
				String ref2 = null;
				Iterator<CompositeMap> iterator = rowList.iterator();
				String value=null;
				boolean is_first = true;
				while (iterator.hasNext()) {
					CompositeMap map1=iterator.next();
					value=((CompositeMap)map1.get("record")).getString(column.getGroupField());
					if (is_first) {
						firstRownum=map1.getInt("rownum");
						ref1 = CellReference.convertNumToColString(cell
								.getColumnIndex()) + firstRownum;
						colBuffer.append(ref1);
						colBuffer.append(":");
						is_first = false;
					} else {
						endRownum=map1.getInt("rownum");
						ref2 = CellReference.convertNumToColString(cell
								.getColumnIndex()) + endRownum;
					}
				}
				if (ref2 == null){
					ref2 = ref1;
					endRownum=firstRownum;
				}
				colBuffer.append(ref2);
				colBuffer.append(")");
//				System.out.println(colBuffer);
				cell.setCellFormula(colBuffer.toString());
				if(is_flag){
					String field = column.getGroupField();
					TableColumn groupColumn = columnMap.get(field);
					Cell groupCell = row.createCell(groupColumn.getIndex());
					if(is_total)
						groupCell.setCellValue(value+" "+column.getTotalDesc());
					else
						groupCell.setCellValue(value+" "+column.getGroupDesc());
					groupCell.setCellStyle(excelFactory.styles.get("group"));
				}
				
			}	
			if(is_group){
				if(!is_total){
					this.sheet.groupRow(firstRownum-1, (currentRownum-2));
//					System.out.println("this.sheet.groupRow("+(firstRownum-1)+","+ (currentRownum-2)+")");
				}		
				is_group=false;
			}			
		}	
		if(is_total){
			CompositeMap map=new CompositeMap();
			map.put("first", firstRownum);
			map.put("end", endRownum);
			return map;
		}else{
			return null;
		}
	}	
	
	CompositeMap createRecord(CompositeMap record, DynamicContent dynamicContent,
			Sheet sheet) {
		CompositeMap map=new CompositeMap();
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
		map.put("rownum", currentRownum);
		map.put("record", record);
		return map;
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
		if (this.getDynamicContent() == null)
			return;
		createTableTitle(context, this.getDynamicContent(), sheet);
		CompositeMap data = (CompositeMap) context.getObject(this
				.getDynamicContent().getDataModel());
		if (data == null)
			return;

		GroupConfig[] groupConfig = createGroupConfig(data,
				this.getDynamicContent());
		if (groupConfig != null) {
			CompositeMap target = GroupTransformer.transform(data, groupConfig);
//			System.out.println(target.toXML());
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
		dynamicContent.createGroupMap();
		CompositeMap groupConfig = new CompositeMap();
		List<TableColumn> list = dynamicContent.getTableColumnList();
		TableColumn column;
		Iterator<TableColumn> it = list.iterator();
		CompositeMap configRecord = null;
		StringBuffer buffer = new StringBuffer();		
		int level = 0;
		int index = CellReference.convertColStringToIndex(dynamicContent
				.getCell());
		while (it.hasNext()) {
			column = it.next();
			column.setIndex(index);
			dynamicContent.putColumnsMap(column.getField(), column);			
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
			index++;
		}
		if (groupConfig.getChildIterator() != null) {
//			System.out.println(groupConfig.toXML());
			GroupConfig[] configs = GroupConfig.createGroupConfigs(groupConfig);
			for (GroupConfig config : configs) {
				config.setExtendParentAttributes(false);
			}
			return configs;
		} else
			return null;
	}
}

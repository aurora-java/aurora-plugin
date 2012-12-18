package aurora.plugin.excelreport;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;

import uncertain.composite.CompositeMap;
import uncertain.composite.transform.GroupConfig;
import uncertain.composite.transform.GroupTransformer;
import uncertain.core.UncertainEngine;
import uncertain.ocm.OCManager;

public class DynamicContent {
	int totalCount = -1;
	String cell = "A";
	String dataModel;
	int row = 1;
	CompositeMap columns;
	List<TableColumn> columnList;
	// LinkedList<LinkedList<Integer[]>> groupRowList=new
	// LinkedList<LinkedList<Integer[]>>();
	// Map<Integer,Integer> limitMap=new HashMap<Integer,Integer>();
	// int groupLimit=6;
	OCManager mOCManager;
	Map<String, TableColumn> columnsMap = new HashMap<String, TableColumn>();
	// Map<String, List<TableColumn>> groupMap = new HashMap<String,
	// List<TableColumn>>();

	private int rowIndex = 0;
	private Sheet excelSheet;
	ExcelFactory excelFactory;

	CompositeMap groupMap = new CompositeMap();

	public CompositeMap getGroupMap() {
		return groupMap;
	}

	public void createGroupMap() {
		Iterator<TableColumn> iterator = columnList.iterator();
		TableColumn column;
		int level = 0;
		CompositeMap record;
		String[] groupFields;
		List<SubtotalConfig> list = new LinkedList<SubtotalConfig>();
		while (iterator.hasNext()) {
			column = iterator.next();
			SubtotalConfig[] groupConfigs = column.getGroups();
			if (groupConfigs != null) {
				for (SubtotalConfig config : groupConfigs) {
					groupFields = config.getGroupField().split(",");
					if (groupFields == null)
						throw new RuntimeException("groupField is null");
					for (String groupField : groupFields) {
						TableColumn groupColumn = columnsMap.get(groupField);
						level = groupColumn.getGroupLevel();
						if (level == 0)
							throw new RuntimeException("groupField:"
									+ groupField + ";groupLevel is null or 0");
						record = groupMap.getChild("level" + level);
						SubtotalConfig configClone = new SubtotalConfig();
						configClone.setGroupDesc(config.getGroupDesc());
						configClone.setGroupField(config.getGroupField());
						configClone.setGroupFormula(config.getGroupFormula());
						configClone.setTotalDesc(config.getTotalDesc());
						configClone.setGroupStyle(config.getGroupStyle());
						configClone.setColumnField(column.getField());
						configClone.setGroupColumnFild(groupColumn.getField());
						list = (LinkedList<SubtotalConfig>) record.get("list");
						if (list == null)
							list = new LinkedList<SubtotalConfig>();
						list.add(configClone);
						record.put("list", list);
					}
				}
			}
		}

	}

	// public void createGroupMap() {
	// Iterator<TableColumn> iterator = columnList.iterator();
	// TableColumn column;
	// List<TableColumn> list = new LinkedList<TableColumn>();
	//
	// int level = 0;
	//
	// while (iterator.hasNext()) {
	// column = iterator.next();
	//
	// if (level != column.getGroupLevel()) {
	// if (list.size() != 0) {
	// groupMap.put("level" + (level + 1), list);
	// list = new LinkedList<TableColumn>();
	// }
	// level = column.getGroupLevel();
	// }
	// if (column.getGroupFormula() != null)
	// list.add(column);
	// }
	// if (list.size() != 0)
	// groupMap.put("level" + (level + 1), list);
	// }

	public Map<String, TableColumn> getColumnsMap() {
		return columnsMap;
	}

	public void putColumnsMap(String name, TableColumn tableColumn) {
		columnsMap.put(name, tableColumn);
	}

	public DynamicContent(UncertainEngine uncertainEngine) {
		mOCManager = uncertainEngine.getOcManager();
	}

	public String getCell() {
		return cell;
	}

	public void setCell(String cell) {
		this.cell = cell;
	}

	public String getDataModel() {
		return dataModel;
	}

	public void setDataModel(String dataModel) {
		this.dataModel = dataModel;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public CompositeMap getColumns() {
		return columns;
	}

	public void setColumns(CompositeMap columns) {
		this.columns = columns;
		setTableColumnList(columns);
	}

	public List<TableColumn> getTableColumnList() {
		return this.columnList;
	}

	public void setTableColumnList(CompositeMap columns) {
		Iterator colIt = columns.getChildIterator();
		if (colIt == null)
			return;

		List<TableColumn> colList = new LinkedList<TableColumn>();
		CompositeMap colConfig;
		while (colIt.hasNext()) {
			colConfig = (CompositeMap) colIt.next();
			TableColumn tableColumn = (TableColumn) mOCManager
					.createObject(colConfig);
			colList.add(tableColumn);
		}
		this.columnList = colList;
	}

	public int createContent(CompositeMap context, ExcelFactory excelFactory,
			Sheet excelSheet) {
		this.excelSheet = excelSheet;
		this.excelFactory = excelFactory;
		createTableTitle(context);
		CompositeMap data = (CompositeMap) context.getObject(getDataModel());
		if (data == null)
			return this.rowIndex;
		GroupConfig[] groupConfig = createGroupConfig(data);
		if (groupConfig != null) {
			CompositeMap target = GroupTransformer.transform(data, groupConfig);
			createGroupMap();
			createTableGroup(target);
		} else {
			Iterator rowIt = data.getChildIterator();
			if (rowIt == null)
				return this.rowIndex;
			while (rowIt.hasNext()) {
				CompositeMap record = (CompositeMap) rowIt.next();
				createRecord(record);
			}
		}
		return this.rowIndex;
	}

	void createTableTitle(CompositeMap context) {
		List<TableColumn> list = getTableColumnList();
		if (list == null)
			return;

		this.rowIndex = getRow();
		Row row = ExcelFactory.createRow(this.excelSheet, this.rowIndex);

		CellStyle style;
		TableColumn column;

		int cellnum = CellReference.convertColStringToIndex(getCell());
		Iterator<TableColumn> it = list.iterator();
		while (it.hasNext()) {
			column = it.next();
			Cell cell = row.createCell(cellnum++);
			this.excelFactory.setCellValue(cell, column.getTitle());
			style = this.excelFactory.getStyle(column.getTitleStyle());
			if (ExcelFactory.isNotNull(style)) {
				cell.setCellStyle(style);
			}
		}
		this.excelSheet.createFreezePane(0, this.rowIndex);// 冻结
	}

	CompositeMap createRecord(CompositeMap record) {
		List<TableColumn> list = getTableColumnList();
		if (list == null)
			return null;

		TableColumn column;
		Row row = ExcelFactory.createRow(this.excelSheet, ++this.rowIndex);
		int cellnum = CellReference.convertColStringToIndex(getCell());
		Iterator<TableColumn> it = list.iterator();
		while (it.hasNext()) {
			column = it.next();
			Cell cell = row.createCell(cellnum++);
			Object value = record.get(column.getField());
			this.excelFactory.setCellValue(cell, value);

			if (column.getCellStyle() != null
					&& !"".equals(column.getCellStyle()))
				cell.setCellStyle(excelFactory.styles.get(column.getCellStyle()));
		}
		CompositeMap map = new CompositeMap();
		map.put("rownum", this.rowIndex);
		map.put("record", record);
		return map;
	}

	GroupConfig[] createGroupConfig(CompositeMap dataModel) {
		List<TableColumn> list = getTableColumnList();
		Iterator<TableColumn> it = list.iterator();
		TableColumn column = null;
		CompositeMap record = null;
		StringBuffer buffer = null;
		int level = 0;
		int index = CellReference.convertColStringToIndex(getCell());
		CompositeMap levelMap = new CompositeMap();
		while (it.hasNext()) {
			column = it.next();
			column.setIndex(index);
			putColumnsMap(column.getField(), column);
			level = column.getGroupLevel();
			if (level != 0) {
				buffer = (StringBuffer) levelMap.get(level);
				if (buffer == null) {
					buffer = new StringBuffer();
				}
				if (buffer.length() != 0)
					buffer.append(",");
				buffer.append(column.getField());
				levelMap.put(level, buffer);
			}
			index++;
		}
		TreeSet<Integer> keySet = new TreeSet<Integer>(levelMap.keySet());
		Iterator<Integer> iterator = keySet.descendingIterator();
		while (iterator.hasNext()) {
			level = iterator.next();
			buffer = (StringBuffer) levelMap.get(level);
			record = new CompositeMap("level" + level);
			record.put(GroupConfig.KEY_GROUP_KEY_FIELDS, buffer.toString());
			record.put(GroupConfig.KEY_RECORD_NAME, "level" + level);
			this.groupMap.addChild(record);
		}

		if (this.groupMap.getChildIterator() != null) {
			GroupConfig[] configs = GroupConfig
					.createGroupConfigs(this.groupMap);
			for (GroupConfig config : configs) {
				config.setExtendParentAttributes(false);
			}
			return configs;
		} else
			return null;
	}

	CompositeMap aaa(List<SubtotalConfig> groupList,
			List<CompositeMap> rowList, boolean is_total) {
		Map<String, TableColumn> columnMap = getColumnsMap();
		Iterator<SubtotalConfig> colIt = groupList.iterator();
		String curGroupFormula = null;
		String curField = null;
		Row row = null;
		TableColumn column;

		SubtotalConfig stConfig;
		LinkedList<Integer[]> list = new LinkedList<Integer[]>();

		int firstRownum = 0;
		int endRownum = 0;
		boolean is_flag = false;
		boolean is_group = false;
		while (colIt.hasNext()) {
			stConfig = colIt.next();
			if (stConfig.getColumnField().equals(curField)
					|| !stConfig.getGroupFormula().equals(curGroupFormula)) {
				curGroupFormula = stConfig.getGroupFormula();
				curField = stConfig.getColumnField();
				row = ExcelFactory.createRow(this.excelSheet, ++this.rowIndex);
				is_flag = true;
				is_group = true;
				if (is_total)
					totalCount++;
			}

			if (stConfig.getGroupFormula() != null) {
				column = this.getColumnsMap().get(curField);
				Cell cell = row.createCell(column.getIndex());
				StringBuffer colBuffer = new StringBuffer("SUBTOTAL(");
				colBuffer.append(stConfig.getGroupFormula());
				colBuffer.append(",");
				String ref1 = null;
				String ref2 = null;
				Iterator<CompositeMap> iterator = rowList.iterator();
				String value = null;
				boolean is_first = true;
				while (iterator.hasNext()) {
					CompositeMap map1 = iterator.next();
					value = ((CompositeMap) map1.get("record"))
							.getString(stConfig.getGroupColumnFild());
					if (is_first) {
						firstRownum = map1.getInt("rownum");
						ref1 = CellReference.convertNumToColString(cell
								.getColumnIndex()) + firstRownum;
						colBuffer.append(ref1);
						colBuffer.append(":");
						is_first = false;
					} else {
						endRownum = map1.getInt("rownum");
						ref2 = CellReference.convertNumToColString(cell
								.getColumnIndex()) + endRownum;
					}
				}
				if (ref2 == null) {
					ref2 = ref1;
					endRownum = firstRownum;
				}
				colBuffer.append(ref2);
				colBuffer.append(")");
				cell.setCellFormula(colBuffer.toString());
				if (is_flag) {
					String field = stConfig.getGroupColumnFild();
					TableColumn groupColumn = columnMap.get(field);
					Cell groupCell = row.createCell(groupColumn.getIndex());
					if (is_total)
						groupCell.setCellValue(stConfig.getTotalDesc());
					else
						groupCell.setCellValue(value + " "
								+ stConfig.getGroupDesc());

					if (ExcelFactory.isNotNull(excelFactory.styles.get(stConfig
							.getGroupStyle()))) {
						groupCell.setCellStyle(excelFactory.styles.get(stConfig
								.getGroupStyle()));
					}

				}

			}
			if (is_group) {
				if (!is_total) {
					// this.groupLimit--;
					this.excelSheet.groupRow(firstRownum - 1,
							(this.rowIndex - 2));
					// Integer[] groupRows={firstRownum - 1,this.rowIndex - 2};
					// limitMap.put(firstRownum - 1, this.groupLimit);
					// list.add(groupRows);
					// System.out.println("this.excelSheet.groupRow("+(firstRownum
					// - 1)+","+ (this.rowIndex - 2)+");");
				}
				is_group = false;
			}
		}
		if (is_total) {
			CompositeMap map = new CompositeMap();
			map.put("first", firstRownum);
			map.put("end", endRownum);
			return map;
		} else {
			// this.groupRowList.add(list);
			return null;
		}
	}

	// CompositeMap aaa(List<TableColumn> groupList, List<CompositeMap> rowList,
	// boolean is_total,String level) {
	// Map<String, TableColumn> columnMap = getColumnsMap();
	// Iterator<TableColumn> colIt = groupList.iterator();
	// String curGroupFormula = null;
	// Row row = null;
	// TableColumn column;
	// int firstRownum = 0;
	// int endRownum = 0;
	// boolean is_flag = false;
	// boolean is_group = false;
	// while (colIt.hasNext()) {
	// column = colIt.next();
	// if (!column.getGroupFormula().equals(curGroupFormula)) {
	// curGroupFormula = column.getGroupFormula();
	// row = ExcelFactory.createRow(this.excelSheet, ++this.rowIndex);
	// is_flag = true;
	// is_group = true;
	// if (is_total)
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
	// String value = null;
	// boolean is_first = true;
	// while (iterator.hasNext()) {
	// CompositeMap map1 = iterator.next();
	// value = ((CompositeMap) map1.get("record"))
	// .getString(column.getGroupField());
	// if (is_first) {
	// firstRownum = map1.getInt("rownum");
	// ref1 = CellReference.convertNumToColString(cell
	// .getColumnIndex()) + firstRownum;
	// colBuffer.append(ref1);
	// colBuffer.append(":");
	// is_first = false;
	// } else {
	// endRownum = map1.getInt("rownum");
	// ref2 = CellReference.convertNumToColString(cell
	// .getColumnIndex()) + endRownum;
	// }
	// }
	// if (ref2 == null) {
	// ref2 = ref1;
	// endRownum = firstRownum;
	// }
	// colBuffer.append(ref2);
	// colBuffer.append(")");
	// cell.setCellFormula(colBuffer.toString());
	// if (is_flag) {
	// String field = column.getGroupField();
	// TableColumn groupColumn = columnMap.get(field);
	// Cell groupCell = row.createCell(groupColumn.getIndex());
	// if (is_total)
	// groupCell.setCellValue(value + " "
	// + column.getTotalDesc());
	// else
	// groupCell.setCellValue(value + " "
	// + column.getGroupDesc());
	// groupCell.setCellStyle(excelFactory.styles.get("group"));
	// }
	//
	// }
	// if (is_group) {
	// if (!is_total) {
	// this.excelSheet.groupRow(firstRownum - 1,
	// (this.rowIndex - 2));
	// }
	// is_group = false;
	// }
	// }
	// if (is_total) {
	// CompositeMap map = new CompositeMap();
	// map.put("first", firstRownum);
	// map.put("end", endRownum);
	// return map;
	// } else {
	// return null;
	// }
	// }

	List<CompositeMap> createTableGroup(CompositeMap dataModel) {
		Iterator it = dataModel.getChildIterator();
		List<CompositeMap> rowList = new LinkedList<CompositeMap>();
		if (it != null) {
			while (it.hasNext()) {
				CompositeMap childMap = (CompositeMap) it.next();
				if (childMap.getChildIterator() != null) {
					rowList.addAll(createTableGroup(childMap));
				} else {
					rowList.add(createRecord(childMap));
				}
			}
			if (rowList.size() != 0) {
				if (dataModel.getName().startsWith("level")) {
					CompositeMap groupMap = getGroupMap();
					String levelName = dataModel.getName();
					List<SubtotalConfig> list = (List<SubtotalConfig>) groupMap
							.getChild(levelName).get("list");
					if (list != null) {
						aaa(list, rowList, false);
					}
				} else {
					CompositeMap m = null;
					Iterator iterator = groupMap.getChildIterator();

					while (iterator.hasNext()) {
						CompositeMap record = (CompositeMap) iterator.next();
						String levelName = record.getName();
						List<SubtotalConfig> list1 = (List<SubtotalConfig>) record
								.get("list");
						m = aaa(list1, rowList, true);
					}
					int firstRownum = m.getInt("first");
					int endRownum = m.getInt("end");
					// Iterator<LinkedList<Integer[]>>
					// groupRowIterator=groupRowList.descendingIterator();
					// while(groupRowIterator.hasNext()){
					// boolean isFirst=true;
					// int firstNum=0;
					// LinkedList<Integer[]> groupRow=groupRowIterator.next();
					// if(isFirst){
					// Iterator<Integer[]> aaa=groupRow.descendingIterator();
					// while(aaa.hasNext()){
					// Integer[] groupRows=aaa.next();
					// firstNum=groupRows[0];
					// this.excelSheet.groupRow(groupRows[0], groupRows[1]);
					// System.out.println("this.excelSheet.groupRow("+groupRows[0]+","+groupRows[1]+");");
					// this.groupLimit--;
					// }
					// }else{
					// int index=0;
					// Iterator<Integer[]> aaa=groupRow.descendingIterator();
					// while(aaa.hasNext()){
					// Integer[] groupRows=aaa.next();
					// if(firstNum<=groupRows[0]){
					// if(index<this.groupLimit){
					// this.excelSheet.groupRow(groupRows[0], groupRows[1]);
					// System.out.println("this.excelSheet.groupRow("+groupRows[0]+","+groupRows[1]+");");
					// index++;
					// }
					// }else{
					// int limit=limitMap.get(groupRows[0]);
					// if(limit>0){
					// this.excelSheet.groupRow(groupRows[0], groupRows[1]);
					// System.out.println("this.excelSheet.groupRow("+groupRows[0]+","+groupRows[1]+");");
					// limit--;
					// limitMap.put(groupRows[0], limit);
					// }
					//
					// }
					// }
					// }
					// isFirst=false;
					// }
					this.excelSheet.groupRow(firstRownum - 1, endRownum
							+ totalCount);
					// System.out.println("this.excelSheet.groupRow("+(firstRownum
					// - 1)+","+ (endRownum + totalCount)+");");
					// this.excelSheet.groupRow(48,65);
					// this.excelSheet.groupRow(48,64);
					// this.excelSheet.groupRow(48,63);
					// this.excelSheet.groupRow(48,62);
					// this.excelSheet.groupRow(58,61);
					// this.excelSheet.groupRow(58,60);
					// this.excelSheet.groupRow(53,56);
					// this.excelSheet.groupRow(53,55);
					// this.excelSheet.groupRow(48,51);
					// this.excelSheet.groupRow(48,50);
					// this.excelSheet.groupRow(37,46);
					// this.excelSheet.groupRow(37,45);
					// this.excelSheet.groupRow(37,44);
					// this.excelSheet.groupRow(37,43);
					// this.excelSheet.groupRow(37,42);
					// this.excelSheet.groupRow(37,41);
					// this.excelSheet.groupRow(22,35);
					// this.excelSheet.groupRow(22,34);
					// this.excelSheet.groupRow(22,33);
					// this.excelSheet.groupRow(22,32);
					// this.excelSheet.groupRow(28,31);
					// this.excelSheet.groupRow(28,30);
					//
					// this.excelSheet.groupRow(22,26);
					// this.excelSheet.groupRow(22,25);
					// this.excelSheet.groupRow(2,20);
					// this.excelSheet.groupRow(2,19);
					// this.excelSheet.groupRow(2,18);
					// this.excelSheet.groupRow(2,17);
					// this.excelSheet.groupRow(13,16);
					// this.excelSheet.groupRow(13,15);
					//
					// this.excelSheet.groupRow(7,11);
					// this.excelSheet.groupRow(7,10);
					//
					// this.excelSheet.groupRow(2,5);
					// this.excelSheet.groupRow(2,4);
					// this.excelSheet.groupRow(2,66);
				}
			}
		}
		return rowList;
	}

}

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
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.composite.transform.GroupConfig;
import uncertain.composite.transform.GroupTransformer;

public class DynamicContent {
	String cell = "A";
	int row = 1;
	String dataModel;
	TableColumn[] tableColumns;
	boolean freezeEnable = false;
	boolean displayTitle = true;

	int totalCount = -1;
	private int rowIndex = 0;
	private Sheet excelSheet;
	ExcelFactory excelFactory;
	Map<String, TableColumn> columnsMap = new HashMap<String, TableColumn>();
	CompositeMap groupMap = new CompositeMap();

	final String KEY_LEVEL_NAME = "level";

	public void createGroupMap() {
		if (this.getColumns() == null)
			return;
		int level = 0;
		CompositeMap record;
		String[] groupFields;
		List<SubtotalConfig> list = new LinkedList<SubtotalConfig>();
		for (TableColumn column : this.getColumns()) {
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
						record = groupMap.getChild(this.KEY_LEVEL_NAME + level);
						SubtotalConfig configClone = new SubtotalConfig();
						configClone.setGroupDesc(config.getGroupDesc());
						configClone.setGroupField(config.getGroupField());
						configClone.setGroupFormula(config.getGroupFormula());
						configClone.setTotalDesc(config.getTotalDesc());
						configClone.setGroupStyle(config.getGroupStyle());
						configClone.setColumnField(column.getField());
						configClone.setGroupColumnFild(groupColumn.getField());
						configClone.setFormulaStyle(config.getFormulaStyle());
						configClone.setLineStyle(config.getLineStyle());
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

	public int createContent(ExcelFactory excelFactory, Sheet excelSheet) {
		this.excelSheet = excelSheet;
		this.excelFactory = excelFactory;
		CompositeMap data = (CompositeMap) excelFactory.getContext().getObject(
				getDataModel());
		if (data == null) {
			createTableTitle(excelFactory.getContext());
			return this.rowIndex;
		}
		GroupConfig[] groupConfig = createGroupConfig(data);
		createTableTitle(excelFactory.getContext());

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
		if (this.getColumns() == null)
			return;
		CellStyle style;
		this.rowIndex = getRow();
		if (!this.getDisplayTitle()){
			this.rowIndex--;
			return;
		}
		Row row = ExcelFactory.createRow(this.excelSheet, this.rowIndex);
		int cellnum = CellReference.convertColStringToIndex(getCell());
		for (TableColumn column : this.getColumns()) {
			Cell cell =ExcelFactory.createCell(row, cellnum++);
			this.excelFactory.setCellValue(cell,
					TextParser.parse(column.getTitle(), context));
			style = this.excelFactory.getStyle(column.getTitleStyle());
			if (ExcelFactory.isNotNull(style)) {
				cell.setCellStyle(style);
			}
		}
		if (this.getFreezeEnable())
			this.excelSheet.createFreezePane(0, this.rowIndex);// 冻结
	}

	CompositeMap createRecord(CompositeMap record) {
		if (this.getColumns() == null)
			return null;
		Row row = ExcelFactory.createRow(this.excelSheet, ++this.rowIndex);
		int cellnum = CellReference.convertColStringToIndex(getCell());
		for (TableColumn column : this.getColumns()) {
			Cell cell = ExcelFactory.createCell(row, cellnum++);
			Object value = record.get(column.getField());
			this.excelFactory.setCellValue(cell, value);
			if (column.getCellStyle() != null
					&& !"".equals(column.getCellStyle()))
				cell.setCellStyle(excelFactory.getStyle(column.getCellStyle()));
		}
		CompositeMap map = new CompositeMap();
		map.put("rownum", this.rowIndex);
		map.put("record", record);
		return map;
	}

	GroupConfig[] createGroupConfig(CompositeMap dataModel) {
		if (this.getColumns() == null)
			return null;

		CompositeMap record = null;
		StringBuffer buffer = null;
		int level = 0;
		int index = CellReference.convertColStringToIndex(getCell());
		if (this.getColumns()[0].isSubtotalSelf() && index == 0) {
			index++;
			this.setCell(CellReference.convertNumToColString(index));
		}
		CompositeMap levelMap = new CompositeMap();
		for (TableColumn column : this.getColumns()) {
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
			record = new CompositeMap(this.KEY_LEVEL_NAME + level);
			record.put(GroupConfig.KEY_GROUP_KEY_FIELDS, buffer.toString());
			record.put(GroupConfig.KEY_RECORD_NAME, this.KEY_LEVEL_NAME + level);
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

	CompositeMap createGroupData(List<SubtotalConfig> groupList,
			List<CompositeMap> rowList, boolean is_total) {
		Map<String, TableColumn> columnMap = getColumnsMap();
		Iterator<SubtotalConfig> colIt = groupList.iterator();
		String curGroupFormula = null;
		String curField = null;
		Row row = null;
		TableColumn column;

		SubtotalConfig stConfig;
		int firstRownum = 0;
		int endRownum = 0;
		boolean is_flag = false;
		boolean is_group = false;
		while (colIt.hasNext()) {
			stConfig = colIt.next();
			column = this.getColumnsMap().get(stConfig.getColumnField());
			if (stConfig.getColumnField().equals(curField)
					|| !stConfig.getGroupFormula().equals(curGroupFormula)
					|| stConfig.getColumnField().equals(column.getField())) {
				curGroupFormula = stConfig.getGroupFormula();
				curField = stConfig.getColumnField();
				row = ExcelFactory.createRow(this.excelSheet, ++this.rowIndex);
				is_flag = true;
				is_group = true;
				if (is_total)
					totalCount++;
			}

			if (stConfig.getGroupFormula() != null) {
				Cell cell = ExcelFactory.createCell(row,column.getIndex());
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
					int groupColumnFildIndex = groupColumn.getIndex();
					if (field != null && field.equals(column.getField()))
						groupColumnFildIndex--;
					Cell groupCell = ExcelFactory.createCell(row,groupColumnFildIndex);
					if (is_total)
						groupCell.setCellValue(stConfig.getTotalDesc());
					else
						groupCell.setCellValue(value + " "
								+ stConfig.getGroupDesc());

					if (ExcelFactory.isNotNull(excelFactory.getStyle(stConfig
							.getGroupStyle()))) {
						groupCell.setCellStyle(excelFactory.getStyle(stConfig
								.getGroupStyle()));
					}
					if (ExcelFactory.isNotNull(excelFactory.getStyle(stConfig
							.getFormulaStyle()))) {
						cell.setCellStyle(excelFactory.getStyle(stConfig
								.getFormulaStyle()));
					}
					int cellnum = CellReference
							.convertColStringToIndex(getCell());
					int tIndex = tableColumns.length + cellnum;
					for (int i = 0 + cellnum; i < tIndex; i++) {
						Cell blankCell = row.getCell(i);
						if (blankCell == null) {
							blankCell = ExcelFactory.createCell(row,i);
							if (ExcelFactory.isNotNull(excelFactory.getStyle(stConfig.getLineStyle()))) {
								blankCell.setCellStyle(excelFactory.getStyle(stConfig.getLineStyle()));
							}
						}
					}

				}

			}
			if (is_group) {
				if (!is_total) {
					this.excelSheet.groupRow(firstRownum - 1,
							(this.rowIndex - 2));
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
			return null;
		}
	}

	void setMergedRegion(List<CompositeMap> rowList, CompositeMap dataModel) {
		if (dataModel == null)
			return;
		Iterator<CompositeMap> iterator = rowList.iterator();
		CompositeMap record;
		int firstLine = 0;
		int endLine = 0;
		int rownum;
		boolean is_first = true;
		while (iterator.hasNext()) {
			record = iterator.next();
			rownum = record.getInt("rownum");
			endLine = rownum;
			if (is_first) {
				firstLine = rownum;
				is_first = false;
			}
		}
		Set<String> keySet = dataModel.keySet();
		Iterator<String> it = keySet.iterator();
		while (it.hasNext()) {
			String levelName = it.next();
			TableColumn tableColumn = this.getColumnsMap().get(levelName);
			if (!tableColumn.getMerged())
				break;
			String colString = CellReference.convertNumToColString(tableColumn
					.getIndex());
			StringBuffer buffer = new StringBuffer();
			buffer.append("$");
			buffer.append(colString);
			buffer.append("$");
			buffer.append(firstLine);
			buffer.append(":");
			buffer.append("$");
			buffer.append(colString);
			buffer.append("$");
			buffer.append(endLine);
			this.excelSheet.addMergedRegion(CellRangeAddress.valueOf(buffer
					.toString()));
		}
	}

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
				if (dataModel.getName().startsWith(this.KEY_LEVEL_NAME)) {
					CompositeMap groupMap = getGroupMap();
					String levelName = dataModel.getName();
					setMergedRegion(rowList, dataModel);
					List<SubtotalConfig> list = (List<SubtotalConfig>) groupMap
							.getChild(levelName).get("list");
					if (list != null) {
						createGroupData(list, rowList, false);
					}
				} else {
					CompositeMap m = null;
					List a = groupMap.getChildsNotNull();
					for (int i = 0, l = a.size(); i < l; l--) {
						CompositeMap record = (CompositeMap) a.get(l - 1);
						String levelName = record.getName();
						List<SubtotalConfig> list1 = (List<SubtotalConfig>) record
								.get("list");
						if (list1 != null)
							m = createGroupData(list1, rowList, true);
					}
					if (m != null) {
						int firstRownum = m.getInt("first");
						int endRownum = m.getInt("end");
						this.excelSheet.groupRow(firstRownum - 1, endRownum
								+ totalCount);
					}
				}
			}
		}
		return rowList;
	}

	public Map<String, TableColumn> getColumnsMap() {
		return columnsMap;
	}

	public void putColumnsMap(String name, TableColumn tableColumn) {
		columnsMap.put(name, tableColumn);
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

	public void setColumns(TableColumn[] tableColumns) {
		this.tableColumns = tableColumns;
	}

	public TableColumn[] getColumns() {
		return this.tableColumns;
	}

	public CompositeMap getGroupMap() {
		return groupMap;
	}

	public boolean getFreezeEnable() {
		return freezeEnable;
	}

	public void setFreezeEnable(boolean freezeEnable) {
		this.freezeEnable = freezeEnable;
	}

	public boolean getDisplayTitle() {
		return displayTitle;
	}

	public void setDisplayTitle(boolean displayTitle) {
		this.displayTitle = displayTitle;
	}

}
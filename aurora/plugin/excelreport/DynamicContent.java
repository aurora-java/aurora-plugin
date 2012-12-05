package aurora.plugin.excelreport;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uncertain.composite.CompositeMap;
import uncertain.core.UncertainEngine;
import uncertain.ocm.OCManager;

public class DynamicContent {
	String cell="A";
	String dataModel;
	int row=1;
	CompositeMap columns;
	List<TableColumn> columnList;
	OCManager mOCManager;
	Map<String, TableColumn> columnsMap = new HashMap<String, TableColumn>();
	Map<String, List<TableColumn>> groupMap = new HashMap<String, List<TableColumn>>();

	public Map<String, List<TableColumn>> getGroupMap() {
		return groupMap;
	}

	public void createGroupMap() {
		Iterator<TableColumn> iterator = columnList.iterator();
		TableColumn column;
		List<TableColumn> list = new LinkedList<TableColumn>();
		int level = 0;
		while (iterator.hasNext()) {
			column = iterator.next();
			if (level != column.getGroupLevel()) {
				if (list.size() != 0) {
					groupMap.put("level" + (level + 1), list);
					list = new LinkedList<TableColumn>();
				}
				level = column.getGroupLevel();
			}
			if (column.getGroupFormula() != null)
				list.add(column);
		}
		if (list.size() != 0)
			groupMap.put("level" + (level + 1), list);
	}

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

}

package aurora.plugin.excelreport;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import uncertain.composite.CompositeMap;
import uncertain.core.UncertainEngine;
import uncertain.ocm.OCManager;

public class DynamicContent {
	String cell;
	String dataModel;
	int row;
	CompositeMap columns;
	List<TableColumn> columnList;
	OCManager mOCManager;

	String groupDesc;	

	public String getGroupDesc() {
		return groupDesc;
	}

	public void setGroupDesc(String groupDesc) {
		this.groupDesc = groupDesc;
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

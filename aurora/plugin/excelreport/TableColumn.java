package aurora.plugin.excelreport;

import uncertain.composite.CompositeMap;

public class TableColumn {
	String title;
	String field;
	String type;
	String titleStyle;
	String titleStyleName;
	String cellStyle;
	String cellStyleName;
	int groupLevel = 0;
	String order;
	CompositeMap columns;
	String groupFormula;
	String groupDesc;	

	public String getGroupDesc() {
		return groupDesc;
	}

	public void setGroupDesc(String groupDesc) {
		this.groupDesc = groupDesc;
	}

	public String getGroupFormula() {
		return groupFormula;
	}

	public void setGroupFormula(String groupFormula) {
		this.groupFormula = groupFormula;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitleStyle() {
		return titleStyle;
	}

	public void setTitleStyle(String titleStyle) {
		this.titleStyle = titleStyle;
	}

	public String getTitleStyleName() {
		return titleStyleName;
	}

	public void setTitleStyleName(String titleStyleName) {
		this.titleStyleName = titleStyleName;
	}

	public String getCellStyle() {
		return cellStyle;
	}

	public void setCellStyle(String cellStyle) {
		this.cellStyle = cellStyle;
	}

	public String getCellStyleName() {
		return cellStyleName;
	}

	public void setCellStyleName(String cellStyleName) {
		this.cellStyleName = cellStyleName;
	}

	public int getGroupLevel() {
		return groupLevel;
	}

	public void setGroupLevel(int groupLevel) {
		this.groupLevel = groupLevel;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public CompositeMap getColumns() {
		return columns;
	}

	public void setColumns(CompositeMap columns) {
		this.columns = columns;
	}

}

package aurora.plugin.excelreport;

import uncertain.composite.CompositeMap;

public class TableColumn {
	String title;
	String field;
	String type;
	String titleStyle;
	String cellStyle;
	boolean merged;
	int groupLevel = 0;
	String order;
	CompositeMap columns;
	SubtotalConfig[] groups;
	int index;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public SubtotalConfig[] getGroups() {
		return groups;
	}

	public void setGroups(SubtotalConfig[] groups) {
		this.groups = groups;
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

	public String getCellStyle() {
		return cellStyle;
	}

	public void setCellStyle(String cellStyle) {
		this.cellStyle = cellStyle;
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

	public boolean getMerged() {
		return merged;
	}

	public void setMerged(boolean merged) {
		this.merged = merged;
	}

	public boolean isSubtotalSelf() {
		SubtotalConfig[] configs = this.getGroups();
		if (configs != null) {
			for (SubtotalConfig config : configs) {
				String groupField = config.getGroupField();
				if (groupField != null) {
					String[] fields = groupField.split(",");
					for (String field : fields) {
						if (field.equals(this.getField()))
							return true;
					}
				}
			}
		}
		return false;
	}

}

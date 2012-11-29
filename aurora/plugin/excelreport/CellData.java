package aurora.plugin.excelreport;

public class CellData {
	int row;
	String cell;
	boolean offset;
	String value;
	String style;
	String styleName;
	String type;
	String range;
	String dataType=KEY_DATA_TYPE_STRING;	
	
	public static final String KEY_CONTENT="content";
	public static final String KEY_FORMULA="formula";
	public static final String KEY_DATA_TYPE_NUMBER="Number";
	public static final String KEY_DATA_TYPE_STRING="String";
	public static final String KEY_DATA_TYPE_DATE="Date";

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public String getCell() {
		return cell;
	}

	public void setCell(String cell) {
		this.cell = cell;
	}

	public boolean getOffset() {
		return offset;
	}

	public void setOffset(boolean offset) {
		this.offset = offset;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getStyleName() {
		return styleName;
	}

	public void setStyleName(String styleName) {
		this.styleName = styleName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}

}

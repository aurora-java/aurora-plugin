package aurora.plugin.export.word;


public class Table{
	
	private static final String DEFAULT_WIDTH_TYPE = "auto";
	private String id;
	private Column[] columns;
	private int width;
	private String widthType = DEFAULT_WIDTH_TYPE;
	private String model;
	

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getId(){
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Column[] getColumns() {
		return columns;
	}

	public void setColumns(Column[] columns) {
		this.columns = columns;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public String getWidthType() {
		return widthType;
	}

	public void setWidthType(String widthType) {
		this.widthType = widthType;
	}

}

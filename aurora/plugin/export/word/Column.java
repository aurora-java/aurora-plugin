package aurora.plugin.export.word;

public class Column {
	
	private static final String DEFAULT_WIDTH_TYPE = "auto";
	private static final String DEFAULT_ALIGN = "center";
	private String name;
	private int width;
	private String title;
	private String widthType = DEFAULT_WIDTH_TYPE;
	private String align = DEFAULT_ALIGN;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getAlign() {
		return align;
	}

	public void setAlign(String align) {
		this.align = align;
	}

}

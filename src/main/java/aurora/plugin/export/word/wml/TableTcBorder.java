package aurora.plugin.export.word.wml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "border")
@XmlAccessorType(XmlAccessType.FIELD)
public class TableTcBorder {
	
	@XmlAttribute
	private String type = "bottom";
	
	@XmlAttribute
	private String style = "single";
	
	@XmlAttribute
	private String size = "4";
	
	@XmlAttribute
	private String color = "000000";

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}


	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}
	
}

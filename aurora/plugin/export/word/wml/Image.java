package aurora.plugin.export.word.wml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "img")
@XmlAccessorType(XmlAccessType.FIELD)
public class Image {
	
	@XmlTransient
	public static final String PATH_TYPE_RELATIVE = "relative";
	
	@XmlTransient
	public static final String PATH_TYPE_ABSOLUTE = "absolute";
	
	@XmlAttribute
	private String src;
	
	@XmlAttribute
	private String type = PATH_TYPE_RELATIVE;

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}

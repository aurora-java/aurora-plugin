package aurora.plugin.export.word.wml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "qr-code")
@XmlAccessorType(XmlAccessType.FIELD)
public class QRCode {
	
	@XmlAttribute
	private Integer width = 100;
	
	@XmlAttribute
	private Integer height = 100;
	
	@XmlAttribute
	private String errorCorrection = "M";
	
	@XmlValue
	private String text = "";

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public String getErrorCorrection() {
		return errorCorrection;
	}

	public void setErrorCorrection(String errorCorrection) {
		this.errorCorrection = errorCorrection;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}

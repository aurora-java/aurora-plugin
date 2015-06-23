package aurora.plugin.export.word.wml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "tbl")
@XmlAccessorType(XmlAccessType.FIELD)
public class Table {
	
	@XmlAttribute
	private Boolean border = true;
	
	@XmlAttribute
	private String align = "left";
	
	@XmlAttribute
	private Float width;
	
	@XmlAttribute
	private Float indLeft;
	
	@XmlElement(name = "tr")
	private List<TableTr> trs = new ArrayList<TableTr>();

	public List<TableTr> getTrs() {
		return trs;
	}

	public void setTrs(List<TableTr> trs) {
		this.trs = trs;
	}

	public Boolean getBorder() {
		return border;
	}

	public void setBorder(Boolean border) {
		this.border = border;
	}

	public String getAlign() {
		return align;
	}

	public void setAlign(String align) {
		this.align = align;
	}

	public Float getIndLeft() {
		return indLeft;
	}

	public void setIndLeft(Float indLeft) {
		this.indLeft = indLeft;
	}

	public Float getWidth() {
		return width;
	}

	public void setWidth(Float width) {
		this.width = width;
	}
}

package aurora.plugin.export.word.wml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "tc")
@XmlAccessorType(XmlAccessType.FIELD)
public class TableTc {
	
	@XmlAttribute
	private Integer span;
	
	@XmlAttribute
	private Double width;
	
	@XmlAttribute
	private String fill;
	
//	@XmlAttribute
//	private String align;
	
	@XmlAttribute
	private String vAlign = "center";
	
	@XmlElement(name = "border")
	private List<TableTcBorder> borders = new ArrayList<TableTcBorder>();

	@XmlElement(name = "p")
	private List<Paragraph> paras = new ArrayList<Paragraph>();

	public List<Paragraph> getParas() {
		return paras;
	}

	public void addPara(Paragraph para) {
		this.paras.add(para);
	}

	public Double getWidth() {
		return width;
	}

	public void setWidth(Double width) {
		this.width = width;
	}

	public String getFill() {
		return fill;
	}

	public void setFill(String fill) {
		this.fill = fill;
	}

//	public String getAlign() {
//		return align;
//	}
//
//	public void setAlign(String align) {
//		this.align = align;
//	}

	public String getVAlign() {
		return vAlign;
	}

	public void setVAlign(String align) {
		vAlign = align;
	}

	public Integer getSpan() {
		return span;
	}

	public void setSpan(Integer span) {
		this.span = span;
	}

	public List<TableTcBorder> getBorders() {
		return borders;
	}

	public void setBorders(List<TableTcBorder> borders) {
		this.borders = borders;
	}
}

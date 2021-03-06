package aurora.plugin.export.word.wml;


import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import aurora.plugin.export.word.WordUtils;

@XmlRootElement(name = "t")
@XmlAccessorType(XmlAccessType.FIELD)
public class Text {
	

	private static final ArrayList<String> wordFonts = new ArrayList<String>(Arrays.asList("宋体","黑体","微软雅黑"));
	private static final ArrayList<String> pdfFonts = new ArrayList<String>(Arrays.asList("SimSun","SimHei","Microsoft YaHei"));
	
	@XmlAttribute
	private Boolean fldCharType = false;
	
	@XmlAttribute
	private Boolean bold = false;
	
	@XmlAttribute
	private String fontFamily = "SimSun";
	
	@XmlAttribute
	private String fontSize = "24";
	
	@XmlAttribute
	private String fontColor = "000000";
	
	@XmlAttribute
	private String underline;
	
	@XmlAttribute
	private Boolean italic = false;
	
	@XmlAttribute
	private String space;
	
	@XmlValue
	private String text = "";
	
	
	public Text(){}
	
	public Text(String text){
		this.text = text;		
	}

	public Boolean isBold() {
		return bold;
	}

	public void setBold(Boolean bold) {
		this.bold = bold;
	}


	public String getFontFamily() {
		if(WordUtils.TYPE_WORD.equals(WordUtils.getObject(WordUtils.EXPORT_TYPE))){
			int index = pdfFonts.indexOf(fontFamily);
			if(index!=-1) fontFamily = wordFonts.get(index);
		}else if(WordUtils.TYPE_PDF.equals(WordUtils.getObject(WordUtils.EXPORT_TYPE))){
			int index = wordFonts.indexOf(fontFamily);
			if(index!=-1) fontFamily = pdfFonts.get(index);
		}
		return fontFamily;
	}

	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}

	public String getFontSize() {
		return fontSize;
	}

	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
	}

	public String getUnderline() {
		return underline;
	}

	public void setUnderline(String underline) {
		this.underline = underline;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	} 

	public String getFontColor() {
		return fontColor;
	}

	public void setFontColor(String fontColor) {
		this.fontColor = fontColor;
	}

	public String getSpace() {
		return space;
	}

	public void setSpace(String space) {
		this.space = space;
	}

	public Boolean isItalic() {
		return italic;
	}

	public void setItalic(Boolean italic) {
		this.italic = italic;
	}

	public Boolean getFldCharType() {
		return fldCharType;
	}

	public void setFldCharType(Boolean fldCharType) {
		this.fldCharType = fldCharType;
	}

}

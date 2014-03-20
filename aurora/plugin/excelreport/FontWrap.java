package aurora.plugin.excelreport;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

public class FontWrap {
	String height;
	boolean bold;
	String fontName;
	boolean italic;
	String color;
	String underline;
	boolean strikeout;

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public boolean getBold() {
		return bold;
	}

	public void setBold(boolean bold) {
		this.bold = bold;
	}

	public String getFontName() {
		return fontName;
	}

	public void setFontName(String fontName) {
		this.fontName = fontName;
	}

	public boolean getItalic() {
		return italic;
	}

	public void setItalic(boolean italic) {
		this.italic = italic;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getUnderline() {
		return underline;
	}

	public void setUnderline(String underline) {
		this.underline = underline;
	}

	public boolean getStrikeout() {
		return strikeout;
	}

	public void setStrikeout(boolean strikeout) {
		this.strikeout = strikeout;
	}

	public short colorConvert(String color) {
		return IndexedColors.valueOf(color).getIndex();
	}

	public Font createFont(Workbook wb) {
		Font font = wb.createFont();
		if (this.getColor() != null) {
			font.setColor(colorConvert(this.getColor()));
		}
		if (this.getFontName() != null)
			font.setFontName(this.getFontName());
		if (this.getHeight() != null)
			font.setFontHeightInPoints(Double.valueOf(this.getHeight())
					.shortValue());
		if (this.getUnderline() != null){
			String type=this.getUnderline();
			if("SINGLE_ACCOUNTING".equalsIgnoreCase(type)){
				font.setUnderline(Font.U_SINGLE_ACCOUNTING);
			}else if("DOUBLE_ACCOUNTING".equalsIgnoreCase(type)){
				font.setUnderline(Font.U_DOUBLE_ACCOUNTING);
			}else if("DOUBLE".equalsIgnoreCase(type)){
				font.setUnderline(Font.U_DOUBLE);
			}else{
				font.setUnderline(Font.U_SINGLE);
			}			
		}			
		if (this.getBold())
			font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		if (this.getItalic())
			font.setItalic(this.getItalic());
		if (this.getStrikeout())
			font.setStrikeout(this.getStrikeout());
		return font;
	}
}

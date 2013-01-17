package aurora.plugin.excelreport;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

public class CellStyleWrap {
	String align;//水平对齐方式
	String vertical;//垂直对齐方式
	String borderRight;//右边框样式（线条）
	String borderLeft;//左边框样式（线条）
	String borderBottom;//底部边框样式（线条）
	String borderTop;//顶部边框样式（线条）
	String bottomBorderColor;//底部边框颜色
	String leftBorderColor;//左边框颜色
	String rightBorderColor;//右边框颜色
	String topBorderColor;//顶部边框颜色
	String backgroundColor;//背景色
	String foregroundColor;//前景色
	String pattern;// 设置填充模式
	boolean hidden;//隐藏
	FontWrap fontWrap;//字体
	short indent;//水平左对齐缩进量
	boolean locked;//锁定
	short rotation;//旋转
	boolean wrapped;//自动换行
	String dataFormat;//数据格式

	String name;
	
	final String KEY_ALIGN_LEFT="ALIGN_LEFT";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(String dataFormat) {
		this.dataFormat = dataFormat;
	}

	public String getAlign() {
		return align;
	}

	public void setAlign(String align) {
		this.align = align;
	}

	public String getVertical() {
		return vertical;
	}

	public void setVertical(String vertical) {
		this.vertical = vertical;
	}

	public String getBorderRight() {
		return borderRight;
	}

	public void setBorderRight(String borderRight) {
		this.borderRight = borderRight;
	}

	public String getBorderLeft() {
		return borderLeft;
	}

	public void setBorderLeft(String borderLeft) {
		this.borderLeft = borderLeft;
	}

	public String getBorderBottom() {
		return borderBottom;
	}

	public void setBorderBottom(String borderBottom) {
		this.borderBottom = borderBottom;
	}

	public String getBorderTop() {
		return borderTop;
	}

	public void setBorderTop(String borderTop) {
		this.borderTop = borderTop;
	}

	public String getBottomBorderColor() {
		return bottomBorderColor;
	}

	public void setBottomBorderColor(String bottomBorderColor) {
		this.bottomBorderColor = bottomBorderColor;
	}

	public String getLeftBorderColor() {
		return leftBorderColor;
	}

	public void setLeftBorderColor(String leftBorderColor) {
		this.leftBorderColor = leftBorderColor;
	}

	public String getRightBorderColor() {
		return rightBorderColor;
	}

	public void setRightBorderColor(String rightBorderColor) {
		this.rightBorderColor = rightBorderColor;
	}

	public String getTopBorderColor() {
		return topBorderColor;
	}

	public void setTopBorderColor(String topBorderColor) {
		this.topBorderColor = topBorderColor;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getForegroundColor() {
		return foregroundColor;
	}

	public void setForegroundColor(String foregroundColor) {
		this.foregroundColor = foregroundColor;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public boolean getHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public FontWrap getFont() {
		return fontWrap;
	}

	public void addFont(FontWrap font) {
		this.fontWrap = font;
	}

	public short getIndent() {
		return indent;
	}

	public void setIndent(short indent) {
		this.indent = indent;
	}

	public boolean getLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public short getRotation() {
		return rotation;
	}

	public void setRotation(short rotation) {
		this.rotation = rotation;
	}

	public boolean getWrapped() {
		return wrapped;
	}

	public void setWrapped(boolean wrapped) {
		this.wrapped = wrapped;
	}

	public CellStyle createStyle(Workbook wb) {
		CellStyle style = wb.createCellStyle();
		FontWrap fontObj = this.getFont();
		if (ExcelFactory.isNotNull(fontObj)) {
			Font font = fontObj.createFont(wb);
			style.setFont(font);
		}
		if (ExcelFactory.isNotNull(this.getAlign())) {
			style.setAlignment(IndexedCellStyle.valueOf(this.getAlign())
					.getIndex());
			if(this.KEY_ALIGN_LEFT.equals(this.getAlign())){
				style.setIndention(this.getIndent());
			}
		}
		if (ExcelFactory.isNotNull(this.getVertical())) {
			style.setVerticalAlignment(IndexedCellStyle.valueOf(
					this.getVertical()).getIndex());
		}
		if (ExcelFactory.isNotNull(this.getBorderLeft())) {
			style.setBorderLeft(IndexedCellStyle.valueOf(this.getBorderLeft())
					.getIndex());
		}
		if (ExcelFactory.isNotNull(this.getBorderRight())) {
			style.setBorderRight(IndexedCellStyle
					.valueOf(this.getBorderRight()).getIndex());
		}
		if (ExcelFactory.isNotNull(this.getBorderBottom())) {
			style.setBorderBottom(IndexedCellStyle.valueOf(
					this.getBorderBottom()).getIndex());
		}
		if (ExcelFactory.isNotNull(this.getBorderTop())) {
			style.setBorderTop(IndexedCellStyle.valueOf(this.getBorderTop())
					.getIndex());
		}
		if (ExcelFactory.isNotNull(this.getLeftBorderColor())) {
			style.setLeftBorderColor(IndexedColors.valueOf(
					this.getLeftBorderColor()).getIndex());
		}
		if (ExcelFactory.isNotNull(this.getRightBorderColor())) {
			style.setRightBorderColor(IndexedColors.valueOf(
					this.getRightBorderColor()).getIndex());
		}
		if (ExcelFactory.isNotNull(this.getBottomBorderColor())) {
			style.setBottomBorderColor(IndexedColors.valueOf(
					this.getBottomBorderColor()).getIndex());
		}
		if (ExcelFactory.isNotNull(this.getTopBorderColor())) {
			style.setTopBorderColor(IndexedColors.valueOf(
					this.getTopBorderColor()).getIndex());
		}
		if (ExcelFactory.isNotNull(this.getBackgroundColor())) {
			style.setFillBackgroundColor(IndexedColors.valueOf(
					this.getBackgroundColor()).getIndex());
		}
		if (ExcelFactory.isNotNull(this.getForegroundColor())) {
			style.setFillForegroundColor(IndexedColors.valueOf(
					this.getForegroundColor()).getIndex());
		}
		if (ExcelFactory.isNotNull(this.getPattern())) {
			style.setFillPattern(IndexedCellStyle.valueOf(this.getPattern())
					.getIndex());
		}
		if (ExcelFactory.isNotNull(this.getDataFormat())) {
			style.setDataFormat(wb.createDataFormat().getFormat(
					this.getDataFormat()));
		}
		style.setHidden(this.getHidden());
		style.setLocked(this.getLocked());
		style.setWrapText(this.getWrapped());		
		style.setRotation(this.getRotation());
		return style;
	}
}

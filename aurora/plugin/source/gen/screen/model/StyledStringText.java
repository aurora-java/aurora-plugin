package aurora.plugin.source.gen.screen.model;

public class StyledStringText extends AuroraComponent {
	public static final String TEXT_ALIGNMENT = "text_alignment";

	private static final String STYLED_STRING_TEXT = "styled_string_text";

	private static final String IS_BOLD = "is_bold";

	private static final String IS_ITALIC = "is_italic";

	private static final String IS_UNDERLINE = "is_underline";

	private static final String IS_STRIKEOUT = "is_strikeout";

	private static final String FOREGROUND = "foreground";

	private static final String BACKGROUND = "background";

	private static final String STRIKEOUT_COLOR = "strikeout_color";

	private static final String UNDERLINE_COLOR = "underline_color";

	private static final String UNDERLINE_STYLE = "underline_style";

	// public static StyledStringText t = new StyledStringText();

	private String text;
	// private boolean bold = false, italic = false, underline = false,
	// strikeout = false;
	private String fontName;
	private int fontSize;
	final public static String UNAVAILABLE_RGB = "-1,-1,-1";

	// private String textForeground = UNAVAILABLE_RGB,
	// textBackground = UNAVAILABLE_RGB, strikeoutColor = UNAVAILABLE_RGB,
	// underlineColor = UNAVAILABLE_RGB;
	// private int underlineStyle;

	public StyledStringText() {
		this.setComponentType(STYLED_STRING_TEXT);
		this.setUnderlineColor(UNAVAILABLE_RGB);
		this.setStrikeoutColor(UNAVAILABLE_RGB);
		this.setTextBackground(UNAVAILABLE_RGB);
		this.setTextForeground(UNAVAILABLE_RGB);
	}

	public boolean isUseless() {
		if (this.isBold()) {
			return false;
		}
		if (this.isItalic()) {
			return false;
		}
		if (this.isStrikeout()) {
			return false;
		}
		if (this.isUnderline()) {
			return false;
		}

		// SWT.UNDERLINE_SINGLE;
		// SWT.UNDERLINE_DOUBLE
		// SWT.UNDERLINE_ERROR
		// SWT.UNDERLINE_SQUIGGLE
		// SWT.UNDERLINE_LINK
		// public static final int UNDERLINE_SINGLE = 0;
		// public static final int UNDERLINE_DOUBLE = 1;
		// public static final int UNDERLINE_ERROR = 2;
		// public static final int UNDERLINE_SQUIGGLE = 3;
		// public static final int UNDERLINE_LINK = 4;
		if (this.getUnderlineStyle() >= 0 && this.getUnderlineStyle() <= 4) {
			return false;
		}
		if (this.getTextBackground().equals(UNAVAILABLE_RGB) == false) {
			return false;
		}
		if (this.getTextForeground().equals(UNAVAILABLE_RGB) == false) {
			return false;
		}
		if (this.getUnderlineColor().equals(UNAVAILABLE_RGB) == false) {
			return false;
		}
		if (this.getStrikeoutColor().equals(UNAVAILABLE_RGB) == false) {
			return false;
		}
		return true;
	}

	public String getText() {
		// return this.getStringPropertyValue("styled_string_text_text");
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isBold() {
		return this.getBooleanPropertyValue(IS_BOLD);
		// return bold;
	}

	public void setBold(boolean bold) {
		// this.bold = bold;
		this.setPropertyValue(IS_BOLD, bold);
	}

	public boolean isItalic() {
		// return italic;
		return this.getBooleanPropertyValue(IS_ITALIC);
	}

	public void setItalic(boolean italic) {
		// this.italic = italic;
		this.setPropertyValue(IS_ITALIC, italic);
	}

	public boolean isUnderline() {
		// return underline;
		return this.getBooleanPropertyValue(IS_UNDERLINE);
	}

	public void setUnderline(boolean underline) {
		// this.underline = underline;
		this.setPropertyValue(IS_UNDERLINE, underline);
	}

	public boolean isStrikeout() {
		// return strikeout;
		return this.getBooleanPropertyValue(IS_STRIKEOUT);
	}

	public void setStrikeout(boolean strikeout) {
		// this.strikeout = strikeout;
		this.setPropertyValue(IS_STRIKEOUT, strikeout);
	}

	public String getFontName() {
		return fontName;
	}

	public void setFontName(String fontName) {
		this.fontName = fontName;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public String getTextForeground() {
		// return textForeground;
		return this.getStringPropertyValue(FOREGROUND);
	}

	public void setTextForeground(String textForeground) {
		// this.textForeground = textForeground;
		this.setPropertyValue(FOREGROUND, textForeground);
	}

	public String getTextBackground() {
		// return textBackground;
		return this.getStringPropertyValue(BACKGROUND);
	}

	public void setTextBackground(String textBackground) {
		// this.textBackground = textBackground;
		this.setPropertyValue(BACKGROUND, textBackground);
	}

	public String getStrikeoutColor() {
		// return strikeoutColor;
		return this.getStringPropertyValue(STRIKEOUT_COLOR);
	}

	public void setStrikeoutColor(String strikeoutColor) {
		// this.strikeoutColor = strikeoutColor;
		this.setPropertyValue(STRIKEOUT_COLOR, strikeoutColor);
	}

	public String getUnderlineColor() {
		// return underlineColor;
		return this.getStringPropertyValue(UNDERLINE_COLOR);
	}

	public void setUnderlineColor(String underlineColor) {
		// this.underlineColor = underlineColor;
		this.setPropertyValue(UNDERLINE_COLOR, underlineColor);
	}

	public int getUnderlineStyle() {
		// return underlineStyle;
		return this.getIntegerPropertyValue(UNDERLINE_STYLE);
	}

	public void setUnderlineStyle(int underlineStyle) {
		// this.underlineStyle = underlineStyle;
		this.setPropertyValue(UNDERLINE_STYLE, underlineStyle);
	}
	
	public int getAlignment(){
		return this.getIntegerPropertyValue(TEXT_ALIGNMENT);
	}
	public void setAlignment(int s){
		this.setPropertyValue(TEXT_ALIGNMENT, s);
	}
	

}

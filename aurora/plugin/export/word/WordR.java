package aurora.plugin.export.word;

import java.io.IOException;

@SuppressWarnings("unchecked")
public class WordR {
	
	private RPr rPr = new RPr();
	private Object obj;	
		
	public RPr getRPr() {
		return rPr;
	}

	public void setRPr(RPr pr) {
		rPr = pr;
	}
	
	public void setText(String text) throws IOException{
		this.obj = new T(text);
	}
	
	public void setBr(){
		this.obj = new Br();
	}
	
	public String toXML(){
		StringBuffer sb = new StringBuffer();
		sb.append("<w:r>");
		if(this.getRPr()!=null){
			sb.append(this.getRPr().toXML());
		}
		if(this.obj instanceof T){
			T t = (T)this.obj;
			sb.append("<w:t>"+t.getText()+"</w:t>");
		}else if(this.obj instanceof Br){
			sb.append("<w:br/>");
		}
		sb.append("</w:r>");
		return sb.toString();
	}
	
	
	class T {
		private String text = null;
		
		public T(String t){
			this.text = t;
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		
	}
	
	class Br {}
	

	class RPr {

		private String font = "宋体";
		private Integer fontSize = 24;
		private boolean isBold = false;
		private boolean isUnderLine = false;
		private String underLineType = "single";

		public String getFont() {
			return font;
		}

		public void setFont(String font) {
			this.font = font;
		}

		public Integer getFontSize() {
			return fontSize;
		}

		public void setFontSize(Integer fontSize) {
			this.fontSize = fontSize;
		}

		public boolean isBold() {
			return isBold;
		}

		public void setBold(boolean isBold) {
			this.isBold = isBold;
		}

		public boolean isUnderLine() {
			return isUnderLine;
		}

		public void setUnderLine(boolean isUnderLine) {
			this.isUnderLine = isUnderLine;
		}

		public String getUnderLineType() {
			return underLineType;
		}

		public void setUnderLineType(String underLineType) {
			this.underLineType = underLineType;
		}
		
		public String toXML(){
			StringBuffer sb = new StringBuffer();
			sb.append("<w:rPr>");
			sb.append("	<w:rFonts w:ascii='"+this.getFont()+"' w:h-ansi='"+this.getFont()+"'/>");
			sb.append("	<wx:font wx:val='"+this.getFont()+"'/>");
			sb.append("	<w:sz w:val='"+this.getFontSize()+"'/>");
			sb.append("	<w:sz-cs w:val='"+this.getFontSize()+"'/>");
			if(this.isBold())sb.append("<w:b/>");
			if(this.isUnderLine())sb.append("<w:u w:val='"+this.getUnderLineType()+"'/>");
			sb.append("</w:rPr>");
			return sb.toString();
		}

	}


	

}

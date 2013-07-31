package aurora.plugin.export.word;

@SuppressWarnings("unchecked")
public class WordPPr {

	private Integer outlineLvl = null;
	private ListPr listPr = null;
	private RPr rPr = new RPr();
	
	public WordPPr createListPr(){
		setListPr(new ListPr());
		return this;
	}
	
	public Integer getOutlineLvl() {
		return outlineLvl;
	}

	public void setOutlineLvl(Integer outlineLvl) {
		this.outlineLvl = outlineLvl;
	}

	public ListPr getListPr() {
		return listPr;
	}

	public void setListPr(ListPr listPr) {
		this.listPr = listPr;
	}

	public RPr getRPr() {
		return rPr;
	}

	public void setRPr(RPr pr) {
		rPr = pr;
	}
	
	public String toXML(){
		StringBuffer sb = new StringBuffer();
		sb.append("<w:pPr>");
		if(this.getOutlineLvl() != null) sb.append("	<w:outlineLvl w:val='"+this.getOutlineLvl()+"'/>");
		if(this.getListPr()!=null){
			sb.append("	<w:listPr>");
			sb.append("	<w:ilvl w:val='"+this.getListPr().getIlvl()+"'/>");
			sb.append("	<w:ilfo w:val='"+(this.getOutlineLvl()==0 ? 1000 : this.getListPr().getIlfo())+"'/>");
			sb.append("	</w:listPr>");			
		}
		if(this.getRPr()!=null){
			sb.append(this.getRPr().toXML());
		}
		sb.append("</w:pPr>");
		return sb.toString();
	}

	class ListPr {
		private Integer ilvl = null;
		private Integer ilfo = null;

		public Integer getIlvl() {
			return ilvl;
		}

		public void setIlvl(Integer ilvl) {
			this.ilvl = ilvl;
		}

		public Integer getIlfo() {
			return ilfo;
		}

		public void setIlfo(Integer ilfo) {
			this.ilfo = ilfo;
		}
	}

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
			if(this.isBold())sb.append("            <w:b/>");
			if(this.isUnderLine())sb.append("<w:u w:val='"+this.getUnderLineType()+"'>");
			sb.append("</w:rPr>");
			return sb.toString();
		}

	}

}

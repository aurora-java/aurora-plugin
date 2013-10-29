package aurora.plugin.export.word.wml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "pbdr")
@XmlAccessorType(XmlAccessType.FIELD)
public class PBdr {
	
	@XmlElement(name = "top",type=Top.class)
	private Border top;
	@XmlElement(name = "bottom",type=Bottom.class)
	private Border bottom;
	@XmlElement(name = "left",type=Left.class)
	private Border left;
	@XmlElement(name = "right",type=Right.class)
	private Border right;
	
	public Border getTop() {
		return top;
	}
	public void setTop(Border top) {
		this.top = top;
	}
	public Border getBottom() {
		return bottom;
	}
	public void setBottom(Border bottom) {
		this.bottom = bottom;
	}
	public Border getLeft() {
		return left;
	}
	public void setLeft(Border left) {
		this.left = left;
	}
	public Border getRight() {
		return right;
	}
	public void setRight(Border right) {
		this.right = right;
	}
	
	public interface Border {
		public String getValue();
		public String getColor();
		public String getSz();
		public String getSpace();
	}
	
	
	@XmlRootElement(name = "top")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Top implements Border{
		@XmlAttribute
		private String value = "single";
		@XmlAttribute
		private String color = "auto";
		@XmlAttribute
		private String sz = "6";
		@XmlAttribute
		private String space = "1";

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getColor() {
			return color;
		}

		public void setColor(String color) {
			this.color = color;
		}

		public String getSz() {
			return sz;
		}

		public void setSz(String sz) {
			this.sz = sz;
		}

		public String getSpace() {
			return space;
		}

		public void setSpace(String space) {
			this.space = space;
		}
	}
	
	@XmlRootElement(name = "left")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Left implements Border{
		@XmlAttribute
		private String value = "single";
		@XmlAttribute
		private String color = "auto";
		@XmlAttribute
		private String sz = "6";
		@XmlAttribute
		private String space = "1";

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getColor() {
			return color;
		}

		public void setColor(String color) {
			this.color = color;
		}

		public String getSz() {
			return sz;
		}

		public void setSz(String sz) {
			this.sz = sz;
		}

		public String getSpace() {
			return space;
		}

		public void setSpace(String space) {
			this.space = space;
		}
	}

	@XmlRootElement(name = "bottom")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Bottom implements Border{
		@XmlAttribute
		private String value = "single";
		@XmlAttribute
		private String color = "auto";
		@XmlAttribute
		private String sz = "6";
		@XmlAttribute
		private String space = "1";

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getColor() {
			return color;
		}

		public void setColor(String color) {
			this.color = color;
		}

		public String getSz() {
			return sz;
		}

		public void setSz(String sz) {
			this.sz = sz;
		}

		public String getSpace() {
			return space;
		}

		public void setSpace(String space) {
			this.space = space;
		}
	}

	@XmlRootElement(name = "right")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Right implements Border{
		@XmlAttribute
		private String value = "single";
		@XmlAttribute
		private String color = "auto";
		@XmlAttribute
		private String sz = "6";
		@XmlAttribute
		private String space = "1";

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getColor() {
			return color;
		}

		public void setColor(String color) {
			this.color = color;
		}

		public String getSz() {
			return sz;
		}

		public void setSz(String sz) {
			this.sz = sz;
		}

		public String getSpace() {
			return space;
		}

		public void setSpace(String space) {
			this.space = space;
		}
	}

}





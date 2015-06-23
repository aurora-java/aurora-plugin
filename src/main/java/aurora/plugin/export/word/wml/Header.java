package aurora.plugin.export.word.wml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "header")
@XmlAccessorType(XmlAccessType.FIELD)
public class Header {
	
	@XmlElement(name = "p")
	private Paragraph para;

	public Paragraph getPara() {
		return para;
	}

	public void setPara(Paragraph para) {
		this.para = para;
	}
	
}

package aurora.plugin.export.word.wml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "toc")
@XmlAccessorType(XmlAccessType.FIELD)
public class Toc {
	
	@XmlTransient
	public static final String TOC_TITLE = "TOC_TITLE";
	@XmlTransient
	public static final String TOC_BOOKMARK = "TOC_BOOKMARK";
}

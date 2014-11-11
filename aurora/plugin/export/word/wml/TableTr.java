package aurora.plugin.export.word.wml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "tr")
@XmlAccessorType(XmlAccessType.FIELD)
public class TableTr {
	
	@XmlAttribute
	private Float height = new Float(0.67);
	
	
	
	@XmlElement(name = "tc")
	private List<TableTc> tcs = new ArrayList<TableTc>();

	public List<TableTc> getTcs() {
		return tcs;
	}

	public void setTcs(List<TableTc> tcs) {
		this.tcs = tcs;
	}

	public Float getHeight() {
		return height;
	}

	public void setHeight(Float height) {
		this.height = height;
	}

}

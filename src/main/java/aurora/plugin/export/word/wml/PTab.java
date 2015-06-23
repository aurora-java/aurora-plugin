package aurora.plugin.export.word.wml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ptab")
@XmlAccessorType(XmlAccessType.FIELD)
public class PTab {
	
	/**对齐基准**/
	@XmlAttribute
	private String relativeTo = "margin";
	
	/**对齐方式**/
	@XmlAttribute
	private String alignment = "right";
	
	/**前导符**/
	@XmlAttribute
	private String leader = "none";

	public String getRelativeTo() {
		return relativeTo;
	}

	public void setRelativeTo(String relativeTo) {
		this.relativeTo = relativeTo;
	}

	public String getAlignment() {
		return alignment;
	}

	public void setAlignment(String alignment) {
		this.alignment = alignment;
	}

	public String getLeader() {
		return leader;
	}

	public void setLeader(String leader) {
		this.leader = leader;
	}
	
}

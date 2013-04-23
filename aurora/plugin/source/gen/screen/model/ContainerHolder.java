package aurora.plugin.source.gen.screen.model;

import java.util.ArrayList;
import java.util.List;


public class ContainerHolder extends AuroraComponent implements
		IDialogEditableObject {
	private Container target = null;
	private AuroraComponent owner = null;
//	private String containerType = BOX.SECTION_TYPE_QUERY;
	private List<String> types = new ArrayList<String>();
	
	

	public ContainerHolder() {
		super();
	}

	public String getDescripition() {
		if (target == null)
			return "";
		return target.getComponentType();
//		.getPrompt();
	}

	public Container getTarget() {
		return target;
	}

	public void setTarget(Container target) {
		this.target = target;
	}

	public AuroraComponent getOwner() {
		return owner;
	}

	public void setOwner(AuroraComponent owner) {
		this.owner = owner;
	}

	public Object getContextInfo() {
		return owner;
	}

//	public String getQueryDateset() {
//		if (target == null || target.getDataset() == null)
//			return "";
//		return target.getDataset().getId();
//	}

	public ContainerHolder clone() {
		ContainerHolder qc = new ContainerHolder();
		qc.target = target;
		qc.owner = owner;
		qc.types = types;
		return qc;
	}

//	public Image getDisplayImage() {
//		if (target == null)
//			return null;
//		return PropertySourceUtil.getImageOf(target);
//	}

	public List<String> getContainerTypes() {
		return types;
	}

//	public void setContainerType(String containerType) {
//		this.containerType = containerType;
//	}

	public void addContainerType(String sectionTypeResult) {
		types.add(sectionTypeResult);
	}

}

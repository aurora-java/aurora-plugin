package aurora.plugin.source.gen.screen.model;

public class CustomTreeNode extends AuroraComponent {

	public static final String CUSTOM_ICON = "custom_tree_node";

	public CustomTreeNode() {
		this.setSize(100, 24);
		this.setComponentType(CUSTOM_ICON);
		this.setPrompt("Node");
	}

	public boolean isResponsibleChild(AuroraComponent component) {
		if (component instanceof CustomTreeNode
				|| component instanceof CustomTreeContainerNode)
			return true;
		return false;
	}

}

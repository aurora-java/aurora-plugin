package aurora.plugin.source.gen.screen.model;


public class CustomTreeNode extends AuroraComponent {

	public static final String CUSTOM_ICON = "custom_tree_node";

	

	public CustomTreeNode() {
		this.setSize(150, 100);
		this.setComponentType(CUSTOM_ICON);
		this.setPrompt(this.getComponentType());
	}

}

package aurora.plugin.source.gen.screen.model;


public class CustomTreeContainerNode extends Container  {

	public static final String CUSTOM_ICON = "custom_tree_container_node";
	
	public static final String CONTAINER_EXPAND = "tree_container_expand";

	
	public boolean isExpand() {
		return this.getBooleanPropertyValue(CONTAINER_EXPAND);
	}

	public void setExpand(boolean isExpand) {
		this.setPropertyValue(CONTAINER_EXPAND, isExpand);
	}

	public CustomTreeContainerNode() {
		this.setSize(100, 24);
		this.setComponentType(CUSTOM_ICON);
		this.setPrompt("Folder");
	}


	public void addNode(AuroraComponent node) {
		this.addChild(node);
	}
	
	public void removeNode(AuroraComponent node) {
		this.removeChild(node);
	}

}

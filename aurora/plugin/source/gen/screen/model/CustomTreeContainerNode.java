package aurora.plugin.source.gen.screen.model;

import java.util.ArrayList;
import java.util.List;

public class CustomTreeContainerNode extends CustomTreeNode {

	public static final String CUSTOM_ICON = "custom_tree_container_node";

	private List<CustomTreeNode> nodes = new ArrayList<CustomTreeNode>();

	private boolean isRoot;

	private boolean isExpand;
	
	public boolean isExpand() {
		return isExpand;
	}

	public void setExpand(boolean isExpand) {
		this.isExpand = isExpand;
	}

	public CustomTreeContainerNode() {
		this.setSize(150, 100);
		this.setComponentType(CUSTOM_ICON);
		this.setPrompt(this.getComponentType());
	}

	public CustomTreeContainerNode(boolean isRoot) {
		this();
		this.isRoot = isRoot;
	}

	public void addNode(CustomTreeNode node) {
		nodes.add(node);
	}
	
	public void removeNode(CustomTreeNode node) {
		nodes.add(node);
	}

	public List<CustomTreeNode> getNodes() {
		return nodes;
	}

	public boolean isRoot() {
		return isRoot;
	}

	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}
	

}

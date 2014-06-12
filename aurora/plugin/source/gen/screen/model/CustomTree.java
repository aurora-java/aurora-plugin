package aurora.plugin.source.gen.screen.model;

public class CustomTree extends BOX {

	public static final String CHECKED_TREE = "checked_tree";
	public static final String CUSTOM_ICON = "custom_tree";


	public CustomTree() {
		this.setSize(200, 180);
		this.setComponentType(CUSTOM_ICON);
		this.setPrompt("Tree");
		this.setLabelWidth(0);
	}

	public boolean isResponsibleChild(AuroraComponent component) {
		if (component instanceof CustomTreeNode
				|| component instanceof CustomTreeContainerNode)
			return true;
		return false;
	}
	

	public int getHeadHight() {
		return 5;
	}

	@Override
	final public int getRow() {
		return 100000;
	}

	@Override
	final public void setRow(int row) {
		// always Integer.MAX_VALUE
	}

	@Override
	final public int getCol() {
		// always 1
		return 1;
	}

	@Override
	final public void setCol(int col) {
		// always 1
	}
	public boolean isCheckedTree(){
		return getBooleanPropertyValue(CHECKED_TREE);
	}
}

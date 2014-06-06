package aurora.plugin.source.gen.screen.model;

import aurora.plugin.source.gen.screen.model.properties.ComponentInnerProperties;

public class CustomTree extends AuroraComponent {

	public static final String CUSTOM_ICON = "custom_tree";
	
	private CustomTreeContainerNode root = new CustomTreeContainerNode(true);

	public CustomTree() {
		this.setSize(150, 100);
		this.setComponentType(CUSTOM_ICON);
		this.setPrompt(this.getComponentType());
	}

	public String getIconByteData() {
		String string = this
				.getStringPropertyValue(ComponentInnerProperties.ICON_BYTES_DATA);
		// if ("".equals(string)) {
		// // return DEFAULT_ICON;
		// string = DEFAULT_DATA;
		// }
		// byte[] _byteArray = AuroraImagesUtils.toBytes(string);
		// return _byteArray;
		return "";
	}

	public void setIconByteData(String bytes) {
		// this.setPropertyValue(ComponentInnerProperties.ICON_BYTES_DATA,
		// AuroraImagesUtils.toString(bytes));
		this.setPropertyValue(ComponentInnerProperties.ICON_BYTES_DATA, bytes);
	}

	public CustomTreeContainerNode getRoot() {
		return root;
	}

	public void setRoot(CustomTreeContainerNode root) {
		this.root = root;
	}

}

package aurora.plugin.source.gen.screen.model;

import aurora.plugin.source.gen.screen.model.properties.ComponentInnerProperties;

public class ToolbarButton extends Button {
	public static final String TOOLBAR_BUTTON = "toolbar_button";

	public ToolbarButton() {
		setSize(80, 20);
		this.setComponentType(TOOLBAR_BUTTON); //$NON-NLS-1$
	}

	public String getIconByteData() {
		String string = this
				.getStringPropertyValue(ComponentInnerProperties.ICON_BYTES_DATA);
		return string;
	}

	public void setIconByteData(String bytes) {
		this.setPropertyValue(ComponentInnerProperties.ICON_BYTES_DATA, bytes);
	}

	@Override
	public Object getPropertyValue(String propId) {
		if ((ComponentInnerProperties.ICON_BYTES_DATA_DEO).equals(propId))
			return createDEO(getIconByteData());
		return super.getPropertyValue(propId);
	}

	private Object createDEO(String iconByteData) {

		DialogEditableObject deo = new DialogEditableObject();
		deo.setData(this.getIconByteData());
		deo.setPropertyId(ComponentInnerProperties.ICON_BYTES_DATA);
		deo.setDescripition("".equals(getIconByteData()) == false ? "Y" : "N");
		deo.setContentInfo(this);
		return deo;
	}

	@Override
	public void setPropertyValue(String propId, Object val) {
		if ((ComponentInnerProperties.ICON_BYTES_DATA_DEO ).equals(propId)) {
			if (val instanceof DialogEditableObject) {
				this.setIconByteData(((DialogEditableObject) val).getData() == null ? ""
						: ((DialogEditableObject) val).getData() + "");
			}
		}
		super.setPropertyValue(propId, val);
	}
}

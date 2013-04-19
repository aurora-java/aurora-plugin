package aurora.plugin.source.gen.screen.model;

import aurora.plugin.source.gen.screen.model.properties.ComponentProperties;


public class CheckBox extends Input {

	public static final String TEXT = "text";
	public static final String CHECKBOX = "checkBox";

	public CheckBox() {
		setSize(120, 20);
		this.setComponentType(CHECKBOX);
		this.setText(TEXT);
		DatasetField datasetField = getDatasetField();
		datasetField.setCheckedValue("Y");
		datasetField.setUncheckedValue("N");
		datasetField.setDefaultValue("N");
	}

	public String getText() {
		return this.getStringPropertyValue(ComponentProperties.text);
	}

	public void setText(String text) {
		this.setPropertyValue(ComponentProperties.text,text);
	}

}

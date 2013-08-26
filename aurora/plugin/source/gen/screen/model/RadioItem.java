package aurora.plugin.source.gen.screen.model;

import aurora.plugin.source.gen.screen.model.properties.ComponentProperties;


public class RadioItem extends Input {

	public static final String TEXT = "text";
	public static final String RADIO_ITEM = "radio_item";

	public RadioItem() {
		setSize(120, 20);
		this.setComponentType(RADIO_ITEM);
		this.setText(TEXT);
		this.setPrompt("radio");
	}

	public String getText() {
		return this.getStringPropertyValue(ComponentProperties.text);
	}

	public void setText(String text) {
		this.setPropertyValue(ComponentProperties.text,text);
	}

}

package aurora.plugin.source.gen.builders;

import java.util.Map;

import aurora.plugin.source.gen.screen.model.properties.IProperties;


public class ButtonBuilder extends DefaultSourceBuilder {
	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put(IProperties.height, IProperties.height);
		attributeMapping.put(IProperties.name, IProperties.name);
		attributeMapping.put(IProperties.width, IProperties.width);
		attributeMapping.put(IProperties.text, IProperties.text);
		attributeMapping.put(IProperties.title, IProperties.title);
		attributeMapping.put(IProperties.click, IProperties.click);
		return attributeMapping;
	}
	
}

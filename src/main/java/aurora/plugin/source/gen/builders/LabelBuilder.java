package aurora.plugin.source.gen.builders;

import java.util.Map;

import aurora.plugin.source.gen.screen.model.properties.IProperties;


public class LabelBuilder extends DefaultSourceBuilder {
	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put(IProperties.width, IProperties.width);
		attributeMapping.put(IProperties.bindTarget, IProperties.bindTarget);
		attributeMapping.put(IProperties.prompt, IProperties.prompt);
		attributeMapping.put(IProperties.renderer, IProperties.renderer);
		return attributeMapping;
	}
}

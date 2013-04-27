package aurora.plugin.source.gen.builders;

import java.util.Map;

import aurora.plugin.source.gen.screen.model.properties.IProperties;

public class LovBuilder extends DefaultSourceBuilder {
	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put(IProperties.width, IProperties.width);
		attributeMapping.put(IProperties.bindTarget, IProperties.bindTarget);
		attributeMapping.put(IProperties.prompt, IProperties.prompt);
		attributeMapping.put(IProperties.emptyText, IProperties.emptyText);
		attributeMapping.put(IProperties.typeCase, IProperties.typeCase);
		attributeMapping.put(IProperties.FOR_DISPLAY_FIELD,
				IProperties.FOR_DISPLAY_FIELD);
		return attributeMapping;
	}
}

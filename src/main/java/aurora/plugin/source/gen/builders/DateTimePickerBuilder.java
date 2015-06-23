package aurora.plugin.source.gen.builders;

import java.util.Map;

import aurora.plugin.source.gen.screen.model.properties.IProperties;


public class DateTimePickerBuilder extends DefaultSourceBuilder {
	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put(IProperties.emptyText, IProperties.emptyText);
		attributeMapping.put(IProperties.bindTarget, IProperties.bindTarget);
		attributeMapping.put(IProperties.prompt, IProperties.prompt);
		attributeMapping.put(IProperties.width, IProperties.width);
		attributeMapping.put(IProperties.enableBesideDays, IProperties.enableBesideDays);
		attributeMapping.put(IProperties.enableMonthBtn, IProperties.enableMonthBtn);
		return attributeMapping;
	}
}

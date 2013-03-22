package aurora.plugin.source.gen.builders;

import java.util.Map;


public class DateTimePickerBuilder extends DefaultSourceBuilder {
	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put("emptyText", "emptyText");
		attributeMapping.put("bindTarget", "bindTarget");
		attributeMapping.put("prompt", "prompt");
		attributeMapping.put("width", "width");
		attributeMapping.put("enableBesideDays", "enableBesideDays");
		attributeMapping.put("enableMonthBtn", "enableMonthBtn");
		return attributeMapping;
	}
}

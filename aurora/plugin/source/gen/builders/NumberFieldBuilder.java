package aurora.plugin.source.gen.builders;

import java.util.Map;


public class NumberFieldBuilder extends DefaultSourceBuilder {
	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put("width", "width");
		attributeMapping.put("bindTarget", "bindTarget");
		attributeMapping.put("prompt", "prompt");
		attributeMapping.put("emptyText", "emptyText");
		attributeMapping.put("allowDecimals", "allowDecimals");
		attributeMapping.put("decimalPrecision", "decimalPrecision");
		attributeMapping.put("allowNegative", "allowNegative");
		attributeMapping.put("allowFormat", "allowFormat");
		return attributeMapping;
	}
}

package aurora.plugin.source.gen.builders;

import java.util.Map;


public class ComboBoxBuilder extends DefaultSourceBuilder {
	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put("bindTarget", "bindTarget");
		attributeMapping.put("prompt", "prompt");
		attributeMapping.put("width", "width");
		attributeMapping.put("emptyText", "emptyText");
		attributeMapping.put("typeCase", "typeCase");
		return attributeMapping;
	}
}

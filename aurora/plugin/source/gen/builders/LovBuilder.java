package aurora.plugin.source.gen.builders;

import java.util.Map;


public class LovBuilder extends DefaultSourceBuilder {
	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put("width", "width");
		attributeMapping.put("bindTarget", "bindTarget");
		attributeMapping.put("prompt", "prompt");
		attributeMapping.put("emptyText", "emptyText");
		attributeMapping.put("typeCase", "typeCase");
		return attributeMapping;
	}
}

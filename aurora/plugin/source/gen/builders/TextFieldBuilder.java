package aurora.plugin.source.gen.builders;

import java.util.Map;


public class TextFieldBuilder extends DefaultSourceBuilder {
	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put("width", "width");
		attributeMapping.put("prompt", "prompt");
		attributeMapping.put("bindTarget", "bindTarget");
		attributeMapping.put("emptyText", "emptyText");
		attributeMapping.put("typeCase", "typeCase");
		return attributeMapping;
	}
}

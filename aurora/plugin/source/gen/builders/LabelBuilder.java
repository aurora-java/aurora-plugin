package aurora.plugin.source.gen.builders;

import java.util.Map;


public class LabelBuilder extends DefaultSourceBuilder {
	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put("width", "width");
		attributeMapping.put("bindTarget", "bindTarget");
		attributeMapping.put("prompt", "prompt");
		attributeMapping.put("renderer", "renderer");
		return attributeMapping;
	}
}

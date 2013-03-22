package aurora.plugin.source.gen.builders;

import java.util.Map;


public class ButtonBuilder extends DefaultSourceBuilder {
	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put("height", "height");
		attributeMapping.put("name", "name");
		attributeMapping.put("width", "width");
		attributeMapping.put("text", "text");
		attributeMapping.put("title", "title");
		attributeMapping.put("click", "click");
		return attributeMapping;
	}
	
}

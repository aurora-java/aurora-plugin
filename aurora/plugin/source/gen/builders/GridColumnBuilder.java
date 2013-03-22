package aurora.plugin.source.gen.builders;

import java.util.Map;


public class GridColumnBuilder extends DefaultSourceBuilder {
	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put("width", "width");
		attributeMapping.put("height", "height");
		attributeMapping.put("renderer", "renderer");
		attributeMapping.put("editor", "editor");
		attributeMapping.put("footerRenderer", "footerRenderer");
		return attributeMapping;
	}
}

package aurora.plugin.source.gen.builders;

import java.util.Map;

import aurora.plugin.source.gen.BuilderSession;


public class GridColumnBuilder extends DefaultSourceBuilder {
	@Override
	public void buildContext(BuilderSession session) {
		super.buildContext(session);
	}

	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put("width", "width");
		attributeMapping.put("renderer", "renderer");
		attributeMapping.put("editor", "editor");
		attributeMapping.put("footerRenderer", "footerRenderer");
		attributeMapping.put("editor_type", "editor_type");
		attributeMapping.put("prompt", "prompt");
		return attributeMapping;
	}
}

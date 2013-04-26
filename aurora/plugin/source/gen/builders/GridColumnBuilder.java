package aurora.plugin.source.gen.builders;

import java.util.Map;

import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.screen.model.properties.IProperties;


public class GridColumnBuilder extends DefaultSourceBuilder {
	@Override
	public void buildContext(BuilderSession session) {
		super.buildContext(session);
	}

	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put(IProperties.width, IProperties.width);
		attributeMapping.put(IProperties.renderer, IProperties.renderer);
		attributeMapping.put(IProperties.editor, IProperties.editor);
		attributeMapping.put(IProperties.footerRenderer, IProperties.footerRenderer);
		attributeMapping.put(IProperties.EDITOR_TYPE, IProperties.EDITOR_TYPE);
		attributeMapping.put(IProperties.prompt, IProperties.prompt);
		return attributeMapping;
	}
}

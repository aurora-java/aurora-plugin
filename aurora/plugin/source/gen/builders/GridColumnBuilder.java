package aurora.plugin.source.gen.builders;

import java.util.Map;

import uncertain.composite.CompositeMap;
import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.screen.model.properties.ComponentInnerProperties;
import aurora.plugin.source.gen.screen.model.properties.IProperties;

public class GridColumnBuilder extends DefaultSourceBuilder {
	@Override
	public void buildContext(BuilderSession session) {
		super.buildContext(session);
		session.getCurrentModel().getChildByAttrib("", "");
		CompositeMap childrenMap = session.getCurrentModel().getChildByAttrib(
				IProperties.PROPERTYE_ID, IProperties.COMPONENT_CHILDREN);
		session.getCurrentContext().put(
				"hasChildren",
				childrenMap == null ? false : childrenMap.getChildsNotNull()
						.size() > 0);
	}

	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put(IProperties.width, IProperties.width);
		attributeMapping.put(IProperties.renderer, IProperties.renderer);
		attributeMapping.put(IProperties.editor, IProperties.editor);
		attributeMapping.put(IProperties.footerRenderer,
				IProperties.footerRenderer);
		attributeMapping.put(IProperties.EDITOR_TYPE, IProperties.EDITOR_TYPE);
		attributeMapping.put(IProperties.prompt, IProperties.prompt);
		attributeMapping.put(IProperties.FOR_DISPLAY_FIELD,
				IProperties.FOR_DISPLAY_FIELD);
		return attributeMapping;
	}

	public void actionEvent(String event, BuilderSession session) {
		if (IProperties.EVENT_CHILDREN.equals(event)
				&& IProperties.GRIDCOLUMN.equalsIgnoreCase(session
						.getCurrentModel().getString(
								ComponentInnerProperties.COMPONENT_TYPE, ""))) {
			buildChildComponent(session);
		}
	}
}

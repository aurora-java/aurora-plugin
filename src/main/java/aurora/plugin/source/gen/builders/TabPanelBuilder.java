package aurora.plugin.source.gen.builders;

import java.util.Map;

import uncertain.composite.CompositeMap;

import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.screen.model.properties.IProperties;

public class TabPanelBuilder extends DefaultSourceBuilder {

	public void actionEvent(String event, BuilderSession session) {
		if (IProperties.EVENT_CHILDREN.equals(event)
				&& IProperties.TAB_PANEL.equalsIgnoreCase(session.getCurrentModel()
						.getString(IProperties.COMPONENT_TYPE, ""))) {

			CompositeMap currentModel = session.getCurrentModel();
			CompositeMap child = currentModel.getChildByAttrib(
					IProperties.PROPERTYE_ID, IProperties.COMPONENT_TABS);
			if (child != null)
				buildChildComponent(session, child);

		}
	}

	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put(IProperties.width, IProperties.width);
		attributeMapping.put(IProperties.height, IProperties.height);
		return attributeMapping;
	}
}

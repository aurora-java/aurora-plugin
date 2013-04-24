package aurora.plugin.source.gen.builders;

import java.util.Map;

import uncertain.composite.CompositeMap;

import aurora.plugin.source.gen.BuilderSession;

public class TabPanelBuilder extends DefaultSourceBuilder {
	public void actionEvent(String event, BuilderSession session) {
		if ("children".equals(event)
				&& "tabPanel".equalsIgnoreCase(session.getCurrentModel()
						.getString("component_type", ""))) {

			CompositeMap currentModel = session.getCurrentModel();
			CompositeMap child = currentModel.getChildByAttrib("propertye_id",
					"component_tabs");
			if (child != null)
				buildChildComponent(session, child);
		
		}
	}
	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put("width", "width");
		attributeMapping.put("height", "height");
		return attributeMapping;
	}
}

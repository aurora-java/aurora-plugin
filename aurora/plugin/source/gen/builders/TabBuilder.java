package aurora.plugin.source.gen.builders;

import java.util.Map;

import aurora.plugin.source.gen.BuilderSession;


public class TabBuilder extends DefaultSourceBuilder {
	public void actionEvent(String event, BuilderSession session) {
		if ("children".equals(event)
				&& "tab".equalsIgnoreCase(session.getCurrentModel()
						.getString("component_type", ""))) {
			buildChildComponent(session);
		}
	}
	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put("width", "width");
		attributeMapping.put("ref", "ref");
		attributeMapping.put("prompt", "prompt");
		return attributeMapping;
	}
}

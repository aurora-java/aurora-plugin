package aurora.plugin.source.gen.builders;

import java.util.Map;

import aurora.plugin.source.gen.BuilderSession;


public class FormBuilder extends DefaultSourceBuilder {
	public void actionEvent(String event, BuilderSession session) {
		if ("children".equals(event)
				&& "form".equalsIgnoreCase(session.getCurrentModel()
						.getString("component_type", ""))) {
			buildChildComponent(session);
		}
	}
	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put("title", "title");
		attributeMapping.put("column", "column");
		attributeMapping.put("prompt", "prompt");
		attributeMapping.put("height", "height");
		attributeMapping.put("row", "row");
		attributeMapping.put("width", "width");
		attributeMapping.put("labelWidth", "labelWidth");
		return attributeMapping;
	}
}

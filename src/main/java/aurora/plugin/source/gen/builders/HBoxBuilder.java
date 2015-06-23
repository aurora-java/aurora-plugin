package aurora.plugin.source.gen.builders;

import java.util.Map;

import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.screen.model.properties.IProperties;

public class HBoxBuilder extends DefaultSourceBuilder {
	public void actionEvent(String event, BuilderSession session) {
		if (IProperties.EVENT_CHILDREN.equals(event)
				&& IProperties.HBOX.equalsIgnoreCase(session.getCurrentModel()
						.getString(IProperties.COMPONENT_TYPE,"" ))) {
			buildChildComponent(session);
		}
	}

	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put(IProperties.width, IProperties.width);
		attributeMapping.put(IProperties.height, IProperties.height);
		attributeMapping.put(IProperties.labelWidth, IProperties.labelWidth);
		return attributeMapping;
	}
}

package aurora.plugin.source.gen.builders;

import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.screen.model.properties.IProperties;

public class ScreenBuilder extends DefaultSourceBuilder {
	
	public void actionEvent(String event, BuilderSession session) {
		if (IProperties.EVENT_CHILDREN.equals(event)
				&& IProperties.SCREEN.equalsIgnoreCase(session.getCurrentModel()
						.getString(IProperties.COMPONENT_TYPE, ""))) {
			buildChildComponent(session);
		}
	}
}

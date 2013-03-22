package aurora.plugin.source.gen.builders;

import aurora.plugin.source.gen.BuilderSession;


public class ToolbarBuilder extends DefaultSourceBuilder {
	public void actionEvent(String event, BuilderSession session) {
		if ("children".equals(event)
				&& "toolbar".equalsIgnoreCase(session.getCurrentModel()
						.getString("component_type", ""))) {
			buildChildComponent(session);
		}
	}
	
}

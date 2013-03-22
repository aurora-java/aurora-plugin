package aurora.plugin.source.gen.builders;

import aurora.plugin.source.gen.BuilderSession;


public class TabBuilder extends DefaultSourceBuilder {
	public void actionEvent(String event, BuilderSession session) {
		if ("children".equals(event)
				&& "tab".equalsIgnoreCase(session.getCurrentModel()
						.getString("component_type", ""))) {
			buildChildComponent(session);
		}
	}
}

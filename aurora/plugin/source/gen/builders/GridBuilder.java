package aurora.plugin.source.gen.builders;

import aurora.plugin.source.gen.BuilderSession;


public class GridBuilder extends DefaultSourceBuilder {
	public void actionEvent(String event, BuilderSession session) {
		if ("children".equals(event)
				&& "grid".equalsIgnoreCase(session.getCurrentModel()
						.getString("component_type", ""))) {
			buildChildComponent(session);
		}
	}
}

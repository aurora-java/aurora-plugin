package aurora.plugin.source.gen.builders;

import java.util.Map;

import uncertain.composite.CompositeMap;
import aurora.plugin.source.gen.BuilderSession;

public class ScreenBuilder extends DefaultSourceBuilder {
	
	@Override
	public void buildContext(BuilderSession session) {
		// TODO Auto-generated method stub
		super.buildContext(session);
	}

	@Override
	protected void buildChildComponent(BuilderSession session) {
		// TODO Auto-generated method stub
		super.buildChildComponent(session);
	}

	@Override
	public void buildChildComponent(BuilderSession session,
			CompositeMap currentModel) {
		// TODO Auto-generated method stub
		super.buildChildComponent(session, currentModel);
	}

	@Override
	protected void buildAttribute(CompositeMap currentModel,
			CompositeMap context, Map<String, String> attributeMapping) {
		// TODO Auto-generated method stub
		super.buildAttribute(currentModel, context, attributeMapping);
	}

	@Override
	protected Map<String, String> getAttributeMapping() {
		// TODO Auto-generated method stub
		return super.getAttributeMapping();
	}

	@Override
	protected CompositeMap createContext(BuilderSession session) {
		// TODO Auto-generated method stub
		return super.createContext(session);
	}

	public void actionEvent(String event, BuilderSession session) {
		if ("children".equals(event)
				&& "screen".equalsIgnoreCase(session.getCurrentModel()
						.getString("component_type", ""))) {
			buildChildComponent(session);
		}
	}
}

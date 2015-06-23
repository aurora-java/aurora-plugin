package aurora.plugin.source.gen.builders;

import java.util.Map;

import uncertain.composite.CompositeMap;
import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.screen.model.properties.IProperties;

public class ScreenBodyBuilder extends DefaultSourceBuilder {

	@Override
	public void buildContext(BuilderSession session) {
		super.buildContext(session);
	}

	@Override
	protected void buildChildComponent(BuilderSession session) {
		super.buildChildComponent(session);
	}

	@Override
	public void buildChildComponent(BuilderSession session,
			CompositeMap currentModel) {
		super.buildChildComponent(session, currentModel);
	}

	@Override
	protected void buildAttribute(CompositeMap currentModel,
			CompositeMap context, Map<String, String> attributeMapping) {
		super.buildAttribute(currentModel, context, attributeMapping);
	}

	@Override
	protected Map<String, String> getAttributeMapping() {
		return super.getAttributeMapping();
	}

	@Override
	protected CompositeMap createContext(BuilderSession session) {
		return super.createContext(session);
	}

	public void actionEvent(String event, BuilderSession session) {
		if (IProperties.EVENT_CHILDREN.equals(event)
				&& IProperties.SCREEN_BODY.equalsIgnoreCase(session
						.getCurrentModel().getString(
								IProperties.COMPONENT_TYPE, ""))) {
			buildChildComponent(session);
		}
	}
}

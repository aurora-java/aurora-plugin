package aurora.plugin.source.gen.builders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uncertain.composite.CompositeMap;
import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.screen.model.properties.IProperties;

public class DefaultSourceBuilder implements ISourceBuilder {

	public void buildContext(BuilderSession session) {
		CompositeMap currentModel = session.getCurrentModel();
		CompositeMap context = createContext(session);
		Map<String, String> attributeMapping = getAttributeMapping();
		buildAttribute(currentModel, context, attributeMapping);
		session.appendContext(context);
		// buildChildContext(session);
	}

	protected void buildChildComponent(BuilderSession session) {
		CompositeMap currentModel = session.getCurrentModel();
		buildChildComponent(session, currentModel);
		CompositeMap child = currentModel.getChildByAttrib(IProperties.PROPERTYE_ID,
				IProperties.COMPONENT_CHILDREN);
		if (child != null)
			buildChildComponent(session, child);
	}

	public void buildChildComponent(BuilderSession session,
			CompositeMap currentModel) {
		List<?> childs = currentModel.getChilds();
		if (childs != null) {
			for (Object object : childs) {
				if (object instanceof CompositeMap) {
					if (IProperties.COMPONENT_CHILDREN
							.equalsIgnoreCase(((CompositeMap) object)
									.getString(IProperties.PROPERTYE_ID, ""))) {
						continue;
					}
					String buildComponent = session
							.buildComponent((CompositeMap) object);
					session.appendResultln(buildComponent);
				}
			}
		}
	}

	protected void buildAttribute(CompositeMap currentModel,
			CompositeMap context, Map<String, String> attributeMapping) {
		Set<String> keySet = attributeMapping.keySet();
		for (String key : keySet) {
			String from = currentModel.getString(key, "");
			if ("".equals(from) == false) {
				String to = attributeMapping.get(key);
				context.put(to, from);
			}
		}
	}

	protected Map<String, String> getAttributeMapping() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(IProperties.MARKID, IProperties.MARKID);
		map.put(IProperties.COMPONENT_TYPE, IProperties.COMPONENT_TYPE);
		map.put(IProperties.name, IProperties.name);
		return map;
	}

	protected CompositeMap createContext(BuilderSession session) {
		CompositeMap map = new CompositeMap();
		map.setNameSpace(Default_prefix, Default_Namespace);
		CompositeMap currentModel = session.getCurrentModel();
		String name = currentModel.getString(IProperties.COMPONENT_TYPE, "");
		map.setName(name);
		return map;
	}

	public void actionEvent(String event, BuilderSession session) {
	}

}

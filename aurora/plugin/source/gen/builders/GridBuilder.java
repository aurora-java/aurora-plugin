package aurora.plugin.source.gen.builders;

import java.util.List;
import java.util.Map;

import uncertain.composite.CompositeMap;
import aurora.plugin.source.gen.BuilderSession;

public class GridBuilder extends DefaultSourceBuilder {

	@Override
	public void buildContext(BuilderSession session) {
		super.buildContext(session);
		CompositeMap gridContext = session.getCurrentContext();

		gridContext.put("navBar",
				!"".equals(gridContext.getString("navBarType", "")));

		CompositeMap gridModel = session.getCurrentModel();
		CompositeMap toolbar = gridModel.getChildByAttrib("component_type","toolbar");
		if (toolbar != null) {
			toolbar = (CompositeMap) toolbar.clone();
			gridContext.addChild(toolbar);
		}
		CompositeMap editors = gridContext.createChild("editors");
		CompositeMap child_list = gridModel.getChildByAttrib("propertye_id",
				"component_children");
		if (child_list != null) {
			List childsNotNull = child_list.getChildsNotNull();
			for (Object object : childsNotNull) {
				if (object instanceof CompositeMap) {
					String editor = ((CompositeMap) object).getString("editor",
							"");
					if ("".equals(editor) == false) {
						CompositeMap editorMap = editors.getChild(editor);
						if (editorMap != null) {
							String id = editorMap.getString("id", "");
							((CompositeMap) object).put("editor", id);
						} else {
							editorMap = editors.createChild(editor);
							String genEditorID = session.getIDGenerator()
									.genEditorID(editor);
							editorMap.put("id", genEditorID);
							editorMap.put("editor_type",editor);
							((CompositeMap) object).put("editor", genEditorID);
						}
						((CompositeMap) object).put("editor_type", editor);
					}
				}
			}
		}
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

	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put("width", "width");
		attributeMapping.put("bindTarget", "bindTarget");
		attributeMapping.put("prompt", "prompt");
		attributeMapping.put("height", "height");
		attributeMapping.put("navBar", "navBar");
		attributeMapping.put("navBarType", "navBarType");
		return attributeMapping;
	}

	@Override
	protected CompositeMap createContext(BuilderSession session) {
		return super.createContext(session);
	}

	public void actionEvent(String event, BuilderSession session) {
		if ("columns".equals(event)
				&& "grid".equalsIgnoreCase(session.getCurrentModel().getString(
						"component_type", ""))) {
			CompositeMap gridModel = session.getCurrentModel();
			CompositeMap child_list = gridModel.getChildByAttrib("propertye_id",
					"component_children");
			if (child_list != null) {
				List childsNotNull = child_list.getChildsNotNull();
				for (Object object : childsNotNull) {
					if (object instanceof CompositeMap) {
						String result = session
								.buildComponent((CompositeMap) object);
						session.appendResultln(result);
					}
				}
			}
		}
	}
}

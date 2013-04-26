package aurora.plugin.source.gen.builders;

import java.util.List;
import java.util.Map;

import uncertain.composite.CompositeMap;
import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.screen.model.properties.IProperties;

public class GridBuilder extends DefaultSourceBuilder {

	@Override
	public void buildContext(BuilderSession session) {
		super.buildContext(session);
		CompositeMap gridContext = session.getCurrentContext();

		gridContext.put(IProperties.navBar,
				!"".equals(gridContext.getString(IProperties.navBarType, "")));

		CompositeMap gridModel = session.getCurrentModel();
		CompositeMap toolbar = gridModel.getChildByAttrib(
				IProperties.COMPONENT_TYPE, IProperties.TOOLBAR);
		if (toolbar != null) {
			toolbar = (CompositeMap) toolbar.clone();
			gridContext.addChild(toolbar);
		}
		CompositeMap editors = gridContext.createChild(IProperties.EDITORS);
		CompositeMap child_list = gridModel.getChildByAttrib(
				IProperties.PROPERTYE_ID, IProperties.COMPONENT_CHILDREN);
		if (child_list != null) {
			List childsNotNull = child_list.getChildsNotNull();
			for (Object object : childsNotNull) {
				if (object instanceof CompositeMap) {
					String editor = ((CompositeMap) object).getString(
							IProperties.editor, "");
					if ("".equals(editor) == false) {
						CompositeMap editorMap = editors.getChild(editor);
						if (editorMap != null) {
							String id = editorMap.getString(IProperties.id, "");
							((CompositeMap) object).put(IProperties.editor, id);
						} else {
							editorMap = editors.createChild(editor);
							String genEditorID = session.getIDGenerator()
									.genEditorID(editor);
							editorMap.put(IProperties.id, genEditorID);
							editorMap.put(IProperties.EDITOR_TYPE, editor);
							((CompositeMap) object).put(IProperties.editor,
									genEditorID);
						}
						((CompositeMap) object).put(IProperties.EDITOR_TYPE,
								editor);
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
		attributeMapping.put(IProperties.width, IProperties.width);
		attributeMapping.put(IProperties.bindTarget, IProperties.bindTarget);
		attributeMapping.put(IProperties.prompt, IProperties.prompt);
		attributeMapping.put(IProperties.height, IProperties.height);
		attributeMapping.put(IProperties.navBar, IProperties.navBar);
		attributeMapping.put(IProperties.navBarType, IProperties.navBarType);
		return attributeMapping;
	}

	@Override
	protected CompositeMap createContext(BuilderSession session) {
		return super.createContext(session);
	}

	public void actionEvent(String event, BuilderSession session) {
		if (IProperties.EVENT_COLUMNS.equals(event)
				&& IProperties.GRID.equalsIgnoreCase(session.getCurrentModel()
						.getString(IProperties.COMPONENT_TYPE, ""))) {
			CompositeMap gridModel = session.getCurrentModel();
			CompositeMap child_list = gridModel.getChildByAttrib(
					IProperties.PROPERTYE_ID, IProperties.COMPONENT_CHILDREN);
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

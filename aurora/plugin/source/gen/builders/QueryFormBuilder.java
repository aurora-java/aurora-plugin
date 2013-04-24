package aurora.plugin.source.gen.builders;

import java.util.List;
import java.util.Map;

import uncertain.composite.CompositeMap;
import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.ModelMapParser;

public class QueryFormBuilder extends DefaultSourceBuilder {

	@Override
	public void buildContext(BuilderSession session) {
		super.buildContext(session);
		CompositeMap currentModel = session.getCurrentModel();
		CompositeMap childByAttrib = currentModel.getChildByAttrib("reference",
				"propertye_id", "resultTargetContainer");
		String ds_id = currentModel.getChildByAttrib("propertye_id",
				"i_dataset_delegate").getString("ds_id", "");
		session.getCurrentContext().put("bindTarget", ds_id);

		if (childByAttrib != null) {
			CompositeMap currentContext = session.getCurrentContext();
			ModelMapParser mmp = session.createModelMapParser(session
					.getModel());
			String string = mmp.getComponentByID(
					childByAttrib.getString("markid", "")).getString(
					"bindTarget", "");
			currentContext.put("resultTarget", string);
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
		attributeMapping.put("defaultQueryField", "defaultQueryField");
		attributeMapping.put("defaultQueryHint", "defaultQueryHint");
		attributeMapping.put("bindTarget", "bindTarget");

		return attributeMapping;
	}

	@Override
	protected CompositeMap createContext(BuilderSession session) {
		return super.createContext(session);
	}

	public void actionEvent(String event, BuilderSession session) {
		if ("form_toolbar_children".equals(event)) {
			CompositeMap currentModel = session.getCurrentModel();
			CompositeMap childByAttrib = currentModel.getChildByAttrib(
					"containmentList", "propertye_id",
					"query_form_toolbar_children");
			List<?> childs = childByAttrib.getChildsNotNull();
			for (Object object : childs) {
				if (object instanceof CompositeMap) {
					String buildComponent = session
							.buildComponent((CompositeMap) object);
					session.appendResultln(buildComponent);
				}
			}
		}
		if ("form_body_children".equals(event)) {
			CompositeMap currentModel = session.getCurrentModel();
			CompositeMap formBody = currentModel.getChildByAttrib("formBody",
					"propertye_id", "queryFormBody");
			CompositeMap childByAttrib = formBody.getChildByAttrib(
					"propertye_id", "component_children");
			List<?> childs = childByAttrib.getChildsNotNull();
			for (Object object : childs) {
				if (object instanceof CompositeMap) {
					String buildComponent = session
							.buildComponent((CompositeMap) object);
					session.appendResultln(buildComponent);
				}
			}
		}
	}
}

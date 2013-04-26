package aurora.plugin.source.gen.builders;

import java.util.List;
import java.util.Map;

import uncertain.composite.CompositeMap;
import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.ModelMapParser;
import aurora.plugin.source.gen.screen.model.io.KEYS;
import aurora.plugin.source.gen.screen.model.properties.IProperties;

public class QueryFormBuilder extends DefaultSourceBuilder {

	@Override
	public void buildContext(BuilderSession session) {
		super.buildContext(session);
		CompositeMap currentModel = session.getCurrentModel();
		CompositeMap childByAttrib = currentModel.getChildByAttrib(
				KEYS.REFERENCE, IProperties.PROPERTYE_ID,
				IProperties.RESULT_TARGET_CONTAINER);
		String ds_id = currentModel.getChildByAttrib(IProperties.PROPERTYE_ID,
				IProperties.I_DATASET_DELEGATE)
				.getString(IProperties.DS_ID, "");
		session.getCurrentContext().put(IProperties.bindTarget, ds_id);

		if (childByAttrib != null) {
			CompositeMap currentContext = session.getCurrentContext();
			ModelMapParser mmp = session.createModelMapParser(session
					.getModel());
			String string = mmp.getComponentByID(
					childByAttrib.getString(IProperties.MARKID, "")).getString(
					IProperties.bindTarget, "");
			currentContext.put(IProperties.resultTarget, string);
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
		attributeMapping.put(IProperties.defaultQueryField,
				IProperties.defaultQueryField);
		attributeMapping.put(IProperties.defaultQueryHint,
				IProperties.defaultQueryHint);
		attributeMapping.put(IProperties.bindTarget, IProperties.bindTarget);

		return attributeMapping;
	}

	@Override
	protected CompositeMap createContext(BuilderSession session) {
		return super.createContext(session);
	}

	public void actionEvent(String event, BuilderSession session) {
		if (IProperties.FORM_TOOLBAR_CHILDREN.equals(event)) {
			CompositeMap currentModel = session.getCurrentModel();
			CompositeMap childByAttrib = currentModel.getChildByAttrib(
					KEYS.CONTAINMENT_LIST, IProperties.PROPERTYE_ID,
					IProperties.QUERY_FORM_TOOLBAR_CHILDREN);
			List<?> childs = childByAttrib.getChildsNotNull();
			for (Object object : childs) {
				if (object instanceof CompositeMap) {
					String buildComponent = session
							.buildComponent((CompositeMap) object);
					session.appendResultln(buildComponent);
				}
			}
		}
		if (IProperties.FORM_BODY_CHILDREN.equals(event)) {
			CompositeMap currentModel = session.getCurrentModel();
			CompositeMap formBody = currentModel.getChildByAttrib(
					IProperties.FORM_BODY, IProperties.PROPERTYE_ID,
					IProperties.QUERY_FORM_BODY);
			CompositeMap childByAttrib = formBody.getChildByAttrib(
					IProperties.PROPERTYE_ID, IProperties.COMPONENT_CHILDREN);
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

package aurora.plugin.source.gen.builders;

import java.util.List;

import uncertain.composite.CompositeMap;
import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.ModelMapParser;

public class WorkflowBuilder extends DefaultSourceBuilder {

	@Override
	public void buildContext(BuilderSession session) {
		// do nothing;
	}

	public void actionEvent(String event, BuilderSession session) {
		if ("workflow_head_model_pk".equals(event)) {
			CompositeMap headDS = getHeadDS(session);
			String string = headDS.getString("model", "");
			CompositeMap model = session.getModel();
			ModelMapParser mmp = session.createModelMapParser(model);
			// ModelMapParser mmp = new ModelMapParser(model);
			CompositeMap modelMap = mmp.loadModelMap(string);
			CompositeMap child = modelMap.getChild("primary-key");
			CompositeMap child2 = child.getChild("pk-field");
			String r = child2.getString("name", "");
			session.appendResult(r);
		}
		if ("workflow_head_ds_id".equals(event)) {
			session.appendResult(getHeadDSID(session));
		}
		if ("is_workflow_head_ds".equals(event)) {
			CompositeMap headDS = getHeadDS(session);
			CompositeMap currentContext = session.getCurrentContext();
			String string = currentContext.getString("markid", "");
			if (string.equals(headDS.getString("markid", ""))) {
				currentContext.put("is_workflow_head_ds", true);
			}
		}
		// workflow_head_model_pk
		// workflow_head_ds_id
		// ${action("is_workflow_head_ds")}
		// <#if context.is_workflow_head_ds??>
	}

	private String getHeadDSID(BuilderSession session) {
		CompositeMap model = session.getModel();
		// ModelMapParser mmp = new ModelMapParser(model);
		ModelMapParser mmp = session.createModelMapParser(model);
		List<CompositeMap> components = mmp
				.getComponents("inner_buttonclicker");
		for (CompositeMap b : components) {
			String string = b.getString("button_click_actionid", "");
			if ("custom".equalsIgnoreCase(string)) {
				String buttonTargetDatasetID = mmp.getButtonTargetDatasetID(b
						.getParent());
				return buttonTargetDatasetID;
			}
		}
		return "";
	}

	private CompositeMap getHeadDS(BuilderSession session) {
		CompositeMap model = session.getModel();

		// ModelMapParser mmp = new ModelMapParser(model);
		ModelMapParser mmp = session.createModelMapParser(model);
		List<CompositeMap> components = mmp
				.getComponents("inner_buttonclicker");
		for (CompositeMap b : components) {
			String string = b.getString("button_click_actionid", "");
			if ("custom".equalsIgnoreCase(string)) {
				CompositeMap childByAttrib = b.getChildByAttrib("propertye_id",
						"button_click_target_component");
				if (childByAttrib != null) {
					String refID = childByAttrib.getString("markid", "");
					CompositeMap map = mmp.getComponentByID(refID);
					CompositeMap child = map.getChildByAttrib("propertye_id",
							"i_dataset_delegate");
					return child;
				}
			}
		}
		return null;
	}

}

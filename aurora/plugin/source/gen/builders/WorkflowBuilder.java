package aurora.plugin.source.gen.builders;

import java.util.List;

import uncertain.composite.CompositeMap;
import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.ModelMapParser;
import aurora.plugin.source.gen.screen.model.properties.IProperties;

public class WorkflowBuilder extends DefaultSourceBuilder {

	@Override
	public void buildContext(BuilderSession session) {
		// do nothing;
	}

	public void actionEvent(String event, BuilderSession session) {
		if (IProperties.WORKFLOW_HEAD_MODEL_PK.equals(event)) {
			CompositeMap headDS = getHeadDS(session);
			if(headDS == null){
				return;
			}
			String string = headDS.getString(IProperties.model, "");
			CompositeMap model = session.getModel();
			ModelMapParser mmp = session.createModelMapParser(model);
			// ModelMapParser mmp = new ModelMapParser(model);
			CompositeMap modelMap = mmp.loadModelMap(string);
			CompositeMap child = modelMap.getChild(IProperties.PRIMARY_KEY);
			CompositeMap child2 = child.getChild(IProperties.PK_FIELD);
			String r = child2.getString(IProperties.name, "");
			session.appendResult(r);
		}
		if (IProperties.WORKFLOW_HEAD_DS_ID.equals(event)) {
			session.appendResult(getHeadDSID(session));
		}
		if (IProperties.IS_WORKFLOW_HEAD_DS.equals(event)) {
			CompositeMap headDS = getHeadDS(session);
			CompositeMap currentContext = session.getCurrentContext();
			String string = currentContext.getString(IProperties.MARKID, "");
			if (headDS != null && string.equals(headDS.getString(IProperties.MARKID, ""))) {
				currentContext.put(IProperties.IS_WORKFLOW_HEAD_DS, true);
				CompositeMap lineDS = getLineDS(session);
				currentContext.put(IProperties.NEED_SUBMIT_URL, lineDS != null);
				if (lineDS != null) {
					currentContext.put(IProperties.LINE_MODEL,
							lineDS.getString(IProperties.model, ""));
					currentContext.put(IProperties.BINDED_NAME,
							lineDS.getString(IProperties.bindName, ""));
				}
			}
		}
	}

	private CompositeMap getLineDS(BuilderSession session) {
		CompositeMap model = session.getModel();
		ModelMapParser mmp = session.createModelMapParser(model);
		List<CompositeMap> datasets = mmp.getDatasets();
		CompositeMap currentContext = session.getCurrentContext();
		String head_id = currentContext.getString(IProperties.DS_ID, "");
		for (CompositeMap compositeMap : datasets) {
			if (head_id.equals(compositeMap.getString(IProperties.bindTarget, ""))) {
				return compositeMap;
			}
		}
		return null;
	}

	private String getHeadDSID(BuilderSession session) {
//		CompositeMap model = session.getModel();
//		// ModelMapParser mmp = new ModelMapParser(model);
//		ModelMapParser mmp = session.createModelMapParser(model);
//		List<CompositeMap> components = mmp
//				.getComponents("inner_buttonclicker");
//		for (CompositeMap b : components) {
//			String string = b.getString("button_click_actionid", "");
//			if ("custom".equalsIgnoreCase(string)) {
//				String buttonTargetDatasetID = mmp.getButtonTargetDatasetID(b
//						.getParent());
//				return buttonTargetDatasetID;
//			}
//		}
		CompositeMap headDS = this.getHeadDS(session);
		if(headDS!=null)
			return headDS.getString(IProperties.DS_ID, "");
		return "";
	}

	private CompositeMap getHeadDS(BuilderSession session) {
		CompositeMap model = session.getModel();
		ModelMapParser mmp = session.createModelMapParser(model);
		List<CompositeMap> components = mmp
				.getComponents(IProperties.INNER_BUTTONCLICKER);
		for (CompositeMap b : components) {
			String string = b.getString(IProperties.BUTTON_CLICK_ACTIONID, "");
			if (IProperties.CUSTOM.equalsIgnoreCase(string)) {
				CompositeMap childByAttrib = b.getChildByAttrib(IProperties.PROPERTYE_ID,
						IProperties.BUTTON_CLICK_TARGET_COMPONENT);
				if (childByAttrib != null) {
					String refID = childByAttrib.getString(IProperties.MARKID, "");
					CompositeMap map = mmp.getComponentByID(refID);
					CompositeMap child = map.getChildByAttrib(IProperties.PROPERTYE_ID,
							IProperties.I_DATASET_DELEGATE);
					return child;
				}
			}
		}
		return getFirstNoBindResultDataset(session);
	}

	private CompositeMap getFirstNoBindResultDataset(BuilderSession session) {
		CompositeMap model = session.getModel();
		ModelMapParser mmp = session.createModelMapParser(model);
		List<CompositeMap> components = mmp.getComponents(IProperties.RESULTDATASET);
		for (CompositeMap d : components) {
			if("".equals(d.getString(IProperties.bindName, ""))){
				return d;
			}
		}
		return null;
	}

}

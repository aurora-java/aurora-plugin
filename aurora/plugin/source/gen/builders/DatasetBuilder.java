package aurora.plugin.source.gen.builders;

import java.util.List;
import java.util.Map;

import uncertain.composite.CompositeMap;
import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.ModelMapParser;
import aurora.plugin.source.gen.SourceGenManager;

public class DatasetBuilder extends DefaultSourceBuilder {

	@Override
	public void buildContext(BuilderSession session) {
		// super.buildContext(session);
	}

	public void actionEvent(String event, BuilderSession session) {
		if ("dataset".equals(event)) {
			CompositeMap currentContext = session.getCurrentContext();
			List<?> childs = currentContext.getChilds();
			if (childs == null)
				return;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < childs.size(); i++) {
				Object c = childs.get(i);
				if (c instanceof CompositeMap) {
					if ("dataset"
							.equalsIgnoreCase(((CompositeMap) c).getName())) {
						SourceGenManager sourceGenManager = session
								.getSourceGenManager();
						BuilderSession copy = session.getCopy();
						String type = ((CompositeMap) c).getString(
								"component_type", "");
						((CompositeMap) c).put("ds_type", type);
						((CompositeMap) c).put("component_type", "dataset");
						copy.setCurrentContext((CompositeMap) c);
						String s = sourceGenManager.bindTemplate(copy);
						sb.append(s);
					}
				}

			}
			session.appendResultln(sb.toString());
		}
		if ("datasetfields".equals(event)) {
			CompositeMap currentContext = session.getCurrentContext();
			String markid = currentContext.getString("markid", "");
			CompositeMap currentModel = session.getCurrentModel();
			ModelMapParser mmp = session.getSourceGenManager()
					.createModelMapParser(currentModel);
			CompositeMap datasetModel = mmp.getComponentByID(markid);
			List<CompositeMap> datasetFields = mmp
					.getDatasetFields(datasetModel);
			// List<?> childsNotNull = datasetModel.getChildsNotNull();
			for (Object object : datasetFields) {
				if (object instanceof CompositeMap) {
					BuilderSession copy = session.getCopy();
					// copy.setCurrentContext((CompositeMap) object);
					String bindTemplate = session.getSourceGenManager()
							.buildComponent(copy, (CompositeMap) object);
					// .bindTemplate(copy);
					session.appendResultln(bindTemplate);
				}
			}
		}
		if ("build_head_ds".equals(event)
				&& isDatasetType(session, "resultdataset")) {
			CompositeMap context = session.getCurrentContext();
			String bt = context.getString("bindTarget", "");
			if ("".equals(bt)) {
				context.put("is_head_ds", "".equals(bt));
				context.put("autoCreate", isAutoCreate(session));
				CompositeMap lineds = getLineDS(session);
				if (lineds != null) {
					context.put("need_master_detail_submit_url", lineds != null);
					context.put("line_model", lineds.getString("model", ""));
					context.put("binded_name", lineds.getString("bindName", ""));
				}
				if (session.getConfig("be_opened_from_another") != null) {
					context.put("need_auto_query_url", true);
				}
			}
		}
	}

	private boolean isAutoCreate(BuilderSession session) {
		CompositeMap model = session.getModel();
		ModelMapParser mmp = session.createModelMapParser(model);
		String markid = session.getCurrentContext().getString("markid", "");
		String type = mmp.getComponentByID(markid).getParent()
				.getString("component_type", "");
		return "grid".equals(type) == false;
	}

	private boolean isDatasetType(BuilderSession session, String type) {
		return session.getCurrentContext().getString("ds_type", "")
				.equals(type);
	}

	private CompositeMap getLineDS(BuilderSession session) {
		CompositeMap model = session.getModel();
		ModelMapParser mmp = session.createModelMapParser(model);
		String ds_id = session.getCurrentContext().getString("ds_id", "");
		List<CompositeMap> datasets = mmp.getDatasets();
		for (CompositeMap ds : datasets) {
			String bt = ds.getString("bindTarget", "");
			if (ds_id.equals(bt)) {
				return ds;
			}
		}
		return null;
	}

	private boolean isMasterDetail(BuilderSession session) {
		String sbt = session.getCurrentContext().getString("bindTarget", "");
		if ("".equals(sbt) == false)
			return false;
		CompositeMap model = session.getModel();
		ModelMapParser mmp = session.createModelMapParser(model);
		String ds_id = session.getCurrentContext().getString("ds_id", "");
		List<CompositeMap> datasets = mmp.getDatasets();
		for (CompositeMap ds : datasets) {
			String bt = ds.getString("bindTarget", "");
			if (ds_id.equals(bt)) {
				return true;
			}
		}
		return false;
	}

	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put("lookupCode", "lookupCode");
		attributeMapping.put("model", "model");
		attributeMapping.put("query_ds", "queryDataSet");
		attributeMapping.put("bindName", "bindName");
		attributeMapping.put("bindTarget", "bindTarget");
		attributeMapping.put("queryUrl", "queryUrl");
		return attributeMapping;
	}
}

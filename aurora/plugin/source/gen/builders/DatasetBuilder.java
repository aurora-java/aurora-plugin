package aurora.plugin.source.gen.builders;

import java.util.List;
import java.util.Map;

import uncertain.composite.CompositeMap;
import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.ModelMapParser;
import aurora.plugin.source.gen.SourceGenManager;
import aurora.plugin.source.gen.screen.model.properties.IProperties;

public class DatasetBuilder extends DefaultSourceBuilder {

	@Override
	public void buildContext(BuilderSession session) {
		// super.buildContext(session);
	}

	public void actionEvent(String event, BuilderSession session) {
		if (IProperties.DATASET.equals(event)) {
			CompositeMap currentContext = session.getCurrentContext();
			List<?> childs = currentContext.getChilds();
			if (childs == null)
				return;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < childs.size(); i++) {
				Object c = childs.get(i);
				if (c instanceof CompositeMap) {
					if (IProperties.DATASET.equalsIgnoreCase(((CompositeMap) c)
							.getName())) {
						SourceGenManager sourceGenManager = session
								.getSourceGenManager();
						BuilderSession copy = session.getCopy();
						String type = ((CompositeMap) c).getString(
								IProperties.COMPONENT_TYPE, "");
						((CompositeMap) c).put(IProperties.DS_TYPE, type);
						((CompositeMap) c).put(IProperties.COMPONENT_TYPE,
								IProperties.DATASET);
						copy.setCurrentContext((CompositeMap) c);
						String s = sourceGenManager.bindTemplate(copy);
						sb.append(s);
					}
				}
			}
			session.appendResultln(sb.toString());
		}
		if (IProperties.DATASETFIELDS.equals(event)) {
			CompositeMap currentContext = session.getCurrentContext();
			String markid = currentContext.getString(IProperties.MARKID, "");
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
		if (IProperties.BUILD_HEAD_DS.equals(event)
				&& isDatasetType(session, IProperties.RESULTDATASET)) {
			CompositeMap context = session.getCurrentContext();
			String bt = context.getString(IProperties.bindTarget, "");
			if ("".equals(bt)) {
				context.put(IProperties.IS_HEAD_DS, "".equals(bt));
				context.put(IProperties.autoCreate, isAutoCreate(session));
				CompositeMap lineds = getLineDS(session);
				if (lineds != null) {
					context.put(IProperties.NEED_MASTER_DETAIL_SUBMIT_URL,
							lineds != null);
					context.put(IProperties.LINE_MODEL,
							lineds.getString(IProperties.model, ""));
					context.put(IProperties.BINDED_NAME,
							lineds.getString(IProperties.bindName, ""));
				}
				if (session.getConfig(IProperties.BE_OPENED_FROM_ANOTHER) != null) {
					context.put(IProperties.NEED_AUTO_QUERY_URL, true);
				}
			}
		}
	}

	private boolean isAutoCreate(BuilderSession session) {
		CompositeMap model = session.getModel();
		ModelMapParser mmp = session.createModelMapParser(model);
		String markid = session.getCurrentContext().getString(
				IProperties.MARKID, "");
		String type = mmp.getComponentByID(markid).getParent()
				.getString(IProperties.COMPONENT_TYPE, "");
		return IProperties.GRID.equals(type) == false;
	}

	private boolean isDatasetType(BuilderSession session, String type) {
		return session.getCurrentContext().getString(IProperties.DS_TYPE, "")
				.equals(type);
	}

	private CompositeMap getLineDS(BuilderSession session) {
		CompositeMap model = session.getModel();
		ModelMapParser mmp = session.createModelMapParser(model);
		String ds_id = session.getCurrentContext().getString(IProperties.DS_ID,
				"");
		List<CompositeMap> datasets = mmp.getDatasets();
		for (CompositeMap ds : datasets) {
			String bt = ds.getString(IProperties.bindTarget, "");
			if (ds_id.equals(bt)) {
				return ds;
			}
		}
		return null;
	}

	private boolean isMasterDetail(BuilderSession session) {
		String sbt = session.getCurrentContext().getString(
				IProperties.bindTarget, "");
		if ("".equals(sbt) == false)
			return false;
		CompositeMap model = session.getModel();
		ModelMapParser mmp = session.createModelMapParser(model);
		String ds_id = session.getCurrentContext().getString(IProperties.DS_ID,
				"");
		List<CompositeMap> datasets = mmp.getDatasets();
		for (CompositeMap ds : datasets) {
			String bt = ds.getString(IProperties.bindTarget, "");
			if (ds_id.equals(bt)) {
				return true;
			}
		}
		return false;
	}

	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put(IProperties.lookupCode, IProperties.lookupCode);
		attributeMapping.put(IProperties.model, IProperties.model);
		attributeMapping.put(IProperties.QUERY_DS, IProperties.queryDataSet);
		attributeMapping.put(IProperties.bindName, IProperties.bindName);
		attributeMapping.put(IProperties.bindTarget, IProperties.bindTarget);
		attributeMapping.put(IProperties.queryUrl, IProperties.queryUrl);
		return attributeMapping;
	}
}

package aurora.plugin.source.gen.builders;

import java.util.List;

import uncertain.composite.CompositeMap;
import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.ModelMapParser;
import aurora.plugin.source.gen.screen.model.properties.ComponentInnerProperties;

public class ViewBuilder extends DefaultSourceBuilder {

	@Override
	public void buildContext(BuilderSession session) {
		super.buildContext(session);
		buildLinkContext(session);
		buildDatasetsContext(session);
		calBindTarget(session);
	}

	private void buildDatasetsContext(BuilderSession session) {
		CompositeMap currentContext = session.getCurrentContext();
		ModelMapParser mmp = getModelMapParser(session);
		List<CompositeMap> datasets = mmp.getDatasets();
		for (CompositeMap ds : datasets) {
			String ds_id = genDatasetID(ds, session);
			ds.put("ds_id", ds_id);
		}
		for (CompositeMap ds : datasets) {
			String type = ds.getString("component_type", "");
			if ("querydataset".equalsIgnoreCase(type)) {
				ds.put("autoCreate", true);
			}
			if ("resultdataset".equalsIgnoreCase(type)) {
				CompositeMap query = ds.getChildByAttrib("propertye_id",
						"dataset_query_container");
				if (query != null) {
					CompositeMap container = mmp.getComponentByID(query
							.getString("markid", ""));
					if (container == null)
						return;
					CompositeMap cds = container.getChildByAttrib(
							"propertye_id", "i_dataset_delegate");
					String fds_type = cds.getString("component_type", "");
					if ("querydataset".equalsIgnoreCase(fds_type)) {
						ds.put("query_ds", cds.getString("ds_id", ""));
					} else if ("resultdataset".equalsIgnoreCase(fds_type)) {
						ds.put("bindName", ds.getString("ds_id", ""));
						ds.put("bindTarget", cds.getString("ds_id", ""));
					}
				} else {
					ds.put("autoQuery", true);
					ds.put("pageSize", null);
				}
			}
		}
		for (CompositeMap ds : datasets) {
			List<CompositeMap> datasetFields = mmp.getDatasetFields(ds);
			CompositeMap newDS = (CompositeMap) ds.clone();
			newDS.getChildsNotNull().clear();
			newDS.setPrefix("a");
			newDS.setName("dataset");
			for (CompositeMap field : datasetFields) {
				field.put("field_name", field.getParent().getString("name", ""));
				String fieldType = field.getParent().getString(
						"component_type", "");
				if ("gridcolumn".equals(fieldType)) {
					fieldType = field.getParent().getString("editor", "");
				}
				field.put("field_type", fieldType);
				if (isLov(field)) {
					genLovDSField(session, field);
				}
				if (isCombo(field)) {
					genComboDSField(session, field);
				}

				CompositeMap clone = (CompositeMap) field.clone();
				clone.setName("field");
				clone.setPrefix("a");
				newDS.addChild((CompositeMap) clone);
			}
			currentContext.addChild(newDS);
		}

	}

	ModelMapParser getModelMapParser(BuilderSession session) {
		CompositeMap currentModel = session.getCurrentModel();
		ModelMapParser mmp = new ModelMapParser(currentModel);
		return mmp;
	}

	public void genLovDSField(BuilderSession session, CompositeMap field) {
		ModelMapParser mmp = getModelMapParser(session);
		// List<CompositeMap> lovMaps = mmp.getLovMaps(field);
		// field.put("lovService", field.getString("options", ""));
		// if (lovMaps != null) {
		// mmp.bindMapping(field, lovMaps);
		// }
		String[] models = mmp.findComboFieldOption(field);
		field.put("displayField", mmp.getComboDisplayField(models, field));
		field.put("valueField", mmp.getComboValueField(models, field));
		field.put("lovService", models[0]);
	}

	public void genComboDSField(BuilderSession session, CompositeMap field) {
		ModelMapParser mmp = getModelMapParser(session);
		// String model = field.getString("options", "");
		String[] models = mmp.findComboFieldOption(field);
		String model = models[0];
		String lookupCode = models[1];
		// if ("".equals(model)) {
		// lookupCode = field.getString("lookupCode", "");
		// }
		if ("".equals(model) == false || "".equals(lookupCode) == false) {
			CompositeMap createComboDatasetMap = createComboDatasetMap(model,
					lookupCode, session);
			session.getCurrentContext().addChild(createComboDatasetMap);
			field.put("options", createComboDatasetMap.getString("ds_id", ""));
		}
		field.put("displayField", mmp.getComboDisplayField(models, field));
		field.put("valueField", mmp.getComboValueField(models, field));
	}

	public boolean isLov(CompositeMap field) {
		String type = field.getParent().getString("component_type", "");
		if ("gridcolumn".equalsIgnoreCase(type)) {
			type = field.getParent().getString("editor", "");
		}
		return "lov".equalsIgnoreCase(type);
	}

	public boolean isCombo(CompositeMap field) {
		String type = field.getParent().getString("component_type", "");
		if ("gridcolumn".equalsIgnoreCase(type)) {
			type = field.getParent().getString("editor", "");
		}
		return "comboBox".equalsIgnoreCase(type);
	}

	private void calBindTarget(BuilderSession session) {
		ModelMapParser mmp = getModelMapParser(session);
		List<CompositeMap> datasets = mmp.getDatasets();
		for (CompositeMap ds : datasets) {
			if (ds.getParent().getString("component_type", "").equals("grid")) {
				ds.getParent().put("bindTarget", ds.getString("ds_id", ""));
			}
			List<CompositeMap> datasetFields = mmp.getDatasetFields(ds);
			for (CompositeMap field : datasetFields) {
				String ds_id = ds.getString("ds_id", "");
				field.getParent().put("bindTarget", ds_id);
				// parser.debug(field.getParent().getParent().toXML());
			}
		}

	}

	private CompositeMap createComboDatasetMap(String model, String lookupCode,
			BuilderSession session) {
		CompositeMap ds = new CompositeMap("dataset");
		ds.setPrefix("a");
		String id = genDatasetID(ds, session);
		ds.put("ds_id", id);
		ds.put("autoCreate", true);
		ds.put("component_type", "combodataset");
		if ("".equals(model) == false)
			ds.put("model", model);
		ds.put("loadData", true);
		if ("".equals(lookupCode) == false)
			ds.put("lookupCode", lookupCode);
		return ds;
	}

	private String genDatasetID(CompositeMap ds, BuilderSession session) {
		return session.getIDGenerator().genDatasetID(ds);
	}

	private CompositeMap genLinkContext(CompositeMap map, BuilderSession session) {
		String openpath = map.getString(ComponentInnerProperties.OPEN_PATH, "");
		if ("".equals(openpath))
			return null;
		CompositeMap link = new CompositeMap("link");
		String id = genLinkID(link, session);
		link.put("url", openpath);
		link.put("id", id);
		return link;
	}

	private String genLinkID(CompositeMap link, BuilderSession session) {
		session.getIDGenerator().genLinkID("f");
		return "linklink";
	}

	private void buildLinkContext(BuilderSession session) {
		ModelMapParser mmp = getModelMapParser(session);
		CompositeMap currentContext = session.getCurrentContext();
		List<CompositeMap> buttons = mmp.getComponents("button");
		for (CompositeMap button : buttons) {
			CompositeMap clicker = button.getChild("inner_buttonclicker");
			if (clicker != null) {
				String id = clicker
						.getString(ComponentInnerProperties.BUTTON_CLICK_ACTIONID);
				if ("open".equals(id)) {
					CompositeMap link = genLinkContext(clicker, session);
					currentContext.addChild(link);
					clicker.put("link_id", link.getString("id", ""));
				}
			}
		}
		List<CompositeMap> renderers = mmp.getComponents("renderer");
		for (CompositeMap renderer : renderers) {
			String type = renderer.getString(
					ComponentInnerProperties.RENDERER_TYPE, "");
			if ("PAGE_REDIRECT".equals(type)) {
				CompositeMap link = genLinkContext(renderer, session);
				currentContext.addChild(link);
				renderer.put("link_id", link.getString("id", ""));
			}
		}
	}

	public void actionEvent(String event, BuilderSession session) {
		if ("children".equals(event)
				&& "view".equalsIgnoreCase(session.getCurrentModel().getString(
						ComponentInnerProperties.COMPONENT_TYPE, ""))) {
			buildChildComponent(session);
		}
	}
}

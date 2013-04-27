package aurora.plugin.source.gen.builders;

import java.util.List;

import uncertain.composite.CompositeMap;
import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.ModelMapParser;
import aurora.plugin.source.gen.Util;
import aurora.plugin.source.gen.screen.model.properties.ComponentInnerProperties;
import aurora.plugin.source.gen.screen.model.properties.IProperties;

public class ViewBuilder extends DefaultSourceBuilder {

	private static final String A = "a";

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
			ds.put(IProperties.DS_ID, ds_id);
		}
		for (CompositeMap ds : datasets) {
			String type = ds.getString(IProperties.COMPONENT_TYPE, "");
			if (IProperties.QUERYDATASET.equalsIgnoreCase(type)) {
				ds.put(IProperties.autoCreate, true);
			}
			if (IProperties.RESULTDATASET.equalsIgnoreCase(type)) {
				CompositeMap query = ds.getChildByAttrib(
						IProperties.PROPERTYE_ID,
						IProperties.DATASET_QUERY_CONTAINER);
				if (query != null) {
					CompositeMap container = mmp.getComponentByID(query
							.getString(IProperties.MARKID, ""));
					if (container == null)
						return;
					CompositeMap cds = container.getChildByAttrib(
							IProperties.PROPERTYE_ID,
							IProperties.I_DATASET_DELEGATE);
					String fds_type = cds.getString(IProperties.COMPONENT_TYPE,
							"");
					if (IProperties.QUERYDATASET.equalsIgnoreCase(fds_type)) {
						ds.put(IProperties.QUERY_DS,
								cds.getString(IProperties.DS_ID, ""));
					} else if (IProperties.RESULTDATASET
							.equalsIgnoreCase(fds_type)) {
						ds.put(IProperties.bindName,
								ds.getString(IProperties.DS_ID, ""));
						ds.put(IProperties.bindTarget,
								cds.getString(IProperties.DS_ID, ""));
					}
				} else {
					ds.put(IProperties.autoQuery, true);
					ds.put(IProperties.pageSize, null);
				}
			}
		}
		for (CompositeMap ds : datasets) {
			List<CompositeMap> datasetFields = mmp.getDatasetFields(ds);
			CompositeMap newDS = (CompositeMap) ds.clone();
			newDS.getChildsNotNull().clear();
			newDS.setPrefix(A);
			newDS.setName(IProperties.DATASET);
			for (CompositeMap field : datasetFields) {
				field.put(IProperties.FIELD_NAME,
						field.getParent().getString(IProperties.name, ""));
				String fieldType = field.getParent().getString(
						IProperties.COMPONENT_TYPE, "");
				if (IProperties.GRIDCOLUMN.equals(fieldType)) {
					fieldType = field.getParent().getString(IProperties.editor,
							"");
				}
				field.put(IProperties.FIELD_TYPE, fieldType);
				if (isLov(field)) {
					genLovDSField(session, field);
				}
				if (isCombo(field)) {
					genComboDSField(session, field);
				}

				CompositeMap clone = (CompositeMap) field.clone();
				clone.setName(IProperties.FIELD);
				clone.setPrefix(A);
				newDS.addChild((CompositeMap) clone);
			}
			currentContext.addChild(newDS);
		}

	}

	ModelMapParser getModelMapParser(BuilderSession session) {
		CompositeMap currentModel = session.getCurrentModel();
		return session.createModelMapParser(currentModel);
		// ModelMapParser mmp = new ModelMapParser(currentModel);
		// return mmp;
	}

	public void genLovDSField(BuilderSession session, CompositeMap field) {
		ModelMapParser mmp = getModelMapParser(session);
		// List<CompositeMap> lovMaps = mmp.getLovMaps(field);
		// field.put("lovService", field.getString("options", ""));
		// if (lovMaps != null) {
		// mmp.bindMapping(field, lovMaps);
		// }
		String[] models = mmp.findComboFieldOption(field);
		CompositeMap lovservice = getLovServiceMap(session, field);
		String lovservice_options = lovservice.getString(
				IProperties.LOVSERVICE_OPTIONS, "");
		String model = "".equals(lovservice_options) ? models[0]
				: lovservice_options;
		models[0] = model;
		field.put(IProperties.displayField,
				mmp.getComboDisplayField(models, field));
		field.put(IProperties.valueField, mmp.getComboValueField(models, field));
		field.put(IProperties.lovService, model);
	}

	private CompositeMap getLovServiceMap(BuilderSession session,
			CompositeMap field) {
		CompositeMap innerLovService = field.getChildByAttrib(
				IProperties.COMPONENT_TYPE, IProperties.INNER_TYPE_LOV_SERVICE);
		return innerLovService;
	}

	public void genComboDSField(BuilderSession session, CompositeMap field) {
		ModelMapParser mmp = getModelMapParser(session);
		// String model = field.getString("options", "");
		String[] models = mmp.findComboFieldOption(field);
		CompositeMap lovservice = getLovServiceMap(session, field);
		String lovservice_options = lovservice.getString(
				IProperties.LOVSERVICE_OPTIONS, "");
		String model = "".equals(lovservice_options) ? models[0]
				: lovservice_options;
		String lookupCode = models[1];
		// if ("".equals(model)) {
		// lookupCode = field.getString("lookupCode", "");
		// }
		if ("".equals(model) == false || "".equals(lookupCode) == false) {
			CompositeMap createComboDatasetMap = createComboDatasetMap(model,
					lookupCode, session);
			session.getCurrentContext().addChild(createComboDatasetMap);
			field.put(IProperties.options,
					createComboDatasetMap.getString(IProperties.DS_ID, ""));
		}
		models[0] = model;
		field.put(IProperties.displayField,
				mmp.getComboDisplayField(models, field));
		field.put(IProperties.valueField, mmp.getComboValueField(models, field));
	}

	public boolean isLov(CompositeMap field) {
		String type = field.getParent().getString(IProperties.COMPONENT_TYPE,
				"");
		if (IProperties.GRIDCOLUMN.equalsIgnoreCase(type)) {
			type = field.getParent().getString(IProperties.editor, "");
		}
		return IProperties.LOV.equalsIgnoreCase(type);
	}

	public boolean isCombo(CompositeMap field) {
		String type = field.getParent().getString(IProperties.COMPONENT_TYPE,
				"");
		if (IProperties.GRIDCOLUMN.equalsIgnoreCase(type)) {
			type = field.getParent().getString(IProperties.editor, "");
		}
		return IProperties.COMBO_BOX.equalsIgnoreCase(type);
	}

	private void calBindTarget(BuilderSession session) {
		ModelMapParser mmp = getModelMapParser(session);
		List<CompositeMap> datasets = mmp.getDatasets();
		for (CompositeMap ds : datasets) {
			if (ds.getParent().getString(IProperties.COMPONENT_TYPE, "")
					.equals(IProperties.GRID)) {
				ds.getParent().put(IProperties.bindTarget,
						ds.getString(IProperties.DS_ID, ""));
			}
			List<CompositeMap> datasetFields = mmp.getDatasetFields(ds);
			for (CompositeMap field : datasetFields) {
				String ds_id = ds.getString(IProperties.DS_ID, "");
				field.getParent().put(IProperties.bindTarget, ds_id);
				// parser.debug(field.getParent().getParent().toXML());
			}
		}

	}

	private CompositeMap createComboDatasetMap(String model, String lookupCode,
			BuilderSession session) {
		CompositeMap ds = new CompositeMap(IProperties.DATASET);
		ds.setPrefix(A);
		String id = genDatasetID(ds, session);
		ds.put(IProperties.DS_ID, id);
		ds.put(IProperties.autoCreate, true);
		ds.put(IProperties.COMPONENT_TYPE, IProperties.COMBODATASET);
		if ("".equals(model) == false)
			ds.put(IProperties.model, model);
		ds.put(IProperties.loadData, true);
		if ("".equals(lookupCode) == false)
			ds.put(IProperties.lookupCode, lookupCode);
		return ds;
	}

	private String genDatasetID(CompositeMap ds, BuilderSession session) {
		return session.getIDGenerator().genDatasetID(ds);
	}

	private CompositeMap genLinkContext(CompositeMap map,
			BuilderSession session, String openpath) {
		CompositeMap link = new CompositeMap(IProperties.LINK);
		String id = genLinkID(link, session);
		link.put(IProperties.url, "${/request/@context_path}/" + openpath);
		link.put(IProperties.COMPONENT_TYPE, IProperties.LINK);
		link.put(IProperties.id, id);
		return link;
	}

	private String genLinkID(CompositeMap link, BuilderSession session) {
		return session.getIDGenerator().genLinkID(IProperties.LINK);
	}

	private void buildLinkContext(BuilderSession session) {
		ModelMapParser mmp = getModelMapParser(session);
		CompositeMap currentContext = session.getCurrentContext();
		List<CompositeMap> buttons = mmp.getComponents(IProperties.BUTTON);
		for (CompositeMap button : buttons) {
			CompositeMap clicker = button
					.getChild(IProperties.INNER_BUTTONCLICKER);
			if (clicker != null) {
				String id = clicker
						.getString(ComponentInnerProperties.BUTTON_CLICK_ACTIONID);
				if (IProperties.OPEN.equals(id)) {
					String openpath = clicker.getString(
							ComponentInnerProperties.OPEN_PATH, "");
					if (openpath.endsWith(".uip")) {
						openpath = openpath.replaceAll(".uip", ".screen");
					}
					CompositeMap link = genLinkContext(clicker, session,
							openpath);
					currentContext.addChild(link);
					clicker.put(IProperties.LINK_ID,
							link.getString(IProperties.id, ""));
				}
			}
		}
		List<CompositeMap> renderers = mmp.getComponents(IProperties.renderer);
		for (CompositeMap renderer : renderers) {
			String type = renderer.getString(
					ComponentInnerProperties.RENDERER_TYPE, "");
			if (IProperties.PAGE_REDIRECT.equals(type)) {
				String openpath = renderer.getString(
						ComponentInnerProperties.OPEN_PATH, "");
				if (openpath.endsWith(".uip")) {
					openpath = Util.getNewLinkFilePath(openpath,
							"" + session.getConfig(IProperties.FILE_NAME));
				}
				CompositeMap link = genLinkContext(renderer, session, openpath);
				currentContext.addChild(link);
				renderer.put(IProperties.LINK_ID,
						link.getString(IProperties.id, ""));
			}
		}
	}

	public void actionEvent(String event, BuilderSession session) {
		if (IProperties.EVENT_CHILDREN.equals(event)
				&& IProperties.VIEW
						.equalsIgnoreCase(session.getCurrentModel().getString(
								ComponentInnerProperties.COMPONENT_TYPE, ""))) {
			buildChildComponent(session);
		}
	}
}

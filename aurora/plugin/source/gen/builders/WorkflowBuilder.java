package aurora.plugin.source.gen.builders;

import java.util.List;

import uncertain.composite.CompositeMap;
import uncertain.composite.IterationHandle;
import uncertain.composite.XMLOutputter;
import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.ButtonScriptGenerator;
import aurora.plugin.source.gen.ModelMapParser;
import aurora.plugin.source.gen.RendererScriptGenerator;
import aurora.plugin.source.gen.screen.model.properties.ComponentInnerProperties;

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
			ModelMapParser mmp = new ModelMapParser(model);
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
			if(string.equals(headDS.getString("markid", ""))){
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
		ModelMapParser mmp = new ModelMapParser(model);
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
		ModelMapParser mmp = new ModelMapParser(model);
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

	public void genScripts(BuilderSession session) {
		CompositeMap currentModel = session.getCurrentModel();
		ModelMapParser mmp = new ModelMapParser(currentModel);
		StringBuilder scripts = new StringBuilder();
		List<CompositeMap> buttons = mmp.getComponents("button");
		ButtonScriptGenerator bsg = new ButtonScriptGenerator(session);
		for (CompositeMap button : buttons) {
			CompositeMap clicker = button.getChild("inner_buttonclicker");
			String functionName = session.getIDGenerator().genID(
					"functionName", 0);
			button.put("click", functionName);
			String datasetID = mmp.getButtonTargetDatasetID(button);
			if (clicker != null) {
				String id = clicker.getString(
						ComponentInnerProperties.BUTTON_CLICK_ACTIONID, "");
				if ("custom".equalsIgnoreCase(id)) {
					String s = clicker.getChild("function").getText();
					functionName = mmp.getFunctionName(s);
					button.put("click", functionName);
					scripts.append(s);
				}
				if ("query".equalsIgnoreCase(id)) {
					String s = bsg.searchScript(functionName, datasetID);
					scripts.append(s);
				}
				if ("save".equalsIgnoreCase(id)) {
					String s = bsg.saveScript(functionName, datasetID);
					scripts.append(s);
				}
				if ("reset".equalsIgnoreCase(id)) {
					String s = bsg.resetScript(functionName, datasetID);
					scripts.append(s);
				}
				if ("open".equalsIgnoreCase(id)) {
					String link_id = clicker.getString("link_id", "");
					String parameters = mmp.getButtonOpenParameters(button);
					String s = bsg.openScript(functionName, link_id);
					scripts.append(s);
				}
				if ("close".equalsIgnoreCase(id)) {
					String windowID = clicker.getString("closeWindowID", "");
					String s = bsg.closeScript(functionName, windowID);
					scripts.append(s);
				}
			}
		}

		List<CompositeMap> renderers = mmp.getComponents("renderer");
		for (CompositeMap renderer : renderers) {
			String type = renderer.getString(
					ComponentInnerProperties.RENDERER_TYPE, "");
			if ("INNER_FUNCTION".equals(type)) {
				renderer.put("renderer", renderer.getString("functionname", ""));
			}
			if ("PAGE_REDIRECT".equals(type)) {
				String functionName = session.getIDGenerator().genID(
						"functionName", 0);
				String[] parametersDetail = mmp.getParametersDetail(renderer,
						"linkUrl");
				renderer.put("renderer", functionName);
				String linkId = renderer.getString("link_id", "");
				String openFunctionName = session.getIDGenerator().genID(
						"functionName", 0);
				RendererScriptGenerator rsg = new RendererScriptGenerator(
						session);
				String s1 = rsg.openScript(openFunctionName, linkId);
				s1 = rsg.buildOpenScript(s1, parametersDetail);
				String s = rsg.hrefScript(functionName,
						renderer.getString("labelText", ""), openFunctionName,
						"");
				s = rsg.buildHrefScript(s, parametersDetail);
				scripts.append(s);
				scripts.append(s1);
			}
			if ("USER_FUNCTION".equals(type)) {
				String s = renderer.getChild("function").getText();
				String functionName = mmp.getFunctionName(s);
				renderer.put("renderer", functionName);
				scripts.append(s);
			}
		}
		String string = scripts.toString();
		session.appendResult(format(string));

		// var renderers = parser.getComponents('renderer');
		// for ( var i = 0; i < renderers.size(); i++) {
		// var renderer = renderers.get(i);
		//
		// }
	}

	private String format(String s) {
		JSBeautifier bf = new JSBeautifier();
		String prefix = XMLOutputter.DEFAULT_INDENT
				+ XMLOutputter.DEFAULT_INDENT;
		String indent = XMLOutputter.DEFAULT_INDENT + prefix;
		String jsCodeNew = (XMLOutputter.LINE_SEPARATOR + bf.beautify(s,
				bf.opts))
				.replaceAll("\n", XMLOutputter.LINE_SEPARATOR + indent)
				+ XMLOutputter.LINE_SEPARATOR + prefix;
		// if (jsCodeNew.equals(jsCode))
		return jsCodeNew;
	}
}

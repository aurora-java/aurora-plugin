package aurora.plugin.source.gen.builders;

import java.util.List;

import uncertain.composite.CompositeMap;
import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.ButtonScriptGenerator;
import aurora.plugin.source.gen.ModelMapParser;
import aurora.plugin.source.gen.RendererScriptGenerator;

public class ScriptBuilder extends DefaultSourceBuilder {

	@Override
	public void buildContext(BuilderSession session) {
		// do nothing;
	}

	public void actionEvent(String event, BuilderSession session) {
		if ("javascript".equals(event)) {
			genScripts(session);
		}
	}

	public void genScripts(BuilderSession session) {
		CompositeMap currentModel = session.getCurrentModel();
		ModelMapParser mmp = new ModelMapParser(currentModel);
		StringBuilder scripts = new StringBuilder();
		List<CompositeMap> buttons = mmp.getComponents("button");
		ButtonScriptGenerator bsg = new ButtonScriptGenerator(session);
		for (CompositeMap button : buttons) {
			CompositeMap clicker = button.getChild("ButtonClicker");
			String functionName = session.getIDGenerator().genID(
					"functionName", 0);
			button.put("click", functionName);
			String datasetID = mmp.getButtonTargetDatasetID(button);
			if (clicker != null) {
				String id = clicker.getString("id", "");
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
			String type = renderer.getString("renderertype", "");
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
		session.appendResult(scripts.toString());

		// var renderers = parser.getComponents('renderer');
		// for ( var i = 0; i < renderers.size(); i++) {
		// var renderer = renderers.get(i);
		//
		// }
	}
}

package aurora.plugin.source.gen.builders;

import java.util.List;

import uncertain.composite.CompositeMap;
import uncertain.composite.XMLOutputter;
import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.ButtonScriptGenerator;
import aurora.plugin.source.gen.ModelMapParser;
import aurora.plugin.source.gen.RendererScriptGenerator;
import aurora.plugin.source.gen.screen.model.properties.ComponentInnerProperties;
import aurora.plugin.source.gen.screen.model.properties.IProperties;

public class ScriptBuilder extends DefaultSourceBuilder {

	@Override
	public void buildContext(BuilderSession session) {
		// do nothing;
	}

	public void actionEvent(String event, BuilderSession session) {
		if (IProperties.JAVASCRIPT.equals(event)) {
			genScripts(session);
		}
	}

	public void genScripts(BuilderSession session) {
		CompositeMap currentModel = session.getCurrentModel();
		ModelMapParser mmp = session.createModelMapParser(currentModel);
		StringBuilder scripts = new StringBuilder();
		List<CompositeMap> buttons = mmp.getComponents(IProperties.BUTTON);
		ButtonScriptGenerator bsg = new ButtonScriptGenerator(session);
		for (CompositeMap button : buttons) {
			CompositeMap clicker = button
					.getChild(IProperties.INNER_BUTTONCLICKER);
			String functionName = session.getIDGenerator().genID(
					IProperties.FUNCTION_NAME, 0);
			button.put(IProperties.click, functionName);
			String datasetID = mmp.getButtonTargetDatasetID(button);
			if (clicker != null) {
				String id = clicker.getString(
						ComponentInnerProperties.BUTTON_CLICK_ACTIONID, "");
				if (IProperties.CUSTOM.equalsIgnoreCase(id)) {
					CompositeMap child = clicker.getChildByAttrib(
							IProperties.PROPERTYE_ID,
							IProperties.BUTTON_CLICK_FUNCTION);
					if (child != null) {
						String s = child.getText();
						functionName = mmp.getFunctionName(s);
						button.put(IProperties.click, functionName);
						scripts.append(s);
					}
				}
				if (IProperties.QUERY.equalsIgnoreCase(id)) {
					String s = bsg.searchScript(functionName, datasetID);
					scripts.append(s);
				}
				if (IProperties.SAVE.equalsIgnoreCase(id)) {
					String s = bsg.saveScript(functionName, datasetID);
					scripts.append(s);
				}
				if (IProperties.RESET.equalsIgnoreCase(id)) {
					String s = bsg.resetScript(functionName, datasetID);
					scripts.append(s);
				}
				if (IProperties.OPEN.equalsIgnoreCase(id)) {
					String link_id = clicker.getString(IProperties.LINK_ID, "");
					// String parameters = mmp.getButtonOpenParameters(button);
					String s = bsg.openScript(functionName, link_id);
					scripts.append(s);
				}
				if (IProperties.CLOSE.equalsIgnoreCase(id)) {
					String windowID = clicker.getString(
							IProperties.CLOSE_WINDOW_ID, "");
					String s = bsg.closeScript(functionName, windowID);
					scripts.append(s);
				}
			}
		}

		List<CompositeMap> renderers = mmp.getComponents(IProperties.renderer);
		for (CompositeMap renderer : renderers) {
			String type = renderer.getString(
					ComponentInnerProperties.RENDERER_TYPE, "");
			if (IProperties.INNER_FUNCTION.equals(type)) {
				renderer.getParent().put(
						IProperties.renderer,
						renderer.getString(IProperties.RENDERER_FUNCTION_NAME,
								""));
			}
			if (IProperties.PAGE_REDIRECT.equals(type)) {
				String functionName = session.getIDGenerator().genID(
						IProperties.FUNCTION_NAME, 0);
				renderer.getParent().put(IProperties.renderer, functionName);
				String linkId = renderer.getString(IProperties.LINK_ID, "");
				String openFunctionName = session.getIDGenerator().genID(
						IProperties.FUNCTION_NAME, 0);
				RendererScriptGenerator rsg = new RendererScriptGenerator(
						session);
				CompositeMap inner_paramerter = renderer.getChildByAttrib(
						IProperties.PROPERTYE_ID,
						IProperties.RENDERER_PARAMETERS).getChildByAttrib(
						IProperties.COMPONENT_TYPE,
						IProperties.INNER_PARAMERTER);
				String para_value = inner_paramerter.getString(
						IProperties.PARAMETER_VALUE, "");
				String para_name = inner_paramerter.getString(
						IProperties.PARAMETER_NAME, "");

				String s1 = rsg.openScript(openFunctionName, linkId, para_name);
				String s = rsg.hrefScript(functionName,
						renderer.getString(IProperties.RENDERER_LABELTEXT, ""),
						openFunctionName, para_value);
				scripts.append(s);
				scripts.append(s1);
			}
			if (IProperties.USER_FUNCTION.equals(type)) {
				String s = renderer.getChild(IProperties.FUNCTION).getText();
				String functionName = mmp.getFunctionName(s);
				renderer.getParent().put(IProperties.renderer, functionName);
				scripts.append(s);
			}
		}
		List<CompositeMap> footrenderers = mmp
				.getComponents(IProperties.FOOTRENDERER);
		for (CompositeMap footrenderer : footrenderers) {
			CompositeMap child = footrenderer.getChild(IProperties.CDATA_NODE);
			if (child != null) {
				String s = child.getText();
				String functionName = mmp.getFunctionName(s);
				footrenderer.getParent().put(IProperties.footerRenderer,
						functionName);
				scripts.append(s);
			}
		}
		String string = scripts.toString();
		session.appendResultln(format(string));
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

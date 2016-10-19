package aurora.plugin.source.gen;

import java.util.List;

import uncertain.composite.CompositeMap;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

public class ActionMethod implements TemplateMethodModel {

	private BuilderSession session;

	public ActionMethod(BuilderSession session) {
		this.session = session;
	}

	public Object exec(List arguments) throws TemplateModelException {
		String event = (String) arguments.get(0);
		// System.out.println(arguments.get(1).getClass());
		// getComponentBuilders
		// builder.dispatch event
		// javascript current_model
		// dataset current_model
		// children current_model

		return session.execActionEvent(event, null);
	}
}

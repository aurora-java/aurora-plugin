package aurora.plugin.source.gen;

import java.util.List;

import uncertain.composite.CompositeMap;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

public class PropertiesMethod implements TemplateMethodModel {

	private BuilderSession session;

	public PropertiesMethod(BuilderSession session) {
		this.session = session;
	}

	public Object exec(@SuppressWarnings("rawtypes") List args)
			throws TemplateModelException {

		if (args.isEmpty()) {
			return "";
		}
		CompositeMap currentContext = session.getCopy().getCurrentContext();
		if (currentContext != null) {
			return properties(currentContext,args);
		}
		return "";
	}

	private Object properties(CompositeMap map,List<?> args) {
		String r = " ";
		for (Object object : args) {
			String string = map.getString(object, "");
			if ("".equals(string)) {
				continue;
			}
			r += object.toString();
			r += "=";
			r += "\"";
			r += string;
			r += "\"";
			r += " ";
		}
		return r;
	}

}

package aurora.plugin.source.gen.screen.model.asm;

import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;
import uncertain.composite.CompositeMapParser;
import aurora.plugin.source.gen.screen.model.ScreenBody;
import aurora.plugin.source.gen.screen.model.io.CompositeMap2Object;

public class TemplateDescriptor {
	public static ScreenBody getModelFromTemplate(String template)
			throws Exception {
		CompositeMapParser parser = new CompositeMapParser(
				new CompositeLoader());
		CompositeMap map = null;
		String tplFileName = getTemplateFileName(template);
		try {
			map = parser.parseStream(TemplateDescriptor.class
					.getResourceAsStream(tplFileName));
		} catch (Exception e) {
			throw new Exception("Error while reading template :" + tplFileName
					+ ".[" + e.getMessage() + "]");
		}
		CompositeMap2Object c2o = new CompositeMap2Object();
		return c2o.createScreenBody(map);
	}

	private static String getTemplateFileName(String template) {
		return template + ".uip";
	}
}

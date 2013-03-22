package aurora.plugin.source.gen.test;

import java.io.IOException;
import java.io.InputStream;

import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;
import aurora.plugin.source.gen.SourceGenManager;
import aurora.plugin.source.gen.SourceTemplateProvider;

public class Test {
	public static void main(String[] args) {
		SourceGenManager sgm = new SourceGenManager();
		SourceTemplateProvider stp = new SourceTemplateProvider();
		sgm.setTemplateProvider(stp);
		stp.setSourceGenManager(sgm);
		stp.initialize();
		sgm.buildScreen(loadCompositeMap());
	}
	static CompositeMap loadCompositeMap() {
		InputStream is = null;
		try {
			is = SourceTemplateProvider.class.getResourceAsStream("test2.uip");
			CompositeLoader parser = new CompositeLoader();
			CompositeMap rootMap = parser.loadFromStream(is);
			rootMap.put("file_path", "a/b/c/d.uip");
			// rootMap.getChildByAttrib(attrib_key, attrib_value)
			// ModelIOManager mim = ModelIOManager.getNewInstance();
			// diagram = mim.fromCompositeMap(rootMap);
			return rootMap;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return new CompositeMap();
	}
}

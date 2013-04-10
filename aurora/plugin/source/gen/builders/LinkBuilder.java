package aurora.plugin.source.gen.builders;

import java.util.List;
import java.util.Map;

import uncertain.composite.CompositeMap;
import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.SourceGenManager;

public class LinkBuilder extends DefaultSourceBuilder {
	public void actionEvent(String event, BuilderSession session) {
		if ("link".equals(event)) {
			CompositeMap currentContext = session.getCurrentContext();
			List<?> childs = currentContext.getChilds();
			if(childs==null)
				return;
			StringBuilder sb = new StringBuilder();
			for (Object c : childs) {
				if (c instanceof CompositeMap) {
					if ("link".equalsIgnoreCase(((CompositeMap) c).getName())) {
						SourceGenManager sourceGenManager = session
								.getSourceGenManager();
						BuilderSession copy = session.getCopy();
						copy.setCurrentContext((CompositeMap) c);
						String s = sourceGenManager.bindTemplate(copy);
						sb.append(s);
					}
				}
			}
			session.appendResultln(sb.toString());
		}
	}
	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put("id", "id");
		attributeMapping.put("url", "url");
		attributeMapping.put("model", "model");
		attributeMapping.put("action", "action");
		return attributeMapping;
	}
}

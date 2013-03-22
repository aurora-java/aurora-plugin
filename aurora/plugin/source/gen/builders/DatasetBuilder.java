package aurora.plugin.source.gen.builders;

import java.util.List;
import java.util.Map;

import uncertain.composite.CompositeMap;
import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.SourceGenManager;

public class DatasetBuilder extends DefaultSourceBuilder {

	@Override
	public void buildContext(BuilderSession session) {
		// super.buildContext(session);
	}

	public void actionEvent(String event, BuilderSession session) {
		if ("dataset".equals(event)) {
			CompositeMap currentContext = session.getCurrentContext();
			List<?> childs = currentContext.getChilds();
			StringBuilder sb = new StringBuilder();
			for (Object c : childs) {
				if (c instanceof CompositeMap) {
					if ("dataset".equalsIgnoreCase(((CompositeMap) c).getName())) {
						SourceGenManager sourceGenManager = session
								.getSourceGenManager();
						BuilderSession copy = session.getCopy();
						String type = ((CompositeMap) c).getString("component_type", "");
						((CompositeMap) c).put("ds_type", type);
						((CompositeMap) c).put("component_type", "dataset");
						copy.setCurrentContext((CompositeMap) c);
						String s = sourceGenManager.bindTemplate(copy);
						sb.append(s);
					}
				}
			}
			session.appendResult(sb.toString());
		}
		if("datasetfields".equals(event)){
			CompositeMap currentContext = session.getCurrentContext();
			List<?> childsNotNull = currentContext.getChildsNotNull();
			for (Object object : childsNotNull) {
				if(object instanceof CompositeMap){
					BuilderSession copy = session.getCopy();
					copy.setCurrentContext((CompositeMap) object);
					String bindTemplate = session.getSourceGenManager().bindTemplate(copy);
					session.appendResult(bindTemplate);
				}
			}
		}
	}
	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put("lookupCode", "lookupCode");
		attributeMapping.put("model", "model");
		attributeMapping.put("query_ds", "queryDataSet");
		attributeMapping.put("bindName", "bindName");
		attributeMapping.put("bindTarget", "bindTarget");
		attributeMapping.put("queryUrl", "queryUrl");
		return attributeMapping;
	}
}

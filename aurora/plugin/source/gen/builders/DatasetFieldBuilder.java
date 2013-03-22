package aurora.plugin.source.gen.builders;

import java.util.Map;

public class DatasetFieldBuilder extends DefaultSourceBuilder {
	protected Map<String, String> getAttributeMapping() {
		Map<String, String> attributeMapping = super.getAttributeMapping();
		attributeMapping.put("required", "required");
		attributeMapping.put("readOnly", "readOnly");
		attributeMapping.put("options", "options");
		attributeMapping.put("displayField", "displayField");
		attributeMapping.put("prompt", "prompt");
		attributeMapping.put("valueField", "valueField");
		attributeMapping.put("field_name", "field_name");
		attributeMapping.put("lovService", "lovService");
		attributeMapping.put("lovWidth", "lovWidth");
		attributeMapping.put("lovLabelWidth", "lovLabelWidth");
		attributeMapping.put("lovHeight", "lovHeight");
		attributeMapping.put("lovGridHeight", "lovGridHeight");
		attributeMapping.put("lovAutoQuery", "lovAutoQuery");
		attributeMapping.put("defaultValue", "defaultValue");
		attributeMapping.put("checkedValue", "checkedValue");
		attributeMapping.put("uncheckedValue", "uncheckedValue");
		attributeMapping.put("field_type", "field_type");
		return attributeMapping;
	}
}

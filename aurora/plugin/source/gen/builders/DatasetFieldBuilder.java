package aurora.plugin.source.gen.builders;

import java.util.Map;

import uncertain.composite.CompositeMap;
import aurora.plugin.source.gen.BuilderSession;

public class DatasetFieldBuilder extends DefaultSourceBuilder {
	public void buildContext(BuilderSession session) {
		super.buildContext(session);
		CompositeMap currentContext = session.getCurrentContext();
		CompositeMap currentModel = session.getCurrentModel();
   
		String type = currentContext.getString("field_type", "");
		if ("comboBox".equalsIgnoreCase(type) || "lov".equalsIgnoreCase(type)) {
			currentContext.put("for_return_field",
					currentContext.getString("field_name", ""));
			currentContext.put("for_display_field",
					currentContext.getString("field_name", "") + "_display");
			CompositeMap innerLovService = currentModel.getChildByAttrib(
					"component_type", "innerLovService");
			String lovservice_for_return = innerLovService.getString(
					"lovservice_for_return", "");
			if ("".equals(lovservice_for_return) == false) {
				currentContext.put("for_return_field", lovservice_for_return);
			}
			String lovservice_for_display = innerLovService.getString(
					"lovservice_for_display", "");
			if ("".equals(lovservice_for_display) == false) {
				currentContext.put("for_display_field", lovservice_for_display);
			}
			CompositeMap mappings = innerLovService.getChildByAttrib(
					"propertye_id", "inner_datset_field_mappings");
			if (mappings != null) {
				CompositeMap clone = (CompositeMap) mappings.clone();
				clone.setName("mappings");
				currentContext.addChild(clone);
			}
		}
		//
		// <innerLovService lovservice_for_return="head_pk"
		// propertye_id="inner_lov_service" component_type="innerLovService"
		// class_name="aurora.plugin.source.gen.screen.model.LovService"
		// markid="206bd69f" lovservice_options="head_for_query"
		// lovservice_for_display="head_pk_display">
		// <containmentList propertye_id="inner_datset_field_mappings">
		// <innerDatasetFieldMapping component_type="innerDatasetFieldMapping"
		// class_name="aurora.plugin.source.gen.screen.model.Mapping"
		// markid="7d7715a" inner_field_isdisplay="false" mapping_to="head_pk"
		// mapping_from="head_pk"/>
		// <innerDatasetFieldMapping component_type="innerDatasetFieldMapping"
		// class_name="aurora.plugin.source.gen.screen.model.Mapping"
		// markid="5152cfbb" inner_field_isdisplay="false"
		// mapping_to="head_pk_display" mapping_from="hea_c1"/>
		// <innerDatasetFieldMapping component_type="innerDatasetFieldMapping"
		// class_name="aurora.plugin.source.gen.screen.model.Mapping"
		// markid="13c427b3" inner_field_isdisplay="false"
		// mapping_to="hea_c1_ref" mapping_from="hea_c1"/>
		// </containmentList>
		// </innerLovService>

	}

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

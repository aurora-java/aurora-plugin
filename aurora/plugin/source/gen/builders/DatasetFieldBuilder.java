package aurora.plugin.source.gen.builders;

import java.util.Map;

import uncertain.composite.CompositeMap;
import aurora.plugin.source.gen.BuilderSession;
import aurora.plugin.source.gen.screen.model.properties.IProperties;

public class DatasetFieldBuilder extends DefaultSourceBuilder {
	private static final String _DISPLAY = "_display";

	public void buildContext(BuilderSession session) {
		super.buildContext(session);
		CompositeMap currentContext = session.getCurrentContext();
		CompositeMap currentModel = session.getCurrentModel();

		String type = currentContext.getString(IProperties.FIELD_TYPE, "");
		if (IProperties.COMBO_BOX.equalsIgnoreCase(type)
				|| IProperties.LOV.equalsIgnoreCase(type)) {
			currentContext.put(IProperties.FOR_RETURN_FIELD,
					currentContext.getString(IProperties.FIELD_NAME, ""));
			currentContext.put(IProperties.FOR_DISPLAY_FIELD,
					currentContext.getString(IProperties.FIELD_NAME, "")
							+ _DISPLAY);
			CompositeMap innerLovService = currentModel.getChildByAttrib(
					IProperties.COMPONENT_TYPE,
					IProperties.INNER_TYPE_LOV_SERVICE);
			String lovservice_for_return = innerLovService.getString(
					IProperties.LOVSERVICE_FOR_RETURN, "");
			if ("".equals(lovservice_for_return) == false) {
				currentContext.put(IProperties.FOR_RETURN_FIELD,
						lovservice_for_return);
			}
			String lovservice_for_display = innerLovService.getString(
					IProperties.LOVSERVICE_FOR_DISPLAY, "");
			if ("".equals(lovservice_for_display) == false) {
				currentContext.put(IProperties.FOR_DISPLAY_FIELD,
						lovservice_for_display);
			}
			CompositeMap mappings = innerLovService.getChildByAttrib(
					IProperties.PROPERTYE_ID,
					IProperties.INNER_DATSET_FIELD_MAPPINGS);
			if (mappings != null) {
				CompositeMap clone = (CompositeMap) mappings.clone();
				clone.setName(IProperties.MAPPINGS);
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
		attributeMapping.put(IProperties.required, IProperties.required);
		attributeMapping.put(IProperties.readOnly, IProperties.readOnly);
		attributeMapping.put(IProperties.options, IProperties.options);
		attributeMapping
				.put(IProperties.displayField, IProperties.displayField);
		attributeMapping.put(IProperties.prompt, IProperties.prompt);
		attributeMapping.put(IProperties.valueField, IProperties.valueField);
		attributeMapping.put(IProperties.FIELD_NAME, IProperties.FIELD_NAME);
		attributeMapping.put(IProperties.lovService, IProperties.lovService);
		attributeMapping.put(IProperties.lovWidth, IProperties.lovWidth);
		attributeMapping.put(IProperties.lovLabelWidth,
				IProperties.lovLabelWidth);
		attributeMapping.put(IProperties.lovHeight, IProperties.lovHeight);
		attributeMapping.put(IProperties.lovGridHeight,
				IProperties.lovGridHeight);
		attributeMapping
				.put(IProperties.lovAutoQuery, IProperties.lovAutoQuery);
		attributeMapping
				.put(IProperties.defaultValue, IProperties.defaultValue);
		attributeMapping
				.put(IProperties.checkedValue, IProperties.checkedValue);
		attributeMapping.put(IProperties.uncheckedValue,
				IProperties.uncheckedValue);
		attributeMapping.put(IProperties.FIELD_TYPE, IProperties.FIELD_TYPE);
		return attributeMapping;
	}
}

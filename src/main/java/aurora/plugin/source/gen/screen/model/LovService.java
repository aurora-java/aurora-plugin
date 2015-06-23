package aurora.plugin.source.gen.screen.model;

import java.util.ArrayList;
import java.util.List;

public class LovService extends AuroraComponent implements
		IDialogEditableObject {

	private List<Mapping> mappings = new ArrayList<Mapping>();

	public LovService() {
		this.setComponentType("innerLovService");
	}

	public String getDescripition() {
		return getOptions(); 
	}

	public Object getContextInfo() {
		return null;
	}

	public LovService clone() {
		LovService r = new LovService();
		r.setOptions(this.getOptions());
		r.set4Display(this.get4Display());
		r.setForReturn(this.getForReturn());
		for (Mapping m : mappings) {
			r.addMapping(m);
		}
		return r;
	}

	public String getOptions() {
		return this.getStringPropertyValue("lovservice_options");
	}

	public void setOptions(String options) {
		this.setPropertyValue("lovservice_options", options);
	}
	public String get4Display() {
		return this.getStringPropertyValue("lovservice_for_display");
	}

	public void set4Display(String displayField) {
		this.setPropertyValue("lovservice_for_display", displayField);
	}
	public String getForReturn() {
		return this.getStringPropertyValue("lovservice_for_return");
	}

	public void setForReturn(String valueField) {
		this.setPropertyValue("lovservice_for_return", valueField);
	}

	public List<Mapping> getMappings() {
		return mappings;
	}

	public void addMapping(Mapping para) {
		mappings.add(para);
	}

	@Override
	public Object getPropertyValue(String propId) {
		if ("inner_datset_field_mappings".equals(propId)) {
			return mappings;
		}
		Object propertyValue = super.getPropertyValue(propId);
		return propertyValue;
	}

	public void setPropertyValue(String propId, Object val) {
		if ("inner_datset_field_mappings".equals(propId) && val instanceof List) {
			mappings = (List<Mapping>) val;
			return;
		}
		super.setPropertyValue(propId, val);
	}


}

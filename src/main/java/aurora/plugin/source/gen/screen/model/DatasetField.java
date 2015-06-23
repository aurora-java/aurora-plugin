package aurora.plugin.source.gen.screen.model;

import aurora.plugin.source.gen.screen.model.properties.ComponentInnerProperties;
import aurora.plugin.source.gen.screen.model.properties.ComponentProperties;

public class DatasetField extends AuroraComponent {

	public Object getPropertyValue(String propName) {
		Object propertyValue = super.getPropertyValue(propName);
		if (propertyValue == null
				&& ComponentInnerProperties.INNER_LOV_SERVICE.equals(propName)) {
			return new LovService();
		}
		return propertyValue;
	}

	public LovService getLovService() {
		return (LovService) this
				.getPropertyValue(ComponentInnerProperties.INNER_LOV_SERVICE);
	}

	public void setLovService(LovService ls) {
		this.setPropertyValue(ComponentInnerProperties.INNER_LOV_SERVICE, ls);
	}

	public String getCheckedValue() {
		return this.getStringPropertyValue(ComponentProperties.checkedValue);
	}

	public void setCheckedValue(String checkedValue) {
		this.setPropertyValue(ComponentProperties.checkedValue, checkedValue);
	}

	public String getUncheckedValue() {
		return this.getStringPropertyValue(ComponentProperties.uncheckedValue);
	}

	public void setUncheckedValue(String uncheckedValue) {
		this.setPropertyValue(ComponentProperties.uncheckedValue,
				uncheckedValue);
	}

	// public String getDisplayField() {
	// return this.getStringPropertyValue(ComponentProperties.displayField);
	// }
	//
	// public void setDisplayField(String displayField) {
	// this.setPropertyValue(ComponentProperties.displayField, displayField);
	// }

	// public String getOptions() {
	// // DataSetFieldUtil dsfu = getNewDsfUtil();
	// // if (dsfu != null)
	// // return nns(getNewDsfUtil().getOptions());
	// // return "";
	// return this.getStringPropertyValue(ComponentProperties.options);
	//
	// // return options;
	// }

	// public void setOptions(String options) {
	// // this.options = options;
	// this.setPropertyValue(ComponentProperties.options, options);
	// }

	// public String getValueField() {
	// return this.getStringPropertyValue(ComponentProperties.valueField);
	// }
	//
	// public void setValueField(String valueField) {
	// this.setPropertyValue(ComponentProperties.valueField, valueField);
	// }
	//
	// public String getReturnField() {
	// return this.getStringPropertyValue(ComponentProperties.returnField);
	// }
	//
	// public void setReturnField(String returnField) {
	// this.setPropertyValue(ComponentProperties.returnField, returnField);
	// }

	public int getLovGridHeight() {
		// return lovGridHeight;
		return this.getIntegerPropertyValue(ComponentProperties.lovGridHeight);
	}

	public void setLovGridHeight(int lovGridHeight) {
		// this.lovGridHeight = lovGridHeight;
		this.setPropertyValue(ComponentProperties.lovGridHeight, lovGridHeight);
	}

	public int getLovHeight() {
		// return lovHeight;
		return this.getIntegerPropertyValue(ComponentProperties.lovHeight);
	}

	public void setLovHeight(int lovHeight) {
		// this.lovHeight = lovHeight;
		this.setPropertyValue(ComponentProperties.lovHeight, lovHeight);
	}

	// public String getLovService() {
	// // DataSetFieldUtil dsfu = getNewDsfUtil();
	// // if (dsfu != null)
	// // return nns(dsfu.getOptions());
	// // return "";
	// // return lovService;
	// return this.getStringPropertyValue(ComponentProperties.lovService);
	// }

	// public void setLovService(String lovService) {
	// // this.lovService = lovService;
	// this.setPropertyValue(ComponentProperties.lovService, lovService);
	// }

	public String getLovUrl() {
		// return lovUrl;
		return this.getStringPropertyValue(ComponentProperties.lovUrl);
	}

	public void setLovUrl(String lovUrl) {
		// this.lovUrl = lovUrl;
		this.setPropertyValue(ComponentProperties.lovUrl, lovUrl);
	}

	public int getLovWidth() {
		// return lovWidth;
		return this.getIntegerPropertyValue(ComponentProperties.lovWidth);
	}

	public void setLovWidth(int lovWidth) {
		// this.lovWidth = lovWidth;
		this.setPropertyValue(ComponentProperties.lovWidth, lovWidth);
	}

	public String getTitle() {
		// return title;
		return this.getStringPropertyValue(ComponentProperties.title);
	}

	public void setTitle(String title) {
		// this.title = title;
		this.setPropertyValue(ComponentProperties.title, title);
	}

	public DatasetField() {
		this.setComponentType("datasetfield");
		this.setLovGridHeight(350);
		this.setLovHeight(500);
		this.setLovWidth(500);
	}

	public boolean isRequired() {
		// return required;
		return this.getBooleanPropertyValue(ComponentProperties.required);
	}

	public void setRequired(boolean required) {
		// this.required = required;
		this.setPropertyValue(ComponentProperties.required, required);
	}

	public boolean isReadOnly() {
		// return readOnly;
		return this.getBooleanPropertyValue(ComponentProperties.readOnly);
	}

	public void setReadOnly(boolean readOnly) {
		// this.readOnly = readOnly;
		this.setPropertyValue(ComponentProperties.readOnly, readOnly);
	}

	public String getDefaultValue() {
		// return defaultValue;
		return this.getStringPropertyValue(ComponentProperties.defaultValue);
	}

	public void setDefaultValue(String defaultValue) {
		// this.defaultValue = defaultValue;
		this.setPropertyValue(ComponentProperties.defaultValue, defaultValue);
	}
}

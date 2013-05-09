package aurora.plugin.source.gen.screen.model;

import java.beans.PropertyChangeListener;

import aurora.plugin.source.gen.screen.model.properties.ComponentInnerProperties;
import aurora.plugin.source.gen.screen.model.properties.ComponentProperties;

abstract public class Input extends AuroraComponent implements
		IDatasetFieldDelegate, DatasetBinder {

	// TYPE
	public static final String TEXT = "textField"; //$NON-NLS-1$
	public static final String NUMBER = "numberField"; //$NON-NLS-1$
	public static final String Combo = "comboBox"; //$NON-NLS-1$
	public static final String LOV = "lov"; //$NON-NLS-1$
	public static final String DATE_PICKER = "datePicker"; //$NON-NLS-1$
	public static final String DATETIMEPICKER = "dateTimePicker"; //$NON-NLS-1$

	public static final String CASE_LOWER = "lower"; //$NON-NLS-1$
	public static final String CASE_UPPER = "upper"; //$NON-NLS-1$
	public static final String CASE_ANY = ""; //$NON-NLS-1$
	public static final String[] CASE_TYPES = { CASE_ANY, CASE_UPPER,
			CASE_LOWER };
	private static final String[] CAL_ENABLES = { "pre", "next", "both", "none" }; 
	public static final String[] INPUT_TYPES = { TEXT, NUMBER, Combo, LOV,
			DATE_PICKER, DATETIMEPICKER, CheckBox.CHECKBOX };
	// property key
	// number
	public static final String ALLOWDECIMALS = "allowDecimals"; //$NON-NLS-1$
	public static final String ALLOWNEGATIVE = "allowNegative"; //$NON-NLS-1$
	public static final String ALLOWFORMAT = "allowFormat"; //$NON-NLS-1$
	// all
	public static final String EMPTYTEXT = "emptyText"; //$NON-NLS-1$
	// combo lov text
	public static final String TYPECASE = "typeCase"; //$NON-NLS-1$
	// cal
	public static final String ENABLE_BESIDE_DAYS = "enableBesideDays"; //$NON-NLS-1$
	public static final String ENABLE_MONTH_BTN = "enableMonthBtn"; //$NON-NLS-1$


	private DatasetField dsField;


	public Input() {
		this.setSize(120, 20);
		this.setComponentType(TEXT);
		this.setAllowDecimals(true);
		this.setAllowFormat(false);
		this.setAllowNegative(true);
		this.setEmptyText("");
		this.setEnableBesideDays(CAL_ENABLES[3]);
		this.setTypeCase(CASE_ANY);
		this.setEnableMonthBtn(CAL_ENABLES[3]);
		setDatasetField(new DatasetField());
	}

	public boolean isRequired() {
		return dsField.isRequired();
	}

	public void setRequired(boolean required) {
		dsField.setRequired(required);
	}

	public boolean isReadOnly() {
		return this.getDatasetField().isReadOnly();
	}

	public void setReadOnly(boolean readOnly) {
		this.getDatasetField().setReadOnly(readOnly);
	}

	/**
	 * NumberField ,是否允许小数
	 * 
	 * @return
	 */
	public boolean isAllowDecimals() {
		return this.getBooleanPropertyValue(ComponentProperties.allowDecimals);
	}

	/**
	 * NumberField ,是否允许小数
	 * 
	 * @return
	 */
	public void setAllowDecimals(boolean allowDecimals) {
		this.setPropertyValue(ComponentProperties.allowDecimals, allowDecimals);
	}

	/**
	 * NumberField ,是否允许负数
	 * 
	 * @return
	 */
	public boolean isAllowNegative() {
		return this.getBooleanPropertyValue(ComponentProperties.allowNegative);
	}

	/**
	 * NumberField ,是否允许负数
	 * 
	 * @return
	 */
	public void setAllowNegative(boolean allowNegative) {
		this.setPropertyValue(ComponentProperties.allowNegative, allowNegative);
	}

	/**
	 * NumberField ,是否允许千分位分隔
	 * 
	 * @return
	 */
	public boolean isAllowFormat() {
		return this.getBooleanPropertyValue(ComponentProperties.allowFormat);
	}

	/**
	 * NumberField ,是否允许千分位分隔
	 * 
	 * @return
	 */
	public void setAllowFormat(boolean allowFormat) {
		this.setPropertyValue(ComponentProperties.allowFormat, allowFormat);
	}

	public String getEmptyText() {
		return this.getStringPropertyValue(ComponentProperties.emptyText);
	}

	public void setEmptyText(String emptyText) {
		this.setPropertyValue(ComponentProperties.emptyText, emptyText);
	}

	/**
	 * TextField ,大小写限制
	 * 
	 * @return
	 */
	public String getTypeCase() {
		// return typeCase;
		return this.getStringPropertyValue(ComponentProperties.typeCase);
	}

	/**
	 * TextField ,大小写限制
	 * 
	 * @return
	 */
	public void setTypeCase(String typeCase) {
		// this.typeCase = typeCase;
		this.setPropertyValue(ComponentProperties.typeCase, typeCase);
	}

	/**
	 * DatePicker ,本月前后月份补齐<br/>
	 * 
	 * @return
	 */
	public String getEnableBesideDays() {
		// return enableBesideDays;
		return this
				.getStringPropertyValue(ComponentProperties.enableBesideDays);
	}

	/**
	 * DatePicker ,本月前后月份补齐<br/>
	 * none|both|pre|next
	 * 
	 * @return
	 */
	public void setEnableBesideDays(String enableBesideDays) {
		this.setPropertyValue(ComponentProperties.enableBesideDays,
				enableBesideDays);
	}

	/**
	 * DatePicker ,月份按钮显示方式
	 * 
	 * @return
	 */
	public String getEnableMonthBtn() {
		return this.getStringPropertyValue(ComponentProperties.enableMonthBtn);
	}

	/**
	 * DatePicker ,月份按钮显示方式<br/>
	 * none|both|pre|next
	 * 
	 * @return
	 */
	public void setEnableMonthBtn(String enableMonthBtn) {
		// this.enableMonthBtn = enableMonthBtn;
		this.setPropertyValue(ComponentProperties.enableMonthBtn,
				enableMonthBtn);
	}

	public void setName(String name) {
		super.setName(name);
		getDatasetField().setName(name);
	}

	private int indexOf(Object[] objs, Object o) {
		for (int i = 0; i < objs.length; i++)
			if (objs[i].equals(o))
				return i;
		return -1;
	}

	public DatasetField getDatasetField() {
		return this.dsField;
	}

	public void setParent(Container part) {
		super.setParent(part);
	}

	public void setDatasetField(DatasetField field) {
		dsField = field;
		dsField.setName(getName());
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener l) {
		super.addPropertyChangeListener(l);
		if (dsField != null) {
			dsField.addPropertyChangeListener(l);
		}
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener l) {
		super.removePropertyChangeListener(l);
		if (dsField != null) {
			dsField.removePropertyChangeListener(l);
		}
	}

	public Object getPropertyValue(String propId) {
		if (ComponentInnerProperties.DATASET_FIELD_DELEGATE.equals(propId)) {
			return dsField;
		}
		Object propertyValue = super.getPropertyValue(propId);
		return propertyValue;
	}

	public void setPropertyValue(String propId, Object val) {
		if (ComponentInnerProperties.DATASET_FIELD_DELEGATE.equals(propId)
				&& val instanceof DatasetField) {
			this.setDatasetField((DatasetField) val);
			return;
		}
		super.setPropertyValue(propId, val);
	}

}

package aurora.plugin.entity.model;

public class EditorType {
	public static String TEXTFIELD = "textField";
	public static String NUMBERFIELD = "numberField";
	public static String LOV = "lov";
	public static String COMBOBOX = "comboBox";
	public static String DATEPICKER = "datePicker";
	public static String DATETIMEPICKER = "dateTimePicker";
	public static String TEXTAREA = "textArea";
	public static String[] types = { TEXTFIELD, NUMBERFIELD, LOV, COMBOBOX,
			DATEPICKER, DATETIMEPICKER, TEXTAREA };

	public static String toNormalCase(String type) {
		for (String s : types) {
			if (s.equalsIgnoreCase(type))
				return s;
		}
		return type;
	}

}

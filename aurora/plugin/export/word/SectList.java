package aurora.plugin.export.word;

public class SectList {
	
	private String id;
	private String model;
	private String idField = "id";
	private String parentIdField = "pid";
	private String textField = "text";

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIdField() {
		return idField;
	}

	public void setIdField(String idField) {
		this.idField = idField;
	}


	public String getTextField() {
		return textField;
	}

	public void setTextField(String textField) {
		this.textField = textField;
	}

	public String getParentIdField() {
		return parentIdField;
	}

	public void setParentIdField(String parentIdField) {
		this.parentIdField = parentIdField;
	}

}

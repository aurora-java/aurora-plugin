package aurora.plugin.export.word;

public class SectList {

	private String id;
	private String model;
	private String idField = "id";
	private String textField = "text";
	private String indFirstLineField = "indfirstline";
	private String indLeftField = "indleft";
	private String ilvlField = "ilvl";
	private String tocField = "toc";
	private String tocTitleField = "toctitle";
	private String alignField = "align";
	private String type = "type";
	private String numId = "1";
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getIdField() {
		return idField;
	}
	public void setIdField(String idField) {
		this.idField = idField;
	}
	public String getNumId() {
		return numId;
	}
	public void setNumId(String numId) {
		this.numId = numId;
	}
	public String getTextField() {
		return textField;
	}
	public void setTextField(String textField) {
		this.textField = textField;
	}
	public String getIndFirstLineField() {
		return indFirstLineField;
	}
	public void setIndFirstLineField(String indFirstLineField) {
		this.indFirstLineField = indFirstLineField;
	}
	public String getIndLeftField() {
		return indLeftField;
	}
	public void setIndLeftField(String indLeftField) {
		this.indLeftField = indLeftField;
	}
	public String getIlvlField() {
		return ilvlField;
	}
	public void setIlvlField(String ilvlField) {
		this.ilvlField = ilvlField;
	}
	public String getTocField() {
		return tocField;
	}
	public void setTocField(String tocField) {
		this.tocField = tocField;
	}
	public String getTocTitleField() {
		return tocTitleField;
	}
	public void setTocTitleField(String tocTitleField) {
		this.tocTitleField = tocTitleField;
	}
	public String getAlignField() {
		return alignField;
	}
	public void setAlignField(String alignField) {
		this.alignField = alignField;
	}
	
}

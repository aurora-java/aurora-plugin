package aurora.plugin.source.gen.screen.model;

public class DemonstrateBind extends AuroraComponent {

	public static final String FOR_QUERY_EDITOR = "for_query_editor";
	public static final String FOR_DISPLAY = "for_display";
	public static final String BIND_MODEL = "bind_model";
	public static final String FOR_QUERY = "for_query";
	public static final String COLUMN_PROMPT = "column_prompt";
	static final public String BIND_COMPONENT = "bind_component";

	
	public DemonstrateBind() {
		this.setComponentType(BIND_COMPONENT);
		this.setForDisplay(true);
	}
	
	public void setBindModel(AuroraComponent v) {
		this.setPropertyValue(BIND_MODEL, v);
	}

	public AuroraComponent getBindModel() {
		return this.getAuroraComponentPropertyValue(BIND_MODEL);
	}
	
	public void setForQuery(boolean v) {
		this.setPropertyValue(FOR_QUERY, v);
	}

	public boolean isForQuery() {
		return this.getBooleanPropertyValue(FOR_QUERY);
	}
	
	public void setForQueryEditor(String v) {
		this.setPropertyValue(FOR_QUERY_EDITOR, v);
	}

	public String getForQueryEditor() {
		return this.getStringPropertyValue(FOR_QUERY_EDITOR);
	}
	
	public void setForDisplay(boolean v) {
		this.setPropertyValue(FOR_DISPLAY, v);
	}

	public boolean isForDisplay() {
		return this.getBooleanPropertyValue(FOR_DISPLAY);
	}
	
	public void setColumnPrompt(String v) {
		this.setPropertyValue(COLUMN_PROMPT, v);
	}

	public String getColumnPrompt() {
		return this.getStringPropertyValue(COLUMN_PROMPT);
	}
	
	
	

}

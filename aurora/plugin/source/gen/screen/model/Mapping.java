package aurora.plugin.source.gen.screen.model;

public class Mapping extends AuroraComponent {

	public Mapping() {
		this.setComponentType("innerDatasetFieldMapping");
	}

	public Mapping(String from, String to) {
		super();
		this.setComponentType("innerDatasetFieldMapping");
		this.setFrom(from);
		this.setTo(to);
	}

	public String getFrom() {
		return this.getStringPropertyValue("mapping_from");
	}

	public void setFrom(String from) {
		this.setPropertyValue("mapping_from", from);
	}

	public String getTo() {
		return this.getStringPropertyValue("mapping_to");
	}

	public void setTo(String to) {
		this.setPropertyValue("mapping_to", to);
	}

	public boolean isDisplay() {
		return this.getBooleanPropertyValue("inner_field_isdisplay");
	}

	public void setDisplay(boolean display) {
		this.setPropertyValue("inner_field_isdisplay", display);
	}

	public Mapping clone() {
		Mapping mapping = new Mapping();
		mapping.setFrom(this.getFrom());
		mapping.setTo(this.getTo());
		mapping.setDisplay(this.isDisplay());
		return mapping;
	}

}

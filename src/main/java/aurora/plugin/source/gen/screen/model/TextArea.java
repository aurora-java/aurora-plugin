package aurora.plugin.source.gen.screen.model;

public class TextArea extends Input {
	public static final String TEXT_AREA = "textArea";

	public TextArea() {
		this.setSize(150, 50);
		this.setComponentType(TEXT_AREA);
		this.setPrompt(this.getComponentType());
	}
}

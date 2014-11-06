package aurora.plugin.export.word.wml;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "settings")
@XmlAccessorType(XmlAccessType.FIELD)
public class Settings {
	
	@XmlAttribute
	private BigInteger drawingGridHorizontalSpacing;
	
	@XmlAttribute
	private BigInteger drawingGridVerticalSpacing;
	
	@XmlAttribute
	private BigInteger displayHorizontalDrawingGridEvery;
	
	@XmlAttribute
	private BigInteger displayVerticalDrawingGridEvery;
	
	@XmlAttribute
	private String characterSpacingControl;
	
	@XmlAttribute
	private Boolean hideSpellingErrors = true;
	
	@XmlAttribute
	private Boolean hideGrammaticalErrors = true;
	

	public Boolean getHideGrammaticalErrors() {
		return hideGrammaticalErrors;
	}

	public void setHideGrammaticalErrors(Boolean hideGrammaticalErrors) {
		this.hideGrammaticalErrors = hideGrammaticalErrors;
	}

	public String getCharacterSpacingControl() {
		return characterSpacingControl;
	}

	public void setCharacterSpacingControl(String characterSpacingControl) {
		this.characterSpacingControl = characterSpacingControl;
	}

	public Boolean getHideSpellingErrors() {
		return hideSpellingErrors;
	}

	public void setHideSpellingErrors(Boolean hideSpellingErrors) {
		this.hideSpellingErrors = hideSpellingErrors;
	}

	public BigInteger getDrawingGridHorizontalSpacing() {
		return drawingGridHorizontalSpacing;
	}

	public void setDrawingGridHorizontalSpacing(
			BigInteger drawingGridHorizontalSpacing) {
		this.drawingGridHorizontalSpacing = drawingGridHorizontalSpacing;
	}

	public BigInteger getDrawingGridVerticalSpacing() {
		return drawingGridVerticalSpacing;
	}

	public void setDrawingGridVerticalSpacing(BigInteger drawingGridVerticalSpacing) {
		this.drawingGridVerticalSpacing = drawingGridVerticalSpacing;
	}

	public BigInteger getDisplayHorizontalDrawingGridEvery() {
		return displayHorizontalDrawingGridEvery;
	}

	public void setDisplayHorizontalDrawingGridEvery(
			BigInteger displayHorizontalDrawingGridEvery) {
		this.displayHorizontalDrawingGridEvery = displayHorizontalDrawingGridEvery;
	}

	public BigInteger getDisplayVerticalDrawingGridEvery() {
		return displayVerticalDrawingGridEvery;
	}

	public void setDisplayVerticalDrawingGridEvery(
			BigInteger displayVerticalDrawingGridEvery) {
		this.displayVerticalDrawingGridEvery = displayVerticalDrawingGridEvery;
	}


}

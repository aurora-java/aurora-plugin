package aurora.plugin.bill99.pos;

import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;

public class EXT extends AbstractEntry {

	@Override
	public void run(ProcedureRunner runner) throws Exception {
	}

	private String propertyName;
	private String chnName;
	private String value;

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getChnName() {
		return chnName;
	}

	public void setChnName(String chnName) {
		this.chnName = chnName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}

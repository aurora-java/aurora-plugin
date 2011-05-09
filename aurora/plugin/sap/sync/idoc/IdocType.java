package aurora.plugin.sap.sync.idoc;

public class IdocType {
	public String idoctyp;
	public String cimtyp;
	/**
	 * @param idoctyp
	 * @param cimtyp
	 */
	public IdocType(String idoctyp, String cimtyp) {
		super();
		this.idoctyp = idoctyp;
		this.cimtyp = cimtyp;
	}
	public String getIdoctyp() {
		return idoctyp;
	}
	public void setIdoctyp(String idoctyp) {
		this.idoctyp = idoctyp;
	}
	public String getCimtyp() {
		return cimtyp;
	}
	public void setCimtyp(String cimtyp) {
		this.cimtyp = cimtyp;
	}
}

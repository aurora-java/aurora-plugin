package aurora.plugin.sap.sync.idoc;

public class IDocFile {
	public static final String IDOC_NODE = "IDOC";
	public static final String TABNAM_NODE = "TABNAM";
	public static final String MANDT_NODE = "MANDT";
	public static final String DOCNUM_NODE = "DOCNUM";
	public static final String DOCREL_NODE = "DOCREL";
	public static final String STATUS_NODE = "STATUS";
	public static final String DIRECT_NODE = "DIRECT";
	public static final String OUTMOD_NODE = "OUTMOD";
	public static final String IDOCTYP_NODE = "IDOCTYP";
	public static final String CIMTYP_NODE = "CIMTYP";
	public static final String MESTYP_NODE = "MESTYP";
	public static final String SNDPOR_NODE = "SNDPOR";
	public static final String SNDPRT_NODE = "SNDPRT";
	public static final String SNDPRN_NODE = "SNDPRN";
	public static final String RCVPOR_NODE = "RCVPOR";
	public static final String RCVPRT_NODE = "RCVPRT";
	public static final String RCVPRN_NODE = "RCVPRN";
	public static final String CREDAT_NODE = "CREDAT";
	public static final String CRETIM_NODE = "CRETIM";
	public static final String SERIAL_NODE = "SERIAL";
	
	private String path;
	private int idocId;
	private int serverId;
	/**
	 * @param path
	 * @param idocId
	 * @param serverId
	 */
	public IDocFile(String path, int idocId, int serverId) {
		super();
		this.path = path;
		this.idocId = idocId;
		this.serverId = serverId;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getIdocId() {
		return idocId;
	}
	public void setIdocId(int idocId) {
		this.idocId = idocId;
	}
	public int getServerId() {
		return serverId;
	}
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
}

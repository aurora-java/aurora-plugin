package aurora.plugin.sap.sync.idoc;



public class AuroraIDocException extends Exception {
	private static final long serialVersionUID = -3184478964424768398L;

	public AuroraIDocException() {
		super("Error occurred in aurora idoc application.");
	}

	public AuroraIDocException(String message) {
		super(message);
	}

	public AuroraIDocException(String message, Throwable cause) {
		super(message,cause);
	}
	public AuroraIDocException(Throwable cause) {
		super(cause);
	}
}

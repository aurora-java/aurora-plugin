package aurora.plugin.mail;

public class SendMailException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private String message;

	public SendMailException(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}

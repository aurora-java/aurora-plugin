package aurora.plugin.ntlm;

public class NtlmException extends RuntimeException{
	public NtlmException(){
		super();
	}
	
	public NtlmException(String message){
		super(message);		
	}
	
	public NtlmException(Throwable cause){
		super(cause);
	}
	
	public NtlmException(Throwable cause,String message){
		super(message, cause);
	}
}

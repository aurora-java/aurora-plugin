package aurora.plugin.sharepoint;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class SharepointAuthenticator extends Authenticator{
    
	private String userName;
	private String password;
	public SharepointAuthenticator(SharePointConfig spConfig){
		this.userName = spConfig.getUserName();
		this.password = spConfig.getPassword();
	}
	public SharepointAuthenticator(String userName,String password){
		this.userName = userName;
		this.password = password;
	}
    
    public PasswordAuthentication getPasswordAuthentication () {
	    return new PasswordAuthentication (        	
	    		userName,password.toCharArray());
	}


}

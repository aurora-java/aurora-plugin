package aurora.plugin.tygps;

import uncertain.core.IGlobalInstance;
import uncertain.ocm.IObjectRegistry;

public class GPSProvider implements IGlobalInstance{
	
	public static final String USER_ACCOUNT = "userAccount";
	public static final String USER_PASSWORD = "password";
	public static final String CUSTOMER_ID = "customerId";
	
	private String customerId;
	private String userAccount;
	private String password;
	
	private IObjectRegistry registry;
	
	public GPSProvider(IObjectRegistry rgt) {
		this.registry = rgt;
	}
	
	public void onInitialize() throws Exception {
		registry.registerInstance(GPSProvider.class, this);
		
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}

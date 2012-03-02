package aurora.plugin.alipay;

import java.util.ResourceBundle;

public class Configuration {
	private static Object lock = new Object();
    private static Configuration config = null;
    private static ResourceBundle rb = null;
    private static final String CONFIG_FILE = "alipayChangeInfo";
    
	private Configuration() {
		rb=ResourceBundle.getBundle(CONFIG_FILE);
	}

	/**
	 * 获取Configuration类的实例
	 */
	public static Configuration getInstance(){
		synchronized (lock) {
			if(config==null){
				config=new Configuration();
			}
			return config;
		}
	}
	
	/**
	 * 从properties中获取到key相对应的值
	 */
	public String getValue(String key){
		return rb.getString(key);
	}
	
	public static void main(String[] args) {
		String keyValue=Configuration.getInstance().getValue("seller_user_id");
		System.out.println(keyValue);
		String batch_no=AlipayUtil.getBatchNO("ADFD23213");
		System.out.println(batch_no);
	}
	
	

}

package aurora.plugin.bill99.pos;

public class StringUtil {
	private String str = "";
	
	public StringUtil(String str){
		this.str = str;
	}
	
	public String getOrgStr(String key){
		int i = str.indexOf(key);
		int i1 = str.indexOf("&",i);
		if(i1 == -1){
			i1 = str.length();
		}
		return str.substring(i,i1);
	}
	
	public String getMac(String key){
		int i = str.indexOf(key);
		int i1 = str.indexOf("&",i);
		if(i1 == -1){
			i1 = str.length();
		} 
		return str.substring(i+key.length(),i1);
	}
	
	/**
	 * ���������ַ�
	 * @param key
	 * @return
	 */
	public String getParam(String key){
		int i = str.indexOf(key);
		int i1 = str.indexOf("&",i);
		if(i1 == -1){
			i1 = str.length();
		}
		return str.substring(i+key.length(),i1);
	}
	public static void main(String args[]){

		String str = "postUrl=http%3A%2F%2F192.168.207.88%3A8080%2Fsha1test%2FOrderInfoServlet&orderId=bj45774&reqTime=20090714100921&ext1=&ext2=&MAC=WhW8Y2V39W3KO%2FS%2Fs6sGJT%2B3K0fpBsRiBcZXHlvTXBUf%2FIm%2FJIbWWnZbtjxyzpJWV1kKW1UoVOPb58F%2F0%2B4mIWcVnOc2cfLgw92LgeI46l55X7SWQhK0xk76oxEZ61AmKpObn%2BxTZFDlpusYCBUsAL8SiOU%2FjBpjDQmPqVUwqJc%3D";
		String key = "orderId=";
//		int i = str.indexOf(key);
//		String s = str.substring(i+key.length(),str.indexOf("&",i));
//		System.out.println(s);
		StringUtil s = new StringUtil(str);

		System.out.println(s.getOrgStr("MAC="));
	}
}

package aurora.plugin.alipay;

public class AlipayConfig {
	//合作身份者ID
	public static String partner=Configuration.getInstance().getValue("partner");
	//交易安全检验码key
	public static String key=Configuration.getInstance().getValue("key");
	// 签约支付宝账号或卖家收款支付宝帐户
	public static String seller_email=Configuration.getInstance().getValue("seller_email");
	// 卖家支付宝账号对应的支付宝唯一用户号。以2088开头的纯16位数字。
	//登录时，seller_email和seller_user_id两者必填一个。如果两者都填，以seller_user_id为准。
	public static String seller_user_id=Configuration.getInstance().getValue("seller_user_id");
	// 支付宝服务器通知的页面 
	public static String notify_url=Configuration.getInstance().getValue("notify_url");
	// 当前页面跳转后的页面 
	public static String return_url=Configuration.getInstance().getValue("return_url");
	//支付宝退款时支付宝服务器通知的页面notify_url_for_refund
	public static String notify_url_for_refund=Configuration.getInstance().getValue("notify_url_for_refund");
	//支付宝提供给商户的服务接入网关URL(新)
	public static String alipay_gateway=Configuration.getInstance().getValue("alipay_gateway_new");
	// 字符编码格式 目前支持 gbk 或 utf-8
	public static String input_charset = "UTF-8";
	// 签名方式 
	public static String sign_type = "MD5";
	//访问模式,根据自己的服务器是否支持ssl访问，若支持请选择https；若不支持请选择http
	public static String transport = "http";
	//信用卡标识开关
	public static String credit_card_pay=Configuration.getInstance().getValue("credit_card_pay");
	//信用卡网关默认显示开关
	public static String credit_card_default_display=Configuration.getInstance().getValue("credit_card_default_display");
}

package aurora.plugin.bill99;

public class Bill99 {

	// 获取快钱交易号
	// /获取该交易在快钱的交易号
	public static final String dealId = "dealId";

	// 获取银行交易号
	// /如果使用银行卡支付时，在银行的交易号。如不是通过银行支付，则为空
	public static final String bankDealId = "bankDealId";
	// 获取在快钱交易时间
	// /14位数字。年[4位]月[2位]日[2位]时[2位]分[2位]秒[2位]
	// /如；20080101010101
	public static final String dealTime = "dealTime";
	// 获取实际支付金额
	// /单位为分
	// /比方 2 ，代表0.02元
	public static final String payAmount = "payAmount";
	// 获取交易手续费
	// /单位为分
	// /比方 2 ，代表0.02元
	public static final String fee = "fee";
	// 获取处理结果
	// /10代表 成功11代表 失败
	public static final String payResult = "payResult";
	// 获取错误代码
	// /详细见文档错误代码列表
	public static final String errCode = "errCode";

	public static final String merchant_id = "merchant_id";

	public static final String drawback_version = "drawback_version";

	public static final String command_type = "command_type";

	public static final String drawbackUrl = "drawbackUrl";

	public static final String merchant_key = "merchant_key";

	public static final String txOrder = "txOrder";

	public static final String postdate = "postdate";

	public static final String amount = "amount";

	public static final String orderid = "orderid";

	public static final String mac = "mac";
	

	// 人民币网关账户号
	// /请登录快钱系统获取用户编号，用户编号后加01即为人民币网关账户号。
	public static String merchantAcctId = "merchantAcctId";

	// 人民币网关密钥
	// /区分大小写.请与快钱联系索取，
	public static String key = "key";

	// 字符集.固定选择值。可为空。
	// /只能选择1、2、3.
	// /1代表UTF-8; 2代表GBK; 3代表gb2312
	// /默认值为1
	public static String inputCharset = "inputCharset";

	// 接受支付结果的页面地址.与[bgUrl]不能同时为空。必须是绝对地址。
	// /如果[bgUrl]为空，快钱将支付结果Post到[pageUrl]对应的地址。
	// /如果[bgUrl]不为空，并且[bgUrl]页面指定的<redirecturl>地址不为空，则转向到<redirecturl>对应的地址
	public static String pageUrl = "pageUrl";

	// 服务器接受支付结果的后台地址.与[pageUrl]不能同时为空。必须是绝对地址。
	// /快钱通过服务器连接的方式将交易结果发送到[bgUrl]对应的页面地址，在商户处理完成后输出的<result>如果为1，页面会转向到<redirecturl>对应的地址。
	// /如果快钱未接收到<redirecturl>对应的地址，快钱将把支付结果post到[pageUrl]对应的页面。
	public static String bgUrl = "bgUrl";

	// 网关版本.固定值
	// /快钱会根据版本号来调用对应的接口处理程序。
	// /本代码版本号固定为v2.0
	public static String version = "version";

	// 语言种类.固定选择值。
	// /只能选择1、2、3
	// /1代表中文；2代表英文
	// /默认值为1
	public static String language = "language";

	// 签名类型.固定值
	// /1代表MD5签名
	// /当前版本固定为1
	public static String signType = "signType";

	// 支付人姓名
	// /可为中文或英文字符
	public static String payerName = "payerName";

	// 支付人联系方式类型.固定选择值
	// /只能选择1
	// /1代表Email
	public static String payerContactType = "payerContactType";

	// 支付人联系方式
	// /只能选择Email或手机号
	public static String payerContact = "payerContact";

	// 商户订单号
	// /由字母、数字、或[-][_]组成
	public static String orderId = "orderId";

	// 订单金额
	// /以分为单位，必须是整型数字
	// /比方2，代表0.02元
	public static String orderAmount = "orderAmount";

	// 订单提交时间
	// /14位数字。年[4位]月[2位]日[2位]时[2位]分[2位]秒[2位]
	// /如；20080101010101
	public static String orderTime = "orderTime";

	// 商品名称
	// /可为中文或英文字符
	public static String productName = "productName";

	// 商品数量
	// /可为空，非空时必须为数字
	public static String productNum = "productNum";

	// 商品代码
	// /可为字符或者数字
	public static String productId = "productId";

	// 商品描述
	public static String productDesc = "productDesc";

	// 扩展字段1
	// /在支付结束后原样返回给商户
	public static String ext1 = "ext1";

	// 扩展字段2
	// /在支付结束后原样返回给商户
	public static String ext2 = "ext2";

	// 支付方式.固定选择值
	// /只能选择00、10、11、12、13、14
	// /00：组合支付（网关支付页面显示快钱支持的各种支付方式，推荐使用）10：银行卡支付（网关支付页面只显示银行卡支付）.11：电话银行支付（网关支付页面只显示电话支付）.12：快钱账户支付（网关支付页面只显示快钱账户支付）.13：线下支付（网关支付页面只显示线下支付方式）.14：B2B支付（网关支付页面只显示B2B支付，但需要向快钱申请开通才能使用）
	public static String payType = "payType";

	// 银行代码
	// /实现直接跳转到银行页面去支付,只在payType=10时才需设置参数
	// /具体代码参见 接口文档银行代码列表
	public static String bankId = "bankId";

	// 同一订单禁止重复提交标志
	// /固定选择值： 1、0
	// /1代表同一订单号只允许提交1次；0表示同一订单号在没有支付成功的前提下可重复提交多次。默认为0建议实物购物车结算类商户采用0；虚拟产品类商户采用1
	public static String redoFlag = "redoFlag";

	// 快钱的合作伙伴的账户号
	// /如未和快钱签订代理合作协议，不需要填写本参数
	public static String pid = "pid";

	// 生成加密签名串
	public static String signMsg = "signMsg";

	public static String payUrl = "payUrl";
	
	public static String isSignFail = "isSignFail";

	// 功能函数。将变量值不为空的参数组成字符串
	static public String appendParam(String returnStr, String paramId,
			String paramValue) {
		if (!returnStr.equals("")) {
			if (paramValue != null && !paramValue.equals("")) {
				returnStr = returnStr + "&" + paramId + "=" + paramValue;
			}
		} else {
			if (paramValue != null && !paramValue.equals("")) {
				returnStr = paramId + "=" + paramValue;
			}
		}
		return returnStr;
	}

}

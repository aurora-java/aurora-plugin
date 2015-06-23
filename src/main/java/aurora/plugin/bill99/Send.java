package aurora.plugin.bill99;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

public class Send extends AbstractEntry {

	// 人民币网关账户号
	// /请登录快钱系统获取用户编号，用户编号后加01即为人民币网关账户号。
	private String merchantAcctId;

	// 人民币网关密钥
	// /区分大小写.请与快钱联系索取，
	private String key;

	// 字符集.固定选择值。可为空。
	// /只能选择1、2、3.
	// /1代表UTF-8; 2代表GBK; 3代表gb2312
	// /默认值为1
	private String inputCharset;

	// 接受支付结果的页面地址.与[bgUrl]不能同时为空。必须是绝对地址。
	// /如果[bgUrl]为空，快钱将支付结果Post到[pageUrl]对应的地址。
	// /如果[bgUrl]不为空，并且[bgUrl]页面指定的<redirecturl>地址不为空，则转向到<redirecturl>对应的地址
	private String pageUrl;

	// 服务器接受支付结果的后台地址.与[pageUrl]不能同时为空。必须是绝对地址。
	// /快钱通过服务器连接的方式将交易结果发送到[bgUrl]对应的页面地址，在商户处理完成后输出的<result>如果为1，页面会转向到<redirecturl>对应的地址。
	// /如果快钱未接收到<redirecturl>对应的地址，快钱将把支付结果post到[pageUrl]对应的页面。
	private String bgUrl;

	// 网关版本.固定值
	// /快钱会根据版本号来调用对应的接口处理程序。
	// /本代码版本号固定为v2.0
	private String version;

	// 语言种类.固定选择值。
	// /只能选择1、2、3
	// /1代表中文；2代表英文
	// /默认值为1
	private String language;

	// 签名类型.固定值
	// /1代表MD5签名
	// /当前版本固定为1
	private String signType;

	// 支付人姓名
	// /可为中文或英文字符
	private String payerName;

	// 支付人联系方式类型.固定选择值
	// /只能选择1
	// /1代表Email
	private String payerContactType;

	// 支付人联系方式
	// /只能选择Email或手机号
	private String payerContact;

	// 商户订单号
	// /由字母、数字、或[-][_]组成
	private String orderId;

	// 订单金额
	// /以分为单位，必须是整型数字
	// /比方2，代表0.02元
	private String orderAmount;

	// 订单提交时间
	// /14位数字。年[4位]月[2位]日[2位]时[2位]分[2位]秒[2位]
	// /如；20080101010101
	private String orderTime;

	// 商品名称
	// /可为中文或英文字符
	private String productName;

	// 商品数量
	// /可为空，非空时必须为数字
	private String productNum;

	// 商品代码
	// /可为字符或者数字
	private String productId;

	// 商品描述
	private String productDesc;

	// 扩展字段1
	// /在支付结束后原样返回给商户
	private String ext1;

	// 扩展字段2
	// /在支付结束后原样返回给商户
	private String ext2;

	// 支付方式.固定选择值
	// /只能选择00、10、11、12、13、14
	// /00：组合支付（网关支付页面显示快钱支持的各种支付方式，推荐使用）10：银行卡支付（网关支付页面只显示银行卡支付）.11：电话银行支付（网关支付页面只显示电话支付）.12：快钱账户支付（网关支付页面只显示快钱账户支付）.13：线下支付（网关支付页面只显示线下支付方式）.14：B2B支付（网关支付页面只显示B2B支付，但需要向快钱申请开通才能使用）
	private String payType;

	// 银行代码
	// /实现直接跳转到银行页面去支付,只在payType=10时才需设置参数
	// /具体代码参见 接口文档银行代码列表
	private String bankId;

	// 同一订单禁止重复提交标志
	// /固定选择值： 1、0
	// /1代表同一订单号只允许提交1次；0表示同一订单号在没有支付成功的前提下可重复提交多次。默认为0建议实物购物车结算类商户采用0；虚拟产品类商户采用1
	private String redoFlag;

	// 快钱的合作伙伴的账户号
	// /如未和快钱签订代理合作协议，不需要填写本参数
	private String pid;
	// 快钱付款地址
	private String payUrl;

	// 配置文件名
	private String configFile;

	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance
				.getInstance(context);

		HttpServletRequest request = serviceInstance.getRequest();
		request.setCharacterEncoding("GBK");
		ServiceContext service = ServiceContext.createServiceContext(context);

		configFile = getValue(context, this.getConfigFile());

		CompositeMap model = service.getModel();
		initConfig();
		CompositeMap bill99 = model.createChild("bill99");

		pageUrl = getValue(context, this.getPageUrl());
		bgUrl = getValue(context, this.getBgUrl());
		payerName = getValue(context, this.getPayerName());
		payerContact = getValue(context, this.getPayerContact());
		orderId = getValue(context, this.getOrderId());
		orderAmount = getValue(context, this.getOrderAmount());
		productName = getValue(context, this.getProductName());
		productNum = getValue(context, this.getProductNum());
		ext2 = getValue(context, this.getExt2());
		ext1 = getValue(context, this.getExt1());
		productDesc = getValue(context, this.getProductDesc());
		productId = getValue(context, this.getProductId());
		orderTime = getValue(context, this.getOrderTime());

		payType = getValue(context, this.getPayType());
		if (payType == null || "".equals(payType))
			this.setPayType(getVaule(Bill99.payType));

		merchantAcctId = getValue(context, this.getMerchantAcctId());
		if (merchantAcctId == null || "".equals(merchantAcctId))
			this.setMerchantAcctId(getVaule(Bill99.merchantAcctId));

		key = getValue(context, this.getKey());
		if (key == null || "".equals(key))
			this.setKey(getVaule(Bill99.key));

		// if (configFile == null || "".equals(configFile))
		// this.setKey(getVaule(Bill99.key));

		bill99.put(Bill99.inputCharset, inputCharset);
		bill99.put(Bill99.pageUrl, pageUrl);
		bill99.put(Bill99.bgUrl, bgUrl);
		bill99.put(Bill99.version, version);
		bill99.put(Bill99.language, language);
		bill99.put(Bill99.signType, signType);
		bill99.put(Bill99.merchantAcctId, getMerchantAcctId());
		bill99.put(Bill99.payerName, payerName);
		bill99.put(Bill99.payerContactType, payerContactType);
		bill99.put(Bill99.payerContact, payerContact);
		bill99.put(Bill99.orderId, orderId);
		bill99.put(Bill99.orderAmount, orderAmount);
		bill99.put(Bill99.orderTime, orderTime);
		bill99.put(Bill99.productName, productName);
		bill99.put(Bill99.productNum, productNum);
		bill99.put(Bill99.productId, productId);
		bill99.put(Bill99.productDesc, productDesc);
		bill99.put(Bill99.ext1, ext1);
		bill99.put(Bill99.ext2, ext2);
		bill99.put(Bill99.payType, payType);
		bill99.put(Bill99.bankId, bankId);
		bill99.put(Bill99.redoFlag, redoFlag);
		bill99.put(Bill99.pid, pid);
		if ("1".equals(signType)) {
			bill99.put(Bill99.key, getKey());
		}
		bill99.put(Bill99.payUrl, payUrl);
		if ("1".equals(signType)) {
			String signMsg = createSignMD5MSG();
			bill99.put(Bill99.signMsg, signMsg);
		} else if ("4".equals(signType)) {
			SendReceivePKipair pki = new SendReceivePKipair(configFile);
			String signMsg = pki.signMsg(createPKIMessage());
			bill99.put(Bill99.signMsg, signMsg);
		} else {
			throw new RuntimeException("signType 配置错误，1：为MD5，4：为签名方式");
		}

	}

	private String getValue(CompositeMap context, String key) {
		if (key != null) {
			return TextParser.parse(key, context);
		} else {
			return "";
		}
	}

	private String createSignMD5MSG() throws UnsupportedEncodingException {
		// 生成加密签名串
		String signMsgVal = createMD5SendMessage();
		String signMsg = MD5Util.md5Hex(signMsgVal.getBytes("gb2312"))
				.toUpperCase();
		return signMsg;
	}

	private String createPKIMessage() {
		// /请务必按照如下顺序和规则组成加密串！
		String signMsgVal = "";
		signMsgVal = Bill99.appendParam(signMsgVal, "inputCharset",
				inputCharset);
		signMsgVal = Bill99.appendParam(signMsgVal, "pageUrl", pageUrl);
		signMsgVal = Bill99.appendParam(signMsgVal, "bgUrl", bgUrl);
		signMsgVal = Bill99.appendParam(signMsgVal, "version", version);
		signMsgVal = Bill99.appendParam(signMsgVal, "language", language);
		signMsgVal = Bill99.appendParam(signMsgVal, "signType", signType);
		signMsgVal = Bill99.appendParam(signMsgVal, "merchantAcctId",
				getMerchantAcctId());
		signMsgVal = Bill99.appendParam(signMsgVal, "payerName", payerName);
		signMsgVal = Bill99.appendParam(signMsgVal, "payerContactType",
				payerContactType);
		signMsgVal = Bill99.appendParam(signMsgVal, "payerContact",
				payerContact);
		signMsgVal = Bill99.appendParam(signMsgVal, "orderId", orderId);
		signMsgVal = Bill99.appendParam(signMsgVal, "orderAmount", orderAmount);
		signMsgVal = Bill99.appendParam(signMsgVal, "orderTime", orderTime);
		signMsgVal = Bill99.appendParam(signMsgVal, "productName", productName);
		signMsgVal = Bill99.appendParam(signMsgVal, "productNum", productNum);
		signMsgVal = Bill99.appendParam(signMsgVal, "productId", productId);
		signMsgVal = Bill99.appendParam(signMsgVal, "productDesc", productDesc);
		signMsgVal = Bill99.appendParam(signMsgVal, "ext1", ext1);
		signMsgVal = Bill99.appendParam(signMsgVal, "ext2", ext2);
		signMsgVal = Bill99.appendParam(signMsgVal, "payType", payType);
		signMsgVal = Bill99.appendParam(signMsgVal, "bankId", bankId);
		signMsgVal = Bill99.appendParam(signMsgVal, "redoFlag", redoFlag);
		signMsgVal = Bill99.appendParam(signMsgVal, "pid", pid);
		return signMsgVal;
	}

	private String createMD5SendMessage() {
		// /请务必按照如下顺序和规则组成加密串！
		String signMsgVal = "";
		signMsgVal = Bill99.appendParam(signMsgVal, Bill99.inputCharset,
				inputCharset);
		signMsgVal = Bill99.appendParam(signMsgVal, Bill99.pageUrl, pageUrl);
		signMsgVal = Bill99.appendParam(signMsgVal, Bill99.bgUrl, bgUrl);
		signMsgVal = Bill99.appendParam(signMsgVal, Bill99.version, version);
		signMsgVal = Bill99.appendParam(signMsgVal, Bill99.language, language);
		signMsgVal = Bill99.appendParam(signMsgVal, Bill99.signType, signType);
		signMsgVal = Bill99.appendParam(signMsgVal, Bill99.merchantAcctId,
				getMerchantAcctId());
		signMsgVal = Bill99
				.appendParam(signMsgVal, Bill99.payerName, payerName);
		signMsgVal = Bill99.appendParam(signMsgVal, Bill99.payerContactType,
				payerContactType);
		signMsgVal = Bill99.appendParam(signMsgVal, Bill99.payerContact,
				payerContact);
		signMsgVal = Bill99.appendParam(signMsgVal, Bill99.orderId, orderId);
		signMsgVal = Bill99.appendParam(signMsgVal, Bill99.orderAmount,
				orderAmount);
		signMsgVal = Bill99
				.appendParam(signMsgVal, Bill99.orderTime, orderTime);
		signMsgVal = Bill99.appendParam(signMsgVal, Bill99.productName,
				productName);
		signMsgVal = Bill99.appendParam(signMsgVal, Bill99.productNum,
				productNum);
		signMsgVal = Bill99
				.appendParam(signMsgVal, Bill99.productId, productId);
		signMsgVal = Bill99.appendParam(signMsgVal, Bill99.productDesc,
				productDesc);
		signMsgVal = Bill99.appendParam(signMsgVal, Bill99.ext1, ext1);
		signMsgVal = Bill99.appendParam(signMsgVal, Bill99.ext2, ext2);
		signMsgVal = Bill99.appendParam(signMsgVal, Bill99.payType, payType);
		signMsgVal = Bill99.appendParam(signMsgVal, Bill99.bankId, bankId);
		signMsgVal = Bill99.appendParam(signMsgVal, Bill99.redoFlag, redoFlag);
		signMsgVal = Bill99.appendParam(signMsgVal, Bill99.pid, pid);
		signMsgVal = Bill99.appendParam(signMsgVal, Bill99.key, getKey());
		return signMsgVal;
	}

	public void initConfig() {
		// 人民币网关账户号
		// 请登录快钱系统获取用户编号，用户编号后加01即为人民币网关账户号。
		this.setMerchantAcctId(getVaule(Bill99.merchantAcctId));
		// 人民币网关密钥
		// 区分大小写.请与快钱联系索取，
		this.setKey(getVaule(Bill99.key));

		// 字符集.固定选择值。可为空。
		// 只能选择1、2、3.
		// 1代表UTF-8; 2代表GBK; 3代表gb2312
		// 默认值为1
		this.inputCharset = getVaule(Bill99.inputCharset);
		// 网关版本.固定值
		// 快钱会根据版本号来调用对应的接口处理程序。
		// 本代码版本号固定为v2.0
		this.version = getVaule(Bill99.version);
		// #语言种类.固定选择值。
		// #只能选择1、2、3
		// #1代表中文；2代表英文
		// #默认值为1
		this.language = getVaule(Bill99.language);
		// #签名类型.固定值
		// #1代表MD5签名
		// #当前版本固定为1
		// 签名类型,该值为4，代表PKI加密方式,该参数必填。
		this.signType = getVaule(Bill99.signType);
		// #支付人联系方式类型.固定选择值
		// #只能选择1
		// #1代表Email
		this.payerContactType = getVaule(Bill99.payerContactType);
		// #支付方式.固定选择值
		// #只能选择00、10、11、12、13、14
		// #00：组合支付（网关支付页面显示快钱支持的各种支付方式，推荐使用）10：银行卡支付（网关支付页面只显示银行卡支付）.11：电话银行支付（网关支付页面只显示电话支付）.12：快钱账户支付（网关支付页面只显示快钱账户支付）.13：线下支付（网关支付页面只显示线下支付方式）.14：B2B支付（网关支付页面只显示B2B支付，但需要向快钱申请开通才能使用）
		//
		// this.setPayType(getVaule(Bill99.payType));
		// #银行代码
		// #实现直接跳转到银行页面去支付,只在payType=10时才需设置参数
		// #具体代码参见 接口文档银行代码列表
		this.bankId = getVaule(Bill99.bankId);
		// #同一订单禁止重复提交标志
		// #固定选择值： 1、0
		// #1代表同一订单号只允许提交1次；0表示同一订单号在没有支付成功的前提下可重复提交多次。默认为0建议实物购物车结算类商户采用0；虚拟产品类商户采用1
		this.redoFlag = getVaule(Bill99.redoFlag);

		// #快钱的合作伙伴的账户号
		// #如未和快钱签订代理合作协议，不需要填写本参数
		this.pid = getVaule(Bill99.pid);

		payUrl = getVaule(Bill99.payUrl);
	}

	private String getVaule(String key) {
		String value = Configuration
				.getValue(
						configFile == null || "".equals(configFile) ? Configuration.DEFAULT_CONFIG_FILE
								: configFile, key);
		// .getInstance().getValue(key);
		return value == null ? "" : value;
	}

	public String getMerchantAcctId() {
		return merchantAcctId;
	}

	public void setOrderTime(String orderTime) {
		this.orderTime = orderTime;
	}

	public String getKey() {
		return key;
	}

	public String getInputCharset() {
		return inputCharset;
	}

	public String getPageUrl() {
		return pageUrl;
	}

	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}

	public String getBgUrl() {
		return bgUrl;
	}

	public void setBgUrl(String bgUrl) {
		this.bgUrl = bgUrl;
	}

	public String getVersion() {
		return version;
	}

	public String getLanguage() {
		return language;
	}

	public String getSignType() {
		return signType;
	}

	public String getPayerName() {
		return payerName;
	}

	public void setPayerName(String payerName) {
		this.payerName = payerName;
	}

	public String getPayerContactType() {
		return payerContactType;
	}

	public String getPayerContact() {
		return payerContact;
	}

	public void setPayerContact(String payerContact) {
		this.payerContact = payerContact;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getOrderAmount() {
		return orderAmount;
	}

	public void setOrderAmount(String orderAmount) {
		this.orderAmount = orderAmount;
	}

	public String getOrderTime() {
		return orderTime;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductNum() {
		return productNum;
	}

	public void setProductNum(String productNum) {
		this.productNum = productNum;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getProductDesc() {
		return productDesc;
	}

	public void setProductDesc(String productDesc) {
		this.productDesc = productDesc;
	}

	public String getExt1() {
		return ext1;
	}

	public void setExt1(String ext1) {
		this.ext1 = ext1;
	}

	public String getExt2() {
		return ext2;
	}

	public void setExt2(String ext2) {
		this.ext2 = ext2;
	}

	public String getPayType() {
		return payType;
	}

	public String getBankId() {
		return bankId;
	}

	public String getRedoFlag() {
		return redoFlag;
	}

	public String getPid() {
		return pid;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public void setMerchantAcctId(String merchantAcctId) {
		this.merchantAcctId = merchantAcctId;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

}
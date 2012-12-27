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

public class Receive extends AbstractEntry {
	// 获取人民币网关账户号
	private String merchantAcctId;

	// 设置人民币网关密钥
	// /区分大小写

	private String key;

	// 获取网关版本.固定值
	// /快钱会根据版本号来调用对应的接口处理程序。
	// /本代码版本号固定为v2.0
	private String version;

	// 获取语言种类.固定选择值。
	// /只能选择1、2、3
	// /1代表中文；2代表英文
	// /默认值为1
	private String language;
	// 签名类型.固定值
	// /1代表MD5签名
	// /当前版本固定为1
	private String signType;

	// 获取支付方式
	// /值为：10、11、12、13、14
	// /00：组合支付（网关支付页面显示快钱支持的各种支付方式，推荐使用）10：银行卡支付（网关支付页面只显示银行卡支付）.11：电话银行支付（网关支付页面只显示电话支付）.12：快钱账户支付（网关支付页面只显示快钱账户支付）.13：线下支付（网关支付页面只显示线下支付方式）.14：B2B支付（网关支付页面只显示B2B支付，但需要向快钱申请开通才能使用）
	private String payType;

	// 获取银行代码
	// /参见银行代码列表
	private String bankId;

	// 获取商户订单号
	private String orderId;

	// 获取订单提交时间
	// /获取商户提交订单时的时间.14位数字。年[4位]月[2位]日[2位]时[2位]分[2位]秒[2位]
	// /如：20080101010101
	private String orderTime;

	// 获取原始订单金额
	// /订单提交到快钱时的金额，单位为分。
	// /比方2 ，代表0.02元
	private String orderAmount;

	// 获取快钱交易号
	// /获取该交易在快钱的交易号
	private String dealId;

	// 获取银行交易号
	// /如果使用银行卡支付时，在银行的交易号。如不是通过银行支付，则为空
	private String bankDealId;

	// 获取在快钱交易时间
	// /14位数字。年[4位]月[2位]日[2位]时[2位]分[2位]秒[2位]
	// /如；20080101010101
	private String dealTime;

	// 获取实际支付金额
	// /单位为分
	// /比方 2 ，代表0.02元
	private String payAmount;

	// 获取交易手续费
	// /单位为分
	// /比方 2 ，代表0.02元
	private String fee;

	// 获取扩展字段1
	private String ext1;

	// 获取扩展字段2
	private String ext2;

	// 获取处理结果
	// /10代表 成功11代表 失败
	private String payResult;

	// 获取错误代码
	// /详细见文档错误代码列表
	private String errCode;

	// 获取加密签名串
	private String signMsg;

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

		CompositeMap bill99 = model.createChild("bill99");

		// 获取人民币网关账户号
		merchantAcctId = (String) request.getParameter("merchantAcctId").trim();

		// 设置人民币网关密钥
		// /区分大小写

		setKey(getVaule(Bill99.key));

		// key = getValue(context, this.getKey());
		// if (key == null || "".equals(key))
		// this.setKey(getVaule(Bill99.key));

		// 获取网关版本.固定值
		// /快钱会根据版本号来调用对应的接口处理程序。
		// /本代码版本号固定为v2.0
		version = (String) request.getParameter("version").trim();

		// 获取语言种类.固定选择值。
		// /只能选择1、2、3
		// /1代表中文；2代表英文
		// /默认值为1
		language = (String) request.getParameter("language").trim();

		// 签名类型.固定值
		// /1代表MD5签名
		// /当前版本固定为1
		signType = (String) request.getParameter("signType").trim();

		// 获取支付方式
		// /值为：10、11、12、13、14
		// /00：组合支付（网关支付页面显示快钱支持的各种支付方式，推荐使用）10：银行卡支付（网关支付页面只显示银行卡支付）.11：电话银行支付（网关支付页面只显示电话支付）.12：快钱账户支付（网关支付页面只显示快钱账户支付）.13：线下支付（网关支付页面只显示线下支付方式）.14：B2B支付（网关支付页面只显示B2B支付，但需要向快钱申请开通才能使用）
		payType = (String) request.getParameter("payType").trim();

		// 获取银行代码
		// /参见银行代码列表
		bankId = (String) request.getParameter("bankId").trim();

		// 获取商户订单号
		orderId = (String) request.getParameter("orderId").trim();

		// 获取订单提交时间
		// /获取商户提交订单时的时间.14位数字。年[4位]月[2位]日[2位]时[2位]分[2位]秒[2位]
		// /如：20080101010101
		orderTime = (String) request.getParameter("orderTime").trim();

		// 获取原始订单金额
		// /订单提交到快钱时的金额，单位为分。
		// /比方2 ，代表0.02元
		orderAmount = (String) request.getParameter("orderAmount").trim();

		// 获取快钱交易号
		// /获取该交易在快钱的交易号
		dealId = (String) request.getParameter("dealId").trim();

		// 获取银行交易号
		// /如果使用银行卡支付时，在银行的交易号。如不是通过银行支付，则为空
		bankDealId = (String) request.getParameter("bankDealId").trim();

		// 获取在快钱交易时间
		// /14位数字。年[4位]月[2位]日[2位]时[2位]分[2位]秒[2位]
		// /如；20080101010101
		dealTime = (String) request.getParameter("dealTime").trim();

		// 获取实际支付金额
		// /单位为分
		// /比方 2 ，代表0.02元
		payAmount = (String) request.getParameter("payAmount").trim();

		// 获取交易手续费
		// /单位为分
		// /比方 2 ，代表0.02元
		fee = (String) request.getParameter("fee").trim();

		// 获取扩展字段1
		ext1 = (String) request.getParameter("ext1").trim();

		// 获取扩展字段2
		ext2 = (String) request.getParameter("ext2").trim();

		// 获取处理结果
		// /10代表 成功11代表 失败
		payResult = (String) request.getParameter("payResult").trim();

		// 获取错误代码
		// /详细见文档错误代码列表
		errCode = (String) request.getParameter("errCode").trim();

		// 获取加密签名串
		signMsg = (String) request.getParameter("signMsg").trim();
		String merchantSignMsg = "";
		boolean isSignFail = true;
		if ("1".equals(signType)) {
			merchantSignMsg = createSignMSG();
			isSignFail = !signMsg.toUpperCase().equals(
					merchantSignMsg.toUpperCase());
		} else if ("4".equals(signType)) {
			String createReceiveMsg = createPKiReceiveMsg();
			SendReceivePKipair pki = new SendReceivePKipair(configFile);
			isSignFail = !pki.enCodeByCer(createReceiveMsg, signMsg);
		} else {
			throw new RuntimeException("接收来自块钱的signType 配置错误，1：为MD5，4：为签名方式");
		}

		bill99.put(Bill99.orderAmount, orderAmount);
		bill99.put(Bill99.orderTime, orderTime);
		bill99.put(Bill99.payType, payType);
		bill99.put(Bill99.bankId, bankId);
		bill99.put(Bill99.orderId, orderId);
		// bill99.put(Bill99.key, key);
		bill99.put(Bill99.merchantAcctId, merchantAcctId);
		bill99.put(Bill99.version, version);
		bill99.put(Bill99.language, language);
		bill99.put(Bill99.signType, signType);
		bill99.put(Bill99.dealId, dealId);
		bill99.put(Bill99.bankDealId, bankDealId);
		bill99.put(Bill99.dealTime, dealTime);
		bill99.put(Bill99.payAmount, payAmount);
		bill99.put(Bill99.fee, fee);
		bill99.put(Bill99.ext1, ext1);
		bill99.put(Bill99.ext2, ext2);
		bill99.put(Bill99.payResult, payResult);
		bill99.put(Bill99.errCode, errCode);
		bill99.put(Bill99.signMsg, signMsg);
		bill99.put(Bill99.isSignFail, isSignFail);
	}

	private String createPKiReceiveMsg() {
		String merchantSignMsgVal = "";
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				"merchantAcctId", merchantAcctId);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal, "version",
				version);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal, "language",
				language);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal, "signType",
				signType);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal, "payType",
				payType);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal, "bankId",
				bankId);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal, "orderId",
				orderId);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				"orderTime", orderTime);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				"orderAmount", orderAmount);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal, "dealId",
				dealId);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				"bankDealId", bankDealId);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal, "dealTime",
				dealTime);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				"payAmount", payAmount);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal, "fee", fee);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal, "ext1",
				ext1);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal, "ext2",
				ext2);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				"payResult", payResult);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal, "errCode",
				errCode);
		return merchantSignMsgVal;
	}

	private String createSignMSG() throws UnsupportedEncodingException {
		String merchantSignMsgVal = createMD5ReceiveMsg();

		String merchantSignMsg = MD5Util.md5Hex(
				merchantSignMsgVal.getBytes("gb2312")).toUpperCase();
		return merchantSignMsg;
	}

	private String createMD5ReceiveMsg() {
		// 生成加密串。必须保持如下顺序。
		String merchantSignMsgVal = "";
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				Bill99.merchantAcctId, merchantAcctId);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				Bill99.version, version);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				Bill99.language, language);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				Bill99.signType, signType);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				Bill99.payType, payType);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				Bill99.bankId, bankId);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				Bill99.orderId, orderId);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				Bill99.orderTime, orderTime);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				Bill99.orderAmount, orderAmount);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				Bill99.dealId, dealId);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				Bill99.bankDealId, bankDealId);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				Bill99.dealTime, dealTime);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				Bill99.payAmount, payAmount);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal, Bill99.fee,
				fee);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				Bill99.ext1, ext1);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				Bill99.ext2, ext2);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				Bill99.payResult, payResult);
		merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
				Bill99.errCode, errCode);
		if ("1".equals(signType)) {
			merchantSignMsgVal = Bill99.appendParam(merchantSignMsgVal,
					Bill99.key, getKey());
		}
		return merchantSignMsgVal;
	}

	private String getVaule(String key) {
		String value = Configuration.getValue(configFile, key);
		// .getInstance().getValue(key);
		return value == null ? "" : value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	private String getValue(CompositeMap context, String key) {
		if (key != null) {
			return TextParser.parse(key, context);
		} else {
			return "";
		}
	}

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

}

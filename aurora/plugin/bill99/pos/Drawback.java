package aurora.plugin.bill99.pos;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.plugin.bill99.Bill99;
import aurora.plugin.bill99.Configuration;
import aurora.plugin.bill99.HttpUtils;
import aurora.plugin.bill99.MD5Util;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

public class Drawback extends AbstractEntry {
	// 商户编号
	// 对于快钱新系统注册商户，该值为商户在快钱的会员号；对于快钱老系统注册商户，该值为商户的商户编号。
	private String merchant_id;

	// 客户编号所对应的密钥。。在账户邮箱中获取
	private String merchant_key;

	// 退款接口版本号 固定值：bill_drawback_api_1
	private String version;

	// 操作类型 固定值：001 001代表下订单请求退款
	private String command_type;

	// 退款请求地址
	private String drawbackUrl;

	// 退款流水号
	private String txOrder;

	// 退款金额 整数或小数，小数位为两位 以人民币元为单位。
	private String amount;

	// 退款提交时间 数字串，一共14位
	// 格式为：年[4位]月[2位]日[2位]时[2位]分[2位]秒[2位]
	// 例如：20071117020101
	private String postdate;

	// 原商户订单号 字符串，与用户支付时的订单号相同
	// 只允许使用字母、数字、- 、_,并以字母或数字开头
	private String orderid;

	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance
				.getInstance(context);
		HttpServletRequest request = serviceInstance.getRequest();
		request.setCharacterEncoding("GBK");
		ServiceContext service = ServiceContext.createServiceContext(context);
		CompositeMap model = service.getModel();
		this.initConfig();
		CompositeMap bill99 = model.createChild("bill99");

		// 退款流水号
		txOrder = getValue(context, this.getTxOrder());

		// 退款金额 整数或小数，小数位为两位 以人民币元为单位。
		amount = getValue(context, this.getAmount());

		// 退款提交时间 数字串，一共14位
		// 格式为：年[4位]月[2位]日[2位]时[2位]分[2位]秒[2位]
		// 例如：20071117020101
		postdate = getValue(context, this.getPostdate());

		// 原商户订单号 字符串，与用户支付时的订单号相同
		// 只允许使用字母、数字、- 、_,并以字母或数字开头
		orderid = getValue(context, this.getOrderid());

		String mac = createSignMSG();

		Map reParams = new HashMap();
		reParams.put(Bill99.mac, mac);
		reParams.put(Bill99.txOrder, txOrder);
		reParams.put(Bill99.postdate, postdate);
		reParams.put(Bill99.amount, amount);
		reParams.put(Bill99.orderid, orderid);
		reParams.put(Bill99.command_type, command_type);
		reParams.put(Bill99.version, version);
		reParams.put(Bill99.merchant_id, merchant_id);

		String xml = drawbackRequest(drawbackUrl, reParams);
		CompositeMap map = CompositeLoader.createInstanceForOCM()
				.loadFromString(xml,"utf-8");
		List childs = map.getChilds();
		for (int i = 0; i < childs.size(); i++) {
			CompositeMap child = (CompositeMap) childs.get(i);
			String name = child.getName().toLowerCase();
			String value = child.getText();
			bill99.put(name, value);
		}
	}

	private String drawbackRequest(String drawbackUrl, Map reParams)
			throws IOException {
		StringBuffer buffer = new StringBuffer();
		List urlPost = HttpUtils.URLPost(drawbackUrl, reParams);
		for (int i = 0; i < urlPost.size(); i++) {
			buffer.append(urlPost.get(i));
		}
		return buffer.toString();
	}

	private String getValue(CompositeMap context, String key) {
		if (key != null) {
			return TextParser.parse(key, context);
		} else {
			return "";
		}

	}

	public void initConfig() {
		// 人民币网关密钥
		// 区分大小写.请与快钱联系索取，
		// 商户编号
		// 对于快钱新系统注册商户，该值为商户在快钱的会员号；对于快钱老系统注册商户，该值为商户的商户编号。
		merchant_id = getVaule(Bill99.merchant_id);

		// 客户编号所对应的密钥。。在账户邮箱中获取
		merchant_key = getVaule(Bill99.merchant_key);

		// 退款接口版本号 固定值：bill_drawback_api_1
		version = getVaule(Bill99.drawback_version);

		// 操作类型 固定值：001 001代表下订单请求退款
		command_type = getVaule(Bill99.command_type);

		// 退款请求地址
		drawbackUrl = getVaule(Bill99.drawbackUrl);
	}

	private String createSignMSG() throws UnsupportedEncodingException {
		// 生成加密签名串
		// /请务必按照如下顺序和规则组成加密串！
		String macstr = "";
		macstr = Bill99.appendParam(macstr, Bill99.merchant_id, merchant_id);
		macstr = Bill99.appendParam(macstr, Bill99.version, version);
		macstr = Bill99.appendParam(macstr, Bill99.command_type, command_type);
		macstr = Bill99.appendParam(macstr, Bill99.orderid, orderid);
		macstr = Bill99.appendParam(macstr, Bill99.amount, amount);
		macstr = Bill99.appendParam(macstr, Bill99.postdate, postdate);
		macstr = Bill99.appendParam(macstr, Bill99.txOrder, txOrder);
		macstr = Bill99.appendParam(macstr, Bill99.merchant_key, merchant_key);

		String mac = MD5Util.md5Hex(macstr.getBytes("gb2312")).toUpperCase();

		return mac;
	}

	private String getVaule(String key) {
		String value = Configuration.getValue(Configuration.DEFAULT_CONFIG_FILE, key);
//		String value = Configuration.getInstance().getValue(key);
		return value == null ? "" : value;
	}

	public String getTxOrder() {
		return txOrder;
	}

	public void setTxOrder(String txOrder) {
		this.txOrder = txOrder;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getPostdate() {
		return postdate;
	}

	public void setPostdate(String postdate) {
		this.postdate = postdate;
	}

	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}

}

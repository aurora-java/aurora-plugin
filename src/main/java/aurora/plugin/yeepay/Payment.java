package aurora.plugin.yeepay;
import javax.servlet.http.HttpServletRequest;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

import com.yeepay.Configuration;
import com.yeepay.PaymentForOnlineService;


public class Payment extends AbstractEntry {
	
	private String order;
	private String amt;
	private String cur;
	private String pid;
	private String pcat;
	private String pdesc;
	private String url;
	private String saf;
	private String mp;
	private String frpid;
	private String needresponse;
	
	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance.getInstance(context);
		HttpServletRequest request = serviceInstance.getRequest();
		request.setCharacterEncoding("GBK");
		ServiceContext service = ServiceContext.createServiceContext(context);
		CompositeMap model = service.getModel();
		
		String keyValue = YeePay.formatString(Configuration.getInstance().getValue(YeePay.KEY_KEY_VALUE)); // 商家密钥
		String nodeAuthorizationURL = YeePay.formatString(Configuration.getInstance().getValue(YeePay.KEY_COMMON_REQ_URL)); // 交易请求地址
		// 商家设置用户购买商品的支付信息
		String p0_Cmd = YeePay.formatString(YeePay.KEY_BUY); //在线支付请求，固定值 ”Buy”
		String p1_MerId = YeePay.formatString(Configuration.getInstance().getValue(YeePay.KEY_P1_MER_ID)); // 商户编号
		
		String p2_Order = YeePay.formatString(getValue(context,getOrder()));//request.getParameter(YeePay.KEY_P2_ORDER)); // 商户订单号
		String p3_Amt = YeePay.formatString(getValue(context,getAmt()));//request.getParameter(YeePay.KEY_P3_ATM)); // 支付金额
		
		String p4_Cur = YeePay.formatString(YeePay.KEY_CNY); // 交易币种
		
		String p5_Pid = YeePay.formatString(getValue(context,getPid()));//request.getParameter(YeePay.KEY_P5_PID)); // 商品名称
		String p6_Pcat = YeePay.formatString(getValue(context,getPcat()));//request.getParameter(YeePay.KEY_P6_PCAT)); // 商品种类
		String p7_Pdesc = YeePay.formatString(getValue(context,getPdesc()));//request.getParameter(YeePay.KEY_P7_PDESC)); // 商品描述
		String p8_Url = YeePay.formatString(getValue(context,getUrl()));//request.getParameter(YeePay.KEY_P8_URL)); // 商户接收支付成功数据的地址
		String p9_SAF = YeePay.formatString(getValue(context,getSaf()));//request.getParameter(YeePay.KEY_P9_SAF)); // 需要填写送货信息 0：不需要  1:需要
		String pa_MP = YeePay.formatString(getValue(context,getMp()));//request.getParameter(YeePay.KEY_PA_MP)); // 商户扩展信息
		String pd_FrpId = YeePay.formatString(getValue(context,getFrpid()));//request.getParameter(YeePay.KEY_PD_FRPID)); // 支付通道编码		
		
		
		pd_FrpId = pd_FrpId.toUpperCase();// 银行编号必须大写
		String pr_NeedResponse = YeePay.formatString("1"); // 默认为"1"，需要应答机制
		String hmac = YeePay.formatString(""); // 交易签名串
		
		// 获得MD5-HMAC签名
		hmac = PaymentForOnlineService.getReqMd5HmacForOnlinePayment(p0_Cmd,
				p1_MerId, p2_Order, p3_Amt, p4_Cur, p5_Pid, p6_Pcat, p7_Pdesc,
				p8_Url, p9_SAF, pa_MP, pd_FrpId, pr_NeedResponse, keyValue);
		
		model.put(YeePay.KEY_P0_CMD, p0_Cmd);
		model.put(YeePay.KEY_P1_MER_ID, p1_MerId);
		model.put(YeePay.KEY_P2_ORDER, p2_Order);
		model.put(YeePay.KEY_P3_ATM, p3_Amt);
		model.put(YeePay.KEY_P4_CUR, p4_Cur);
		model.put(YeePay.KEY_P5_PID, p5_Pid);
		model.put(YeePay.KEY_P6_PCAT, p6_Pcat);
		model.put(YeePay.KEY_P7_PDESC, p7_Pdesc);
		model.put(YeePay.KEY_P8_URL, p8_Url);
		model.put(YeePay.KEY_P9_SAF, p9_SAF);
		model.put(YeePay.KEY_PA_MP, pa_MP);
		model.put(YeePay.KEY_PD_FRPID, pd_FrpId);
		model.put(YeePay.KEY_NEED_RESPONSE, pr_NeedResponse);
		model.put(YeePay.KEY_HMAC, hmac);
		model.put(YeePay.KEY_URL, nodeAuthorizationURL);
	}
	
	private String getValue(CompositeMap context, String key){
		if(key!=null){
			return TextParser.parse(key, context);
		}else{
			return null;
		}
		
	}


	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getAmt() {
		return amt;
	}

	public void setAmt(String amt) {
		this.amt = amt;
	}

	public String getCur() {
		return cur;
	}

	public void setCur(String cur) {
		this.cur = cur;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getPcat() {
		return pcat;
	}

	public void setPcat(String pcat) {
		this.pcat = pcat;
	}

	public String getPdesc() {
		return pdesc;
	}

	public void setPdesc(String pdesc) {
		this.pdesc = pdesc;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSaf() {
		return saf;
	}

	public void setSaf(String saf) {
		this.saf = saf;
	}

	public String getMp() {
		return mp;
	}

	public void setMp(String mp) {
		this.mp = mp;
	}

	public String getFrpid() {
		return frpid;
	}

	public void setFrpid(String frpid) {
		this.frpid = frpid;
	}

	public String getNeedresponse() {
		return needresponse;
	}

	public void setNeedresponse(String needresponse) {
		this.needresponse = needresponse;
	}
	
	
//	public static void main(String[] args){
//		String keyValue = "69cl522AV6q613Ii4W6u8K6XuW8vM1N6bFgyv769220IuYe9u37N4y7rI4Pl"; // 商家密钥
//		String p0_Cmd = "Buy"; // 在线支付请求，固定值 ”Buy”
//		String p1_MerId = "10001126856"; // 商户编号
//		String p2_Order = "58c5a67938ce4c2fa7d555aac5c39bc1"; // 商户订单号
//		String p3_Amt = "0.01"; // 支付金额
//		String p4_Cur = "CNY"; // 交易币种
//		String p5_Pid = "葆婴商品名称"; // 商品名称
//		String p6_Pcat = ""; // 商品种类
//		String p7_Pdesc = ""; // 商品描述
//		String p8_Url = "http://localhost:8080/yeepay/callback.jsp"; // 商户接收支付成功数据的地址
//		String p9_SAF = "0"; // 需要填写送货信息 0：不需要  1:需要
//		String pa_MP = ""; // 商户扩展信息
//		String pd_FrpId = ""; // 支付通道编码		
//		pd_FrpId = pd_FrpId.toUpperCase();// 银行编号必须大写
//		String pr_NeedResponse = YeePay.formatString("1"); // 默认为"1"，需要应答机制
//		
//		System.out.println(PaymentForOnlineService.getReqMd5HmacForOnlinePayment(p0_Cmd,
//				p1_MerId, p2_Order, p3_Amt, p4_Cur, p5_Pid, p6_Pcat, p7_Pdesc,
//				p8_Url, p9_SAF, pa_MP, pd_FrpId, pr_NeedResponse, keyValue));
//	}
}
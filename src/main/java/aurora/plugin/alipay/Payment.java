package aurora.plugin.alipay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;
import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;

public class Payment extends AbstractEntry {
	
	private String out_trade_no;
	private String subject;
	private String body;
	private String total_fee;
	private String paymethod;
	private String defaultbank;
	private String credit_card_pay;
	private String credit_card_default_display;
	private String extra_common_param;
	private String return_url;
	private String notify_url;
	
	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance.getInstance(context);
		HttpServletRequest request = serviceInstance.getRequest();
		request.setCharacterEncoding("UTF-8");
		ServiceContext service = ServiceContext.createServiceContext(context);
		CompositeMap model = service.getModel();
		
		//把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
        sParaTemp.put("payment_type", "1");
        sParaTemp.put("out_trade_no", getValue(context,getOut_trade_no()));//request.getParameter("out_trade_no"));
        sParaTemp.put("subject", getValue(context,getSubject()));//request.getParameter("subject"));
        sParaTemp.put("body", getValue(context,getBody()));//request.getParameter("body"));
        sParaTemp.put("total_fee", getValue(context,getTotal_fee()));//request.getParameter("total_fee"));
        //sParaTemp.put("show_url", show_url);
        sParaTemp.put("paymethod", getValue(context,getPaymethod()));//request.getParameter("paymethod"));
        sParaTemp.put("defaultbank", getValue(context,getDefaultbank()));//request.getParameter("defaultbank"));
        sParaTemp.put("credit_card_pay", getValue(context,getCredit_card_pay()));//request.getParameter("credit_card_pay"));
        sParaTemp.put("credit_card_default_display", getValue(context,getCredit_card_default_display()));//request.getParameter("credit_card_default_display"));
        sParaTemp.put("extra_common_param",getValue(context,getExtra_common_param()));//request.getParameter("extra_common_param"));
        //sParaTemp.put("buyer_email", buyer_email);
        //sParaTemp.put("royalty_type", royalty_type);
        //sParaTemp.put("royalty_parameters", royalty_parameters);
        sParaTemp.put("return_url", getValue(context,getReturn_url()));
        sParaTemp.put("notify_url", getValue(context,getNotify_url()));
        
        //增加基本配置
        sParaTemp.put("service", "create_direct_pay_by_user");
        sParaTemp.put("partner", AlipayConfig.partner);
        sParaTemp.put("seller_email", AlipayConfig.seller_email);
        sParaTemp.put("_input_charset", AlipayConfig.input_charset);
        //大额信用卡支付
        if("CREDITCARD".equals(getValue(context,getPaymethod()))){ 
        	//credit_card_pay信用卡标识开关
            sParaTemp.put("credit_card_pay", AlipayConfig.credit_card_pay);
            //credit_card_default_display信用卡网关默认显示开关
            sParaTemp.put("credit_card_default_display", AlipayConfig.credit_card_default_display);
        }
       
    	//扩展功能参数——防钓鱼
    	//防钓鱼时间戳
    	//注意：
    	//1.请慎重选择是否开启防钓鱼功能
    	//2.exter_invoke_ip、anti_phishing_key一旦被设置过，那么它们就会成为必填参数
    	//3.开启防钓鱼功能后，服务器、本机电脑必须支持远程XML解析，请配置好该环境。
    	//4.建议使用POST方式请求数据
    	String anti_phishing_key  = AlipayUtil.query_timestamp();
    	//获取客户端的IP地址
    	String exter_invoke_ip= AlipayUtil.getIp();
    	sParaTemp.put("anti_phishing_key", anti_phishing_key);
    	sParaTemp.put("exter_invoke_ip", exter_invoke_ip);
    	
        //生成要请求给支付宝的参数数组
        Map<String, String> sPara = AlipayUtil.buildRequestPara(sParaTemp);
        //将所有参数存入model，返回给页面
        model.put("gateway",AlipayConfig.alipay_gateway);
        List<String> keys = new ArrayList<String>(sPara.keySet());
        for (int i = 0; i < keys.size(); i++) {
            String name = (String) keys.get(i);
            String value = (String) sPara.get(name);
            model.put(name,value);
            //System.out.println(name+"----"+value);
        }
	}
	
	
	private String getValue(CompositeMap context, String key){
		if(key!=null){
			return TextParser.parse(key, context);
		}else{
			return null;
		}
		
	}
	
	public String getOut_trade_no() {
		return out_trade_no;
	}

	public void setOut_trade_no(String outTradeNo) {
		out_trade_no = outTradeNo;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getTotal_fee() {
		return total_fee;
	}

	public void setTotal_fee(String totalFee) {
		total_fee = totalFee;
	}

	public String getPaymethod() {
		return paymethod;
	}

	public void setPaymethod(String paymethod) {
		this.paymethod = paymethod;
	}

	public String getDefaultbank() {
		return defaultbank;
	}

	public void setDefaultbank(String defaultbank) {
		this.defaultbank = defaultbank;
	}
	
	public String getCredit_card_pay() {
		return credit_card_pay;
	}

	public void setCredit_card_pay(String creditCardPay) {
		credit_card_pay = creditCardPay;
	}

	public String getCredit_card_default_display() {
		return credit_card_default_display;
	}

	public void setCredit_card_default_display(String creditCardDefaultDisplay) {
		credit_card_default_display = creditCardDefaultDisplay;
	}
	
	public String getExtra_common_param() {
		return extra_common_param;
	}

	public void setExtra_common_param(String extraCommonParam) {
		extra_common_param = extraCommonParam;
	}


	public String getReturn_url() {
		return return_url;
	}


	public void setReturn_url(String returnUrl) {
		return_url = returnUrl;
	}


	public String getNotify_url() {
		return notify_url;
	}


	public void setNotify_url(String notifyUrl) {
		notify_url = notifyUrl;
	}

}

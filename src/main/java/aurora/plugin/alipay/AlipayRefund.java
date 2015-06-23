package aurora.plugin.alipay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

public class AlipayRefund extends AbstractEntry {
	private String batch_no;
	private String batch_num;
	private String detail_data;
	private String notify_url;

	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance.getInstance(context);
		HttpServletRequest request = serviceInstance.getRequest();
		request.setCharacterEncoding("UTF-8");
		ServiceContext service = ServiceContext.createServiceContext(context);
		CompositeMap model = service.getModel();
		
		String batch_no=getValue(context,getBatch_no());
		//batch_no的格式为：退款日期（8位）+流水号（3～24位）。
		String date_batch_no=AlipayUtil.getBatchNO(batch_no);
		//把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
        sParaTemp.put("batch_no", date_batch_no);
        sParaTemp.put("batch_num", getValue(context,getBatch_num()));
        sParaTemp.put("detail_data", getValue(context,getDetail_data()));
        sParaTemp.put("refund_date",AlipayUtil.getCurrentDate());
        sParaTemp.put("notify_url", getValue(context,getNotify_url()));
      
        //增加基本配置
        sParaTemp.put("service", "refund_fastpay_by_platform_pwd");
        sParaTemp.put("partner", AlipayConfig.partner);
        sParaTemp.put("seller_email", AlipayConfig.seller_email);
        sParaTemp.put("seller_user_id", AlipayConfig.seller_user_id);
        sParaTemp.put("_input_charset", AlipayConfig.input_charset);
       
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
	
	public String getBatch_no() {
		return batch_no;
	}


	public void setBatch_no(String batchNo) {
		batch_no = batchNo;
	}


	public String getBatch_num() {
		return batch_num;
	}


	public void setBatch_num(String batchNum) {
		batch_num = batchNum;
	}


	public String getDetail_data() {
		return detail_data;
	}


	public void setDetail_data(String detailData) {
		detail_data = detailData;
	}

	public String getNotify_url() {
		return notify_url;
	}


	public void setNotify_url(String notifyUrl) {
		notify_url = notifyUrl;
	}
}

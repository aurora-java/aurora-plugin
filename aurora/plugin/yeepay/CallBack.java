package aurora.plugin.yeepay;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uncertain.composite.CompositeMap;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;

import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

import com.yeepay.Configuration;
import com.yeepay.PaymentForOnlineService;

public class CallBack extends AbstractEntry {
	
	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance.getInstance(context);
		HttpServletRequest request = serviceInstance.getRequest();
		ServiceContext service = ServiceContext.createServiceContext(context);
		CompositeMap model = service.getModel();
		CompositeMap para = service.getParameter();
		
		String keyValue   = YeePay.formatString(Configuration.getInstance().getValue("keyValue"));   // 商家密钥
		String r0_Cmd 	  = YeePay.formatString(request.getParameter("r0_Cmd")); // 业务类型
		String p1_MerId   = YeePay.formatString(Configuration.getInstance().getValue("p1_MerId"));   // 商户编号
		String r1_Code    = YeePay.formatString(request.getParameter("r1_Code"));// 支付结果
		String r2_TrxId   = YeePay.formatString(request.getParameter("r2_TrxId"));// 易宝支付交易流水号
		String r3_Amt     = YeePay.formatString(request.getParameter("r3_Amt"));// 支付金额
		String r4_Cur     = YeePay.formatString(request.getParameter("r4_Cur"));// 交易币种
		String r5_Pid     = new String(YeePay.formatString(request.getParameter("r5_Pid")).getBytes("iso-8859-1"),"gbk");// 商品名称
		String r6_Order   = YeePay.formatString(request.getParameter("r6_Order"));// 商户订单号
		String r7_Uid     = YeePay.formatString(request.getParameter("r7_Uid"));// 易宝支付会员ID
		String r8_MP      = new String(YeePay.formatString(request.getParameter("r8_MP")).getBytes("iso-8859-1"),"gbk");// 商户扩展信息
		String r9_BType   = YeePay.formatString(request.getParameter("r9_BType"));// 交易结果返回类型
		String hmac       = YeePay.formatString(request.getParameter("hmac"));// 签名数据
		boolean isOK = false;
		// 校验返回数据包
		isOK = PaymentForOnlineService.verifyCallback(hmac,p1_MerId,r0_Cmd,r1_Code, 
				r2_TrxId,r3_Amt,r4_Cur,r5_Pid,r6_Order,r7_Uid,r8_MP,r9_BType,keyValue);
		model.putString("r1_Code", r1_Code);
		model.putString("r9_BType", r9_BType);
		if(isOK){
			para.putString("r5_Pid", r5_Pid);
			para.putString("r8_MP", r8_MP);
		}else{
			HttpServletResponse response = serviceInstance.getResponse();
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html;charset=utf-8");
			PrintWriter out = serviceInstance.getResponse().getWriter();
            out.write("交易签名被篡改!");
            out.close();
		}
	}
	
}

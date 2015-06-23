package aurora.plugin.alipay;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;
import uncertain.composite.CompositeMap;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;

public class AlipayRefundCallback extends AbstractEntry{
	
	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance.getInstance(context);
		HttpServletRequest request = serviceInstance.getRequest();
		request.setCharacterEncoding("UTF-8");
		ServiceContext service = ServiceContext.createServiceContext(context);
		CompositeMap model = service.getModel();
		//获取支付宝GET过来反馈信息
		Map<String,String> params = new HashMap<String,String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
			//valueStr = new String(valueStr.getBytes("ISO-8859-1"), "UTF-8");
			System.out.println("name="+name+", valueStr="+valueStr);
			params.put(name, valueStr);
		}

		//对支付宝反馈回来的信息进行校验
		String result="";
		String batch_no = request.getParameter("batch_no").substring(8);
		String result_details = request.getParameter("result_details");
		Pattern pattern=Pattern.compile("[\\$\\^]");
		String[] details=pattern.split(result_details);
		
		boolean verify_result = AlipayUtil.verify(params);
		if(verify_result){
			if("SUCCESS".equals(details[2])){
				result="right";
				//退款金额
				model.put("refund_amount", details[1]);
			    //原付款流水号
				model.put("alipay_id", details[0]);
			    //退款订单号
				model.put("batch_no",batch_no);
			}else{
				HttpServletResponse response = serviceInstance.getResponse();
				response.setCharacterEncoding("UTF-8");
				response.setContentType("text/html;charset=utf-8");
				PrintWriter out = serviceInstance.getResponse().getWriter();
	            out.write("支付宝退款失败!");
	            out.close();
			}
		}else{
			//result="error";
			HttpServletResponse response = serviceInstance.getResponse();
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html;charset=utf-8");
			PrintWriter out = serviceInstance.getResponse().getWriter();
            out.write("支付宝退款notify_url签名验证失败!");
            out.close();
		}

		System.out.println("--------------返回结果----------");
		System.out.println(result);
		System.out.println("--------------返回结果----------");
		model.put("result", result);
	}

}

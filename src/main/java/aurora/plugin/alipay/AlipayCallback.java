package aurora.plugin.alipay;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;
import uncertain.composite.CompositeMap;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;

public class AlipayCallback extends AbstractEntry{
	
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
			params.put(name, valueStr);
		}
		//对支付宝反馈回来的信息进行校验
		String result="";
		String trade_status = request.getParameter("trade_status");	
		boolean verify_result = AlipayUtil.verify(params);
		if(verify_result){
			if(trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")){
				result="right";
			}else{
				HttpServletResponse response = serviceInstance.getResponse();
				response.setCharacterEncoding("UTF-8");
				response.setContentType("text/html;charset=utf-8");
				PrintWriter out = serviceInstance.getResponse().getWriter();
	            out.write("支付未成功!");
	            out.close();
			}
		}else{
			//result="error";
			HttpServletResponse response = serviceInstance.getResponse();
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html;charset=utf-8");
			PrintWriter out = serviceInstance.getResponse().getWriter();
            out.write("验证失败!");
            out.close();
		}

		model.put("result", result);
	}

}

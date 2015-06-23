package aurora.plugin.alipay;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import uncertain.composite.CompositeMap;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

public class AlipaySuccess extends AbstractEntry{
	
	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance.getInstance(context);
		HttpServletResponse response = serviceInstance.getResponse();
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=gbk");
		PrintWriter out = serviceInstance.getResponse().getWriter();
        out.write("success");
        out.close();
	}
}

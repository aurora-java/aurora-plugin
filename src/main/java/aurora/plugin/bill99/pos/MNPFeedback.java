package aurora.plugin.bill99.pos;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

public class MNPFeedback extends AbstractEntry {

	private String result;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance
				.getInstance(context);
		HttpServletRequest request = serviceInstance.getRequest();
		request.setCharacterEncoding("GBK");

		String value = getValue(context, this.getResult());
		result = value == null ? "0" : value;

		HttpServletResponse response = serviceInstance.getResponse();
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=gbk");
		PrintWriter out = serviceInstance.getResponse().getWriter();
		out.write(result);
		out.close();
	}

	private String getValue(CompositeMap context, String key) {
		if (key != null) {
			return TextParser.parse(key, context);
		} else {
			return null;
		}
	}
}

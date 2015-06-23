package aurora.plugin.bill99;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

public class Feedback extends AbstractEntry {

	private String result;
	private String redirecturl;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getRedirecturl() {
		return redirecturl;
	}

	public void setRedirecturl(String redirecturl) {
		this.redirecturl = redirecturl;
	}

	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance
				.getInstance(context);
		HttpServletRequest request = serviceInstance.getRequest();
		request.setCharacterEncoding("GBK");

		result = getValue(context, this.getResult());
		redirecturl = getValue(context, this.getRedirecturl());

		HttpServletResponse response = serviceInstance.getResponse();
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=gbk");
		PrintWriter out = serviceInstance.getResponse().getWriter();
		out.write("<result>" + result + "</result><redirecturl>" + redirecturl
				+ "</redirecturl>");
		out.close();

	}

	private String getValue(CompositeMap context, String key) {
		if (key != null) {
			return TextParser.parse(key, context);
		} else {
			return "";
		}

	}
}

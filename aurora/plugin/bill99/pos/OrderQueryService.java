package aurora.plugin.bill99.pos;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLDecoder;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.plugin.bill99.Bill99;
import aurora.plugin.bill99.Configuration;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

public class OrderQueryService extends AbstractEntry {

	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance
				.getInstance(context);
		HttpServletRequest request = serviceInstance.getRequest();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(ServletInputStream) request.getInputStream()));
		String line = null;
		StringBuilder sb = new StringBuilder();
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		if (sb.length() == 0)
			return;

		ServiceContext service = ServiceContext.createServiceContext(context);
		CompositeMap model = service.getModel();
		CompositeMap bill99 = model.createChild("bill99");

		StringUtil su = new StringUtil(sb.toString());
		// "加密明文="
		String orgstr = getString(sb.toString());
		// "签名MAC="
		String mac = URLDecoder.decode(su.getParam("MAC="), "UTF-8");
		String reqTime = su.getParam("reqTime=");
		String orderId = su.getParam("orderId=");
		String ext1 = su.getParam("ext1=");
		String ext2 = su.getParam("ext2=");
		boolean f = false;

		try {
			f = CertificateCoderUtil.verify(orgstr.getBytes(), mac);
		} catch (Exception e) {
		}

		bill99.put("isSuccess", f);
		bill99.put("reqTime", reqTime);
		bill99.put("orderId", orderId);
		bill99.put("ext1", ext1);
		bill99.put("ext2", ext2);
	}

	private String getString(String str) {
		StringUtil su = new StringUtil(str);
		return su.getOrgStr("orderId=") + su.getOrgStr("reqTime=");
	}
}
package aurora.plugin.bill99.pos;

import javax.servlet.http.HttpServletRequest;

import uncertain.composite.CompositeMap;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

public class OrderQueryService extends AbstractEntry {

	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance
				.getInstance(context);
		HttpServletRequest request = serviceInstance.getRequest();

		String orderId = request.getParameter("orderId");
		String reqTime = request.getParameter("reqTime");
		String ext1 = request.getParameter("ext1");
		String ext2 = request.getParameter("ext2");
		String MAC = request.getParameter("MAC");

		String merchantSignMsgVal = "";
		merchantSignMsgVal = appendParam(merchantSignMsgVal, "orderId",
				orderId == null ? "" : orderId);
		merchantSignMsgVal = appendParam(merchantSignMsgVal, "reqTime",
				reqTime == null ? "" : reqTime);
		merchantSignMsgVal = appendParam(merchantSignMsgVal, "ext1",
				ext1 == null ? "" : ext1);
		merchantSignMsgVal = appendParam(merchantSignMsgVal, "ext2",
				ext2 == null ? "" : ext2);

		CerEncode ce = new CerEncode();
		boolean flag = ce.enCodeByCer(merchantSignMsgVal, MAC == null ? ""
				: MAC);

		ServiceContext service = ServiceContext.createServiceContext(context);
		CompositeMap model = service.getModel();
		CompositeMap bill99 = model.createChild("bill99");

		bill99.put("isSuccess", flag);
		bill99.put("reqTime", reqTime);
		bill99.put("orderId", orderId);
		bill99.put("ext1", ext1);
		bill99.put("ext2", ext2);
	}

	public String appendParam(String returnStr, String paramId,
			String paramValue) {
		if (!returnStr.equals("")) {
			if (!paramValue.equals("")) {
				returnStr = returnStr + paramId + "=" + paramValue;
			}
		} else {
			if (!paramValue.equals("")) {
				returnStr = paramId + "=" + paramValue;
			}
		}
		return returnStr;
	}
}
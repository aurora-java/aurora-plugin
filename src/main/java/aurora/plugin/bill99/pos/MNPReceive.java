package aurora.plugin.bill99.pos;

import javax.servlet.http.HttpServletRequest;

import uncertain.composite.CompositeMap;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

public class MNPReceive extends AbstractEntry {

	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance
				.getInstance(context);
		HttpServletRequest request = serviceInstance.getRequest();
		request.setCharacterEncoding("utf-8");
		ServiceContext service = ServiceContext.createServiceContext(context);
		CompositeMap model = service.getModel();

		CompositeMap bill99 = model.createChild("bill99");

		String str = request.getQueryString();

		String signature = request.getParameter("signature");

		StringBuffer sb = new StringBuffer();

		String processFlag = request.getParameter("processFlag");
		String txnType = request.getParameter("txnType");
		String orgTxnType = request.getParameter("orgTxnType");
		String amt = request.getParameter("amt");
		String externalTraceNo = request.getParameter("externalTraceNo");
		String orgExternalTraceNo = request.getParameter("orgExternalTraceNo");
		String terminalOperId = request.getParameter("terminalOperId");
		String authCode = request.getParameter("authCode");
		String RRN = request.getParameter("RRN");
		String txnTime = request.getParameter("txnTime");
		String shortPAN = request.getParameter("shortPAN");
		String responseCode = request.getParameter("responseCode");
		String cardType = request.getParameter("cardType");
		String issuerId = request.getParameter("issuerId");

		sb.append(processFlag).append(txnType).append(orgTxnType);
		sb.append(amt).append(externalTraceNo).append(orgExternalTraceNo)
				.append(terminalOperId);
		sb.append(authCode).append(RRN).append(txnTime).append(shortPAN)
				.append(responseCode);
		sb.append(cardType).append(issuerId);
		boolean f = false;
		try {

			f = CertificateCoderUtil
					.verify(sb.toString().getBytes(), signature);

		} catch (Exception e) {
		}

		bill99.put("isSuccess", f);
		bill99.put("signature", signature);
		bill99.put("processFlag", processFlag);
		bill99.put("txnType", txnType);
		bill99.put("orgTxnType", orgTxnType);
		bill99.put("amt", amt);
		bill99.put("externalTraceNo", externalTraceNo);
		bill99.put("orgExternalTraceNo", orgExternalTraceNo);
		bill99.put("terminalOperId", terminalOperId);
		bill99.put("authCode", authCode);
		bill99.put("RRN", RRN);
		bill99.put("txnTime", txnTime);
		bill99.put("shortPAN", shortPAN);
		bill99.put("cardType", cardType);
		bill99.put("responseCode", responseCode);
		bill99.put("issuerId", issuerId);
	}
}

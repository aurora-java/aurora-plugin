package aurora.plugin.bill99.pos;

import java.io.BufferedWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.plugin.bill99.Configuration;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

public class OQSFeedback extends AbstractEntry {

	private String responseCode;
	private String reqTime;
	private String respTime;
	private String orderId;
	private String merchantId;
	private String merchantName;
	private String amt;
	private String amt2;
	private String amt3;
	private String amt4;
	private EXT[] exts;

	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance
				.getInstance(context);
		HttpServletRequest request = serviceInstance.getRequest();
		request.setCharacterEncoding("GBK");
		responseCode = getValue(context, this.getResponseCode());
		reqTime = getValue(context, this.getReqTime());
		respTime = getValue(context, this.getRespTime());
		orderId = getValue(context, this.getOrderId());
		merchantId = getValue("posMerchantId");
		merchantName = getValue("posMerchantName");
		amt = getValue(context, this.getAmt());
		amt2 = getValue(context, this.getAmt2());
		amt3 = getValue(context, this.getAmt3());
		amt4 = getValue(context, this.getAmt4());

		StringBuffer sbr = new StringBuffer();
		sbr.append("<MessageContent>");
		sbr.append("<reqTime>");
		sbr.append(reqTime);
		sbr.append("</reqTime>");
		sbr.append("<respTime>");
		sbr.append(respTime);
		sbr.append("</respTime>");
		sbr.append("<responseCode>");
		sbr.append(responseCode);
		sbr.append("</responseCode>");
		sbr.append("<message>");
		sbr.append("<orderId>");
		sbr.append(orderId);
		sbr.append("</orderId>");
		sbr.append("<merchantId>");
		sbr.append(merchantId);
		sbr.append("</merchantId>");
		sbr.append("<merchantName>" + merchantName + "</merchantName>");
		sbr.append("<amt>" + amt + "</amt>");
		sbr.append("<amt2>" + amt2 + "</amt2>");
		sbr.append("<amt3>" + amt3 + "</amt3>");
		sbr.append("<amt4>" + amt4 + "</amt4>");
		StringBuffer extXml = createExtXml(context);
		sbr.append(extXml);

		sbr.append("</message>");
		sbr.append("</MessageContent>");

		Pkipair pk = new Pkipair();
		String signMAC = pk.signMsg(sbr.toString());

		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<ResponseMessage>" + "<MAC>"
				+ signMAC.replaceAll("\r", "").replaceAll("\n", "") + "</MAC>"
				+ sbr.toString() + "</ResponseMessage>";
		HttpServletResponse response = serviceInstance.getResponse();
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=gbk");
		BufferedWriter outW = new BufferedWriter(response.getWriter());
		String str = new String(xml.getBytes("gbk"));
		outW.write(str);
		outW.flush();
		outW.close();

	}

	private StringBuffer createExtXml(CompositeMap context) {
		// <ext><property1><value>propertyvalue</value>
		// <chnName>chinesename1</chnName> </property1> <property2>
		// <value>propertyValue</value> <chnName>chinesename2</chnName>
		// </property2></ext>
		// <property1
		// sbr.append("<ext><orderId><value>"
		// + su.getParam("orderId=")
		// +
		// "</value><chnName>订单号</chnName></orderId><address><value>南泉北路 201 号 9楼</value><chnName>收件人地址</chnName></address></ext>");
		StringBuffer sbr = new StringBuffer();
		sbr.append("<ext>");
		EXT[] exts = this.getExts();
		if (exts != null) {
			for (int i = 0; i < exts.length; i++) {
				String property = getValue(context, exts[i].getPropertyName());
				if ("".equals(property))
					continue;
				String value = getValue(context, exts[i].getValue());
				String chnName = getValue(context, exts[i].getChnName());
				StringBuffer s = new StringBuffer();
				s.append("<");
				s.append(property);
				s.append(">");

				s.append("<value>");
				s.append(value);
				s.append("</value>");

				s.append("<chnName>");
				s.append(chnName);
				s.append("</chnName>");

				s.append("</");
				s.append(property);
				s.append(">");
				sbr.append(s);
			}
		}
		sbr.append("</ext>");
		return sbr;
	}

	private String getValue(CompositeMap context, String key) {
		if (key != null) {
			return TextParser.parse(key, context);
		} else {
			return "";
		}
	}

	private String getValue(String key) {
		String value = Configuration.getValue(Configuration.DEFAULT_CONFIG_FILE, key);
//		String value = Configuration.getInstance().getValue(key);
		return value == null ? "" : value;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getReqTime() {
		return reqTime;
	}

	public void setReqTime(String reqTime) {
		this.reqTime = reqTime;
	}

	public String getRespTime() {
		return respTime;
	}

	public void setRespTime(String respTime) {
		this.respTime = respTime;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public String getAmt() {
		return amt;
	}

	public void setAmt(String amt) {
		this.amt = amt;
	}

	public String getAmt2() {
		return amt2;
	}

	public void setAmt2(String amt2) {
		this.amt2 = amt2;
	}

	public String getAmt3() {
		return amt3;
	}

	public void setAmt3(String amt3) {
		this.amt3 = amt3;
	}

	public String getAmt4() {
		return amt4;
	}

	public void setAmt4(String amt4) {
		this.amt4 = amt4;
	}

	public EXT[] getExts() {
		return exts;
	}

	public void setExts(EXT[] exts) {
		this.exts = exts;
	}

}
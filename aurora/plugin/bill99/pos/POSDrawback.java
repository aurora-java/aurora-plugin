package aurora.plugin.bill99.pos;

import javax.servlet.http.HttpServletRequest;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.plugin.bill99.Configuration;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

public class POSDrawback extends AbstractEntry {
	private String version;
	private String txnType;
	private String interactiveStatus;
	private String amount;
	private String merchantId;
	private String settleMerchantId;
	private String terminalId;
	private String entryTime;
	private String origRefNumber;
	private String externalRefNumber;
	private String ext;
	private String ext1;
	private String tr3Url;

	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance
				.getInstance(context);
		HttpServletRequest request = serviceInstance.getRequest();
		ServiceContext service = ServiceContext.createServiceContext(context);
		CompositeMap model = service.getModel();
		CompositeMap bill99 = model.createChild("bill99");
		request.setCharacterEncoding("utf-8");
		RefundFunction pu = new RefundFunction();
		MRefund pf = new MRefund();
	
		// pos_refund_orignalTxnType文档和例子没有说明目前无意义，也没处理。请与快钱确认
		//
		String version = getValue(context, this.getVersion());
		pf.setVersion("".equals(version) ? getValue("pos_refund_version")
				: version);
		String txnType = getValue(context, this.getTxnType());
		pf.setTxnType("".equals(txnType) ? getValue("pos_refund_txnType")
				: txnType);
		String interactiveStatus = getValue(context,
				this.getInteractiveStatus());
		pf.setInteractiveStatus("".equals(interactiveStatus) ? getValue("pos_refund_interactiveStatus")
				: interactiveStatus);
		pf.setAmount(getValue(context, this.getAmount()));
		String merchantId = getValue(context, this.getMerchantId());
		pf.setMerchantId("".equals(merchantId) ? getValue("pos_refund_merchantId")
				: merchantId);
		String settleMerchandId = getValue(context, this.getSettleMerchantId());
		pf.setSettleMerchantId("".equals(settleMerchandId) ? getValue("pos_refund_settleMerchantId")
				: settleMerchandId);
		String terminalId = getValue(context, this.getTerminalId());
		pf.setTerminalId("".equals(terminalId) ? getValue("pos_refund_terminalId")
				: terminalId);
		pf.setEntryTime(getValue(context, this.getEntryTime()));
		// 文档无此节点？？？有：RefNumber
		pf.setOrigRefNumber(getValue(context, this.getOrigRefNumber()));
		pf.setExternalRefNumber(getValue(context, this.getExternalRefNumber()));
		pf.setExt(getValue(context, this.getExt()));
		pf.setExt1(getValue(context, this.getExt1()));
		pf.setTr3Url(getValue(context, this.getTr3Url()));

		MRefund f = pu.action(pf, new MRefund());

		bill99.put("merchantId", f.getMerchantId2());
		bill99.put("txnType", f.getTxnType2());
		bill99.put("version", f.getVersion2());
		bill99.put("refNumber", f.getRefNumber2());
		bill99.put("interactiveStatus", f.getInteractiveStatus2());
		bill99.put("amount", f.getAmount2());
		bill99.put("terminalId", f.getTerminalId2());
		bill99.put("entryTime", f.getEntryTime2());
		bill99.put("externalRefNumber", f.getExternalRefNumber2());
		bill99.put("transTime", f.getTransTime2());
		bill99.put("responseCode", f.getResponseCode2());
		bill99.put("errorCode", f.getErrorCode2());
		bill99.put("errorMessage", f.getErrorMessage2());

	}

	private String getValue(String key) {
//		String value = Configuration.getInstance().getValue(key);
		String value = Configuration.getValue(Configuration.DEFAULT_CONFIG_FILE, key);
		return value == null ? "" : value;
	}

	private String getValue(CompositeMap context, String key) {
		if (key != null) {
			return TextParser.parse(key, context);
		} else {
			return "";
		}

	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getInteractiveStatus() {
		return interactiveStatus;
	}

	public void setInteractiveStatus(String interactiveStatus) {
		this.interactiveStatus = interactiveStatus;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getSettleMerchantId() {
		return settleMerchantId;
	}

	public void setSettleMerchantId(String settleMerchantId) {
		this.settleMerchantId = settleMerchantId;
	}

	public String getTerminalId() {
		return terminalId;
	}

	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}

	public String getEntryTime() {
		return entryTime;
	}

	public void setEntryTime(String entryTime) {
		this.entryTime = entryTime;
	}

	public String getOrigRefNumber() {
		return origRefNumber;
	}

	public void setOrigRefNumber(String origRefNumber) {
		this.origRefNumber = origRefNumber;
	}

	public String getExternalRefNumber() {
		return externalRefNumber;
	}

	public void setExternalRefNumber(String externalRefNumber) {
		this.externalRefNumber = externalRefNumber;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

	public String getExt1() {
		return ext1;
	}

	public void setExt1(String ext1) {
		this.ext1 = ext1;
	}

	public String getTr3Url() {
		return tr3Url;
	}

	public void setTr3Url(String tr3Url) {
		this.tr3Url = tr3Url;
	}

}

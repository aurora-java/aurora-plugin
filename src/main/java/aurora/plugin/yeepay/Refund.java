package aurora.plugin.yeepay;

import javax.servlet.http.HttpServletRequest;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

import com.yeepay.PaymentForOnlineService;
import com.yeepay.RefundResult;

public class Refund extends AbstractEntry {

	private String trxID;// 易宝交易流水号
	private String amt;// 退款金额
	private String cur;// 交易币种
	private String desc;// 退款说明

	public String getTrxID() {
		return trxID;
	}

	public void setTrxID(String trxID) {
		this.trxID = trxID;
	}

	public String getAmt() {
		return amt;
	}

	public void setAmt(String amt) {
		this.amt = amt;
	}

	public String getCur() {
		return cur;
	}

	public void setCur(String cur) {
		this.cur = cur;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void run(ProcedureRunner runner) throws Exception {

		CompositeMap context = runner.getContext();
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance
				.getInstance(context);
		HttpServletRequest request = serviceInstance.getRequest();
		request.setCharacterEncoding("GBK");
		ServiceContext service = ServiceContext.createServiceContext(context);
		CompositeMap model = service.getModel();

		String pb_TrxId = YeePay
				.formatString(getValue(context, this.getTrxID())); // 易宝交易流水号
		String p3_Amt = YeePay.formatString(getValue(context, this.getAmt())); // 退款金额
		String p4_Cur = YeePay.formatString(YeePay.KEY_CNY); // 交易币种
		String p5_Desc = YeePay.formatString(getValue(context, this.getDesc())); // 退款说明

		try {
			RefundResult rr = PaymentForOnlineService.refundByTrxId(pb_TrxId,
					p3_Amt, p4_Cur, p5_Desc); // 调用后台外理查询方法
			model.put(YeePay.KEY_R0_CMD, rr.getR0_Cmd());// 业务类型
			model.put(YeePay.KEY_R1_CODE, rr.getR1_Code());// 查询结果
			model.put(YeePay.KEY_R2_TRXID, rr.getR2_TrxId());// 易宝支付交易流水号
			model.put(YeePay.KEY_R3_AMT, rr.getR3_Amt());// 支付金额
			model.put(YeePay.KEY_R4_CUR, rr.getR4_Cur());// 交易币种

		} catch (Exception e) {
			catchException(model, e);
		}

	}

	private void catchException(CompositeMap model, Exception e)
			throws Exception {
		if (e instanceof RuntimeException) {
			String message = e.getMessage();
			String[] split = message.split(":");
			if (split.length != 2)
				throw e;
			if ("Query fail.Error code".equals(split[0])) {
				try {
					model.put(YeePay.KEY_R1_CODE, Integer.valueOf(split[1]));
				} catch (NumberFormatException nfe) {
					throw e;
				}
			}
		} else
			throw e;

	}

	private String getValue(CompositeMap context, String key) {

		if (key != null) {
			return TextParser.parse(key, context);
		} else {
			return null;
		}

	}

	public static void main(String[] args) {

	}
}
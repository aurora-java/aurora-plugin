package aurora.plugin.yeepay;

public class YeePay {

	public static final String KEY_KEY_VALUE = "keyValue";
	public static final String KEY_COMMON_REQ_URL = "yeepayCommonReqURL";
	public static final String KEY_BUY = "Buy";
	public static final String KEY_CNY = "CNY";
	public static final String KEY_P0_CMD = "p0_Cmd";
	public static final String KEY_P1_MER_ID = "p1_MerId";
	public static final String KEY_P2_ORDER = "p2_Order";
	public static final String KEY_P3_ATM = "p3_Amt";//退款,付款金额
	public static final String KEY_P4_CUR = "p4_Cur";//交易币种
	public static final String KEY_P5_PID = "p5_Pid";
	public static final String KEY_P6_PCAT = "p6_Pcat";
	public static final String KEY_P7_PDESC = "p7_Pdesc";
	public static final String KEY_P8_URL = "p8_Url";
	public static final String KEY_P9_SAF = "p9_SAF";
	public static final String KEY_PA_MP = "pa_MP";
	public static final String KEY_PD_FRPID = "pd_FrpId";
	public static final String KEY_NEED_RESPONSE = "pr_NeedResponse";
	public static final String KEY_HMAC = "hmac";
	public static final String KEY_URL = "nodeAuthorizationURL";
	public static final String KEY_P5_DESC ="p5_Desc";//退款说明
	public static final String KEY_PB_TRXID ="pb_TrxId";//易宝交易流水号
	public static final String KEY_R0_CMD ="r0_Cmd";//业务类型 
	public static final String KEY_R1_CODE ="r1_Code";//查询结果
	public static final String KEY_R2_TRXID ="r2_TrxId";//易宝支付交易流水号
	public static final String KEY_R3_AMT ="r3_Amt";//支付金额
	public static final String KEY_R4_CUR ="r4_Cur";//交易币种

	public static String formatString(String text) {
		if (text == null) {
			return "";
		}
		return text;
	}
}

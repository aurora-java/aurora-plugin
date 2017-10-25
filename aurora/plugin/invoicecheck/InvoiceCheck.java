package aurora.plugin.invoicecheck;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONException;
import org.json.JSONObject;

import aurora.database.DBUtil;
import aurora.database.service.DatabaseServiceFactory;
import aurora.database.service.SqlServiceContext;
import aurora.plugin.invoicecheck.util.BaseUtil;
import aurora.plugin.invoicecheck.util.HttpClientUtil;
import aurora.plugin.invoicecheck.util.RuoKuaiUtil;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;
import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;

/**
 * @Author: xuzhao
 * @Email: mailto:zhao.xu@hand-china.com
 * @Description: 增值税发票验证爬虫测试
 * @Time: 2017/10/17 16:47
 */
public class InvoiceCheck extends AbstractEntry {

	public  final String VERSION = "$Revision$";
	protected DatabaseServiceFactory databasefactory;
	IObjectRegistry registry;
	private ILogger logger;
	private String queryString = "";
	private String resultString = "";

	private  final String jsUrl = "https://inv-veri.chinatax.gov.cn/js/";
	private  String invoiceType = null;
	private  String invoiceCode = null;
	private  String invoiceNo = null;
	private  String invoiceDate = null;
	private  String checkCode = null;
	private  String verificationCode = "";
	private  String verificationCodeTime = null;// 验证码
	private  String encryptedKey = null;// 加密密钥
	private  String RuoKuaiUserName = "twtyjvkg";
	private  String RuoKuaiPassword = "XZ1006066";
	private  String RuoKuaiSoftId = "89749";
	private  String RuoKuaiSoftkey = "5c9027fd681745d6ba12e611585c3f58";
	private  String PATH = null;
	private  ScriptEngine engine = null;
	private  boolean status1 = true;//验证码请求状态
	private JSONObject yzmResponseResult = null;//验证码请求结果
	private JSONObject errorMsg1 = null;//验证码请求错误消息
	private boolean status2 = true;//发票验证请求状态
	private JSONObject fpyzResponseResult = null;//发票验证请求结果
	private JSONObject errorMsg2 = null;//发票验证请求错误消息

	public InvoiceCheck(IObjectRegistry registry) {
		this.registry = registry;
		databasefactory = (DatabaseServiceFactory)registry.getInstanceOfType(DatabaseServiceFactory.class);
	}

	public Connection getContextConnection(CompositeMap context) throws SQLException {
		/**
		 * @Author: xuzhao
		 * @Email: mailto:zhao.xu@hand-china.com
		 * @Description: 获取数据库连接
		 * @Time: 2017/10/24 14:54
		 * @param context 上下文
		 * @Return: 数据库连接对象
		 */
		if (context == null)
			throw new IllegalStateException("Can not get context from ServiceThreadLocal!");
		SqlServiceContext sqlServiceContext = SqlServiceContext.createSqlServiceContext(context);
		Connection conn = sqlServiceContext.getNamedConnection(null);
		if (conn == null) {
			sqlServiceContext.initConnection(registry, null);
			conn = sqlServiceContext.getNamedConnection(null);
		}
		return conn; 
	}

	public void run(ProcedureRunner runner) throws Exception {
		logger = LoggingContext.getLogger(runner.getContext(), this.getClass().getCanonicalName());
		CompositeMap context = runner.getContext();
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance.getInstance(context);
		HttpServletResponse response = serviceInstance.getResponse();
		ServiceContext service = ServiceContext.createServiceContext(context);
		queryString = TextParser.parse(getQueryString(), context);
		System.out.println("--------------这是一个发票校验控件------------------------");
		response.setContentType("text/html;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		System.out.println("校验参数为" + getQueryString());
		JSONObject result = queryInvoiceFromDb(context, getQueryString());
//		JSONObject result = null;
		if(result == null) {
			System.out.println("--------------------数据库中不存在此发票信息----------------------");
			result = new JSONObject(invoiceCheck(context, getQueryString()));
//			result = new JSONObject("{\"header_information\":{\"invoice_code\":\"3200172130\",\"invoice_number\":\"36871519\",\"invoice_area\":\"江苏\",\"invoice_type\":\"增值税专用发票\",\"invoice_date\":\"2017年10月10日\",\"machine_number\":\"661619893841\",\"check_code\":\"79114655461512865538\",\"purchaser_name\":\"上海汉得信息技术股份有限公司\",\"purchaser_tax_number\":\"9131000074027295XF\",\"purchaser_address_phone\":\"上海市青浦区汇联路33号021-67002300\",\"purchaser_bank_account\":\"招商银行上海分行张江支行121904631510902\",\"amount\":\"￥46.04\",\"amount_zhs\":\"⊗肆拾陆圆肆分\",\"without_tax_amount\":\"￥39.34\",\"tax_amount\":\"￥6.70\",\"seller_name\":\"昆山京东尚信贸易有限公司\",\"seller_tax_number\":\"913205830880018839\",\"seller_address_phone\":\"千灯镇圣祥东路25号2号 39915704\",\"seller_bank_account\":\"中国建设银行股份有限公司昆山市千灯支行32201986438051511848\",\"remark\":\"dd62168061931(00001,63263932583)\"},\"lines_information\":{\"1\":{\"goods_or_taxable_services\":\"黑人（DARLIE）超白 牙膏 190g（矿物盐）（新老包装随机发放）\",\"pecification\":\"牙膏\",\"unit\":\"个\",\"quantity\":\"1\",\"unit_price\":\"16.15384615\",\"without_tax_amount\":\"16.15\",\"tax_rate\":\"17%\",\"tax_amount\":\"2.75\"},\"2\":{\"goods_or_taxable_services\":\"黑人（DARLIE）超白 牙膏 190g（矿物盐）（新老包装随机发放）\",\"pecification\":\" \",\"unit\":\" \",\"quantity\":\" \",\"unit_price\":\" \",\"without_tax_amount\":\"-4.36\",\"tax_rate\":\"17%\",\"tax_amount\":\"-0.74\"},\"3\":{\"goods_or_taxable_services\":\"配送费\",\"pecification\":\" \",\"unit\":\"个\",\"quantity\":\"1\",\"unit_price\":\"1.70940171\",\"without_tax_amount\":\"1.71\",\"tax_rate\":\"17%\",\"tax_amount\":\"0.29\"},\"4\":{\"goods_or_taxable_services\":\"黑人（DARLIE）超白 牙膏 190g（竹炭深洁）（新老包装随机发放）\",\"pecification\":\"牙膏\",\"unit\":\"个\",\"quantity\":\"1\",\"unit_price\":\"16.15384615\",\"without_tax_amount\":\"16.15\",\"tax_rate\":\"17%\",\"tax_amount\":\"2.75\"},\"5\":{\"goods_or_taxable_services\":\"黑人（DARLIE）超白 牙膏 190g（竹炭深洁）（新老包装随机发放）\",\"pecification\":\" \",\"unit\":\" \",\"quantity\":\" \",\"unit_price\":\" \",\"without_tax_amount\":\"-5.47\",\"tax_rate\":\"17%\",\"tax_amount\":\"-0.93\"},\"6\":{\"goods_or_taxable_services\":\"配送费\",\"pecification\":\" \",\"unit\":\"个\",\"quantity\":\"1\",\"unit_price\":\"1.70940171\",\"without_tax_amount\":\"1.71\",\"tax_rate\":\"17%\",\"tax_amount\":\"0.29\"},\"7\":{\"goods_or_taxable_services\":\"黑人（DARLIE）超白 牙膏 190g（多效护理）\",\"pecification\":\"牙膏\",\"unit\":\"个\",\"quantity\":\"1\",\"unit_price\":\"16.15384615\",\"without_tax_amount\":\"16.15\",\"tax_rate\":\"17%\",\"tax_amount\":\"2.75\"},\"8\":{\"goods_or_taxable_services\":\"黑人（DARLIE）超白 牙膏 190g（多效护理）\",\"pecification\":\" \",\"unit\":\" \",\"quantity\":\" \",\"unit_price\":\" \",\"without_tax_amount\":\"-4.41\",\"tax_rate\":\"17%\",\"tax_amount\":\"-0.75\"},\"9\":{\"goods_or_taxable_services\":\"配送费\",\"pecification\":\" \",\"unit\":\"个\",\"quantity\":\"1\",\"unit_price\":\"1.70940171\",\"without_tax_amount\":\"1.71\",\"tax_rate\":\"17%\",\"tax_amount\":\"0.29\"},\"10\":{\"goods_or_taxable_services\":\"原价合计\",\"pecification\":\" \",\"unit\":\" \",\"quantity\":\" \",\"unit_price\":\" \",\"without_tax_amount\":\"53.58\",\"tax_rate\":\"17%\",\"tax_amount\":\"9.12\"},\"11\":{\"goods_or_taxable_services\":\"折扣额合计\",\"pecification\":\" \",\"unit\":\" \",\"quantity\":\" \",\"unit_price\":\" \",\"without_tax_amount\":\"-14.24\",\"tax_rate\":\"17%\",\"tax_amount\":\"-2.42\"}}}");
			//		JSONObject result = new JSONObject("{\"header_information\":{\"tax_amount\":\"￥1.12\",\"amount\":\"￥38.40\",\"purchaser_bank_account\":\" \",\"amount_zhs\":\"⊗叁拾捌圆肆角\",\"remark\":\"\",\"without_tax_amount\":\"￥ \",\"seller_tax_number\":\"973707781878508592\",\"invoice_date\":\"2017年06月16日\",\"invoice_code\":\"3100171320\",\"invoice_area\":\"上海\",\"seller_address_phone\":\"上海市青浦区盈港东路8300弄36号 021-69734160\",\"seller_name\":\"上海好缘商贸有限公司\",\"purchaser_address_phone\":\" \",\"machine_number\":\"661540507214\",\"check_code\":\"58178041473130902764\",\"purchaser_name\":\"上海汉得信息技术股份有限公司\",\"invoice_type\":\"增值税普通发票\",\"purchaser_tax_number\":\" \",\"invoice_number\":\"64780120\",\"seller_bank_account\":\"农行青浦开发支行\"},\"lines_information\":{\"1\":{\"tax_amount\":\"1.12\",\"unit\":\" \",\"quantity\":\" \",\"goods_or_taxable_services\":\"办公用品\",\"unit_price\":\"\",\"without_tax_amount\":\"37.28\",\"pecification\":\" \",\"tax_rate\":\"3%\"}}}");
		}
		service.getCurrentParameter().put("resultString", result);
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public String getResultString() {
		return this.resultString;
	}

	public void setResultString(String resultString) {
		this.resultString = resultString;
	}

	private JSONObject queryInvoiceFromDb(CompositeMap context, String queryString) throws Exception{
		/**
		 * @Author: xuzhao
		 * @Email: mailto:zhao.xu@hand-china.com
		 * @Description: 从数据库中查询发票信息
		 * @Time: 2017/10/25 12:36
		 * @param context 上下文
		 * @param queryString 查询参数
		 * @Return: 查询结果
		 */
		// TODO Auto-generated method stub
		String invoice_number = queryString.split(",")[3];
		System.out.println("-----------------系统正在从数据库中查询发票号码为'" + invoice_number + "'的发票信息------------------");
		Connection conn = null;
		PreparedStatement head_pst = null;
		ResultSet head_rs = null;
		PreparedStatement lines_pst = null;
		ResultSet lines_rs = null;
		JSONObject invoice_result = null;
		try {
			conn = getContextConnection(context);
			JSONObject header_info = new JSONObject();
			JSONObject lines_info = new JSONObject();
			head_pst = conn.prepareStatement("select header_id, invoice_code,invoice_number,invoice_date,invoice_area,invoice_type,machine_number,check_code,"
					+ "purchaser_name,purchaser_tax_number,purchaser_address_phone,purchaser_bank_account,"
					+ "amount,amount_zhs,tax_amount,without_tax_amount,"
					+ "seller_name,seller_tax_number,seller_address_phone,seller_bank_account,remark from inv_invoice_head where invoice_number = ? and verification_status = 'Y'");
			head_pst.setString(1, invoice_number);
			head_rs = head_pst.executeQuery();
			while(head_rs.next()) {
				ResultSetMetaData header_data = head_rs.getMetaData();
				invoice_result = new JSONObject();
				for(int i = 2; i <= header_data.getColumnCount(); i++) {
					header_info = header_info.put(header_data.getColumnName(i).toLowerCase(), head_rs.getString(i));
				}
				String header_id = head_rs.getString("header_id");
				lines_pst = conn.prepareStatement("select line_number, goods_or_taxable_services,pecification,unit,quantity,unit_price,without_tax_amount,tax_rate,tax_amount "
						+ "from inv_invoice_lines where header_id = ? order by line_number asc");
				lines_pst.setString(1, header_id);
				lines_rs = lines_pst.executeQuery();
				while(lines_rs.next()) {
					JSONObject line_info = new JSONObject();
					ResultSetMetaData line_data = lines_rs.getMetaData();
					for(int i = 2; i <= line_data.getColumnCount(); i++) {
						line_info = line_info.put(line_data.getColumnName(i).toLowerCase(), lines_rs.getString(i));
					}
					lines_info.put(lines_rs.getString(1), line_info);
				}
				invoice_result.put("header_information", header_info);
				invoice_result.put("lines_information", lines_info);
				System.out.println("--------------------数据库中存在此发票信息----------------------");
			}
		}catch (Exception e) {
			// TODO: handle exception
			throw e;
		}finally {
			DBUtil.closeResultSet(lines_rs);
			DBUtil.closeStatement(lines_pst);
			DBUtil.closeResultSet(head_rs);
			DBUtil.closeStatement(head_pst);
		}
		return invoice_result;
	}


	private void saveInvoice(CompositeMap context, JSONObject result) throws Exception{
		/**
		 * @Author: xuzhao
		 * @Email: mailto:zhao.xu@hand-china.com
		 * @Description: 
		 * @Time: 2017/10/24 17:12
		 * @param context 上下文
		 * @param result 发票验证结果
		 * @Return: 
		 */
		JSONObject headInfo = result.getJSONObject("header_information");
		JSONObject linesInfo = result.getJSONObject("lines_information");
		String invoice_number = headInfo.getString("invoice_number");
		saveInvoiceHead(context, headInfo);
		saveInvoiceLines(context, linesInfo, invoice_number);
	}

	private void saveInvoiceHead(CompositeMap context, JSONObject headInfo) throws Exception {
		/**
		 * @Author: xuzhao
		 * @Email: mailto:zhao.xu@hand-china.com
		 * @Description: 将查询到的发票头信息保存到数据库中
		 * @Time: 2017/10/24 14:56
		 * @param context 上下文
		 * @param headInfo 头信息
		 * @Return: 
		 */
		Connection conn = getContextConnection(context);
		PreparedStatement pst = null;
		try {
			pst = conn.prepareStatement(
					"insert into inv_invoice_head"
							+ "(header_id,invoice_code,invoice_number,invoice_date,invoice_area,invoice_type,machine_number,check_code,"
							+ "purchaser_name,purchaser_tax_number,purchaser_address_phone,purchaser_bank_account,"
							+ "amount,amount_zhs,tax_amount,without_tax_amount,"
							+ "seller_name,seller_tax_number,seller_address_phone,seller_bank_account,remark,verification_status)"
							+ "values(INV_INVOICE_HEAD_S.nextval,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

			pst.setString(1, headInfo.getString("invoice_code"));
			pst.setString(2, headInfo.getString("invoice_number"));
			pst.setString(3, headInfo.getString("invoice_date"));
			pst.setString(4, headInfo.getString("invoice_area"));
			pst.setString(5, headInfo.getString("invoice_type"));
			pst.setString(6, headInfo.getString("machine_number"));
			pst.setString(7, headInfo.getString("check_code"));
			pst.setString(8, headInfo.getString("purchaser_name"));
			pst.setString(9, headInfo.getString("purchaser_tax_number"));
			pst.setString(10, headInfo.getString("purchaser_address_phone"));
			pst.setString(11, headInfo.getString("purchaser_bank_account"));
			pst.setString(12, headInfo.getString("amount"));
			pst.setString(13, headInfo.getString("amount_zhs"));
			pst.setString(14, headInfo.getString("tax_amount"));
			pst.setString(15, headInfo.getString("without_tax_amount"));
			pst.setString(16, headInfo.getString("seller_name"));
			pst.setString(17, headInfo.getString("seller_tax_number"));
			pst.setString(18, headInfo.getString("seller_address_phone"));
			pst.setString(19, headInfo.getString("seller_bank_account"));
			pst.setString(20, headInfo.getString("remark"));
			pst.setString(21, "Y");
			pst.execute();
		}catch(Exception e) {
			throw e;
		}finally {
			DBUtil.closeStatement(pst);
		}
	}

	private void saveInvoiceLines(CompositeMap context, JSONObject linesInfo, String invoice_number) throws Exception {
		/**
		 * @Author: xuzhao
		 * @Email: mailto:zhao.xu@hand-china.com
		 * @Description: 将查询到的发票行信息保存到数据库中
		 * @Time: 2017/10/24 17:09
		 * @param context 上下文
		 * @param linesInfo 行信息
		 * @param invoice_number 发票号码
		 * @Return: 
		 */
		Connection conn = getContextConnection(context);
		PreparedStatement pstQuery = null;
		ResultSet rs = null;
		PreparedStatement pst = null;
		try {
			pstQuery = conn.prepareStatement("select header_id from inv_invoice_head where invoice_number = ?");
			pstQuery.setString(1, invoice_number);
			rs = pstQuery.executeQuery();
			Iterator<String> it = linesInfo.keys();
			String header_id = null;
			while(rs.next()) {
				header_id = rs.getString("header_id");
			}
			pst = conn.prepareStatement(
					"insert into inv_invoice_lines"
							+ "(line_id,header_id,line_number,"
							+ "goods_or_taxable_services,pecification,unit,quantity,unit_price,without_tax_amount,tax_rate,tax_amount)"
							+ "values(INV_INVOICE_lines_S.nextval,?,?,?,?,?,?,?,?,?,?)");
			while(it.hasNext()) {
				String key = it.next();
				JSONObject lineInfo = linesInfo.getJSONObject(key);
				pst.setString(1, header_id);
				pst.setString(2, key);
				pst.setString(3, lineInfo.getString("goods_or_taxable_services"));
				pst.setString(4, lineInfo.getString("pecification"));
				pst.setString(5, lineInfo.getString("unit"));
				pst.setString(6, lineInfo.getString("quantity"));
				pst.setString(7, lineInfo.getString("unit_price"));
				pst.setString(8, lineInfo.getString("without_tax_amount"));
				pst.setString(9, lineInfo.getString("tax_rate"));
				pst.setString(10, lineInfo.getString("tax_amount"));
				pst.execute();
			}
		}catch (Exception e) {
			// TODO: handle exception
			throw e;
		}finally {
			DBUtil.closeResultSet(rs);
			DBUtil.closeStatement(pstQuery);
			DBUtil.closeStatement(pst);
		}
	}

	private void autoRecognition(String scriptName,String yzmColor) throws Exception{
		/**
		 * @Author: xuzhao
		 * @Email: mailto:zhao.xu@hand-china.com
		 * @Description: 使用python脚本以及第三方api自动识别验证码
		 * @Time: 2017/10/17 18:20
		 * @param yzmColor 待识别颜色
		 * @Return: 正确验证码
		 */
		verificationCode = "";
		String ruokuaiResult = RuoKuaiUtil.createByPost(RuoKuaiUserName, RuoKuaiPassword, "3060", "90", RuoKuaiSoftId, RuoKuaiSoftkey, PATH + invoiceNo + ".png");//若快答题识别结果
		if(ruokuaiResult.indexOf("errorCode") != -1) {
			verificationCode = ruokuaiResult;
		}else {
			Document document = DocumentHelper.parseText(ruokuaiResult);
			String result = document.getRootElement().element("Result").getText();
			if(yzmColor.equals("00"))
				verificationCode = result;
			else {
				String cmd = "python " + PATH.trim() + scriptName.trim() + " " + PATH.trim()+invoiceNo.trim() + ".png";
				Process process = Runtime.getRuntime().exec(cmd);//控制台执行python脚本识别验证码颜色信息
				process.waitFor();
				String colorSequence = BaseUtil.convertStreamToString(process.getInputStream()).replaceAll("\n", "");//获取python脚本执行结果
				if(colorSequence.indexOf("Err") != -1){
					verificationCode = "{errorCode:'python',errorMsg:'" + colorSequence + "'}";;
				}
				colorSequence = colorSequence.substring(1, colorSequence.length() - 1);
				System.out.println(colorSequence);
				String[] colors = colorSequence.split(",");
				char verificationCodes[] = result.toCharArray();
				for (int i = 0; i < verificationCodes.length; i++) {
					if (yzmColor.equals(colors[i].replaceAll("\'", "").trim()))
						verificationCode += verificationCodes[i];
				}
			}
		}
	}

	private  Object getValueByName(String name) {
		/**
		 * @Author: xuzhao
		 * @Email: mailto:zhao.xu@hand-china.com
		 * @Description: 根据变量名获取变量的值
		 * @Time: 2017/10/17 18:22
		 * @param name
		 *            js变量名
		 * @Return: 变量的值
		 */
		return engine.get(name);
	}

	private  void importJsFiles() throws Exception{
		/**
		 * @Author: xuzhao
		 * @Email: mailto:zhao.xu@hand-china.com
		 * @Description: 引入js文件
		 * @Time: 2017/10/17 17:04
		 * @param
		 * @Return:
		 */
		importRemoteJs("aes");
		importRemoteJs("pbkdf2");
		importRemoteJs("AesUtil");
		importLocalJs("validate");
		importLocalJs("result");
	}

	private  void importRemoteJs(String jsName) throws Exception{
		/**
		 * @Author: xuzhao
		 * @Email: mailto:zhao.xu@hand-china.com
		 * @Description: 引入远程站点的js文件
		 * @Time: 2017/10/17 17:09
		 * @param jsName
		 *            js文件名
		 * @Return:
		 */
		engine.eval(HttpClientUtil.getResponseBody(jsUrl + jsName + ".js", null));
	} 

	private  void eval(String name, String value) throws Exception{
		/**
		 * @Author: xuzhao
		 * @Email: mailto:zhao.xu@hand-china.com
		 * @Description: 执行js赋值语句
		 * @Time: 2017/10/17 17:14
		 * @param name
		 *            变量名
		 * @param value
		 *            变量值
		 * @Return:
		 */
		engine.eval("var " + name + "= '" + value + "'");
	}

	private  void importLocalJs(String jsName) throws Exception{
		/**
		 * @Author: xuzhao
		 * @Email: mailto:zhao.xu@hand-china.com
		 * @Description: 引入本地js文件
		 * @Time: 2017/10/17 17:10
		 * @param jsName
		 *            js文件名
		 * @Return:
		 */
		engine.eval(BaseUtil.getLocalJs(jsName));
	}

	private void splitQueryString(String queryString) throws Exception{
		/**
		 * @Author: xuzhao
		 * @Email: mailto:zhao.xu@hand-china.com
		 * @Description: 解析二维码扫描结果
		 * @Time: 2017/10/17 16:51
		 * @param
		 * @Return:
		 */
		String[] queryKeys = queryString.split(",");
		invoiceType = queryKeys[1];
		eval("fplx", invoiceType);
		invoiceCode = queryKeys[2];
		eval("fpdm", invoiceCode);
		invoiceNo = queryKeys[3];
		eval("fphm", invoiceNo);
		invoiceDate = queryKeys[5];
		eval("kprq", invoiceDate);
		if (invoiceType.equals("01")) {// 增值税专票，checkCode字段为不含税金额
			checkCode = queryKeys[4];
		} else {// 增值税普票开票checkCode字段为校验码后六位
			checkCode = queryKeys[6].substring(queryKeys[6].length() - 6, queryKeys[6].length());
		}
		eval("kjje", checkCode);
	}

	private String invoiceCheck(CompositeMap context, String queryString) {
		/**
		 * @Author: xuzhao
		 * @Email: mailto:zhao.xu@hand-china.com
		 * @Description: 发票验证
		 * @Time: 2017/10/18 16:02
		 * @param queryString 发票二维码扫描结果
		 * @Return: 验证结果
		 */
		String result = null;
		try {
			String windowsPath = BaseUtil.getPropertity("windowsPath");
			String linuxPath = BaseUtil.getPropertity("linuxPath");
			String pythonName = BaseUtil.getPropertity("pythonName");
			File file = new File(windowsPath + pythonName);
			if(file.exists()){
				PATH = windowsPath;
			}else{
				file = new File(linuxPath + pythonName);
				if(file.exists()){
					PATH = linuxPath;
				}
			}
			if(PATH == null){
				result = "{errorCode:'system',errorMsg:'[" + windowsPath + "]或[" + linuxPath +"]路径下不存在文件" + pythonName + "'}";
				return result;
			}
			engine = new ScriptEngineManager().getEngineByName("Nashorn");
			importJsFiles();// 引入js文件
			splitQueryString(queryString);
			importLocalJs("handler1");
			do {// 循环获取验证码直到获得正确的发票验证结果
				errorMsg1 = new JSONObject("{'code':'','msg':''}");
				status1 = false;
				errorMsg2 = new JSONObject("{'code':'','msg':''}");
				status2 = false;
				yzmResponseResult = new JSONObject();
				fpyzResponseResult = new JSONObject();
				String yzmResponseBody = HttpClientUtil.getResponseBody((String) getValueByName("yzmQueryUrl"),
						"{'fpdm': '" + invoiceCode + "', " + // 发票代码
								"'r': '" + getValueByName("rad") + "'" + // 随机字符串
						"}");
				System.out.println(yzmResponseBody);
				if (yzmResponseBody.indexOf("responseStatus") == -1) {// 当服务器正确响应请求时，执行一下操作
					yzmResponseResult = new JSONObject(yzmResponseBody);
					String base64Str = (String) yzmResponseResult.get("key1");// 验证码base64字符串
					String yzmColor = (String) yzmResponseResult.get("key4");// 正确验证码颜色
					System.out.println(yzmColor);
					engine.eval("getErrorMsg1('" + base64Str + "')");
					status1 = (boolean) getValueByName("status1");// 从js中获得验证码请求结果
					errorMsg1 = new JSONObject((String) getValueByName("errorMsg1"));//从js中获得验证码请求错误消息
					if (status1 && BaseUtil.GenerateImage(base64Str,PATH, invoiceNo)) {// 当验证码正确返回时，执行下列操作
						autoRecognition(pythonName, yzmColor);
						System.out.println(verificationCode);
						if(verificationCode.indexOf("errorCode") != -1) {
							return verificationCode;
						}
						eval("yzmSj", (String) yzmResponseResult.get("key2"));
						String fpyzResponseBody = HttpClientUtil.getResponseBody((String) getValueByName("queryUrl"),
								"{'fpdm': '" + invoiceCode + "', " + // 发票代码
										"'fphm': '" + invoiceNo + "', " + // 发票号码
										"'kprq': '" + invoiceDate + "', " + // 开票日期
										"'fpje': '" + checkCode + "', " + // 发票金额，增值税专票为不含税金额，增值税普票为校验码后六位
										"'fplx': '" + invoiceType + "', " + // 发票类型， 04->增值税普票，01->增值税专票，10->增值税电子普票
										"'yzm': '" + verificationCode.toLowerCase() + "', " + // 系统识别出的验证码
										"'yzmSj': '" + (String) yzmResponseResult.get("key2") + "', " + // 验证码请求时间
										"'index': '" + (String) yzmResponseResult.get("key3") + "', " + // 验证码请求返回的加密密钥
										"'iv': '" + (String) getValueByName("iv") + "', " + // 加密字段
										"'salt': '" + (String) getValueByName("salt") + "' " + // 加密字段
								"}");
						System.out.println("发票验证结果：" + fpyzResponseBody);
						if (!fpyzResponseBody.equals("")) {
							fpyzResponseBody = fpyzResponseBody.substring(fpyzResponseBody.indexOf("{"),
									fpyzResponseBody.lastIndexOf("}") + 1);// 截取响应报文的json字符串
							System.out.println("发票验证结果截取字段：" + fpyzResponseBody);
							fpyzResponseResult = new JSONObject(fpyzResponseBody);
							System.out.println("发票验证结果json：" + fpyzResponseBody);
							engine.eval("getErrorMsg2('" + (String) fpyzResponseResult.get("key1") + "')");// 解析发票验证请求结果代码
							status2 = (boolean) getValueByName("status2");// 从js中获取发票验证请求错误代码
							errorMsg2 = new JSONObject((String) getValueByName("errorMsg2"));;//从js中获取发票验证请求错误消息
							System.out.println("发票验证请求状态：" + status2);
						}
						if (status2) {// 当发票验证结果正确返回时执行以下操
							JSONObject fpyzResponseObj = new JSONObject(fpyzResponseBody);
							engine.eval("jsonData = " + fpyzResponseObj);
							engine.eval(HttpClientUtil.getResponseBody(
									jsUrl + "/" + (String) fpyzResponseObj.get("key11") + ".js", null));
							System.out.println("解密规则：" + getValueByName("rule"));
							importLocalJs("handler2");
							System.out.println("排序顺序：" + getValueByName("jmsort"));
							System.out.println("发票验证结果格式：" + getValueByName("tt"));
							System.out.println("发票验证结果密文：" + getValueByName("jsonResult"));
							System.out.println("发票类型：" + getValueByName("fplx"));
							System.out.println("发票信息密文：" + getValueByName("fpxxs"));
							System.out.println("货物信息密文：" + getValueByName("hwxxs"));
							System.out.println("响应报文明文：" + getValueByName("checkRsult"));
							result = (String) getValueByName("checkRsult");
							saveInvoice(context, new JSONObject(result));
						} else {
							System.out.println("发票验证请求失败，错误代码：" + errorMsg2.get("code") + "，错误消息：" +errorMsg2.get("msg"));//输出发票验证请求错误消息
						}
					}else {
						System.out.println("验证码请求失败，错误代码：" + errorMsg1.get("code") + "，错误消息：" +errorMsg1.get("msg"));//输出验证码请求错误消息
					}
				}else{
					if(yzmResponseBody.indexOf("503") != -1){
						result = yzmResponseBody;
						return result;
					}
				} 
			} while ((!status1 && (errorMsg1.get("code").equals("010")))|| (!status2 && (errorMsg2.get("code").equals("007") || errorMsg2.get("code").equals("008"))));
			System.out.println("-----------------发票验证结束-------------------");
		} catch (Exception e) {
			e.printStackTrace();
			result = "{errorCode:'system',errorMsg:'" + e.getMessage() + "'}";
			return result;
		}
		try{
			if(!status1)
				result = "{errorCode:'" + (String) errorMsg1.get("code") + "',errorMsg:'" + (String) errorMsg1.get("msg") + "'}";
			else if(!status2)
				result = "{errorCode:'" + (String) errorMsg2.get("code") + "',errorMsg:'" + (String) errorMsg2.get("msg") + "'}";
		}catch(Exception e) {
			e.printStackTrace();
			result = "{errorCode:'system',errorMsg:'" + e.getMessage() + "'}";
			return result;
		}
		return result;
	}
}

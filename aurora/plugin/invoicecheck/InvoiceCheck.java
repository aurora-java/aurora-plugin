package aurora.plugin.invoicecheck;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.json.JSONObject;

import aurora.database.DBUtil;
import aurora.database.service.DatabaseServiceFactory;
import aurora.database.service.SqlServiceContext;
import aurora.plugin.invoicecheck.util.BaseUtil;
import aurora.plugin.invoicecheck.util.HttpClientUtil;
import aurora.plugin.invoicecheck.util.RuoKuaiUtil;
import uncertain.composite.CompositeMap;
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
	private String invoiceType = null;
    private String invoiceCode = null;
    private String invoiceNo = null;
    private String invoiceDate = null;
    private String checkCode = null;
    private String withoutTaxAmount = null;

    private final String RuoKuaiUserName = "twtyjvkg";
    private final String RuoKuaiPassword = "XZ1006066";
    private final String RuoKuaiSoftId = "89749";
    private final String RuoKuaiSoftkey = "5c9027fd681745d6ba12e611585c3f58";
    private String PATH = null;
    private ScriptEngine engine = null;
    private final String jsUrl = "https://inv-veri.chinatax.gov.cn/js/";
    private String verificationCode = "";

    private int status = 0;//验证码请求状态
    private JSONObject yzmResponseResult = null;//验证码请求结果
    private JSONObject fpyzResponseResult = null;//发票验证请求结果
    
    public String getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(String invoiceType) {
        String invoiceTypeCode = null;
        if(invoiceType.equalsIgnoreCase("vat_normal")) {
            invoiceTypeCode = "04";
        }else if(invoiceType.equalsIgnoreCase("vat_special")) {
            invoiceTypeCode = "01";
        }else if(invoiceType.equalsIgnoreCase("vat_electronic_normal")) {
            invoiceTypeCode = "10";
        }
        this.invoiceType = invoiceTypeCode;
    }

    public void setInvoiceCode(String invoiceCode) {
        this.invoiceCode = invoiceCode;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public void setInvoiceDate(Date invoiceDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        this.invoiceDate = formatter.format(invoiceDate);
    }

    public java.sql.Date getInvoiceDate(String invoiceDate) throws Exception {
        if(invoiceDate == null || invoiceDate.trim().length() == 0){
            return null;
        }
        SimpleDateFormat sdf = null;
        java.sql.Date sqlDate = null;
        sdf = new SimpleDateFormat( "yyyy年MM月dd日" );
        Date date = sdf.parse(invoiceDate);
        sqlDate = new java.sql.Date(date.getTime());
        return sqlDate;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }

    public void setWithoutTaxAmount(double withoutTaxAmount) {
        DecimalFormat df = new DecimalFormat();
        this.withoutTaxAmount = df.format(withoutTaxAmount);
    }

    public void setPATH(String PATH) {
        this.PATH = PATH;
    }

    public void setEngine(ScriptEngine engine) {
        this.engine = engine;
    }

    public Number getTaxRate(String taxRate) throws Exception{
        if(taxRate == null || taxRate.trim().length() == 0){
            return null;
        }
        NumberFormat nf= NumberFormat.getPercentInstance();
        return nf.parse(taxRate);
    }

    public Number convertStrToNumber(String str) throws Exception{
        if(str == null || str.trim().length() == 0){
            return null;
        }
        NumberFormat nf = NumberFormat.getNumberInstance();
        return nf.parse(str);
    }
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
		System.out.println(getContextConnection(context).getAutoCommit());
		doCertification(context);
	}

	public void doCertification(CompositeMap context){
        /**
         * @Author: xuzhao
         * @Email: mailto:zhao.xu@hand-china.com
         * @Description:
         * @Time: 2017/12/7 9:43
         * @param
         * @Return:
         */
		Connection conn = null;
        PreparedStatement certification_query_pst = null;
        PreparedStatement certification_upadte_pst = null;
        ResultSet certification_query_rs = null;
        Number certification_id = null;
        try {
            conn = getContextConnection(context);
            certification_query_pst = conn.prepareStatement("SELECT  CERTIFICATION_ID, INVOICE_TYPE, INVOICE_CODE, INVOICE_NUMBER, " +
                    "INVOICE_DATE, WITHOUT_TAX_AMOUNT, CHECK_CODE_LAST_CHARS, USER_ID" +
                    " FROM INV_INVOICE_CERTIFICATION WHERE CERTIFICATE_STATUS = ? AND (CERTIFICATION_RESULT_CODE IS NULL OR CERTIFICATION_RESULT_CODE != ?) ORDER BY CERTIFICATION_ID");
            certification_query_pst.setString(1, "N");
            certification_query_pst.setString(2, "009");
            certification_query_rs = certification_query_pst.executeQuery();
            while (certification_query_rs.next()) {
                certification_id = convertStrToNumber(certification_query_rs.getString(1));
                setInvoiceType(certification_query_rs.getString(2));
                setInvoiceCode(certification_query_rs.getString(3));
                String invoice_number = certification_query_rs.getString(4);
                setInvoiceNo(invoice_number);
                setInvoiceDate(certification_query_rs.getDate(5));
                setWithoutTaxAmount(certification_query_rs.getDouble(6));
                setCheckCode(certification_query_rs.getString(7));
                Number user_id = convertStrToNumber(certification_query_rs.getString(8));
                certification_upadte_pst = conn.prepareStatement("UPDATE INV_INVOICE_CERTIFICATION SET " +
                        "CERTIFICATE_STATUS = ? WHERE CERTIFICATION_ID = ?");
                certification_upadte_pst.setString(1, "I");
                certification_upadte_pst.setObject(2, certification_id);
                certification_upadte_pst.executeUpdate();
                conn.commit();
                String result = invoiceCheck();
                JSONObject resultObj = new JSONObject(result);
                Iterator<String> it = resultObj.keys();
                while (it.hasNext()) {
                    String key = it.next();
                    Object value = resultObj.get(key);
                    if(value.getClass().equals(String.class)){
                        certification_upadte_pst = conn.prepareStatement("UPDATE INV_INVOICE_CERTIFICATION SET " +
                                "CERTIFICATE_STATUS = ?, " +
                                "CERTIFICATION_RESULT_CODE = ?, " +
                                "CERTIFICATION_RESULT_MSG = ? WHERE " +
                                "CERTIFICATION_ID = ?");
                        if(key.equalsIgnoreCase("errorcode")) {
                            if(value.equals("1") || value.equals("009") || value.equals("fpdmerr")) {
                                certification_upadte_pst.setString(1, "F");
                            }else {
                                certification_upadte_pst.setString(1, "N");
                            }
                            certification_upadte_pst.setObject(2, value);
                            certification_upadte_pst.setString(3, resultObj.getString("errorMsg"));
                            certification_upadte_pst.setObject(4, certification_id);
                            certification_upadte_pst.executeUpdate();
                        }
                    }else if(value.getClass().equals(JSONObject.class)){
                        saveInvoice(conn, resultObj, invoice_number, certification_id, user_id);
                    }
                    break;
                }
                conn.commit();
            }
        }catch (Exception e){
            e.printStackTrace();
            rollBack(conn, certification_id, e);
        }finally {
            DBUtil.closeResultSet(certification_query_rs);
            DBUtil.closeStatement(certification_upadte_pst);
            DBUtil.closeStatement(certification_query_pst);
        }
    }

	public void rollBack(Connection conn, Number certification_id, Exception exception){
        /**
         * @Author: xuzhao
         * @Email: mailto:zhao.xu@hand-china.com
         * @Description:
         * @Time: 2017/12/8 13:58
         * @param conn
         * @param certification_id
         * @Return:
         */
        PreparedStatement pst = null;
        try{
            conn.rollback();
            pst = conn.prepareStatement("UPDATE INV_INVOICE_CERTIFICATION SET CERTIFICATE_STATUS = ?, " +
                    "CERTIFICATION_RESULT_CODE = ?, " +
                    "CERTIFICATION_RESULT_MSG = ? WHERE CERTIFICATION_ID = ?");
            String classType = exception.getClass().toString();
            pst.setString(1,  "N");
            pst.setString(2,  classType.split(" ")[1]);
            pst.setString(3, exception.getLocalizedMessage());
            pst.setObject(4, certification_id);
            pst.executeUpdate();
            conn.commit();
            System.out.println("-----------------数据库操作过程中出现异常------------------");
            System.out.println("-----------------操作已回滚--------------------");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            DBUtil.closeStatement(pst);
        }
    }

    public void saveInvoice(Connection conn, JSONObject result, String invoice_number, Number certification_id, Number user_id) throws Exception{
        /**
         * @Author: xuzhao
         * @Email: mailto:zhao.xu@hand-china.com
         * @Description:
         * @Time: 2017/12/7 14:55
         * @param
         * @Return:
         */

        PreparedStatement pst = null;
        try {
            JSONObject invocie = result.getJSONObject("header_information");
            pst = conn.prepareStatement("INSERT INTO INV_INVOICES " +
                    "(INVOICE_ID, INVOICE_CODE, INVOICE_NUMBER, INVOICE_DATE, INVOICE_DISTRECT, INVOICE_TYPE, " +
                    "MACHINE_NUMBER, CHECK_CODE, PURCHASER_NAME, PURCHASER_TAX_NUMBER, PURCHASER_ADDRESS_PHONE, PURCHASER_BANK_ACCOUNT, " +
                    "AMOUNT, AMOUNT_ZHS, TAX_AMOUNT, WITHOUT_TAX_AMOUNT, SELLER_NAME, SELLER_TAX_NUMBER, " +
                    "SELLER_ADDRESS_PHONE, SELLER_BANK_ACCOUNT) VALUES (" +
                    "INV_INVOICES_S.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            pst.setString(1, invocie.getString("INVOICE_CODE".toLowerCase()));
            pst.setString(2, invocie.getString("INVOICE_NUMBER".toLowerCase()));
            pst.setDate(3, getInvoiceDate(invocie.getString("INVOICE_DATE".toLowerCase())));
            pst.setString(4, invocie.getString("INVOICE_DISTRECT".toLowerCase()));
            pst.setString(5, invocie.getString("INVOICE_TYPE".toLowerCase()));
            pst.setString(6, invocie.getString("MACHINE_NUMBER".toLowerCase()));
            pst.setString(7, invocie.getString("CHECK_CODE".toLowerCase()));
            pst.setString(8, invocie.getString("PURCHASER_NAME".toLowerCase()));
            pst.setString(9, invocie.getString("PURCHASER_TAX_NUMBER".toLowerCase()));
            pst.setString(10, invocie.getString("PURCHASER_ADDRESS_PHONE".toLowerCase()));
            pst.setString(11, invocie.getString("PURCHASER_BANK_ACCOUNT".toLowerCase()));
            pst.setObject(12, convertStrToNumber(invocie.getString("AMOUNT".toLowerCase())));
            pst.setString(13, invocie.getString("AMOUNT_ZHS".toLowerCase()));
            pst.setObject(14, convertStrToNumber(invocie.getString("TAX_AMOUNT".toLowerCase())));
            pst.setObject(15, convertStrToNumber(invocie.getString("WITHOUT_TAX_AMOUNT".toLowerCase())));
            pst.setString(16, invocie.getString("SELLER_NAME".toLowerCase()));
            pst.setString(17, invocie.getString("SELLER_TAX_NUMBER".toLowerCase()));
            pst.setString(18, invocie.getString("SELLER_ADDRESS_PHONE".toLowerCase()));
            pst.setString(19, invocie.getString("SELLER_BANK_ACCOUNT".toLowerCase()));
            pst.execute();
            JSONObject lines = result.getJSONObject("lines_information");
            saveInvoiceLines(conn, lines, invoice_number, certification_id, user_id);
        }finally {
            DBUtil.closeStatement(pst);
        }

    }

    public void saveInvoiceLines(Connection conn, JSONObject lines, String invoice_number, Number certification_id, Number user_id) throws Exception{
        /**
         * @Author: xuzhao
         * @Email: mailto:zhao.xu@hand-china.com
         * @Description:
         * @Time: 2017/12/7 16:23
         * @param
         * @Return:
         */
        Number invoice_id = null;
        PreparedStatement queryPst = null;
        PreparedStatement insertPst = null;
        ResultSet rs = null;
        try{
            queryPst = conn.prepareStatement("SELECT INVOICE_ID FROM INV_INVOICES WHERE INVOICE_NUMBER = ?");
            queryPst.setString(1, invoice_number);
            rs = queryPst.executeQuery();
            while (rs.next()){
                invoice_id = convertStrToNumber(rs.getString(1));
            }
            insertPst = conn.prepareStatement("INSERT INTO INV_INVOICE_LINES " +
                    "(INVOICE_LINE_ID, INVOICE_ID, LINE_NUMBER, GOODS_OR_TAXABLE_SERVICE, SPECIFICATIONS, UNIT, " +
                    "QUANTITY, UNIT_PRICE, WITHOUT_TAX_AMOUNT, TAX_RATE, TAX_AMOUNT) VALUES " +
                    "(INV_INVOICE_LINES_S.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            Iterator<String> it = lines.keys();
            while (it.hasNext()){
                String line_number = it.next();
                JSONObject line = lines.getJSONObject(line_number);
                insertPst.setObject(1, invoice_id);
                insertPst.setObject(2, convertStrToNumber(line_number));
                insertPst.setString(3, line.getString("GOODS_OR_TAXABLE_SERVICE".toLowerCase()));
                insertPst.setString(4, line.getString("SPECIFICATIONS".toLowerCase()));
                insertPst.setString(5, line.getString("UNIT".toLowerCase()));
                insertPst.setObject(6, convertStrToNumber(line.getString("QUANTITY".toLowerCase())));
                insertPst.setObject(7, convertStrToNumber(line.getString("UNIT_PRICE".toLowerCase())));
                insertPst.setObject(8, convertStrToNumber(line.getString("WITHOUT_TAX_AMOUNT".toLowerCase())));
                insertPst.setObject(9, getTaxRate(line.getString("TAX_RATE".toLowerCase())));
                insertPst.setObject(10, convertStrToNumber(line.getString("TAX_AMOUNT".toLowerCase())));
                insertPst.execute();
            }
            afterSaveInvoice(conn, invoice_id, invoice_number, certification_id, user_id);
        }finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closeStatement(queryPst);
            DBUtil.closeStatement(insertPst);
        }
    }

    public void afterSaveInvoice(Connection conn, Number invoice_id, String invoice_number, Number certification_id, Number user_id) throws Exception{
        /**
         * @Author: xuzhao
         * @Email: mailto:zhao.xu@hand-china.com
         * @Description:
         * @Time: 2017/12/7 17:24
         * @param
         * @Return:
         */
        saveWallet(conn, certification_id, user_id, invoice_id);
        PreparedStatement pst = null;
        try {
            pst = conn.prepareStatement("UPDATE INV_INVOICE_CERTIFICATION SET " +
                    "INVOICE_ID = ?, " +
                    "CERTIFICATION_RESULT_CODE = ?, " +
                    "CERTIFICATION_RESULT_MSG = ?, " +
                    "CERTIFICATE_STATUS = ? " +
                    "WHERE INVOICE_NUMBER = ?");
            pst.setObject(1, invoice_id);
            pst.setNull(2, Types.LONGNVARCHAR);
            pst.setNull(3, Types.LONGNVARCHAR);
            pst.setString(4, "Y");
            pst.setString(5, invoice_number);
            pst.executeUpdate();
        }finally {
            DBUtil.closeStatement(pst);
        }
    }

    public void saveWallet(Connection conn, Number certification_id, Number user_id, Number invoice_id) throws Exception{
        /**
         * @Author: xuzhao
         * @Email: mailto:zhao.xu@hand-china.com
         * @Description:
         * @Time: 2017/12/7 18:16
         * @param
         * @Return:
         */
        PreparedStatement pst = null;
        try {
            pst = conn.prepareStatement("INSERT INTO INV_INVOICE_WALLET (USER_INVOICE_ID, USER_ID, CERTIFICATION_ID, INVOICE_ID) " +
                    "VALUES (INV_INVOICE_WALLET_S.nextval, ?, ?, ?)");
            pst.setObject(1, user_id);
            pst.setObject(2, certification_id);
            pst.setObject(3, invoice_id);
            pst.execute();
        }finally {
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
        new File(PATH.trim()+invoiceNo.trim() + ".png").delete();
    }

    private Object getValueByName(String name) {
        /**
         * @Author: xuzhao
         * @Email: mailto:zhao.xu@hand-china.com
         * @Description: 根据变量名获取变量的值
         * @Time: 2017/10/17 18:22
         * @param name js变量名
         * @Return: 变量的值
         */
        return engine.get(name);
    }

    private void importJsFiles() throws Exception{
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
        importLocalJs("init");
    }

    private void importRemoteJs(String jsName) throws Exception{
        /**
         * @Author: xuzhao
         * @Email: mailto:zhao.xu@hand-china.com
         * @Description: 引入远程站点的js文件
         * @Time: 2017/10/17 17:09
         * @param jsName js文件名
         * @Return:
         */
        engine.eval(HttpClientUtil.getResponseBody(jsUrl + jsName + ".js", null));
    }

    private void eval(String name, String value) throws Exception{
        /**
         * @Author: xuzhao
         * @Email: mailto:zhao.xu@hand-china.com
         * @Description: 执行js赋值语句
         * @Time: 2017/10/17 17:14
         * @param name 变量名
         * @param value 变量值
         * @Return:
         */
        engine.eval("var " + name + "= '" + value + "'");

    }

    private void importLocalJs(String jsName) throws Exception{
        /**
         * @Author: xuzhao
         * @Email: mailto:zhao.xu@hand-china.com
         * @Description: 引入本地js文件
         * @Time: 2017/10/17 17:10
         * @param jsName js文件名
         * @Return:
         */
        engine.eval(BaseUtil.getLocalJs(jsName));
    }

    private void initQueryString()throws Exception{
        eval("fplx", invoiceType);
        eval("fpdm", invoiceCode);
        eval("fphm", invoiceNo);
        eval("kprq", invoiceDate);
        if(invoiceType.equals("01")) {//增值税专票，checkCode字段为不含税金额
            eval("kjje", withoutTaxAmount);
        }else{//增值税普票开票checkCode字段为校验码后六位
            eval("kjje", checkCode.substring(checkCode.length()-6, checkCode.length()));
        }
    }

    private String invoiceCheck() throws Exception{
        /**
         * @Author: xuzhao
         * @Email: mailto:zhao.xu@hand-china.com
         * @Description:
         * @Time: 2017/12/7 13:37
         * @param
         * @Return:
         */
        String windowsPath = BaseUtil.getPropertity("windowsPath");
        String linuxPath = BaseUtil.getPropertity("linuxPath");
        String pythonName = BaseUtil.getPropertity("pythonName");
        File file = new File(windowsPath + pythonName);
        if (file.exists()) {
            setPATH(windowsPath);
        } else {
            file = new File(linuxPath + pythonName);
            if (file.exists()) {
                setPATH(linuxPath);
            }
        }
        if (PATH == null) {
            return "{errorCode:'system',errorMsg:'" + windowsPath + "'或'" + linuxPath + "'路径下不存在文件'" + pythonName + "'}";
        }
        setEngine(new ScriptEngineManager().getEngineByName("Nashorn"));
        importJsFiles();//引入js文件
        initQueryString();
        importLocalJs("handler1");
        do {//循环获取验证码直到获得正确的发票验证结果
            yzmResponseResult = new JSONObject();
            fpyzResponseResult = new JSONObject();
            engine.eval("var nowtime = showTime().toString()");
            engine.eval("publickey = $I.ck(fpdm,nowtime)");
            String yzmResponseBody = HttpClientUtil.getResponseBody((String) getValueByName("yzmQueryUrl"),
                    "{'fpdm': '" + invoiceCode + "', " +//发票代码
                            "'r': '" + getValueByName("rad") + "', " +//随机字符串
                            "'callback': 'jQuery110206610264423992902_1512534941292', " +//无用字段，为满足参数要求
                            "'nowtime': '" + (String) getValueByName("nowtime") + "', " +//当前事件
                            "'publickey': '" + (String) getValueByName("publickey") + "'" +//验证公钥
                            "}");
            System.out.println(yzmResponseBody);
            if (yzmResponseBody.toLowerCase().indexOf("errorcode") != -1) {//当服务器正确响应请求时，执行一下操作，只有响应状态码非200时，才会返回错误消息
                return yzmResponseBody;
            }
            yzmResponseBody = yzmResponseBody.substring(yzmResponseBody.indexOf("{"), yzmResponseBody.lastIndexOf("}") + 1);//截取响应报文的json字符串
            yzmResponseResult = new JSONObject(yzmResponseBody);
            String base64Str = (String) yzmResponseResult.get("key1");//验证码base64字符串
            engine.eval("handleMsg('" + base64Str + "')");
            status = (Integer) getValueByName("status");//从js中获得验证码请求结果
            if(status == -1){
                return (String)getValueByName("errorMsg");
            }else if(status == 0){
                continue;
            }
            String yzmColor = (String) yzmResponseResult.get("key4");//正确验证码颜色
            System.out.println(yzmColor);
            System.out.println(base64Str);
            if (BaseUtil.GenerateImage(base64Str, PATH, invoiceNo)) {//当验证码正确返回时，执行下列操作
                autoRecognition(pythonName, yzmColor);
                verificationCode = verificationCode.toUpperCase();
                System.out.println(verificationCode);
                if (verificationCode.indexOf("errorCode") != -1) {
                    return verificationCode;
                }
                eval("yzmSj", (String) yzmResponseResult.get("key2"));
                eval("yzm", verificationCode);
                engine.eval("publickey = $I.ck(fpdm,fphm,kjje,kprq,yzmSj,yzm)");
                String fpyzResponseBody = HttpClientUtil.getResponseBody((String) getValueByName("queryUrl"),
                        "{'fpdm': '" + invoiceCode + "', " +//发票代码
                                "'fphm': '" + invoiceNo + "', " +//发票号码
                                "'callback': 'jQuery110206610264423992902_1512534941292', " +
                                "'kprq': '" + invoiceDate + "', " +//开票日期
                                "'fpje': '" + (String) getValueByName("kjje") + "', " +//发票金额，增值税专票为不含税金额，增值税普票为校验码后六位
                                "'fplx': '" + invoiceType + "', " +//发票类型， 04->增值税普票，01->增值税专票，10->增值税电子普票
                                "'yzm': '" + verificationCode + "', " +//系统识别出的验证码
                                "'yzmSj': '" + (String) yzmResponseResult.get("key2") + "', " +//验证码请求时间
                                "'index': '" + (String) yzmResponseResult.get("key3") + "', " +//验证码请求返回的加密密钥
                                "'iv': '" + (String) getValueByName("iv") + "', " +//加密字段
                                "'salt': '" + (String) getValueByName("salt") + "', " +//加密字段
                                "'publickey': '" + (String) getValueByName("publickey") + "','_':'1512525397802' " +//加密字段 +
                                "}");
                System.out.println("发票验证结果：" + fpyzResponseBody);
                if (!fpyzResponseBody.equals("")) {
                    fpyzResponseBody = fpyzResponseBody.substring(fpyzResponseBody.indexOf("{"), fpyzResponseBody.lastIndexOf("}") + 1);//截取响应报文的json字符串
                    System.out.println("发票验证结果截取字段：" + fpyzResponseBody);
                    if (fpyzResponseBody.toLowerCase().indexOf("errorcode") != -1) {
                        return fpyzResponseBody;
                    }
                    fpyzResponseResult = new JSONObject(fpyzResponseBody);
                    System.out.println("发票验证结果json：" + fpyzResponseBody);
                    engine.eval("handleMsg('" + (String) fpyzResponseResult.get("key1") + "')");//解析发票验证请求结果代码
                    status = (Integer) getValueByName("status");//从js中获取发票验证请求错误代码
                    //从js中获取发票验证请求错误消息
                    System.out.println("发票验证请求状态：" + status);
                    if (status == -1) {
                        return (String) getValueByName("errorMsg");
                    } else if (status == 0) {
                        continue;
                    }
                }
                System.out.println(fpyzResponseBody);
                JSONObject fpyzResponseObj = new JSONObject(fpyzResponseBody);
                engine.eval("jsonData = " + fpyzResponseObj);
                engine.eval(HttpClientUtil.getResponseBody(jsUrl + "/" + (String) fpyzResponseObj.get("key11") + ".js", null));
                System.out.println("解密规则：" + getValueByName("rule"));
                importLocalJs("handler2");
                System.out.println("排序顺序：" + getValueByName("jmsort"));
                System.out.println("发票验证结果格式：" + getValueByName("tt"));
                System.out.println("发票验证结果密文：" + getValueByName("jsonResult"));
                System.out.println("发票类型：" + getValueByName("fplx"));
                System.out.println("发票信息密文：" + getValueByName("fpxxs"));
                System.out.println("货物信息密文：" + getValueByName("hwxxs"));
                System.out.println("响应报文明文：" + getValueByName("checkRsult"));
                return  (String) getValueByName("checkRsult");
            }
        } while (true);
    }
}
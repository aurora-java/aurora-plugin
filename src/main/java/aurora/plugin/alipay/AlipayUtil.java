package aurora.plugin.alipay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;


public class AlipayUtil {
	/**
     * 支付宝提供给商户的服务接入网关URL(新)
     */
    private static final String ALIPAY_GATEWAY_NEW = "https://mapi.alipay.com/gateway.do?";
	/**
     * HTTPS形式消息验证地址
     */
    private static final String HTTPS_VERIFY_URL = "https://www.alipay.com/cooperate/gateway.do?service=notify_verify&";

    /**
     * HTTP形式消息验证地址
     */
    private static final String HTTP_VERIFY_URL  = "http://notify.alipay.com/trade/notify_query.do?";
    
    /**
     * 生成要请求给支付宝的参数数组
     * @param sParaTemp 请求前的参数数组
     * @return 要请求的参数数组
     */
    public static Map<String, String> buildRequestPara(Map<String, String> sParaTemp) {
        //除去数组中的空值和签名参数
        Map<String, String> sPara = AlipayCore.paraFilter(sParaTemp);
        //生成签名结果
        String mysign = AlipayCore.buildMysign(sPara);
        //System.out.println("签名结果="+mysign);
        //签名结果与签名方式加入请求提交参数组中
        sPara.put("sign", mysign);
        sPara.put("sign_type", AlipayConfig.sign_type);

        return sPara;
    }


    /**
     * 验证消息是否是支付宝发出的合法消息
     */
    public static boolean verify(Map<String, String> params) {

        String mysign = getMysign(params);
        String responseTxt = "true";
        if(params.get("notify_id") != null) {
        	responseTxt = verifyResponse(params.get("notify_id"));
    	}
        String sign = "";
        if(params.get("sign") != null) {
        	sign = params.get("sign");
    	}

        //写日志记录（若要调试，请取消下面两行注释）
        //String sWord = "responseTxt=" + responseTxt + "\n notify_url_log:sign=" + sign + "&mysign="
        //              + mysign + "\n 返回参数：" + AlipayCore.createLinkString(params);
        //AlipayCore.logResult(sWord);


        //验证
        //responsetTxt的结果不是true，与服务器设置问题、合作身份者ID、notify_id一分钟失效有关
        //mysign与sign不等，与安全校验码、请求时的参数格式（如：带自定义参数等）、编码格式有关
        if (mysign.equals(sign) && responseTxt.equals("true")) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * 用于防钓鱼，调用接口query_timestamp来获取时间戳的处理函数
     * 注意：远程解析XML出错，与服务器是否支持SSL等配置有关
     * @return 时间戳字符串
     * 注意导入jaxen-1.1-beta-6.jar
     */
	public static String query_timestamp() throws MalformedURLException,
                                                        DocumentException, IOException {

        //构造访问query_timestamp接口的URL串
        String strUrl = ALIPAY_GATEWAY_NEW + "service=query_timestamp&partner=" + AlipayConfig.partner;
        StringBuffer result = new StringBuffer();

        SAXReader reader = new SAXReader();
        Document doc = reader.read(new URL(strUrl).openStream());

        List<Node> nodeList = doc.selectNodes("//alipay/*");

        for (Node node : nodeList) {
            // 截取部分不需要解析的信息
            if (node.getName().equals("is_success") && node.getText().equals("T")) {
                // 判断是否有成功标示
                List<Node> nodeList1 = doc.selectNodes("//response/timestamp/*");
                for (Node node1 : nodeList1) {
                    result.append(node1.getText());
                }
            }
        }

        return result.toString();
    }
    
    /*
     * 获取本机ip
     */
    public static String getIp(){
    	InetAddress addr;
    	String ip="";
		try {
			addr = InetAddress.getLocalHost();
	    	ip=addr.getHostAddress().toString();//获得本机IP
		} catch (UnknownHostException e) {
			ip="Bad IP Address!"+e;
			System.out.println("Bad IP Address!"+e);
		}
		return ip;
    }
    
    /**
     * 根据反馈回来的信息，生成签名结果
     * @param Params 通知返回来的参数数组
     * @return 生成的签名结果
     */
    private static String getMysign(Map<String, String> Params) {
        Map<String, String> sParaNew = AlipayCore.paraFilter(Params);//过滤空值、sign与sign_type参数
        String mysign = AlipayCore.buildMysign(sParaNew);//获得签名结果
        return mysign;
    }

    /**
    * 获取远程服务器ATN结果,验证返回URL
    * @param notify_id 通知校验ID
    * @return 服务器ATN结果
    * 验证结果集：
    * invalid命令参数不对 出现这个错误，请检测返回处理中partner和key是否为空 
    * true 返回正确信息
    * false 请检查防火墙或者是服务器阻止端口问题以及验证时间是否超过一分钟
    */
    private static String verifyResponse(String notify_id) {
        //获取远程服务器ATN结果，验证是否是支付宝服务器发来的请求
        String transport = AlipayConfig.transport;
        String partner = AlipayConfig.partner;
        String veryfy_url = "";
        if (transport.equalsIgnoreCase("https")) {
            veryfy_url = HTTPS_VERIFY_URL;
        } else {
            veryfy_url = HTTP_VERIFY_URL;
        }
        veryfy_url = veryfy_url + "partner=" + partner + "&notify_id=" + notify_id;

        return checkUrl(veryfy_url);
    }

    /**
    * 获取远程服务器ATN结果
    * @param urlvalue 指定URL路径地址
    * @return 服务器ATN结果
    * 验证结果集：
    * invalid命令参数不对 出现这个错误，请检测返回处理中partner和key是否为空 
    * true 返回正确信息
    * false 请检查防火墙或者是服务器阻止端口问题以及验证时间是否超过一分钟
    */
    private static String checkUrl(String urlvalue) {
        String inputLine = "";

        try {
            URL url = new URL(urlvalue);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection
                .getInputStream()));
            inputLine = in.readLine().toString();
        } catch (Exception e) {
            e.printStackTrace();
            inputLine = "";
        }

        return inputLine;
    }
    //获取系统当前时间
    public static String getCurrentDate(){
    	SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    	String date_now=fmt.format(new Date());
    	return date_now;
    }
    //组装退款批次号，batch_no的格式为：退款日期（8位）+流水号（3～24位）。
    public static String getBatchNO(String id){
    	SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
    	String date_now=fmt.format(new Date());
    	return date_now+id;
    }
    
    
}

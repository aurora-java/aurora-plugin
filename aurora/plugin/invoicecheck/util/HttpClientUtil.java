package aurora.plugin.invoicecheck.util;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * @Author: xuzhao
 * @Email: mailto:zhao.xu@hand-china.com
 * @Description: 基于HttpClient的网页数据爬虫
 * @Time: 2017/10/17 14:27
 */
public class HttpClientUtil {

	private static final String CHARSET = "UTF-8";

	private static CloseableHttpClient httpClient = null;
	private static HttpClientContext context = null;
	private static CookieStore cookieStore = null;
	private static RequestConfig requestConfig = null;

	static {
		try {
			init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void init() throws Exception{
		/**
		 * @Author: xuzhao
		 * @Email: mailto:zhao.xu@hand-china.com
		 * @Description: 初始化httpClient，绕过证书验证
		 * @Time: 2017/10/17 14:28
		 * @param
		 * @Return:
		 */
		SSLContext sslContext = createIgnoreVerifySSL();
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", new SSLConnectionSocketFactory(sslContext))
				.build();
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		HttpClients.custom().setConnectionManager(connectionManager);
		context = HttpClientContext.create();
		cookieStore = new BasicCookieStore();
		requestConfig = RequestConfig.custom().setConnectTimeout(120000)
				.setSocketTimeout(60000)
				.setConnectionRequestTimeout(60000).build();
		httpClient = HttpClientBuilder.create().setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
				.setRedirectStrategy(new DefaultRedirectStrategy())
				.setDefaultRequestConfig(requestConfig)
				.setConnectionManager(connectionManager)
				.setDefaultCookieStore(cookieStore).build();
	}

	private static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
		/**
		 * @Author: xuzhao
		 * @Email: mailto:zhao.xu@hand-china.com
		 * @Description: 创建SSL上下文，忽略证书验证
		 * @Time: 2017/10/17 14:31
		 * @param
		 * @Return: SSL上下文
		 */
		SSLContext sc = SSLContext.getInstance("SSLv3");
		X509TrustManager trustManager = new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		};
		sc.init(null, new TrustManager[]{trustManager}, null);
		return sc;
	}

	private static CloseableHttpResponse doGet(String url) throws Exception{
		/**
		 * @Author: xuzhao
		 * @Email: mailto:zhao.xu@hand-china.com
		 * @Description: 根据url发起doGet请求
		 * @Time: 2017/10/17 14:33
		 * @param url 请求地址
		 * @Return: 响应的response对象
		 */
		CloseableHttpResponse response = null;
		HttpGet httpGet = new HttpGet(url);
		response = httpClient.execute(httpGet);
		return response;
	}

	private static Map<String, String> initParamMap(String params) throws Exception{
		/**
		 * @Author: xuzhao
		 * @Email: mailto:zhao.xu@hand-china.com
		 * @Description: 根据参数字符串初始化参数列表
		 * @Time: 2017/10/17 14:39
		 * @param params 参数json字符串
		 * @Return: 包含参数列表的map
		 */
		Map<String, String> map = new HashMap<String, String>();
		JSONObject paramsObj = new JSONObject(params);
		Iterator it = paramsObj.keys();
		while (it.hasNext()){
			String key = (String) it.next();
			map.put(key, (String) paramsObj.get(key));
		}
		return map;
	}

	private static CloseableHttpResponse doPost(String url, String params) throws Exception{
		/**
		 * @Author: xuzhao
		 * @Email: mailto:zhao.xu@hand-china.com
		 * @Description:
		 * @Time: 2017/10/17 14:55
		 * @param url 请求地址
		 * @param params 参数列表
		 * @Return: 响应的response对象
		 */
		CloseableHttpResponse response = null;
		Map<String, String> map = initParamMap(params);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		if(map != null){
			for (Map.Entry<String, String> entry : map.entrySet()) {
				nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
		}
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(new UrlEncodedFormEntity(nvps, CHARSET));
		response = httpClient.execute(httpPost, context);
		return response;
	}

	public static String getResponseBody(String url, String params) throws Exception{
		/**
		 * @Author: xuzhao
		 * @Email: mailto:zhao.xu@hand-china.com
		 * @Description: 获取请求响应报文
		 * @Time: 2017/10/17 15:06
		 * @param url 请求地址
		 * @param params 参数列表
		 * @Return: response报文
		 */
		CloseableHttpResponse response = null;
		if(params == null){
			response = doGet(url);
		}else{
			response = doPost(url, params);
		}
		if(response.getStatusLine().getStatusCode() != 200){
			return "{'responseStatus'：'" + response.getStatusLine().getStatusCode() + "'}";
		}
		HttpEntity entity = response.getEntity();
		String body = "";
		if(entity != null){
			body = EntityUtils.toString(entity, CHARSET);
		}
		EntityUtils.consume(entity);
		response.close();
		return body;
	}
}

package aurora.plugin.sharepoint;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;

import sun.misc.BASE64Encoder;
import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.composite.XMLOutputter;
import uncertain.exception.ConfigurationFileException;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import aurora.service.ServiceThreadLocal;
import aurora.service.ws.SOAPServiceInterpreter;

public class WebServiceUtil {
	public static final int DEFAULT_CONNECT_TIMEOUT = 60 * 1000;
	public static final int DEFAULT_READ_TIMEOUT = 0;//600 * 1000;

	boolean raiseExceptionOnError = true;
	int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
	int readTimeout = DEFAULT_READ_TIMEOUT;
	String user;
	String password;
	boolean noCDATA = false;

	public static final String WS_INVOKER_ERROR_CODE = "aurora.service.ws.invoker_error";

	public WebServiceUtil(){
	}
	
	public WebServiceUtil(String user,String password){
		this.user = user;
		this.password = password;
	}
	
	public CompositeMap request(String requestUrl,CompositeMap requestBody) throws Exception {
		return request(requestUrl,"urn:anonOutInOp",requestBody);
	}
	
	public CompositeMap request(String requestUrl,String soapAction,CompositeMap requestBody) throws Exception {
		CompositeMap context = ServiceThreadLocal.getCurrentThreadContext();
		ILogger logger = LoggingContext.getLogger(context, this.getClass().getCanonicalName());
		URI uri = new URI(requestUrl);
		URL url = uri.toURL();
		OutputStreamWriter writer = null;
		InputStream inputStream = null;
		CompositeMap soapBody = createSOAPBody();
		soapBody.addChild(requestBody);
		String content = XMLOutputter.defaultInstance().toXML(soapBody.getRoot(), true);
//		logger.config("request:\r\n" + content);
		if (isNoCDATA()) {
			content = removeCDATA(content);
		}
		HttpURLConnection httpUrlConnection = null;
		try {
			httpUrlConnection = (HttpURLConnection) url.openConnection();

			httpUrlConnection.setDoInput(true);
			httpUrlConnection.setDoOutput(true);
			httpUrlConnection.setRequestMethod("POST");

			httpUrlConnection.setConnectTimeout(connectTimeout);
			httpUrlConnection.setReadTimeout(readTimeout);

			// set request header
			
			httpUrlConnection.setRequestProperty("Accept", "text/xml, multipart/related");
			addAuthorization(httpUrlConnection, context);
			httpUrlConnection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
			httpUrlConnection.setRequestProperty("SOAPAction", "\""+soapAction+"\"");
			
			
			httpUrlConnection.connect();
			OutputStream os = httpUrlConnection.getOutputStream();
			writer = new OutputStreamWriter(os, "UTF-8");
			writer.write("<?xml version='1.0' encoding='UTF-8'?>");
			writer.write(content);
			writer.flush();
			writer.close();
			String soapResponse = null;
			CompositeLoader cl = new CompositeLoader();
			// http status ok
			if (HttpURLConnection.HTTP_OK == httpUrlConnection.getResponseCode()) {
				inputStream = httpUrlConnection.getInputStream();
				soapResponse = inputStream2String(inputStream);
//				logger.config("HTTP_OK. response:" + soapResponse);
			} else {
				inputStream = httpUrlConnection.getInputStream();
				soapResponse = inputStream2String(inputStream);
//				logger.config("HTTP_ERROR. response:" + soapResponse);
				if (raiseExceptionOnError) {
					throw new ConfigurationFileException(WS_INVOKER_ERROR_CODE, new Object[] { url, soapResponse }, null);
				}
			}
			httpUrlConnection.disconnect();
			CompositeMap soap = cl.loadFromString(soapResponse, "UTF-8");
			CompositeMap result = (CompositeMap) soap.getChild(SOAPServiceInterpreter.BODY.getLocalName()).getChilds().get(0);
			return result;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "", e);
			throw new RuntimeException(e);
		} finally {
			colse(writer);
			colse(inputStream);
			if (httpUrlConnection != null) {
				httpUrlConnection.disconnect();
			}
		}
	}

	private CompositeMap createSOAPBody() {
		CompositeMap env = new CompositeMap(SOAPServiceInterpreter.ENVELOPE.getPrefix(), "http://schemas.xmlsoap.org/soap/envelope/",
				SOAPServiceInterpreter.ENVELOPE.getLocalName());
		CompositeMap body = new CompositeMap(SOAPServiceInterpreter.BODY.getPrefix(), "http://schemas.xmlsoap.org/soap/envelope/",
				SOAPServiceInterpreter.BODY.getLocalName());
		env.addChild(body);
		return body;
	}

	public boolean isNoCDATA() {
		return noCDATA;
	}

	public void setNoCDATA(boolean noCDATA) {
		this.noCDATA = noCDATA;
	}

	private String removeCDATA(String source) {
		source = source.replaceAll("<!\\[CDATA\\[", "");
		source = source.replaceAll("]]>", "");
		return source;

	}

	private void addAuthorization(HttpURLConnection httpUrlConnection, CompositeMap context) {
		if (user == null)
			return;
		ILogger logger = LoggingContext.getLogger(context, this.getClass().getCanonicalName());
		String userName = TextParser.parse(user, context);
		String passwd = TextParser.parse(password, context);
		String fullText = userName + ":" + passwd;
		logger.config("plan user/password:" + fullText);
		BASE64Encoder base64 = new BASE64Encoder();
		String decodeText = "Basic " + base64.encode(fullText.getBytes());
		logger.config("decode user/password:" + decodeText);
		httpUrlConnection.setRequestProperty("Authorization", decodeText);
	}

	public String inputStream2String(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int i = -1;
		while ((i = is.read()) != -1) {
			baos.write(i);
		}
		String result = new String(baos.toByteArray(), "UTF-8");
		return result;
	}

	private void colse(Closeable resource) {
		if (resource == null)
			return;
		try {
			resource.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}

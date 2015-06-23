package aurora.plugin.ws.proxy;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;
import uncertain.composite.QualifiedName;
import uncertain.core.UncertainEngine;
import uncertain.event.RuntimeContext;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;
import aurora.application.features.ServiceLogging;
import aurora.service.http.FacadeServlet;
import aurora.service.http.HttpServiceFactory;
import aurora.service.http.HttpServiceInstance;
import aurora.service.http.WebContextInit;

import com.sun.xml.internal.messaging.saaj.util.Base64;

public class WebServiceProxy extends HttpServlet {

	private static final long serialVersionUID = -278029498162151076L;

	public static final String HEAD_SOAP_PARAMETER = "soapaction";
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static final String DEFAULT_SOAP_CONTENT_TYPE = "text/xml;charset=utf-8";

	public static final QualifiedName ENVELOPE = new QualifiedName("soapenv", "http://schemas.xmlsoap.org/soap/envelope/", "Envelope");
	public static final QualifiedName HEADER = new QualifiedName("soapenv", "http://schemas.xmlsoap.org/soap/envelope/", "Header");
	public static final QualifiedName BODY = new QualifiedName("soapenv", "http://schemas.xmlsoap.org/soap/envelope/", "Body");
	public static final QualifiedName FAULT = new QualifiedName("soapenv", "http://schemas.xmlsoap.org/soap/envelope/", "Fault");
	public static final QualifiedName FAULTSTRING = new QualifiedName("soapenv", "http://schemas.xmlsoap.org/soap/envelope/", "faultstring");

	public static final String INIT_P_USER = "user";
	public static final String INIT_P_PASSWORD = "password";

	public static final int DEFAULT_CONNECT_TIMEOUT = 60 * 1000;
	public static final int DEFAULT_READ_TIMEOUT = 600 * 1000;

	String topic = this.getClass().getCanonicalName();
	int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
	int readTimeout = DEFAULT_READ_TIMEOUT;

	private IObjectRegistry mRegistry;
	private ILogger globalLogger;

	String user;
	String password;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ServletContext context = config.getServletContext();
		UncertainEngine uncertainEngine = WebContextInit.getUncertainEngine(context);
		if (uncertainEngine == null)
			throw new ServletException("Uncertain engine not initialized");

		// get global service config
		mRegistry = uncertainEngine.getObjectRegistry();
		if (mRegistry == null)
			throw new ServletException("IObjectRegistry not initialized");
		globalLogger = LoggingContext.getLogger(topic, mRegistry);
		user = super.getInitParameter(INIT_P_USER);
		globalLogger.config("user:" + user);
		password = super.getInitParameter(INIT_P_PASSWORD);
		globalLogger.config("password:" + password);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!isSOAPRequest(request))
			return;
		if (user != null || password != null) {
			if (!validAuthorization(request,globalLogger)) {
				onCreateFailResponse(response, new IllegalArgumentException("user/password is invalid!"), globalLogger);
				return;
			}
		}
		String soapContent = inputStream2String(request.getInputStream());
		HttpServiceInstance svc = null;
		try {
			svc = createServiceInstance(request, response);
		} catch (Exception e) {
			globalLogger.log(Level.SEVERE, "", e);
			onCreateFailResponse(response, e, globalLogger);
			return;
		}
		CompositeMap context = svc.getContextMap();
		ILogger logger = LoggingContext.getLogger(context, this.getClass().getCanonicalName());
		logger.config("request Full:" + LINE_SEPARATOR + soapContent);
		if (soapContent == null || "".equals(soapContent))
			return;
		try {
			String targetUrl = getTargetUrl(request);
			logger.config("targetUrl:" + targetUrl);
			Map headers = getRequestHeader(request, context);
			String userPassword = getUserPassword(request);
			logger.config("userPassword:" + userPassword);
			String soapReponse = callWebService(targetUrl, soapContent, headers, userPassword, logger);
			onCreateSuccessResponse(response, soapReponse, logger);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "", e);
			onCreateFailResponse(response, e, logger);
			return;
		}
	}

	private String getUserPassword(HttpServletRequest request) {
		return request.getHeader("user");
	}

	private String getTargetUrl(HttpServletRequest request) {
		return request.getHeader("targetUrl");
	}


	private Map getRequestHeader(HttpServletRequest request, CompositeMap context) {
		CompositeMap req_map = context.getChild("request");
		if (req_map == null)
			req_map = context.createChild("request");
		Enumeration head_enum = request.getHeaderNames();
		while (head_enum.hasMoreElements()) {
			String head = (String) head_enum.nextElement();
			String head_value = request.getHeader(head);
			req_map.put(head, head_value);
		}
		return req_map;
	}

	public String callWebService(String targetUrl, String soap, Map headers, String userPassword, ILogger logger) throws Exception {

		URI uri = new URI(targetUrl);
		URL url = uri.toURL();
		PrintWriter out = null;
		BufferedReader br = null;

		HttpURLConnection httpUrlConnection = null;
		try {
			httpUrlConnection = (HttpURLConnection) url.openConnection();

			httpUrlConnection.setDoInput(true);
			httpUrlConnection.setDoOutput(true);
			httpUrlConnection.setRequestMethod("POST");

			httpUrlConnection.setConnectTimeout(connectTimeout);
			httpUrlConnection.setReadTimeout(readTimeout);

			// set request header
			Iterator headerIt = headers.entrySet().iterator();
			while (headerIt.hasNext()) {
				Map.Entry entry = (Map.Entry) headerIt.next();
				String headerName = entry.getKey().toString();
				String headerValue = entry.getValue().toString();
				logger.config("headerName:" + headerName + " headerValue:" + headerValue);
				if (headerName != null && headerValue != null) {
					httpUrlConnection.setRequestProperty(headerName, headerValue);
				}
			}
			addAuthorization(httpUrlConnection, userPassword, logger);
			httpUrlConnection.connect();
			OutputStream os = httpUrlConnection.getOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
			writer.write(soap);
			writer.flush();
			writer.close();
			String soapResponse = null;
			CompositeLoader cl = new CompositeLoader();
			// http status ok
			if (HttpURLConnection.HTTP_OK == httpUrlConnection.getResponseCode()) {
				soapResponse = inputStream2String(httpUrlConnection.getInputStream());
				logger.config("HTTP_OK. response:" + soapResponse);
			} else {
				soapResponse = inputStream2String(httpUrlConnection.getInputStream());
				logger.config("HTTP_ERROR. response:" + soapResponse);
			}
			httpUrlConnection.disconnect();
			return soapResponse;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) {
				out.close();
			}
			if (br != null) {
				br.close();
			}
			if (httpUrlConnection != null) {
				httpUrlConnection.disconnect();
			}
		}
	}

	private void onCreateSuccessResponse(HttpServletResponse response, String soapResponse, ILogger logger) throws IOException {
		prepareResponse(response);
		PrintWriter out = response.getWriter();
		out.print(soapResponse);
		out.flush();
		out.close();
	}

	public void onCreateFailResponse(HttpServletResponse response, Exception thr, ILogger logger) {
		String stackTraceInfo = getStackTraceInfo(thr);
		// log exception
		if (thr != null) {
			logger.log(Level.SEVERE, stackTraceInfo);
		}
		// set status and message
		CompositeMap fault = new CompositeMap(FAULT.getPrefix(), FAULT.getNameSpace(), FAULT.getLocalName());
		CompositeMap faultstring = new CompositeMap(FAULTSTRING.getPrefix(), FAULTSTRING.getNameSpace(), FAULTSTRING.getLocalName());
		faultstring.setText(stackTraceInfo);
		prepareResponse(response);
		PrintWriter out;
		try {
			out = response.getWriter();
			out.append("<?xml version='1.0' encoding='UTF-8'?>");
			CompositeMap body = createSOAPBody();
			fault.addChild(faultstring);
			body.addChild(fault);
			out.println(body.getRoot().toXML());
			out.flush();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "", e);
		}
	}

	public String getStackTraceInfo(Throwable thr) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		thr.printStackTrace(new PrintStream(baos));
		String str = baos.toString();
		return str;
	}

	private void addAuthorization(HttpURLConnection httpUrlConnection, String userPassword, ILogger logger) {
		httpUrlConnection.setRequestProperty("Authorization", null);
		if (userPassword == null)
			return;
		httpUrlConnection.setRequestProperty("Authorization", userPassword);
	}

	void prepareResponse(HttpServletResponse response)

	{
		response.setContentType(DEFAULT_SOAP_CONTENT_TYPE);
		response.setHeader("Cache-Control", "no-cache, must-revalidate");
		response.setHeader("Server", "Simple-Server/1.1");
		response.setHeader("Transfer-Encoding", "chunked");
		response.setCharacterEncoding("UTF-8");
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

	private boolean isSOAPRequest(HttpServletRequest request) {
		String soapParam = getSOAPAction(request);
		if (soapParam != null)
			return true;
		return false;
	}

	private String getSOAPAction(HttpServletRequest request) {
		String soapParam = request.getHeader(HEAD_SOAP_PARAMETER);
		if (soapParam != null)
			return soapParam;
		soapParam = request.getParameter(HEAD_SOAP_PARAMETER);
		if (soapParam != null)
			return soapParam;
		return null;
	}

	private HttpServiceInstance createServiceInstance(HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String name = generateServiceName(request);
		HttpServiceFactory serviceFactory = (HttpServiceFactory) mRegistry.getInstanceOfType(HttpServiceFactory.class);
		if (serviceFactory == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(null, HttpServiceFactory.class, this.getClass().getName());
		final HttpServiceInstance svc = serviceFactory.createHttpService(name, request, response, this);

		ServiceLogging serviceLogging = (ServiceLogging) mRegistry.getInstanceOfType(ServiceLogging.class);
		if (serviceLogging == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(null, ServiceLogging.class, this.getClass().getName());
		CompositeMap context = svc.getContextMap();
		serviceLogging.onContextCreate(RuntimeContext.getInstance(context));
		return svc;
	}

	private String generateServiceName(HttpServletRequest request) {
		return FacadeServlet.getServiceName(request) + "_" + System.currentTimeMillis();
	}

	private CompositeMap createSOAPBody() {
		CompositeMap env = new CompositeMap(ENVELOPE.getPrefix(), ENVELOPE.getNameSpace(), ENVELOPE.getLocalName());
		CompositeMap body = new CompositeMap(BODY.getPrefix(), BODY.getNameSpace(), BODY.getLocalName());
		env.addChild(body);
		return body;
	}

	private boolean validAuthorization(HttpServletRequest request, ILogger logger) {
		String authorization = request.getHeader("Authorization");
		if (authorization != null) {
			String encodeAuth = authorization.substring("Basic ".length());
			String decode = Base64.base64Decode(encodeAuth);
			String[] strs = decode.split(":");
			String requestUser = strs[0];
			String requestPassword = strs[1];
			logger.config("requestUser:"+requestUser+" requestPassword:"+requestPassword);
			if (user != null && !user.equals(requestUser)) {
				return false;
			}
			if (password != null && !password.equals(requestPassword)) {
				return false;
			}
			return true;
		}
		return false;
	}

	class UserPassword {
		String userName;
		String password;

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}
}

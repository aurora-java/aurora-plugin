package aurora.plugin.bill99.pos;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.KeyStore;

import javax.net.SocketFactory;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import aurora.plugin.bill99.Configuration;

public class RefundFunction {

	public static final String TITLE = "(直连)退货交易";
	// public static final String KEYSTORE_FILE =
	// "D:\\dewei.wang\\workspace\\vposInterface\\src\\sslfunction\\19881998200800190.jks";
	// public static final String KEYSTORE_FILE = "19881998200800190.jks";
	public static final String KEYSTORE_FILE = getValue("pos_refund_keystore_file");
	public static final String ALGORITHM = "sunx509";
	// public static final String PASSWORD = "vpos123";
	public static final String PASSWORD = getValue("pos_refund_password");

	private static String getValue(String key) {
//		String value = Configuration.getInstance().getValue(key);
		String value = Configuration.getValue(Configuration.DEFAULT_CONFIG_FILE, key);
		return value == null ? "" : value;
	}

	public MRefund action(MRefund pf1, MRefund pf) {

		String str = getXml(pf1);
		try {
			KeyManagerFactory kmf;
			KeyStore ks;
			TrustManagerFactory tmf;
			SSLContext sslc;

			kmf = KeyManagerFactory.getInstance(ALGORITHM);
			ks = KeyStore.getInstance("JKS");
			ks.load(this.getClass().getClassLoader()
					.getResourceAsStream(KEYSTORE_FILE), PASSWORD.toCharArray());
			kmf.init(ks, PASSWORD.toCharArray());
			tmf = TrustManagerFactory.getInstance(ALGORITHM);
			tmf.init(ks);

			X509TrustManager defaultTrustManager = (X509TrustManager) tmf
					.getTrustManagers()[0];
			SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);

			sslc = SSLContext.getInstance("SSL");
			sslc.init(kmf.getKeyManagers(), new TrustManager[] { tm },
					new java.security.SecureRandom());

			SocketFactory sf = sslc.getSocketFactory();
			String refundURL = getValue("pos_refund_url");
			// URL url = new URL("https://mas.99bill.com/cnp/refund");// 提交地址
			URL url = new URL(refundURL);
			// 想快钱提交商户编号和私钥密码
			sun.misc.BASE64Encoder en = new sun.misc.BASE64Encoder();
			String merchantId = getValue("pos_refund_merchantId");
			String s = "Basic "
					+ en.encode((merchantId + ":" + PASSWORD).getBytes());

			HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url
					.openConnection();
			httpsURLConnection.setRequestProperty("Authorization", s);

			httpsURLConnection.setSSLSocketFactory((SSLSocketFactory) sf);
			post(httpsURLConnection, str, pf);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return pf;
	}

	private String getXml(MRefund pf) {

		String xml = "";
		SimpleXmlCreater sxc = new SimpleXmlCreater();
		xml += SimpleXmlCreater.xmlHead;
		xml += SimpleXmlCreater.xmlHead2;

		xml += sxc.addFullNode("version", pf.getVersion());
		xml += sxc.addFatherNode("TxnMsgContent", false);
		xml += sxc.addFullNode("txnType", pf.getTxnType());
		xml += sxc.addFullNode("interactiveStatus", pf.getInteractiveStatus());
		xml += sxc.addFullNode("orignalTxnType", pf.getOrignalTxnType());
		xml += sxc.addFullNode("amount", pf.getAmount());
		xml += sxc.addFullNode("merchantId", pf.getMerchantId());
		xml += sxc.addFullNode("terminalId", pf.getTerminalId());
		xml += sxc.addFullNode("entryTime", pf.getEntryTime());
		xml += sxc.addFullNode("origRefNumber", pf.getOrigRefNumber());
		xml += sxc.addFullNode("externalRefNumber", pf.getExternalRefNumber());
		xml += sxc.addFatherNode("TxnMsgContent", true);

		xml += SimpleXmlCreater.xmlEnd;
		return xml;

	}

	private static MRefund printXml(InputStream is, MRefund pf) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(is);

			// ErrorMsgContent/errorCode.errorMessage
			// 读取方法，意义
			NodeList error = doc.getElementsByTagName("ErrorMsgContent");
			for (int i = 0; i < error.getLength(); i++) {
				pf.setErrorCode2(doc.getElementsByTagName("errorCode").item(i)
						.getFirstChild().getNodeValue());
				pf.setErrorMessage2(doc.getElementsByTagName("errorMessage")
						.item(i).getFirstChild().getNodeValue());
			}
			NodeList nl = doc.getElementsByTagName("TxnMsgContent");
			for (int i = 0; i < nl.getLength(); i++) {
				pf.setMerchantId2(doc.getElementsByTagName("merchantId")
						.item(i).getFirstChild().getNodeValue());
				pf.setTxnType2(doc.getElementsByTagName("txnType").item(i)
						.getFirstChild().getNodeValue());
				pf.setVersion2(doc.getElementsByTagName("version").item(i)
						.getFirstChild().getNodeValue());
				pf.setRefNumber2(doc.getElementsByTagName("refNumber").item(i)
						.getFirstChild().getNodeValue());
				pf.setInteractiveStatus2(doc
						.getElementsByTagName("interactiveStatus").item(i)
						.getFirstChild().getNodeValue());
				pf.setAmount2(doc.getElementsByTagName("amount").item(i)
						.getFirstChild().getNodeValue());
				pf.setTerminalId2(doc.getElementsByTagName("terminalId")
						.item(i).getFirstChild().getNodeValue());
				pf.setEntryTime2(doc.getElementsByTagName("entryTime").item(i)
						.getFirstChild().getNodeValue());
				pf.setExternalRefNumber2(doc
						.getElementsByTagName("externalRefNumber").item(i)
						.getFirstChild().getNodeValue());
				// 交易传输时间需要确认，1拼写，2结果含义
				pf.setTransTime2(doc.getElementsByTagName("transTime").item(i)
						.getFirstChild().getNodeValue());
				// 应答结果需要确认，1拼写，2结果含义
				pf.setResponseCode2(doc.getElementsByTagName("responseCode")
						.item(i).getFirstChild().getNodeValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
			}
		}
		return pf;
	}

	public static void post(HttpsURLConnection httpsURLConnection, String str,
			MRefund pf) throws IOException {

		httpsURLConnection.setDoOutput(true);// 打开写入属性
		httpsURLConnection.setDoInput(true);// 打开读取属性
		httpsURLConnection.setRequestMethod("POST");// 设置提交方法
		httpsURLConnection.setRequestProperty("Content-Type",
				"text/xml;charset=UTF-8");//
		httpsURLConnection.setRequestProperty("SOAPAction",
				" http://WebXml.com.cn/getWeatherbyCityName");
		httpsURLConnection.setRequestProperty("User-Agent",
				"Jakarta Commons-HttpClient/3.1");
		httpsURLConnection.setConnectTimeout(50000);// 连接超时时间
		httpsURLConnection.setReadTimeout(50000);
		httpsURLConnection.connect();

		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				httpsURLConnection.getOutputStream()));

		out.write(str);

		out.flush();
		out.close();
		// BufferedReader buf = new BufferedReader(new InputStreamReader(
		// httpsURLConnection.getInputStream(), "UTF-8"));
		// System.out.println("[Server]: " + buf.readLine());
		pf = printXml((InputStream) httpsURLConnection.getInputStream(), pf);
		httpsURLConnection.disconnect();// 断开连接
	}

}

package aurora.plugin.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import uncertain.util.FastStringReader;

public class HttpUtils {

	private HttpUtils() {
	}

	/**
	 * GET METHOD
	 *
	 * @param strUrl   String
	 * @param map      存放参数的map,可以为null
	 * @param encoding 对参数编码时用的编码
	 * @return List
	 * @throws IOException
	 */
	public static InputStream urlGet(String strUrl, Map map, String encoding)
			throws IOException {
		String strTotalURL = strUrl;
		String queryString = getQueryString(map, encoding);
		if (queryString.length() > 0) {
			if (strUrl.indexOf("?") == -1) {
				strTotalURL += "?" + queryString;
			} else {
				strTotalURL += "&" + queryString;
			}
		}
		URL url = new URL(strTotalURL);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		// con.setUseCaches(false);
		// con.setFollowRedirects(true);
		return con.getInputStream();
	}

	/**
	 * POST METHOD
	 *
	 * @param strUrl String
	 * @param map    Map 中的key-value会拼接成queryString形式的数据,value会encode
	 * @return List
	 * @throws IOException
	 */

	public static InputStream urlPost(String strUrl, Map map, String encoding)
			throws IOException {
		String content = getQueryString(map, encoding);
		return urlPost(strUrl, content, "application/x-www-form-urlencoded;charset=" + encoding, encoding);
	}

	/**
	 * default contentType is application/json;charset={@code encoding}
	 *
	 * @param strUrl
	 * @param postData
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static InputStream urlPost(String strUrl, String postData,
	                                  String encoding) throws IOException {
		return urlPost(strUrl, postData, "application/json;charset=" + encoding, encoding);
	}

	public static InputStream urlPost(String strUrl, String postData, String contentType,
	                                  String encoding) throws IOException {
		return urlPost(strUrl, new FastStringReader(postData), contentType, encoding);
	}

	/**
	 * do post for plain text<br/>
	 *
	 * @param strUrl
	 * @param postDataReader
	 * @param contentType
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static InputStream urlPost(String strUrl, Reader postDataReader, String contentType,
	                                  String encoding) throws IOException {

		URL url = new URL(strUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setAllowUserInteraction(false);
		con.setUseCaches(false);
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", contentType);
		OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(),
				encoding);
		IOUtilsEx.transfer(postDataReader, osw);
		osw.flush();
		osw.close();
		con.connect();
		return con.getInputStream();
	}

	/**
	 * @param strUrl         target url
	 * @param postDataStream data source
	 * @param contentType    charset should be specified (if needed). <br/>e.g.
	 *                       <ul>
	 *                       <li>text/xml;charset=UTF-8</li>
	 *                       <li>application/xml</li>
	 *                       <li>application/json;charset=UTF-8</li>
	 *                       <li>application/octet-stream</li>
	 *                       </ul>
	 * @return
	 * @throws IOException
	 */
	public static InputStream urlPost(String strUrl, InputStream postDataStream, String contentType) throws IOException {

		URL url = new URL(strUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setAllowUserInteraction(false);
		con.setUseCaches(false);
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", contentType);
		OutputStream os = con.getOutputStream();
		IOUtilsEx.transfer(postDataStream, os);
		os.flush();
		os.close();
		con.connect();
		return con.getInputStream();
	}

	private static String getQueryString(Map map, String encoding) {
		if (null == map || map.isEmpty()) {
			return "";
		}
		StringBuilder param = new StringBuilder();
		Set keys = map.keySet();
		for (Iterator i = keys.iterator(); i.hasNext(); ) {
			String key = String.valueOf(i.next());
			if (map.containsKey(key)) {
				Object val = map.get(key);
				String str = val != null ? val.toString() : "";
				try {
					str = URLEncoder.encode(str, encoding);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				param.append(key).append("=").append(str).append('&');
			}
		}
		if (param.length() > 0 && '&' == param.charAt(param.length() - 1)) {
			param.deleteCharAt(param.length() - 1);
		}
		return param.toString();
	}

}

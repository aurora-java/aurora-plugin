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
	 * @param strUrl
	 *            String
	 * @param map
	 *            Map
	 * @throws IOException
	 * @return List
	 */
	public static InputStream urlGet(String strUrl, Map map, String encoding)
			throws IOException {
		String strtTotalURL = "";
		if (strUrl.indexOf("?") == -1) {
			strtTotalURL = strUrl + "?" + getQueryString(map, encoding);
		} else {
			strtTotalURL = strUrl + "&" + getQueryString(map, encoding);
		}
		URL url = new URL(strtTotalURL);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		// con.setUseCaches(false);
		// con.setFollowRedirects(true);
		return con.getInputStream();
	}

	/**
	 * POST METHOD
	 * 
	 * @param strUrl
	 *            String
	 * @param content
	 *            Map
	 * @throws IOException
	 * @return List
	 */
	public static InputStream urlPost(String strUrl, Map map, String encoding)
			throws IOException {

		String content = getQueryString(map, encoding);
		return urlPost(strUrl, content, encoding);
	}

	public static InputStream urlPost(String strUrl, String postData,
			String encoding) throws IOException {
		return urlPost(strUrl, new FastStringReader(postData), encoding);
	}

	/**
	 * for plain text
	 * 
	 * @param strUrl
	 * @param postDataReader
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static InputStream urlPost(String strUrl, Reader postDataReader,
			String encoding) throws IOException {

		URL url = new URL(strUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setAllowUserInteraction(false);
		con.setUseCaches(false);
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded;charset=" + encoding);
		OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(),
				encoding);
		IOUtilsEx.transfer(postDataReader, osw);
		osw.flush();
		osw.close();
		con.connect();
		return con.getInputStream();
	}

	/**
	 * for binary data
	 * 
	 * @param strUrl
	 * @param postDataStream
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static InputStream urlPost(String strUrl, InputStream postDataStream,
			String encoding) throws IOException {

		URL url = new URL(strUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setAllowUserInteraction(false);
		con.setUseCaches(false);
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded;charset=" + encoding);
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
		for (Iterator i = keys.iterator(); i.hasNext();) {
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

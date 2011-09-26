package aurora.plugin.sso;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import uncertain.composite.CompositeMap;
import uncertain.core.UncertainEngine;
import uncertain.ocm.IObjectRegistry;

import aurora.service.http.WebContextInit;

public final class SpnegoLoginFilter implements Filter {
	UncertainEngine mUncertainEngine;

	public void init(FilterConfig config) throws ServletException {
		ServletContext context = config.getServletContext();
		mUncertainEngine = WebContextInit.getUncertainEngine(context);
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		if (httpRequest.getRemoteUser() != null) {
			try {
				doLogin(httpRequest, httpResponse);
			} catch (SQLException e) {
				throw new ServletException(e.getCause());
			}
		} else {
			throw new ServletException("userName is null");
		}
		chain.doFilter(request, response);
	}

	private void doLogin(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws SQLException {
		int session_id = -1;
		int user_id = -1;
		int role_id = -1;
		int company_id = -1;
		String lang = null;		
		HttpSession session = httpRequest.getSession(true);
		if (session.getAttribute("user_id") != null)
			return;
		String encrypted_session_id = getCookie(httpRequest.getCookies(),
		"JSID");
		String userName = httpRequest.getRemoteUser();
		String ip = httpRequest.getRemoteHost();		
		IObjectRegistry mObjectRegistry = mUncertainEngine.getObjectRegistry();
		DataSource ds = (DataSource) mObjectRegistry
				.getInstanceOfType(DataSource.class);
		Connection conn = null;
		CallableStatement cstm = null;
		try {
			conn = ds.getConnection();
			cstm = conn
					.prepareCall("{call sys_login_pkg.ad_login(?,?,?,?,?,?,?,?)}");
			cstm.setString(1, userName.toUpperCase());
			cstm.setString(2, ip);
			if (encrypted_session_id == null)
				cstm.setNull(8, Types.VARCHAR);
			else
				cstm.setString(8, encrypted_session_id);
			cstm.registerOutParameter(3, Types.NUMERIC);
			cstm.registerOutParameter(4, Types.NUMERIC);
			cstm.registerOutParameter(5, Types.NUMERIC);
			cstm.registerOutParameter(6, Types.NUMERIC);
			cstm.registerOutParameter(7, Types.VARCHAR);
			cstm.registerOutParameter(8, Types.VARCHAR);
			cstm.execute();
			conn.commit();
			session_id = cstm.getInt(3);
			user_id = cstm.getInt(4);
			role_id = cstm.getInt(5);
			company_id = cstm.getInt(6);
			lang = cstm.getString(7);
			encrypted_session_id = cstm.getString(8);
			if (user_id == -1) {
				PrintWriter out = null;
				try {
					out = httpResponse.getWriter();
					out.println("<script>alert('userName is invalid')</script>");
					return;
				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					if (out != null)
						out.close();
				}
			}
			writeCookie(httpRequest, httpResponse, encrypted_session_id);
			CompositeMap map = new CompositeMap();
			map.put("session_id", session_id);
			map.put("user_id", user_id);
			map.put("lang", lang);
			if (role_id != -1 && company_id != -1) {
				map.put("role_id", role_id);
				map.put("company_id", company_id);					
			}			
			writeSession(map, session);
		} catch (SQLException e) {
			if (conn != null)
				conn.rollback();
			throw e;
		} finally {
			if (conn != null)
				conn.close();
		}
	}

	private void writeSession(CompositeMap map, HttpSession session) {
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Object key = entry.getKey();
			if (key != null)
				session.setAttribute(key.toString(), entry.getValue());
		}
	}

	private void writeCookie(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, String value) {
		Cookie cookie = new Cookie("JSID", value);
		String path = httpRequest.getContextPath();
		path = (path == null || path.length() == 0) ? "/" : path;
		cookie.setPath(path);
		cookie.setMaxAge(-1);
		httpResponse.addCookie(cookie);
	}

	String getCookie(Cookie[] cookies, String key) {
		String value = null;
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(key))
				value = cookie.getValue();
		}
		return value;
	}

	public void destroy() {

	}

}

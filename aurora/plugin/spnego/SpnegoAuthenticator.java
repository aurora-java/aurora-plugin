package aurora.plugin.spnego;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.PrivilegedActionException;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;

import aurora.plugin.spnego.SpnegoConfig.Constants;

public class SpnegoAuthenticator {

	/** GSSContext is not thread-safe. */
	private static final Lock LOCK = new ReentrantLock();

	/** Default GSSManager. */
	private static final GSSManager MANAGER = GSSManager.getInstance();

	/** Flag to indicate if BASIC Auth is allowed. */
	private final transient boolean allowBasic;

	/** Flag to indicate if credential delegation is allowed. */
	private final transient boolean allowDelegation;

	/** Flag to skip auth if localhost. */
	private final transient boolean allowLocalhost;

	/** Flag to indicate if non-SSL BASIC Auth allowed. */
	private final transient boolean allowUnsecure;

	/** Flag to indicate if NTLM is accepted. */
	private final transient boolean promptIfNtlm;

	/** Login Context module name for client auth. */
	private final transient String clientModuleName;

	/** Login Context server uses for pre-authentication. */
	private final transient LoginContext loginContext;

	/** Credentials server uses for authenticating requests. */
	private final transient GSSCredential serverCredentials;

	/** Server Principal used for pre-authentication. */
	private final transient KerberosPrincipal serverPrincipal;

	public SpnegoAuthenticator(final SpnegoConfig config)
			throws LoginException, GSSException, PrivilegedActionException {
		this.allowBasic = config.getAllowBasic();
		this.allowUnsecure = config.getAllowUnsecure();
		this.clientModuleName = config.getClientModuleName();
		this.allowLocalhost = config.getAllowLocalhost();
		this.promptIfNtlm = config.getPromptIfNtlm();
		this.allowDelegation = config.getAllowDelegation();

		final CallbackHandler handler = SpnegoProvider
				.getUsernamePasswordHandler(config.getUsername(),
						config.getPassword());

		this.loginContext = new LoginContext(config.getServerModuleName(),
				handler);

		this.loginContext.login();

		this.serverCredentials = SpnegoProvider
				.getServerCredential(this.loginContext.getSubject());

		this.serverPrincipal = new KerberosPrincipal(this.serverCredentials
				.getName().toString());
	}

	// public SpnegoAuthenticator(final Map<String, String> config)
	// throws LoginException, GSSException, PrivilegedActionException,
	// FileNotFoundException, URISyntaxException {
	//
	// this(SpnegoFilterConfig.getInstance(new FilterConfig() {
	//
	// private final Map<String, String> map = Collections
	// .unmodifiableMap(config);
	//
	// @Override
	// public String getFilterName() {
	// throw new UnsupportedOperationException();
	// }
	//
	// @Override
	// public String getInitParameter(final String param) {
	// if (null == map.get(param)) {
	// throw new NullPointerException(
	// "Config missing param value for: " + param);
	// }
	// return map.get(param);
	// }
	//
	// @SuppressWarnings("rawtypes")
	// @Override
	// public Enumeration getInitParameterNames() {
	// throw new UnsupportedOperationException();
	// }
	//
	// @Override
	// public ServletContext getServletContext() {
	// throw new UnsupportedOperationException();
	// }
	// }));
	// }

	/**
	 * Returns the KerberosPrincipal of the user/client making the HTTP request.
	 * 
	 * <p>
	 * Null may be returned if client did not provide auth info.
	 * </p>
	 * 
	 * <p>
	 * Method will throw UnsupportedOperationException if client authz request
	 * is NOT "Negotiate" or "Basic".
	 * </p>
	 * 
	 * @param req
	 *            servlet request
	 * @param resp
	 *            servlet response
	 * 
	 * @return null if auth not complete else SpnegoPrincipal of client
	 * @throws GSSException
	 * @throws IOException
	 */
	public SpnegoPrincipal authenticate(final HttpServletRequest req,
			final SpnegoHttpServletResponse resp) throws GSSException,
			IOException {

		// determine if we allow basic
		final boolean basicSupported = this.allowBasic
				&& (this.allowUnsecure || req.isSecure());

		// domain/realm of server
		final String serverRealm = this.serverPrincipal.getRealm();

		// Skip auth if localhost
		if (this.allowLocalhost && this.isLocalhost(req)) {
			return doLocalhost();
		}

		final SpnegoPrincipal principal;
		final SpnegoAuthScheme scheme = SpnegoProvider.negotiate(req, resp,
				basicSupported, this.promptIfNtlm, serverRealm);

		// NOTE: this may also occur if we do not allow Basic Auth and
		// the client only supports Basic Auth
		if (null == scheme) {
			// LOGGER.finer("scheme null.");
			return null;
		}

		// NEGOTIATE scheme
		if (scheme.isNegotiateScheme()) {
			principal = doSpnegoAuth(scheme, resp);

			// BASIC scheme
		} else if (scheme.isBasicScheme()) {
			// check if we allow Basic Auth
			if (basicSupported) {
				principal = doBasicAuth(scheme, resp);
			} else {
				// LOGGER.severe("allowBasic=" + this.allowBasic
				// + "; allowUnsecure=" + this.allowUnsecure
				// + "; req.isSecure()=" + req.isSecure());
				throw new UnsupportedOperationException(
						"Basic Auth not allowed" + " or SSL required.");
			}

			// Unsupported scheme
		} else {
			throw new UnsupportedOperationException("scheme=" + scheme);
		}

		return principal;
	}

	/**
	 * Logout. Since server uses LoginContext to login/pre-authenticate, we must
	 * also logout when we are done using this object.
	 * 
	 * <p>
	 * Generally, instantiators of this class should be the only to call
	 * dispose() as it indicates that this class will no longer be used.
	 * </p>
	 */
	public void dispose() {
		if (null != this.serverCredentials) {
			try {
				this.serverCredentials.dispose();
			} catch (GSSException e) {
				// LOGGER.log(Level.WARNING, "Dispose failed.", e);
			}
		}
		if (null != this.loginContext) {
			try {
				this.loginContext.logout();
			} catch (LoginException le) {
				// LOGGER.log(Level.WARNING, "Logout failed.", le);
			}
		}
	}

	/**
	 * Performs authentication using the BASIC Auth mechanism.
	 * 
	 * <p>
	 * Returns null if authentication failed or if the provided the auth scheme
	 * did not contain BASIC Auth data/token.
	 * </p>
	 * 
	 * @return SpnegoPrincipal for the given auth scheme.
	 */
	private SpnegoPrincipal doBasicAuth(final SpnegoAuthScheme scheme,
			final SpnegoHttpServletResponse resp) throws IOException {

		final byte[] data = scheme.getToken();

		if (0 == data.length) {
			// LOGGER.finer("Basic Auth data was NULL.");
			return null;
		}

		final String[] basicData = new String(data).split(":", 2);

		// assert
		if (basicData.length != 2) {
			throw new IllegalArgumentException("Username/Password may"
					+ " have contained an invalid character. basicData.length="
					+ basicData.length);
		}

		// substring to remove domain (if provided)
		final String username = basicData[0].substring(basicData[0]
				.indexOf('\\') + 1);
		final String password = basicData[1];
		final CallbackHandler handler = SpnegoProvider
				.getUsernamePasswordHandler(username, password);

		SpnegoPrincipal principal = null;

		try {
			// assert
			if (null == username || username.isEmpty()) {
				throw new LoginException("Username is required.");
			}

			final LoginContext cntxt = new LoginContext(this.clientModuleName,
					handler);

			// validate username/password by login/logout
			cntxt.login();
			cntxt.logout();

			principal = new SpnegoPrincipal(username + '@'
					+ this.serverPrincipal.getRealm(),
					KerberosPrincipal.KRB_NT_PRINCIPAL);

		} catch (LoginException le) {
			// LOGGER.info(le.getMessage() + ": Login failed. username="
			// + username + "; password.hashCode()=" + password.hashCode());

			resp.setHeader(Constants.AUTHN_HEADER, Constants.NEGOTIATE_HEADER);
			resp.addHeader(Constants.AUTHN_HEADER, Constants.BASIC_HEADER
					+ " realm=\"" + this.serverPrincipal.getRealm() + '\"');

			resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED, true);
		}

		return principal;
	}

	private SpnegoPrincipal doLocalhost() {
		final String username = System.getProperty("user.name");

		if (null == username || username.isEmpty()) {
			return new SpnegoPrincipal(this.serverPrincipal.getName() + '@'
					+ this.serverPrincipal.getRealm(),
					this.serverPrincipal.getNameType());
		} else {
			return new SpnegoPrincipal(username + '@'
					+ this.serverPrincipal.getRealm(),
					KerberosPrincipal.KRB_NT_PRINCIPAL);
		}
	}

	/**
	 * Performs authentication using the SPNEGO mechanism.
	 * 
	 * <p>
	 * Returns null if authentication failed or if the provided the auth scheme
	 * did not contain the SPNEGO/GSS token.
	 * </p>
	 * 
	 * @return SpnegoPrincipal for the given auth scheme.
	 */
	private SpnegoPrincipal doSpnegoAuth(final SpnegoAuthScheme scheme,
			final SpnegoHttpServletResponse resp) throws GSSException,
			IOException {

		final String principal;
		final byte[] gss = scheme.getToken();

		if (0 == gss.length) {
			// LOGGER.finer("GSS data was NULL.");
			return null;
		}

		GSSContext context = null;
		GSSCredential delegCred = null;

		try {
			byte[] token = null;

			SpnegoAuthenticator.LOCK.lock();
			try {
				context = SpnegoAuthenticator.MANAGER
						.createContext(this.serverCredentials);
				token = context.acceptSecContext(gss, 0, gss.length);
			} finally {
				SpnegoAuthenticator.LOCK.unlock();
			}

			if (null == token) {
				// LOGGER.finer("Token was NULL.");
				return null;
			}

			resp.setHeader(Constants.AUTHN_HEADER, Constants.NEGOTIATE_HEADER
					+ ' ' + Base64.encode(token));

			if (!context.isEstablished()) {
				// LOGGER.fine("context not established");
				resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED, true);
				return null;
			}

			principal = context.getSrcName().toString();

			if (this.allowDelegation && context.getCredDelegState()) {
				delegCred = context.getDelegCred();
			}

		} finally {
			if (null != context) {
				SpnegoAuthenticator.LOCK.lock();
				try {
					context.dispose();
				} finally {
					SpnegoAuthenticator.LOCK.unlock();
				}
			}
		}

		return new SpnegoPrincipal(principal,
				KerberosPrincipal.KRB_NT_PRINCIPAL, delegCred);
	}

	/**
	 * Returns true if HTTP request is from the same host (localhost).
	 * 
	 * @param req
	 *            servlet request
	 * @return true if HTTP request is from the same host (localhost)
	 */
	private boolean isLocalhost(final HttpServletRequest req) {

		return req.getLocalAddr().equals(req.getRemoteAddr());
	}
}

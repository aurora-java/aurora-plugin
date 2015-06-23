package aurora.plugin.spnego;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.ietf.jgss.GSSCredential;

import aurora.plugin.spnego.SpnegoConfig.Constants;

public class SpnegoHttpServletRequest extends HttpServletRequestWrapper
		implements DelegateServletRequest {
	
	private final transient SpnegoPrincipal principal;

	public SpnegoHttpServletRequest(final HttpServletRequest request,
			final SpnegoPrincipal spnegoPrincipal) {
		super(request);
		this.principal = spnegoPrincipal;
	}

	public String getAuthType() {

		final String authType;
		final String header = this.getHeader(Constants.AUTHZ_HEADER);

		if (header.startsWith(Constants.NEGOTIATE_HEADER)) {
			authType = Constants.NEGOTIATE_HEADER;

		} else if (header.startsWith(Constants.BASIC_HEADER)) {
			authType = Constants.BASIC_HEADER;

		} else {
			authType = super.getAuthType();
		}

		return authType;
	}

	public GSSCredential getDelegatedCredential() {
		return this.principal.getDelegatedCredential();
	}

	public String getRemoteUser() {

		if (null == this.principal) {
			return super.getRemoteUser();

		} else {
			final String[] username = this.principal.getName().split("@", 2);
			return username[0];
		}
	}

	public Principal getUserPrincipal() {
		return this.principal;
	}

}

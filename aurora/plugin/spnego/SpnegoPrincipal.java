package aurora.plugin.spnego;

import java.security.Principal;

import javax.security.auth.kerberos.KerberosPrincipal;

import org.ietf.jgss.GSSCredential;

public class SpnegoPrincipal implements Principal {
	private final transient KerberosPrincipal kerberosPrincipal;

	private final transient GSSCredential delegatedCred;

	public SpnegoPrincipal(final String name) {
		this.kerberosPrincipal = new KerberosPrincipal(name);
		this.delegatedCred = null;
	}

	public SpnegoPrincipal(final String name, final int nameType) {
		this.kerberosPrincipal = new KerberosPrincipal(name, nameType);
		this.delegatedCred = null;
	}

	public SpnegoPrincipal(final String name, final int nameType,
			final GSSCredential delegCred) {

		this.kerberosPrincipal = new KerberosPrincipal(name, nameType);
		this.delegatedCred = delegCred;
	}

	public GSSCredential getDelegatedCredential() {
		return this.delegatedCred;
	}

	public String getName() {
		return this.kerberosPrincipal.getName();
	}

	public int getNameType() {
		return this.kerberosPrincipal.getNameType();
	}

	public String getRealm() {
		return this.kerberosPrincipal.getRealm();
	}

	public int hashCode() {
		return this.kerberosPrincipal.hashCode();
	}

	public String toString() {
		return this.kerberosPrincipal.toString();
	}
}

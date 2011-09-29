package aurora.plugin.spnego;

import aurora.plugin.spnego.SpnegoConfig.Constants;

public class SpnegoAuthScheme {
	 /** Zero length byte array. */
    private static final transient byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /** HTTP (Request) "Authorization" Header scheme. */ 
    private final transient String scheme;

    /** HTTP (Request) scheme token. */
    private final transient String token;
    
    /** true if Basic Auth scheme. */
    private final transient boolean basicScheme;
    
    /** true if Negotiate scheme. */
    private final transient boolean negotiateScheme;
    
    /** true if NTLM token. */
    private final transient boolean ntlm;

    /**
     * 
     * @param authScheme 
     * @param authToken 
     */
    public SpnegoAuthScheme(final String authScheme, final String authToken) {
        this.scheme = authScheme;
        this.token = authToken;
        
        if (null == authToken || authToken.isEmpty()) {
            this.ntlm = false;
        } else {
            this.ntlm = authToken.startsWith(Constants.NTLM_PROLOG);
        }
        
        this.negotiateScheme = Constants.NEGOTIATE_HEADER.equalsIgnoreCase(authScheme);
        this.basicScheme = Constants.BASIC_HEADER.equalsIgnoreCase(authScheme);
    }
    
    /**
     * Returns true if this SpnegoAuthScheme is of type "Basic".
     * 
     * @return true if Basic Auth scheme
     */
    boolean isBasicScheme() {
        return this.basicScheme;
    }
    
    /**
     * Returns true if this SpnegoAuthScheme is of type "Negotiate".
     * 
     * @return true if Negotiate scheme
     */
    boolean isNegotiateScheme() {
        return this.negotiateScheme;
    }
    /**
     * Returns true if NTLM.
     * 
     * @return true if Servlet Filter received NTLM token
     */
    boolean isNtlmToken() {
        return this.ntlm;
    }

    /**
     * Returns HTTP Authorization scheme.
     * 
     * @return "Negotiate" or "Basic"
     */
    public String getScheme() {
        return this.scheme;
    }

    /**
     * Returns a copy of byte[].
     * 
     * @return copy of token
     */
    public byte[] getToken() {
        return (null == this.token) ? EMPTY_BYTE_ARRAY : Base64.decode(this.token);
    }
}

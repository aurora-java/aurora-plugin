package aurora.plugin.spnego;

import javax.servlet.ServletRequest;

import org.ietf.jgss.GSSCredential;

public interface DelegateServletRequest extends ServletRequest {    
    GSSCredential getDelegatedCredential();
}

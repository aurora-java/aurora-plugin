package aurora.plugin.spnego;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class SpnegoHttpServletResponse extends HttpServletResponseWrapper{
	private transient boolean statusSet = false;

    public SpnegoHttpServletResponse(final HttpServletResponse response) {
        super(response);
    }
    
    public boolean isStatusSet() {
        return this.statusSet;
    }
  
    public void setStatus(final int status) {
        super.setStatus(status);
        this.statusSet = true;
    }
 
    public void setStatus(final int status, final boolean immediate) throws IOException {
        setStatus(status);
        if (immediate) {
            setContentLength(0);
            flushBuffer();
        }
    }
}

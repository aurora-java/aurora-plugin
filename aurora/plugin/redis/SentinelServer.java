package aurora.plugin.redis;

/**
 * Created by jessen on 15/8/24.
 */
public class SentinelServer {
	String host;
	int port;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}

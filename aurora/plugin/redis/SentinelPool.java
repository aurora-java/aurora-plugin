package aurora.plugin.redis;

import java.util.Collections;
import java.util.List;

/**
 * Created by jessen on 15/8/24.
 */
public class SentinelPool {
	String name;
	String host;
	String port;
	String master;
	long connectionTimeout;
	List<SentinelServer> servers = Collections.emptyList();


	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getMaster() {
		return master;
	}

	public void setMaster(String master) {
		this.master = master;
	}

	public long getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(long connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public List<SentinelServer> getServers() {
		return servers;
	}

	public void setServers(List<SentinelServer> servers) {
		this.servers = servers;
	}
}

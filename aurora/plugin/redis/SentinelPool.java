package aurora.plugin.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import uncertain.composite.CompositeMap;
import uncertain.ocm.IConfigurable;
import uncertain.ocm.IObjectRegistry;
import uncertain.ocm.OCManager;

import java.util.Collections;
import java.util.List;

/**
 * Created by jessen on 15/8/24.
 */
public class SentinelPool implements IConfigurable {
	String name;
	String master;
	int connectionTimeout;
	List<SentinelServer> servers = Collections.emptyList();
	GenericObjectPoolConfig config = new GenericObjectPoolConfig();
	OCManager ocManager;

	public SentinelPool(IObjectRegistry registry) {
		super();
		ocManager = (OCManager) registry.getInstanceOfType(OCManager.class);
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMaster() {
		return master;
	}

	public void setMaster(String master) {
		this.master = master;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public List<SentinelServer> getServers() {
		return servers;
	}

	public void setServers(List<SentinelServer> servers) {
		this.servers = servers;
	}

	public GenericObjectPoolConfig getConfig() {
		return config;
	}

	public void setConfig(GenericObjectPoolConfig config) {
		this.config = config;
	}

	@Override
	public void beginConfigure(CompositeMap thisMap) {
		CompositeMap map = thisMap.getChild("config");
		if (map == null || map.getText() == null) {
			return;
		}
		String text = map.getText();
		String[] lines = text.split("\n");
		CompositeMap properties = new CompositeMap();
		for (String line : lines) {
			line = line.trim();
			if (line.length() == 0)
				continue;
			String[] ss = splitAndTrim(line, "=");
			properties.put(ss[0].toLowerCase(), ss[1]);
		}

		ocManager.populateObject(properties, config);
	}

	String[] splitAndTrim(String line, String reg) {
		String[] ss = line.split(reg);
		for (int i = 0; i < ss.length; i++)
			ss[i] = ss[i].trim();
		return ss;
	}

	@Override
	public void endConfigure() {

	}
}

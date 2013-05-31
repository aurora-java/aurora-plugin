package aurora.plugin.sap.sync.idoc;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import javax.sql.DataSource;

import uncertain.core.ILifeCycle;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.AbstractLocatableObject;
import uncertain.ocm.IObjectRegistry;

public class IDocServerManager extends AbstractLocatableObject implements ILifeCycle {

	public static final String PLUGIN = IDocServerManager.class.getCanonicalName();
	public static final String SERVER_NAME_SEPARATOR = ",";
	public static final String AURORA_IDOC_PLUGIN_VERSION = "2.0";

	private IObjectRegistry registry;

	private String serverNameList;
	private String idocFileDir;
	private boolean keepIdocFile = true;
	private boolean interfaceEnabledFlag = true;
	private boolean enabledJCo = true;
	private int reconnectTime = 60000;// 1 minute
	private int maxReconnectTime = 3600000;// 1 hour

	private List<IDocServer> runningServerList = new LinkedList<IDocServer>();
	private ILogger logger;
	private DataSource datasource;
	private boolean running = true;
	

	public IDocServerManager(IObjectRegistry registry) {
		this.registry = registry;
	}

	private void initParameters() {
		datasource = (DataSource) registry.getInstanceOfType(DataSource.class);
		if (datasource == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(this, DataSource.class, this.getClass().getCanonicalName());
		if (serverNameList == null)
			throw BuiltinExceptionFactory.createAttributeMissing(this, "serverNameList");
		if (idocFileDir == null)
			throw BuiltinExceptionFactory.createAttributeMissing(this, "idocFileDir");
		File file = new File(idocFileDir);
		if (!file.exists()) {
			throw BuiltinExceptionFactory.createRequiredFileNotFound(idocFileDir);
		}
	}

	@Override
	public boolean startup() {
		logger = LoggingContext.getLogger(PLUGIN, registry);
		logger.info("Aurora IDoc Plugin Version: " + AURORA_IDOC_PLUGIN_VERSION);
		initParameters();

		String[] servers = serverNameList.split(SERVER_NAME_SEPARATOR);
		for (int i = 0; i < servers.length; i++) {
			String serverName = servers[i];
			try {
				IDocServer server = new IDocServer(registry,this, serverName);
				ServerConnectionMonitor serverMonitor = new ServerConnectionMonitor(this,server);
				serverMonitor.setDaemon(true);
				serverMonitor.start();
				runningServerList.add(server);
			} catch (Throwable e) {
				logger.log(Level.SEVERE, "start server " + serverName + " failed!", e);
			}
		}
		return true;
	}

	@Override
	public void shutdown() {
		setRunning(false);
		
		if (runningServerList != null && !runningServerList.isEmpty()) {
			for (IDocServer server : runningServerList) {
				try {
					server.shutdown();
				} catch (Throwable e) {
					logger.log(Level.SEVERE, "shutdown server " + server.getServerName() + " failed!", e);
				}
			}
			runningServerList = null;
		}
	}
	

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public String getServerNameList() {
		return serverNameList;
	}

	public void setServerNameList(String serverNameList) {
		this.serverNameList = serverNameList;
	}

	public String getIdocFileDir() {
		return idocFileDir;
	}

	public void setIdocFileDir(String idocFileDir) {
		this.idocFileDir = idocFileDir;
	}

	public boolean isKeepIdocFile() {
		return keepIdocFile;
	}

	public boolean getKeepIdocFile() {
		return keepIdocFile;
	}

	public void setKeepIdocFile(boolean keepIdocFile) {
		this.keepIdocFile = keepIdocFile;
	}

	public boolean isInterfaceEnabledFlag() {
		return interfaceEnabledFlag;
	}

	public boolean getInterfaceEnabledFlag() {
		return interfaceEnabledFlag;
	}

	public void setInterfaceEnabledFlag(boolean interfaceEnabledFlag) {
		this.interfaceEnabledFlag = interfaceEnabledFlag;
	}

	public int getReconnectTime() {
		return reconnectTime;
	}

	public void setReconnectTime(int reconnectTime) {
		this.reconnectTime = reconnectTime;
	}

	public int getMaxReconnectTime() {
		return maxReconnectTime;
	}

	public void setMaxReconnectTime(int maxReconnectTime) {
		this.maxReconnectTime = maxReconnectTime;
	}

	public ILogger getLogger() {
		return logger;
	}

	public void setLogger(ILogger logger) {
		this.logger = logger;
	}

	public DataSource getDatasource() {
		return datasource;
	}

	public void setDatasource(DataSource datasource) {
		this.datasource = datasource;
	}
	
	public boolean isEnabledJCo() {
		return enabledJCo;
	}

	public boolean getEnabledJCo() {
		return enabledJCo;
	}

	public void setEnabledJCo(boolean enabledJCo) {
		this.enabledJCo = enabledJCo;
	}
	
}

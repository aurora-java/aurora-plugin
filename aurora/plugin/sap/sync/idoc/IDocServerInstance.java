package aurora.plugin.sap.sync.idoc;

import java.io.File;

import uncertain.core.IGlobalInstance;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;

public class IDocServerInstance implements IGlobalInstance {
	public static final String PLUGIN = "aurora.plugin.sap.sync.idoc";
	public String SERVER_NAME_LIST;
	public String IDOC_DIR;
	private IObjectRegistry registry;
	public ILogger logger;
	public static final String SEPARATOR = ",";
	public String DeleteImmediately = "Y";

	public IDocServerInstance(IObjectRegistry registry) {
		this.registry = registry;
	}
	public ILogger getLogger() {
		return logger;
	}
	public IObjectRegistry getRegistry() {
		return registry;
	}

	public void log(String message) {
		if (logger != null) {
			logger.info(message);
		} else {
			System.out.println(message);
		}
	}
	// Framework function
	public void onInitialize() throws Exception {
		logger = LoggingContext.getLogger(PLUGIN, registry);
		run();
	}
	public String getIdocDir() {
		return IDOC_DIR;
	}
	public String getServerNameList() {
		return SERVER_NAME_LIST;
	}
	public void run() {
		if (IDOC_DIR == null || "".equals(IDOC_DIR)) {
			throw new IllegalArgumentException("IDOC_DIR can not be null !");
		} else {
			File file = new File(IDOC_DIR);
			if (!file.exists()) {
				throw new IllegalArgumentException("IDOC_DIR:" + IDOC_DIR + " is not exists!");
			}
		}

		log("SERVER_NAME_LIST:" + SERVER_NAME_LIST);
		if (SERVER_NAME_LIST == null || SERVER_NAME_LIST.equals("")) {
			throw new IllegalArgumentException("SERVER_NAME_LIST can not be null !");
		}
		String[] servers = SERVER_NAME_LIST.split(SEPARATOR);
		String serverName = null;
		IDocServer server = null;
		for (int i = 0; i < servers.length; i++) {
			serverName = servers[i];
			server = new IDocServer(this, serverName);
			server.start();
		}
	}
	public void setLogger(ILogger logger) {
		this.logger = logger;
	}
	public void setRegistry(IObjectRegistry registry) {
		this.registry = registry;
	}
	public boolean isDeleteImmediately() {
		return "Y".equals(DeleteImmediately);
	}
}

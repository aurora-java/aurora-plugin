package aurora.plugin.sap.sync.idoc;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import javax.sql.DataSource;

import uncertain.core.IGlobalInstance;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;

public class IDocServerInstance implements IGlobalInstance {
	public static final String PLUGIN = "aurora.plugin.sap.sync.idoc";
	public static final String SEPARATOR = ",";
	public String DeleteImmediately = "Y";
	public String INTERFACE_HISTORY_FLAG = "Y";
	public String SERVER_NAME_LIST;
	public String IDOC_DIR;
	public String RECONNECT_TIME = "60000";// 1 minute
	public String MAX_RECONNECT_TIME = "3600000";// 1 hour
	private List serverList;
	private IObjectRegistry registry;
	private String version = "1.4";

	public IDocServerInstance(IObjectRegistry registry) {
		this.registry = registry;
		serverList = new LinkedList();
	}

	// Framework function
	public void onInitialize() throws Exception {
		initLoggerUtil();
		run();
		Runnable shutdownHook = new Runnable() {
			public void run() {
				try {
					onShutdown();
					System.out.println("shutdown idoc finished!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));
	}

	private void initLoggerUtil() {
		ILogger logger = LoggingContext.getLogger(PLUGIN, registry);
		if (logger == null)
			throw new RuntimeException("Can not get logger from registry!");
		LoggerUtil.setLogger(logger);
	}

	public void onShutdown() throws Exception {
		if (serverList != null && !serverList.isEmpty()) {
			for (Iterator it = serverList.iterator(); it.hasNext();) {
				IDocServer server = (IDocServer) it.next();
				server.setShutdownByCommand(true);
				server.shutdown();
			}
			serverList = null;
		}
	}

	public void run() throws AuroraIDocException {
		LoggerUtil.getLogger().info("Aurora IDoc Plugin version: " + version);
		LoggerUtil.getLogger().info("IDoc Dir:" + IDOC_DIR);
		if (IDOC_DIR == null || "".equals(IDOC_DIR)) {
			throw new IllegalArgumentException("IDOC_DIR can not be null !");
		} else {
			File file = new File(IDOC_DIR);
			if (!file.exists()) {
				throw new IllegalArgumentException("IDOC_DIR:" + IDOC_DIR + " is not exists!");
			}
		}
		LoggerUtil.getLogger().info("Server name list:" + SERVER_NAME_LIST);
		if (SERVER_NAME_LIST == null || SERVER_NAME_LIST.equals("")) {
			throw new IllegalArgumentException("SERVER_NAME_LIST can not be null !");
		}
		int reconnectTime = Integer.parseInt(RECONNECT_TIME);
		int maxReconnectTime = Integer.parseInt(MAX_RECONNECT_TIME);
		String[] servers = SERVER_NAME_LIST.split(SEPARATOR);

		DataSource ds = (DataSource) registry.getInstanceOfType(DataSource.class);
		if (ds == null)
			throw new AuroraIDocException("Can not get DataSource from registry " + registry);
		for (int i = 0; i < servers.length; i++) {
			String serverName = servers[i];
			try {
				IDocServer server = new IDocServer(IDOC_DIR, ds, serverName, isDeleteFileImmediately(),
						isEnableInterfaceHistory(), reconnectTime, maxReconnectTime);
				server.start();
				serverList.add(server);
			} catch (Throwable e) {
				LoggerUtil.getLogger().log(Level.SEVERE, "start server " + serverName + " failed!", e);
			}
		}
	}

	public boolean isDeleteFileImmediately() {
		return "Y".equals(DeleteImmediately);
	}

	public boolean isEnableInterfaceHistory() {
		return "Y".equals(INTERFACE_HISTORY_FLAG);
	}
}

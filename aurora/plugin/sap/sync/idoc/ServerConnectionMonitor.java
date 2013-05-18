package aurora.plugin.sap.sync.idoc;


public class ServerConnectionMonitor extends Thread {

	private IDocServerManager idocServerManager;
	private IDocServer idocServer;
	private int minReconnectTime;
	private int maxReconnectTime;

	private int reconnectTime;

	public ServerConnectionMonitor(IDocServerManager idocServerManager, IDocServer idocServer) {
		this.idocServerManager = idocServerManager;
		this.idocServer = idocServer;
		this.minReconnectTime = idocServerManager.getReconnectTime();
		this.maxReconnectTime = idocServerManager.getMaxReconnectTime();
		this.reconnectTime = minReconnectTime;
	}

	public void run() {
		while (idocServerManager.isRunning()) {
			if (idocServer.isRunning()) {
				reconnectTime = minReconnectTime;
				sleepOneSecond();
			} else {
				startServer();
			}
		}
	}

	private void sleepOneSecond() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
	}

	private void startServer() {
		int thisReconnectTime = computeConnectTime();
		try {
			Thread.sleep(thisReconnectTime);
		} catch (InterruptedException e) {
		}
		idocServer.start();
	}

	private int computeConnectTime() {
		if (reconnectTime < maxReconnectTime) {
			if (reconnectTime * 2 <= maxReconnectTime) {
				reconnectTime = reconnectTime * 2;
			} else {
				reconnectTime = maxReconnectTime;
			}
		}
		return reconnectTime;

	}

}

package aurora.plugin.sap.sync.idoc;


public class ListenerMonitor extends Thread {

	private IDocServerManager idocServerManager;
	private IDocFileListener listener;
	private int minReconnectTime;
	private int maxReconnectTime;

	private int reconnectTime;

	public ListenerMonitor(IDocServerManager idocServerManager, IDocFileListener listener) {
		this.idocServerManager = idocServerManager;
		this.listener = listener;
		this.minReconnectTime = idocServerManager.getReconnectTime();
		this.maxReconnectTime = idocServerManager.getMaxReconnectTime();
		this.reconnectTime = minReconnectTime;
	}

	public void run() {
		while (idocServerManager.isRunning()) {
			if (listener.isRunning()) {
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
		listener.start();
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

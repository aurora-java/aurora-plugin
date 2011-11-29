package aurora.plugin.sap.sync.idoc;

public class ServerConnection extends Thread{
	
	private IDocServer iDocServer;
	private int reconnectTime;
	private int maxReconnectTime;
	private int currentConnectTime;
	public ServerConnection(IDocServer iDocServer,int reconnectTime,int maxReconnectTime) {
		this.iDocServer = iDocServer;
		this.reconnectTime = reconnectTime;
		this.maxReconnectTime = maxReconnectTime;
		this.currentConnectTime = reconnectTime;
	}
	public void run() {
		while(!iDocServer.isShutdownByCommand()){
			if(iDocServer.getJCoIDocServer() == null){
				startServer(false);
				continue;
			}
			if(iDocServer.isShutdown()){
				startServer(true);
			}
			else{
				currentConnectTime = reconnectTime;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					iDocServer.log(e);
				}
			}
		}
	}
	private int computeConnectTime(){
		if(currentConnectTime < maxReconnectTime){
			if(currentConnectTime*2<=maxReconnectTime){
				currentConnectTime = currentConnectTime*2;
			}else{
				currentConnectTime = maxReconnectTime;
			}
		}
		return currentConnectTime;
			
	}
	private void startServer(boolean isRestart){
		try {
			Thread.sleep(computeConnectTime());
		} catch (InterruptedException e) {
			iDocServer.log(e);
		}
		LoggerUtil.getLogger().log("begin ReConnection IDocServer " + iDocServer.getServerName() + "...");
		if(!isRestart)
			iDocServer.start();
		else{
			iDocServer.reStart();
		}
	}
}

package aurora.plugin.sap.sync.idoc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.logging.Level;

import javax.sql.DataSource;

import com.sap.conn.idoc.IDocDocumentList;
import com.sap.conn.idoc.IDocXMLProcessor;
import com.sap.conn.idoc.jco.JCoIDoc;
import com.sap.conn.idoc.jco.JCoIDocHandler;
import com.sap.conn.idoc.jco.JCoIDocHandlerFactory;
import com.sap.conn.idoc.jco.JCoIDocServer;
import com.sap.conn.idoc.jco.JCoIDocServerContext;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerContextInfo;
import com.sap.conn.jco.server.JCoServerErrorListener;
import com.sap.conn.jco.server.JCoServerExceptionListener;
import com.sap.conn.jco.server.JCoServerState;
import com.sap.conn.jco.server.JCoServerTIDHandler;

public class IDocServer {
	public JCoIDocServer iDocServer;
	private String serverName;
	private DataBaseUtil dbUtil;
	public LinkedList syncFiles = new LinkedList();
	public LinkedList backupFils = new LinkedList();

	private String idocDir;
	private DataSource dataSource;
	private int server_id = -1;
	private boolean isDeleteFileImmediately;
	private boolean isEnableInterfaceHistory;
	private ServerConnection reConnect;
	private boolean shutdownByCommand = false;
	public IDocServer(String idocDir, DataSource ds, String serverName, boolean isDeleteFileImmediately,boolean isEnableInterfaceHistory,
			int reconnectTime, int maxReconnectTime) {
		this.idocDir = idocDir;
		this.dataSource = ds;
		this.serverName = serverName;
		this.isDeleteFileImmediately = isDeleteFileImmediately;
		this.isEnableInterfaceHistory = isEnableInterfaceHistory;
		reConnect = new ServerConnection(this,reconnectTime, maxReconnectTime);
	}
	public void start(){
		start(false);
		if(!reConnect.isAlive())
			reConnect.start();
	}
	public void reStart(){
		start(true);
	}
	private void start(boolean isRestart) {
		if(shutdownByCommand)
			return;
		String context = "";
		try {
			dbUtil = getConnection();
			// see provided examples of configuration files MYSERVER.jcoServer
			// and BCE.jcoDestination
			LoggerUtil.getLogger().config("begin start IDocServer " + serverName + "...");
			context = "Get Server by Config File:" + serverName;
			if(!isRestart){
				iDocServer = JCoIDoc.getServer(serverName);
				context = "";
				addListeners(iDocServer);
				LoggerUtil.getLogger().config(serverName + " ConnectionCount= " + iDocServer.getConnectionCount());
				if (iDocServer.getConnectionCount() == 0) {
					iDocServer.setConnectionCount(1);
				}
				server_id = dbUtil.registerSapServers(iDocServer);
			}	
			context = "Get HistoryIdocs " + iDocServer.getProgramID();
			handleHistoryFile();
			context = "";
			iDocServer.start();
			if (!isRunning()) {
				LoggerUtil.getLogger().log(serverName + "'s status is " + iDocServer.getState());
				shutdown();
				return;
			}
			IDocSync sync = new IDocSync(this);
			sync.start();
			LoggerUtil.getLogger().config("begin handle " + serverName + " HistoryIdocs...");
			IDocBackup backup = new IDocBackup(this);
			backup.start();
			LoggerUtil.getLogger().config("IDocServer " + serverName + " start successful!");
			System.out.println("IDocServer " + serverName + " start successful!");
		} catch (Throwable e) {
			shutdown(context, e);
			return;
		}

	}
	private void handleHistoryFile(){
		try {
			dbUtil.getHistoryIdocs(iDocServer.getProgramID(), syncFiles);
		} catch (AuroraIDocException e) {
			log(e);
		}
	}
	private void addListeners(JCoIDocServer iDocServer) {
		if (iDocServer == null)
			return;
		MyIDocHandlerFactory idocHanlerFactory = new MyIDocHandlerFactory();
		iDocServer.setIDocHandlerFactory(idocHanlerFactory);
		iDocServer.setTIDHandler(new MyTidHandler());

		MyThrowableListener listener = new MyThrowableListener();
		iDocServer.addServerErrorListener(listener);
		iDocServer.addServerExceptionListener(listener);
	}

	private DataBaseUtil getConnection() throws SQLException, AuroraIDocException {
		Connection dbConn = dataSource.getConnection();
		if (dbConn == null)
			throw new AuroraIDocException("Can not get Connection from DataSource");
		DataBaseUtil dbUtil = new DataBaseUtil(dbConn);
		return dbUtil;
	}
	public DataBaseUtil getDbUtil() {
		return dbUtil;
	}

	public void setDbUtil(DataBaseUtil dbUtil) {
		this.dbUtil = dbUtil;
	}

	class MyIDocHandler implements JCoIDocHandler {
		public void handleRequest(JCoServerContext serverCtx, IDocDocumentList idocList) {
			FileOutputStream fos = null;
			OutputStreamWriter osw = null;
			try {
				IDocXMLProcessor xmlProcessor = JCoIDoc.getIDocFactory().getIDocXMLProcessor();
				String fileName = serverCtx.getTID() + "_idoc.xml";
				String filePath = idocDir + File.separator + fileName;
				fos = new FileOutputStream(filePath);
				osw = new OutputStreamWriter(fos, "UTF8");
				xmlProcessor.render(idocList, osw, IDocXMLProcessor.RENDER_WITH_TABS_AND_CRLF);
				osw.flush();
				osw.close();
				// handleXMLFile(filePath);
				int idoc_id = dbUtil.addIdoc(server_id, filePath);
				LoggerUtil.getLogger().log("Receive Idoc file, id=" + idoc_id);
				addSyncFile(new IDocFile(filePath, idoc_id, server_id));
			} catch (Throwable thr) {
				shutdown("", thr);
			} finally {
				try {
					if (osw != null)
						osw.close();
					if (fos != null)
						fos.close();
				} catch (IOException e) {
					LoggerUtil.getLogger().log(Level.SEVERE, "", e);
				}
			}
		}
	}

	public void handleXMLFile(String file) throws IOException {
		BufferedReader input = null;
		PrintWriter output = null;
		File inFile = new File(file);
		File outFile = new File(file + ".temp");
		boolean containSpecialChar = false;
		try {
			input = new BufferedReader(new FileReader(inFile));
			output = new PrintWriter(new FileWriter(outFile));
			String line = null;
			while ((line = input.readLine()) != null) {
				int index = line.indexOf("&");
				if (index != -1) {
					containSpecialChar = true;
					int firstTagEnd = line.indexOf(">");
					int secondTagbegin = line.indexOf("<", index);
					if (firstTagEnd < index && secondTagbegin != -1) {
						String text = "<![CDATA[" + line.substring(firstTagEnd + 1, secondTagbegin) + "]]>";
						line = line.substring(0, firstTagEnd + 1) + text + line.substring(secondTagbegin);
					}
				}
				output.println(line);
			}
			input.close();
			output.flush();
			output.close();
			if (containSpecialChar) {
				if (inFile.delete()) {
					outFile.renameTo(new File(file));
				}
			} else {
				outFile.delete();
			}
		} finally {
			if (input != null) {
				input.close();
			}
			if (output != null) {
				output.close();
			}
		}
	}

	public synchronized void addSyncFile(IDocFile file) {
		syncFiles.addLast(file);
	}

	public synchronized IDocFile getSyncFile() {
		if (syncFiles.size() <= 0) {
			return null;
		}
		IDocFile file = (IDocFile) syncFiles.getFirst();
		syncFiles.remove(0);
		return file;
	}

	public synchronized void addBackupFile(IDocFile file) {
		if(isEnableInterfaceHistory)
			backupFils.addLast(file);
	}

	public synchronized IDocFile getBckupFile() {
		if (backupFils.size() <= 0) {
			return null;
		}
		IDocFile file = (IDocFile) backupFils.getFirst();
		backupFils.remove(0);
		return file;
	}

	class MyIDocHandlerFactory implements JCoIDocHandlerFactory {
		private JCoIDocHandler handler = new MyIDocHandler();

		public JCoIDocHandler getIDocHandler(JCoIDocServerContext serverCtx) {
			return handler;
		}

		public JCoIDocHandler getIDocHandler() {
			return handler;
		}
	}

	class MyThrowableListener implements JCoServerErrorListener, JCoServerExceptionListener {

		public void serverErrorOccurred(JCoServer server, String connectionId, JCoServerContextInfo ctx, Error error) {
			shutdown(">>> Error occured on " + server.getProgramID() + " connection " + connectionId, error);
		}

		public void serverExceptionOccurred(JCoServer server, String connectionId, JCoServerContextInfo ctx,
				Exception error) {
			shutdown(">>> Exception occured on " + server.getProgramID() + " connection " + connectionId, error);
		}
	}

	class MyTidHandler implements JCoServerTIDHandler {
		public boolean checkTID(JCoServerContext serverCtx, String tid) {
			return true;
		}

		public void confirmTID(JCoServerContext serverCtx, String tid) {
		}

		public void commit(JCoServerContext serverCtx, String tid) {
		}

		public void rollback(JCoServerContext serverCtx, String tid) {
		}
	}

	public boolean isRunning() {
		return JCoServerState.ALIVE.equals(iDocServer.getState())
				|| JCoServerState.STARTED.equals(iDocServer.getState());
	}

	public boolean isShutdown() {
		return JCoServerState.DEAD.equals(iDocServer.getState())
				|| JCoServerState.STOPPED.equals(iDocServer.getState());
	}

	public boolean isFinished() {
		return syncFiles.size() <= 0;
	}

	public int getServerId() {
		return server_id;
	}

	public void setServerId(int serverId) {
		this.server_id = serverId;
	}

	public String getServerName() {
		return serverName;
	}
	public JCoIDocServer getJCoIDocServer() {
		return iDocServer;
	}
	public boolean isDeleteFileImmediately() {
		return isDeleteFileImmediately;
	}

	public void log(Throwable e) {
		if (e instanceof AuroraIDocException && e.getCause() != null)
			LoggerUtil.getLogger().log(Level.SEVERE, e.getMessage(), e);
		else
			LoggerUtil.getLogger().log(Level.SEVERE, "", e);
	}

	public void shutdown(String errorMessage, Throwable e) {
		LoggerUtil.getLogger().log(Level.SEVERE, errorMessage, e);
		shutdown();
	}

	public void shutdown() {
		LoggerUtil.getLogger().log("close dbconnection ");
		shutdownDB();
		LoggerUtil.getLogger().log("stop iDocServer ：" + serverName);
		shutdownIDocServer();
		if(shutdownByCommand){
			reConnect.interrupt();
		}
		LoggerUtil.getLogger().log("...........shutdown " + serverName + " finished.............. ");
		
	}
	private void shutdownDB() {
		if (dbUtil != null) {
			try {
				dbUtil.getConnection().rollback();
				dbUtil.getConnection().setAutoCommit(true);
				if (server_id != -1)
					dbUtil.stopSapServers(server_id);
			} catch (Throwable e) {
				log(e);
			}
			dbUtil.dispose();
		}
	}

	private void shutdownIDocServer() {
		if (iDocServer == null)
			return;
		try {
			if (isRunning()) {
				iDocServer.stop();
			}
		} catch (Throwable e) {
			log(e);
		}
	}
	public void setShutdownByCommand(boolean shutdownByCommand){
		this.shutdownByCommand = shutdownByCommand;
	}
	public boolean isShutdownByCommand(){
		return shutdownByCommand;
	}
}
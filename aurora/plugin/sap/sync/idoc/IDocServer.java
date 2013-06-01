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
import java.util.List;
import java.util.logging.Level;

import javax.sql.DataSource;

import uncertain.logging.ILogger;
import uncertain.ocm.IObjectRegistry;

import aurora.plugin.sap.ISapConfig;

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

	private IObjectRegistry registry;
	private IDocServerManager serverManager;
	private String serverName;

	private ILogger logger;
	private DataSource datasource;
	private boolean keepIdocFile = true;
	private boolean interfaceEnabledFlag = true;

	public JCoIDocServer jcoIDocServer;
	public LinkedList<IDocFile> syncFileList = new LinkedList<IDocFile>();
	public LinkedList<IDocFile> backupFileList = new LinkedList<IDocFile>();
	private int idocServerId = -1;

	public IDocServer(IObjectRegistry registry,IDocServerManager serverManager, String serverName) {
		this.registry = registry;
		this.serverManager = serverManager;
		this.serverName = serverName;
		logger = serverManager.getLogger();
		keepIdocFile = serverManager.getKeepIdocFile();
		interfaceEnabledFlag = serverManager.getInterfaceEnabledFlag();
		datasource = serverManager.getDatasource();
	}

	public void start() {
		if (!serverManager.isRunning())
			return;
		String processMessage = "";
		DatabaseManager databaseManager = null;
		try {
			databaseManager = getDatabaseManager();
			if (jcoIDocServer == null) {
				processMessage = "start IDocServer " + serverName;
				jcoIDocServer = JCoIDoc.getServer(serverName);
				addListeners();
				logger.config(serverName + " ConnectionCount= " + jcoIDocServer.getConnectionCount());
				if (jcoIDocServer.getConnectionCount() == 0) {
					jcoIDocServer.setConnectionCount(1);
				}
				SyncFileData sync = new SyncFileData(registry,serverManager, this, logger);
				sync.start();
				BackupFileDataToInterface backup = new BackupFileDataToInterface(serverManager, this, logger);
				backup.start();
			}
			if (idocServerId < 0) {
				processMessage = "fetch idocServerId for IDocServer " + serverName;
				idocServerId = databaseManager.addIDocServer(jcoIDocServer, serverName);
				logger.log("IDocServer " + serverName + " 's idoc_server_id is " + idocServerId);
				String programID = jcoIDocServer.getProgramID();
				processMessage = "fetch fetchUnsettledIdocFiles for programID " + programID;
				fetchUnsettledIdocFiles(databaseManager,programID);
			}
			jcoIDocServer.start();
			if (!isRunning()) {
				System.err.println("Connect IDocServer " + serverName + " failed!");
				logger.log(serverName + "'s status is " + jcoIDocServer.getState());
				databaseManager.updateIDocServerStatus(idocServerId, "Error occurred:please check the console or log for details.");
			} else {
				serverManager.addDestination(jcoIDocServer.getRepositoryDestination());
				System.out.println("Connect IDocServer " + serverName + " successful!");
				logger.log("Connect IDocServer " + serverName + " successful!");
				databaseManager.updateIDocServerStatus(idocServerId, "OK");
			}
		} catch (Throwable e) {
			System.err.println("Connect IDocServer " + serverName + " failed!");
			logger.log(Level.SEVERE, processMessage, e);
		} finally {
			if (databaseManager != null)
				databaseManager.close();
		}
	}
	public void fetchUnsettledIdocFiles(DatabaseManager databaseManager,String programID) throws AuroraIDocException{
		List<IDocFile> unsettledIdocFiles = databaseManager.fetchUnsettledIdocFiles(programID);
		if (unsettledIdocFiles != null)
			syncFileList.addAll(unsettledIdocFiles);
	}

	private void addListeners() {
		IDocHandlerFactory idocHanlerFactory = new IDocHandlerFactory();
		jcoIDocServer.setIDocHandlerFactory(idocHanlerFactory);
		jcoIDocServer.setTIDHandler(new TidHandler());

		ThrowableListener listener = new ThrowableListener();
		jcoIDocServer.addServerErrorListener(listener);
		jcoIDocServer.addServerExceptionListener(listener);
	}

	public DatabaseManager getDatabaseManager() throws SQLException {
		Connection connection = datasource.getConnection();
		DatabaseManager dm = new DatabaseManager(connection, logger);
		return dm;
	}

	class IDocHandler implements JCoIDocHandler {
		private String idocFileDir = serverManager.getIdocFileDir();

		public void handleRequest(JCoServerContext serverCtx, IDocDocumentList idocList) {
			FileOutputStream fos = null;
			OutputStreamWriter osw = null;
			try {
				IDocXMLProcessor xmlProcessor = JCoIDoc.getIDocFactory().getIDocXMLProcessor();
				String fileName = serverCtx.getTID() + "_idoc.xml";
				File file = new File(idocFileDir, fileName);
				String fileFullPath = file.getCanonicalPath();
				fos = new FileOutputStream(file);
				osw = new OutputStreamWriter(fos, "UTF8");
				xmlProcessor.render(idocList, osw, IDocXMLProcessor.RENDER_WITH_TABS_AND_CRLF);
				osw.flush();
				osw.close();
				DatabaseManager databaseManager = getDatabaseManager();
				int idocFileId = databaseManager.addIDocFile(idocServerId, fileFullPath);
				logger.config("Receive idoc file. fileName=" + fileName + " and id=" + idocFileId);
				IDocFile idocFile = new IDocFile(fileFullPath, idocFileId, idocServerId);
				addSyncFile(idocFile);
			} catch (Throwable e) {
				logger.log(Level.SEVERE, "", e);
			} finally {
				closeOutputStreamWriter(osw);
				closeFileOutputStream(fos);
			}
		}

		private void closeOutputStreamWriter(OutputStreamWriter osw) {
			if (osw != null)
				try {
					osw.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE, "", e);
				}
		}

		private void closeFileOutputStream(FileOutputStream fos) {
			if (fos != null)
				try {
					fos.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE, "", e);
				}
		}
	}

	public void addCDATATag(String idocFile) throws IOException {
		BufferedReader input = null;
		PrintWriter output = null;
		File inFile = new File(idocFile);
		File outFile = new File(idocFile + ".temp");
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
					outFile.renameTo(new File(idocFile));
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

	public synchronized void addSyncFile(IDocFile idocFile) {
		syncFileList.addLast(idocFile);
	}

	public synchronized IDocFile pollSyncFile() {
		IDocFile idocFile = syncFileList.poll();
		return idocFile;
	}

	public synchronized void addBackupFile(IDocFile idocFile) {
		if (interfaceEnabledFlag)
			backupFileList.addLast(idocFile);
	}

	public synchronized IDocFile pollBckupFile() {
		IDocFile idocFile = backupFileList.poll();
		return idocFile;
	}

	public boolean isRunning() {
		if (jcoIDocServer == null)
			return false;
		JCoServerState jCoServerState = jcoIDocServer.getState();
		if (JCoServerState.ALIVE.equals(jCoServerState) || JCoServerState.STARTED.equals(jCoServerState)) {
			return true;
		}
		return false;
	}

	public int getIDocServerId() {
		return idocServerId;
	}

	public void setIDocServerId(int idocServerId) {
		this.idocServerId = idocServerId;
	}

	public String getServerName() {
		return serverName;
	}

	public JCoIDocServer getJCoIDocServer() {
		return jcoIDocServer;
	}

	public boolean isKeepIdocFile() {
		return keepIdocFile;
	}

	public void shutdown() {
		DatabaseManager databaseManager = null;
		try {
			databaseManager = getDatabaseManager();
			if (idocServerId != -1)
				databaseManager.updateIDocServerStatus(idocServerId, "disconnect");
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "", e);
		} finally {
			if (databaseManager != null)
				databaseManager.close();
		}
		logger.log("disconnect iDocServer ：" + serverName);
		stopIDocServer();
	}

	private void stopIDocServer() {
		try {
			if (isRunning()) {
				jcoIDocServer.stop();
			}
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "", e);
		}
	}

	class IDocHandlerFactory implements JCoIDocHandlerFactory {
		private JCoIDocHandler handler = new IDocHandler();

		public JCoIDocHandler getIDocHandler(JCoIDocServerContext serverCtx) {
			return handler;
		}

		public JCoIDocHandler getIDocHandler() {
			return handler;
		}
	}

	class ThrowableListener implements JCoServerErrorListener, JCoServerExceptionListener {

		public void serverErrorOccurred(JCoServer server, String connectionId, JCoServerContextInfo ctx, Error error) {
			logger.log(Level.SEVERE, ">>> Error occured on " + server.getProgramID() + " connection " + connectionId, error);
		}

		public void serverExceptionOccurred(JCoServer server, String connectionId, JCoServerContextInfo ctx, Exception error) {
			logger.log(Level.SEVERE, ">>> Exception occured on " + server.getProgramID() + " connection " + connectionId, error);
		}
	}

	class TidHandler implements JCoServerTIDHandler {
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
}
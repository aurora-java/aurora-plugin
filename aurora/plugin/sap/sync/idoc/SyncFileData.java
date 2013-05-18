package aurora.plugin.sap.sync.idoc;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;
import uncertain.logging.ILogger;

public class SyncFileData extends Thread {
	public static final String DONE_STATUS = "DONE";

	private IDocServerManager serverManager;
	private IDocServer iDocServer;
	public IDocType idocType;
	private DatabaseManager databaseManager;
	private List<IDocType> errorIdocTypes = new LinkedList<IDocType>();
	private ILogger logger;

	public SyncFileData(IDocServerManager serverManager, IDocServer iDocServer, ILogger logger) {
		this.serverManager = serverManager;
		this.iDocServer = iDocServer;
		this.logger = logger;
	}

	public void run() {
		while (serverManager.isRunning()) {
			IDocFile idocFile = iDocServer.pollSyncFile();
			if (idocFile == null) {
				sleepOneSecond();
			} else {
				try {
					idocType = null;
					databaseManager = iDocServer.getDatabaseManager();
					syncFileData(idocFile);
					iDocServer.addBackupFile(idocFile);
				} catch (Throwable e) {
					logger.log(Level.SEVERE, "", e);
					if (idocType != null) {
						errorIdocTypes.add(idocType);
					}
					updateIdocStatus(idocFile, "syncFileData failed!");
				} finally {
					if (databaseManager != null)
						databaseManager.close();
				}
			}
		}
	}

	private void sleepOneSecond() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
	}

	private void updateIdocStatus(IDocFile idocFile, String status) {
		try {
			int idocFileId = idocFile.getIdocFileId();
			logger.log("update idocFileId=" + idocFileId + " status as " + status);
			databaseManager.updateIdocFileStatus(idocFileId, status);
		} catch (AuroraIDocException e) {
			logger.log(Level.SEVERE, "", e);
		}
	}

	private void syncFileData(IDocFile idocFile) throws AuroraIDocException {
		int idocFileId = idocFile.getIdocFileId();
		try {
			CompositeLoader loader = new CompositeLoader();
			CompositeMap iDocData = loader.loadByFile(idocFile.getFileFullPath());
			List<CompositeMap> childList = iDocData.getChilds();
			if (childList == null)
				return;
			databaseManager.setConnectionAutoCommit(false);
			for (CompositeMap idoc_node : childList) {
				if (idoc_node == null || idoc_node.getChildIterator() == null || idoc_node.getChilds().size() < 2) {
					return;
				}
				CompositeMap control_node = (CompositeMap) idoc_node.getChilds().get(0);
				idocType = databaseManager.getIdocType(control_node);
				if (isIdocTypeStop(idocType))
					throw new AuroraIDocException("This idocType:" + idocType + " has error before");
				databaseManager.updateIdocFileInfo(idocFileId, control_node);
				for (int i = 1; i < idoc_node.getChilds().size(); i++) {
					CompositeMap content_node = (CompositeMap) idoc_node.getChilds().get(i);
					databaseManager.syncMapTables(idocFileId, content_node);
				}
			}
			syncTrxTables(idocFile);
			databaseManager.commitConnection();
			logger.log("Sync Idoc File Id=" + idocFile.getIdocFileId() + " Execute Successful !");
		} catch (Throwable e) {
			databaseManager.rollbackConnection();
			throw new AuroraIDocException(e);
		} finally {
			databaseManager.setConnectionAutoCommit(true);
		}
	}

	private void syncTrxTables(IDocFile file) throws SQLException, AuroraIDocException {
		int idocFileId = file.getIdocFileId();
		String executePkg = databaseManager.queryExecutePkg(idocFileId);
		String errorMessage = databaseManager.executePkg(executePkg, idocFileId);
		if (errorMessage != null && !"".equals(errorMessage)) {
			throw new AuroraIDocException("execute transaction Pkg " + executePkg + " failed:" + errorMessage);
		}
		databaseManager.updateIdocFileStatus(idocFileId, DONE_STATUS);
	}

	private boolean isIdocTypeStop(IDocType idocType) throws SQLException, AuroraIDocException {
		boolean isOrdinal = databaseManager.isOrdinal(idocType.getIdoctyp(), idocType.getCimtyp());
		if (isOrdinal && errorIdocTypes.contains(idocType)) {
			return true;
		}
		return false;
	}
}

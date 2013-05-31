package aurora.plugin.sap.sync.idoc;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;
import uncertain.logging.ILogger;

public class BackupFileDataToInterface extends Thread {

	private IDocServerManager serverManager;
	private IDocServer iDocServer;
	private DatabaseManager databaseManager;
	private ILogger logger;

	public BackupFileDataToInterface(IDocServerManager serverManager, IDocServer iDocServer, ILogger logger) {
		this.serverManager = serverManager;
		this.iDocServer = iDocServer;
		this.logger = logger;
	}

	public void run() {
		while (serverManager.isRunning()) {
			IDocFile idocFile = iDocServer.pollBckupFile();
			if (idocFile == null) {
				sleepOneSecond();
				continue;
			}
			try {
				databaseManager = iDocServer.getDatabaseManager();
				insertInterface(idocFile);
			} catch (Throwable e) {
				logger.log(Level.SEVERE, "", e);
				updateIdocStatus(idocFile, "backup to interface failed!");
			} finally {
				if (databaseManager != null)
					databaseManager.close();
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

	private void insertInterface(IDocFile idocFile) throws AuroraIDocException {
		int header_id = -1;
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
				header_id = databaseManager.addInterfaceHeader(idocFileId, control_node);
				for (int i = 1; i < idoc_node.getChilds().size(); i++) {
					CompositeMap content_node = (CompositeMap) idoc_node.getChilds().get(i);
					databaseManager.addInterfaceLine(header_id, content_node);
				}
			}
			databaseManager.updateInterfaceHeaderStatus(header_id, "DONE");
			databaseManager.commitConnection();
			if (!iDocServer.isKeepIdocFile()) {
				File deleteFile = new File(idocFile.getFileFullPath());
				if (deleteFile.exists()) {
					logger.log("delete file " + idocFile.getFileFullPath() + " " + deleteFile.delete());
				}
			}
			logger.config("Backup Idoc File Id=" + idocFile.getIdocFileId() + "  to Interface  Execute Successful !");
		} catch (Throwable e) {
			databaseManager.rollbackConnection();
			throw new AuroraIDocException(e);
		} finally {
			databaseManager.setConnectionAutoCommit(true);
		}
	}
}

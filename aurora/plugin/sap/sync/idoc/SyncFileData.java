package aurora.plugin.sap.sync.idoc;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import aurora.service.IServiceFactory;
import aurora.service.ServiceInvoker;
import aurora.service.ServiceThreadLocal;

import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.IProcedureManager;
import uncertain.proc.Procedure;

public class SyncFileData extends Thread {
	public static final String DONE_STATUS = "DONE";

	private IObjectRegistry registry;
	private IDocServerManager serverManager;
	private IDocServer iDocServer;
	public IDocType idocType;
	private DatabaseManager databaseManager;
	private Set<IDocType> errorIdocTypes = new HashSet<IDocType>();
	private ILogger logger;

	public SyncFileData(IObjectRegistry registry, IDocServerManager serverManager, IDocServer iDocServer, ILogger logger) {
		this.registry = registry;
		this.serverManager = serverManager;
		this.iDocServer = iDocServer;
		this.logger = logger;
	}

	public void run() {
		while (serverManager.isRunning()) {
			IDocFile idocFile = iDocServer.pollSyncFile();
			if (idocFile == null) {
				sleepOneSecond();
				continue;
			}
			idocType = null;
			try {
				databaseManager = iDocServer.getDatabaseManager();
				syncFileData(idocFile);
				iDocServer.addBackupFile(idocFile);
			} catch (Throwable e) {
				logger.log(Level.SEVERE, "", e);
				addErrorIdocType(idocType);
				updateIdocStatus(idocFile, "syncFileData failed!");
			}
			executeFeedbackProc(idocFile);
			if (databaseManager != null)
				databaseManager.close();
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

	private void syncTrxTables(IDocFile file) throws Exception {
		int idocFileId = file.getIdocFileId();
		String executePkg = databaseManager.queryExecutePkg(idocFileId);
		if (executePkg == null || "".equals(executePkg))
			throw new IllegalStateException("Please define execute_pkg for idoc file id =" + file.getIdocFileId());
		String errorMessage = databaseManager.executePkg(executePkg, idocFileId);
		if (errorMessage != null && !"".equals(errorMessage)) {
			throw new AuroraIDocException("execute transaction Pkg " + executePkg + " failed:" + errorMessage);
		}
		databaseManager.updateIdocFileStatus(idocFileId, DONE_STATUS);
	}

	private void executeFeedbackProc(IDocFile idocFile) {
		int idocFileId = idocFile.getIdocFileId();
		try {
			String feedback_proc = databaseManager.queryFeedbackProc(idocFileId);
			if (feedback_proc == null)
				return;
			executeProc(idocFile, feedback_proc, databaseManager.getConnection());
			recordFeedback(idocFileId, DONE_STATUS, "");
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "", e);
			recordFeedback(idocFileId, "EXCEPTION", getFullStackTrace(e));
		}
	}

	private void recordFeedback(int idoc_file_id, String status, String message) {
		try {
			databaseManager.recordFeedback(idoc_file_id, status, message);
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "", e);
		}
	}

	private String getFullStackTrace(Throwable exception) {
		String message = getExceptionStackTrace(exception);
		return message;
	}

	private String getExceptionStackTrace(Throwable exception) {
		if (exception == null)
			return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream pw = new PrintStream(baos);
		exception.printStackTrace(pw);
		pw.close();
		return baos.toString();
	}

	protected void executeProc(IDocFile idocFile, String procedure_name, Connection connection) throws Exception {
		logger.log(Level.CONFIG, "load procedure:{0}", new Object[] { procedure_name });
		CompositeMap context = new CompositeMap("context");
		int idoc_file_id = idocFile.getIdocFileId();
		context.putObject("/parameter/@idoc_file_id", idoc_file_id, true);
		context.putObject("/session/@user_id", 0, true);
		ServiceThreadLocal.setCurrentThreadContext(context);
		IProcedureManager procedureManager = (IProcedureManager) registry.getInstanceOfType(IProcedureManager.class);
		if (procedureManager == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(null, IProcedureManager.class, this.getClass().getName());
		IServiceFactory serviceFactory = (IServiceFactory) registry.getInstanceOfType(IServiceFactory.class);
		if (serviceFactory == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(null, IServiceFactory.class, this.getClass().getName());
		Procedure proc = procedureManager.loadProcedure(procedure_name);
		ServiceInvoker.invokeProcedureWithTransaction(procedure_name, proc, serviceFactory,context,connection);
	}

	private boolean isIdocTypeStop(IDocType idocType) throws SQLException, AuroraIDocException {
		boolean isOrdinal = databaseManager.isOrdinal(idocType.getIdoctyp(), idocType.getCimtyp());
		if (isOrdinal && errorIdocTypes.contains(idocType)) {
			return true;
		}
		return false;
	}

	private void addErrorIdocType(IDocType idocType) {
		if (idocType != null) {
			errorIdocTypes.add(idocType);
		}
	}
}

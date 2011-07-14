package aurora.plugin.sap.sync.idoc;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.SAXException;

import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;

public class IDocXMLParser extends Thread {
	public static final String SYNC = "sync";
	public IDocServer iDocServer;
	public List errorIdocTypes = new LinkedList();
	public int header_id;
	public IdocType idocType;
	public IDocXMLParser(IDocServer iDocServer) {
		this.iDocServer = iDocServer;
	}
	public void run() {
		while (!isFinished()) {
			idocType = null;
			header_id = -1;
			IDocFile file = iDocServer.getIdocFile();
			if (file == null)
				continue;
			try {
				header_id = iDocServer.getDbUtil().existHeaders(file.getIdocId());
				insertInterface(file);
			} catch (Throwable e) {
				try {
					iDocServer.log(e);
					if (idocType != null) {
						errorIdocTypes.add(idocType);
					}
					String errorMessage= "interface failed";
					iDocServer.log("updateIdocStatus for idoc:" + file.getIdocId()+" "+errorMessage);
					iDocServer.getDbUtil().updateIdocsStatus(file.getIdocId(), errorMessage);
				} catch (Throwable e1) {
					iDocServer.log(e1);
				}
				continue;
			}
			try {
				iDocServer.log("insertMiddleTables for idoc:" + file.getIdocId());
				insertMiddleTables(file);
			} catch (Throwable e) {
				iDocServer.log(e);
				try {
					String errorMessage= "middle failed";
					iDocServer.log("updateIdocStatus for idoc:" + file.getIdocId()+" "+errorMessage);
					iDocServer.getDbUtil().updateIdocStatus(header_id, file.getIdocId(), errorMessage);
				} catch (SQLException e1) {
					iDocServer.log(e1);
				}
				continue;
			}
			try {
				iDocServer.log("insertFormalTables for idoc:" + file.getIdocId());
				insertFormalTables(file);
			} catch (Throwable e) {
				iDocServer.log(e);
				try {
					String errorMessage= "formal failed";
					iDocServer.log("updateIdocStatus for idoc:" + file.getIdocId()+" "+errorMessage);
					iDocServer.getDbUtil().updateIdocStatus(header_id, file.getIdocId(),errorMessage );
				} catch (SQLException e1) {
					iDocServer.log(e1);
				}
				continue;
			}
			iDocServer.log("idoc:" + file.getIdocId() + " execute successful !");
		}
	}
	private void insertInterface(IDocFile file) throws ApplicationException {
		try {
			if (header_id == -1) {
				iDocServer.log("parser " + file.getPath() + " file");
				CompositeLoader loader = new CompositeLoader();
				CompositeMap iDocData = loader.loadByFile(file.getPath());
				CompositeMap idoc_node = iDocData.getChild(IDocFile.IDOC_NODE);
				if (idoc_node == null || idoc_node.getChildIterator() == null || idoc_node.getChilds().size() < 2) {
					return;
				}
				CompositeMap control_node = (CompositeMap) idoc_node.getChilds().get(0);
				idocType = iDocServer.getDbUtil().getIdocType(control_node);
				if (isStop()) {
					throw new ApplicationException("This idocType:" + idocType + " has error before");
				}
				iDocServer.getDbUtil().getConnection().setAutoCommit(false);
				header_id = iDocServer.getDbUtil().registerInterfaceHeader(file.getIdocId(), control_node);
				iDocServer.getDbUtil().updateIdocInfo(file.getIdocId(), control_node);
				for (int i = 1; i < idoc_node.getChilds().size(); i++) {
					CompositeMap content_node = (CompositeMap) idoc_node.getChilds().get(i);
					iDocServer.getDbUtil().registerInterfaceLine(header_id, content_node);
				}
				iDocServer.getDbUtil().getConnection().commit();
				iDocServer.getDbUtil().getConnection().setAutoCommit(true);
			}
		} catch (IOException e) {
			throw new ApplicationException(e);
		} catch (SAXException e) {
			throw new ApplicationException(e);
		} catch (SQLException e) {
			throw new ApplicationException(e);
		} finally {
			try {
				iDocServer.getDbUtil().getConnection().rollback();
				iDocServer.getDbUtil().getConnection().setAutoCommit(true);
			} catch (SQLException e) {
				iDocServer.log(e);
			}
		}
		if (iDocServer.isDeleteImmediately()) {
			File deleteFile = new File(file.getPath());
			if (deleteFile.exists()) {
				iDocServer.log("delete file " + file.getPath() + " " + deleteFile.delete());
			}
		}
	}
	private void insertMiddleTables(IDocFile file) throws ApplicationException, SQLException {
			String executePkg = iDocServer.getDbUtil().getMiddleExecutePkg(file.getIdocId());
			String errorMessage = iDocServer.getDbUtil().executePkg(executePkg, header_id);
			if (errorMessage != null && !"".equals(errorMessage)) {
				throw new ApplicationException("execute middle Pkg " + executePkg + " failed:" + errorMessage);
			}
			iDocServer.getDbUtil().updateIdocStatus(header_id, file.getIdocId(), "middle");
	}
	private void insertFormalTables(IDocFile file) throws SQLException, ApplicationException {
		String executePkg = iDocServer.getDbUtil().getFormalExecutePkg(file.getIdocId());
		String errorMessage = iDocServer.getDbUtil().executePkg(executePkg, header_id);
		if (errorMessage != null && !"".equals(errorMessage)) {
			throw new ApplicationException("execute Formal Pkg " + executePkg + " failed:" + errorMessage);
		}
		iDocServer.getDbUtil().updateIdocStatus(header_id, file.getIdocId(), "done");
	}
	public boolean isFinished() {
		return iDocServer.isShutDown();
	}
	private boolean isStop() throws SQLException, ApplicationException {
		String handleModel = iDocServer.getDbUtil().getHandleModel(idocType.getIdoctyp(), idocType.getCimtyp());
		if (SYNC.equals(handleModel) && errorIdocTypes.contains(idocType)) {
			return true;
		}
		return false;
	}
}

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
				if(idocType != null){
					errorIdocTypes.add(idocType);
				}
				try {
					iDocServer.getDbUtil().updateIdocsStatus(file.getIdocId(), "failed");
				} catch (Throwable e1) {
					iDocServer.log(e1);
				}
				continue;
			}
			try {
				insertFormalTables(file);
				iDocServer.log("handle idoc:" + file.getIdocId() + " successful!");
			} catch (Throwable e) {
				iDocServer.log(e);
				try {
					iDocServer.getDbUtil().updateInterfaceLineStatus(header_id, file.getIdocId(), "failed");
				} catch (SQLException e1) {
					iDocServer.log(e1);
				}
				
			} 
		}
	}
	public boolean isFinished() {
		return iDocServer.isShutDown();
	}
	private void insertInterface(IDocFile file) throws ApplicationException {
		try {
			if (header_id == -1) {
				iDocServer.log("begin parser " + file.getPath() + " file");
				CompositeLoader loader = new CompositeLoader();
				CompositeMap iDocData = loader.loadByFile(file.getPath());
				CompositeMap idoc_node = iDocData.getChild(IDocFile.IDOC_NODE);
				if (idoc_node == null || idoc_node.getChildIterator() == null || idoc_node.getChilds().size() < 2) {
					return;
				}
				CompositeMap control_node = (CompositeMap) idoc_node.getChilds().get(0);
				CompositeMap content_node = (CompositeMap) idoc_node.getChilds().get(1);
				iDocServer.log("handle " + file.getPath() + " control_node ");
				idocType = iDocServer.getDbUtil().getIdocType(control_node);
				if(isStop()){
					throw new ApplicationException("This idocType:"+idocType+" has error before");
				}
				header_id = iDocServer.getDbUtil().registerInterfaceHeader(file.getIdocId(), control_node);
				iDocServer.getDbUtil().updateIdocInfo(file.getIdocId(), control_node);
				iDocServer.log("handle " + file.getPath() + " content_node ");
				iDocServer.getDbUtil().registerInterfaceLine(header_id, content_node);
			}
		} catch (IOException e) {
			throw new ApplicationException(e);
		} catch (SAXException e) {
			throw new ApplicationException(e);
		} catch (SQLException e) {
			throw new ApplicationException(e);
		}
		if (iDocServer.isDeleteImmediately()) {
			File deleteFile = new File(file.getPath());
			if (deleteFile.exists()) {
				iDocServer.log("delete file " + file.getPath() + " " + deleteFile.delete());
			}
		}
	}
	private void insertFormalTables(IDocFile file) throws ApplicationException {
		try {
			String executePkg = iDocServer.getDbUtil().getExecutePkg(file.getIdocId());
			iDocServer.log("get executePkg:" + executePkg);
			String errorMessage = iDocServer.getDbUtil().executePkg(executePkg, header_id);
			if (errorMessage != null && !"".equals(errorMessage)) {
				iDocServer.handleException("executePkg " + executePkg + " failed:" + errorMessage);
			}
			iDocServer.log("executePkg:" + executePkg + " successful!");
			iDocServer.getDbUtil().updateInterfaceLineStatus(header_id, file.getIdocId(), "done");
		} catch (SQLException e) {
			throw new ApplicationException(e);
		}
	}
	private boolean isStop() throws SQLException, ApplicationException{
		String handleModel = iDocServer.getDbUtil().getHandleModel(idocType.getIdoctyp(), idocType.getCimtyp());
		if(SYNC.equals(handleModel)||errorIdocTypes.contains(idocType)){
			return true;
		}
		return false;
	}
}

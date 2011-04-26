package aurora.plugin.sap.sync.idoc;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.xml.sax.SAXException;

import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;

public class IDocXMLParser extends Thread {
	public IDocServer iDocServer;
	public IDocXMLParser(IDocServer iDocServer) {
		this.iDocServer = iDocServer;
	}
	public void run() {
		while (!isFinished()) {
			IDocFile file = iDocServer.getIdocFile();
			if (file == null)
				continue;
			try {
				int header_id = iDocServer.getDbUtil().existHeaders(file.getIdocId());
				if (header_id == -1) {
					iDocServer.log("begin parser " + file.getPath() + " file");
					CompositeLoader loader = new CompositeLoader();
					CompositeMap iDocData = loader.loadByFile(file.getPath());
					CompositeMap idoc_node = iDocData.getChild(IDocFile.IDOC_NODE);
					if (idoc_node == null || idoc_node.getChildIterator() == null || idoc_node.getChilds().size() < 2) {
						continue;
					}
					CompositeMap control_node = (CompositeMap) idoc_node.getChilds().get(0);
					CompositeMap content_node = (CompositeMap) idoc_node.getChilds().get(1);
					iDocServer.log("handle " + file.getPath() + " control_node ");
					header_id = iDocServer.getDbUtil().registerInterfaceHeader(file.getIdocId(), control_node);
					iDocServer.getDbUtil().updateIdocInfo(file.getIdocId(), control_node);
					iDocServer.log("handle " + file.getPath() + " content_node ");
					iDocServer.getDbUtil().registerInterfaceLine(header_id, content_node);
				}
				if (iDocServer.isDeleteImmediately()) {
					File deleteFile = new File(file.getPath());
					if (deleteFile.exists()) {
						iDocServer.log("delete file " + file.getPath() + " " + deleteFile.delete());
					}
				}
				String executePkg = iDocServer.getDbUtil().getExecutePkg(file.getIdocId());
				iDocServer.log("get executePkg:" + executePkg);
				String errorMessage = iDocServer.getDbUtil().executePkg(executePkg, header_id);
				if (errorMessage != null && !"".equals(errorMessage)) {
					iDocServer.handleException("executePkg " + executePkg + " failed:" + errorMessage);
				}
				iDocServer.log("executePkg:" + executePkg + " successful!");
				iDocServer.getDbUtil().updateInterfaceLineStatus(header_id, file.getIdocId());
			} catch (IOException e) {
				iDocServer.handleException(e);
			} catch (SAXException e) {
				iDocServer.handleException(e);
			} catch (SQLException e) {
				iDocServer.handleException(e);
			} catch (ApplicationException e) {
				iDocServer.handleException(e);
			}
		}
	}
	public boolean isFinished() {
		return iDocServer.isShutDown();
	}
}

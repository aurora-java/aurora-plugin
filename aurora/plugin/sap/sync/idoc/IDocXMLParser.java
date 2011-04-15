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
			iDocServer.log("begin parser " + file.getPath() + " file");
			CompositeLoader loader = new CompositeLoader();
			try {
				CompositeMap iDocData = loader.loadByFile(file.getPath());
				CompositeMap idoc_node = iDocData.getChild(IDocFile.IDOC_NODE);
				if (idoc_node == null || idoc_node.getChildIterator() == null || idoc_node.getChilds().size() < 2) {
					return;
				}
				iDocServer.log("handle "+file.getPath()+" control_node ");
				CompositeMap control_node = (CompositeMap) idoc_node.getChilds().get(0);
				int header_id = -1;
				header_id = iDocServer.getDbUtil().registerInterfaceHeader(file.getIdocId(), control_node);
				iDocServer.getDbUtil().updateIdocInfo(file.getIdocId(), control_node);
				iDocServer.log("handle "+file.getPath()+" content_node ");
				CompositeMap content_node = (CompositeMap) idoc_node.getChilds().get(1);
				iDocServer.getDbUtil().registerInterfaceLine(header_id, content_node);
				iDocServer.getDbUtil().updateInterfaceLineStatus(header_id, file.getIdocId());
				if (iDocServer.isDeleteImmediately()) {
					File deleteFile = new File(file.getPath());
					if (deleteFile.exists()) {
						iDocServer.log("delete file " + file.getPath() + " " + deleteFile.delete());
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (SAXException e) {
				throw new RuntimeException(e);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}
	public boolean isFinished() {
		return iDocServer.isShutDown();
	}

}

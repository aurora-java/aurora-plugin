package aurora.plugin.sap.sync.idoc;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.LinkedList;

import uncertain.logging.ILogger;

import com.sap.conn.idoc.IDocDocumentList;
import com.sap.conn.idoc.IDocXMLProcessor;
import com.sap.conn.idoc.jco.JCoIDoc;
import com.sap.conn.idoc.jco.JCoIDocHandler;
import com.sap.conn.idoc.jco.JCoIDocHandlerFactory;
import com.sap.conn.idoc.jco.JCoIDocServer;
import com.sap.conn.idoc.jco.JCoIDocServerContext;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerContextInfo;
import com.sap.conn.jco.server.JCoServerErrorListener;
import com.sap.conn.jco.server.JCoServerExceptionListener;
import com.sap.conn.jco.server.JCoServerState;
import com.sap.conn.jco.server.JCoServerTIDHandler;
public class IDocServer{
	public JCoIDocServer iDocServer;
	private String serverName;
	private DataBaseUtil dbUtil;
	public LinkedList idocFils = new LinkedList();
	private IDocServerInstance iDocServerInstance;
	private ILogger logger;
	private int server_id = -1;

	public IDocServer(IDocServerInstance iDocServerInstance, String serverName) {
		this.iDocServerInstance = iDocServerInstance;
		this.serverName = serverName;
	}
	public void start(){
		logger = iDocServerInstance.getLogger();
		log("get database connection for "+serverName);
		dbUtil = new DataBaseUtil(iDocServerInstance.getRegistry(),logger);
		try {
			// see provided examples of configuration files MYSERVER.jcoServer and BCE.jcoDestination
			log("start IDocServer "+serverName);
			iDocServer = JCoIDoc.getServer(serverName);
		} catch (JCoException e) {
			throw new RuntimeException(serverName+" is not valid.", e);
		}
		MyIDocHandlerFactory idocHanlerFactory= new MyIDocHandlerFactory();
		iDocServer.setIDocHandlerFactory(idocHanlerFactory);
		iDocServer.setTIDHandler(new MyTidHandler());

		MyThrowableListener listener = new MyThrowableListener();
		iDocServer.addServerErrorListener(listener);
		iDocServer.addServerExceptionListener(listener);
		iDocServer.setConnectionCount(1);
		try {
			server_id = dbUtil.registerSapServers(iDocServer);
			log("get server_id "+server_id);
			iDocServer.start();
			if(!JCoServerState.ALIVE.equals(iDocServer.getState())){
				log("unRegisterSapServers server_id "+server_id);
				dbUtil.unRegisterSapServers(server_id);
				dbUtil.dispose();
				return;
			}
			log("start IDocXMLParser ");
			IDocXMLParser parser = new IDocXMLParser(this);
			parser.start();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		log("IDocServer "+serverName+" start finished.");
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
				String filePath = iDocServerInstance.getIdocDir()+File.separator+serverCtx.getTID() + "_idoc.xml";
				log("receive idoc "+filePath);
				fos = new FileOutputStream(filePath);
				osw = new OutputStreamWriter(fos, "UTF8");
				xmlProcessor.render(idocList, osw, IDocXMLProcessor.RENDER_WITH_TABS_AND_CRLF);
				osw.flush();
				int idoc_id = dbUtil.addIdoc(server_id,filePath);
				log("add idoc_id "+idoc_id);
				addIdocFile(new IDocFile(filePath, idoc_id, server_id));
			} catch (Throwable thr) {
				throw new RuntimeException(thr);
			} finally {
				try {
					if (osw != null)
						osw.close();
					if (fos != null)
						fos.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	public synchronized void addIdocFile(IDocFile file){
		idocFils.addLast(file);
	}
	public synchronized IDocFile getIdocFile(){
		if(idocFils.size()<=0){
			return null;
		}
		IDocFile file = (IDocFile)idocFils.getFirst();
		idocFils.remove(0);
		return file;
	}
	
	class MyIDocHandlerFactory implements JCoIDocHandlerFactory {
		private JCoIDocHandler handler = new MyIDocHandler();
		public JCoIDocHandler getIDocHandler(JCoIDocServerContext serverCtx) {
			return handler;
		}
		public JCoIDocHandler getIDocHandler(){
			return handler;
		}
	}

	static class MyThrowableListener implements JCoServerErrorListener, JCoServerExceptionListener {

		public void serverErrorOccurred(JCoServer server, String connectionId, JCoServerContextInfo ctx, Error error) {
			throw new RuntimeException(">>> Error occured on " + server.getProgramID() + " connection " + connectionId,error);
		}
		public void serverExceptionOccurred(JCoServer server, String connectionId, JCoServerContextInfo ctx,
				Exception error) {
			throw new RuntimeException(">>> Exception occured on " + server.getProgramID() + " connection " + connectionId,error);
		}
	}

	class MyTidHandler implements JCoServerTIDHandler {
		public boolean checkTID(JCoServerContext serverCtx, String tid) {
			log("checkTID called for TID=" + tid);
			return true;
		}

		public void confirmTID(JCoServerContext serverCtx, String tid) {
			log("confirmTID called for TID=" + tid);
		}

		public void commit(JCoServerContext serverCtx, String tid) {
			log("commit called for TID=" + tid);
		}

		public void rollback(JCoServerContext serverCtx, String tid) {
			log("rollback called for TID=" + tid);
		}
	}
	public boolean isShutDown(){
		return !(JCoServerState.ALIVE.equals(iDocServer.getState()));
	}
	public boolean isFinished(){
		return idocFils.size()<=0;
	}
	public void log(String message){
		if(logger != null){
			logger.info(message);
		}else{
			System.out.println(message);
		}
	}
	public int getServerId() {
		return server_id;
	}
	public void setServerId(int serverId) {
		this.server_id = serverId;
	}
	public boolean isDeleteImmediately() {
		return iDocServerInstance.isDeleteImmediately();
	}
}

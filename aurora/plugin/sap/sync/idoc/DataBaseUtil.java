package aurora.plugin.sap.sync.idoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import javax.sql.DataSource;

import uncertain.composite.CompositeMap;
import uncertain.logging.ILogger;
import uncertain.ocm.IObjectRegistry;

import com.sap.conn.idoc.jco.JCoIDocServer;

public class DataBaseUtil {
	private Connection dbConn;
	private ILogger logger;
	public DataBaseUtil(IObjectRegistry registry, ILogger logger) {
		dbConn = initConnection(registry);
		this.logger = logger;
	}
	private Connection initConnection(IObjectRegistry registry) {
		DataSource ds = (DataSource) registry.getInstanceOfType(DataSource.class);
		try {
			if (ds == null)
				throw new IllegalArgumentException("Can not get DataSource from registry " + registry);
			return ds.getConnection();
		} catch (SQLException e) {
			throw new RuntimeException("Can not get Connection from DataSource", e);
		}
	}
	public int registerSapServers(JCoIDocServer server) throws SQLException {
		// SERVER_ID,PROGRAM_ID,REPOSITORY_NAME,GATEWAY_HOST,GATEWAY_SERVICE,RESPOSITORY_DESTINATION,STATUS,CREATED_BY,CREATION_DATE,LAST_UPDATED_BY,LAST_UPDATE_DATE,
		int server_id = -1;
		String program_id = server.getProgramID();
		String repository_name = server.getRepository().getName();
		String gateway_host = server.getGatewayHost();
		String gateway_service = server.getGatewayService();
		String respository_destination = server.getRepositoryDestination();
		String select_sql = "select t.SERVER_ID from fnd_sap_servers t where t.PROGRAM_ID=? and t.REPOSITORY_NAME=? and t.GATEWAY_HOST=?"
				+ " and t.GATEWAY_SERVICE=?";
		PreparedStatement statement = dbConn.prepareStatement(select_sql);
		int index = 1;
		statement.setString(index++, program_id);
		statement.setString(index++, repository_name);
		statement.setString(index++, gateway_host);
		statement.setString(index++, gateway_service);
		ResultSet rs = statement.executeQuery();
		if (rs.next()) {
			server_id = rs.getInt(1);
			return server_id;
		}
		statement = dbConn.prepareStatement("select fnd_sap_servers_s.nextval from dual");
		rs = statement.executeQuery();
		if (rs.next()) {
			server_id = rs.getInt(1);
		}
		rs.close();
		String insert_sql = "insert into fnd_sap_servers(" + "SERVER_ID,PROGRAM_ID,REPOSITORY_NAME,GATEWAY_HOST,"
				+ "GATEWAY_SERVICE,RESPOSITORY_DESTINATION,STATUS,CREATED_BY,"
				+ "CREATION_DATE,LAST_UPDATED_BY,LAST_UPDATE_DATE" + ") values" + "(?,?,?,?,?,?,?,0,sysdate,0,sysdate)";
		statement = dbConn.prepareStatement(insert_sql);
		index = 1;
		statement.setInt(index++, server_id);
		statement.setString(index++, program_id);
		statement.setString(index++, repository_name);
		statement.setString(index++, gateway_host);
		statement.setString(index++, gateway_service);
		statement.setString(index++, respository_destination);
		statement.setString(index++, "");
		statement.executeUpdate();
		statement.close();
		return server_id;
	}
	public void unRegisterSapServers(int serverId) throws SQLException {
		String delete_sql = "delete from fnd_sap_servers s where s.server_id=" + serverId;
		Statement statement = dbConn.createStatement();
		statement.executeUpdate(delete_sql);
		statement.close();
	}
	public int addIdoc(int serverId, String filePath) throws SQLException {
		String get_idoc_id_sql = "select fnd_sap_idocs_s.nextval from dual";
		Statement statement = dbConn.createStatement();
		ResultSet rs = statement.executeQuery(get_idoc_id_sql);
		int idoc_id = -1;
		if (rs.next()) {
			idoc_id = rs.getInt(1);
		} else {
			throw new RuntimeException("Can not create idoc id !");
		}
		rs.close();
		String insert_sql = "insert into fnd_sap_idocs(IDOC_ID,SERVER_ID,FILE_PATH, CREATED_BY,CREATION_DATE,LAST_UPDATED_BY,LAST_UPDATE_DATE) values(?,?,?,0,sysdate,0,sysdate) ";
		PreparedStatement pStatement = dbConn.prepareStatement(insert_sql);
		int index = 1;
		pStatement.setInt(index++, idoc_id);
		pStatement.setInt(index++, serverId);
		pStatement.setString(index++, filePath);
		pStatement.executeUpdate();
		pStatement.close();
		return idoc_id;
	}
	public void updateIdocInfo(int idocId, CompositeMap control_node) throws SQLException {
		if (idocId < 1 || control_node == null)
			return;
		String tabnam = getChildNodeText(control_node, IDocFile.TABNAM_NODE);
		String mandt = getChildNodeText(control_node, IDocFile.MANDT_NODE);
		String docnum = getChildNodeText(control_node, IDocFile.DOCNUM_NODE);
		String docrel = getChildNodeText(control_node, IDocFile.DOCREL_NODE);
		String status = getChildNodeText(control_node, IDocFile.STATUS_NODE);
		String direct = getChildNodeText(control_node, IDocFile.DIRECT_NODE);
		String outmod = getChildNodeText(control_node, IDocFile.OUTMOD_NODE);
		String idoctyp = getChildNodeText(control_node, IDocFile.IDOCTYP_NODE);
		String cimtyp = getChildNodeText(control_node, IDocFile.CIMTYP_NODE);
		String mestyp = getChildNodeText(control_node, IDocFile.MESTYP_NODE);
		String sndpor = getChildNodeText(control_node, IDocFile.SNDPOR_NODE);
		String sndprt = getChildNodeText(control_node, IDocFile.SNDPRT_NODE);
		String sndprn = getChildNodeText(control_node, IDocFile.SNDPRN_NODE);
		String rcvpor = getChildNodeText(control_node, IDocFile.RCVPOR_NODE);
		String rcvprt = getChildNodeText(control_node, IDocFile.RCVPRT_NODE);
		String rcvprn = getChildNodeText(control_node, IDocFile.RCVPRN_NODE);
		String credat = getChildNodeText(control_node, IDocFile.CREDAT_NODE);
		String cretim = getChildNodeText(control_node, IDocFile.CRETIM_NODE);
		String serial = getChildNodeText(control_node, IDocFile.SERIAL_NODE);

		String update_sql = "update fnd_sap_idocs set tabnam=?, mandt=?, docnum=?, docrel=?, cimtyp=?,"
				+ " status=?, direct=?, outmod=?, idoctyp=? ,mestyp=?, sndpor=? ,sndprt=? ,sndprn=?, rcvpor=?, rcvprt=? ,"
				+ " rcvprn=?,credat=?, cretim=?, serial=?, last_updated_by=0, last_update_date=sysdate where idoc_id = ?";
		PreparedStatement statement = dbConn.prepareStatement(update_sql);
		int index = 1;
		statement.setString(index++, tabnam);
		statement.setString(index++, mandt);
		statement.setString(index++, docnum);
		statement.setString(index++, docrel);
		statement.setString(index++, cimtyp);
		statement.setString(index++, status);
		statement.setString(index++, direct);
		statement.setString(index++, outmod);
		statement.setString(index++, idoctyp);
		statement.setString(index++, mestyp);
		statement.setString(index++, sndpor);
		statement.setString(index++, sndprt);
		statement.setString(index++, sndprn);
		statement.setString(index++, rcvpor);
		statement.setString(index++, rcvprt);
		statement.setString(index++, rcvprn);
		statement.setString(index++, credat);
		statement.setString(index++, cretim);
		statement.setString(index++, serial);
		statement.setInt(index++, idocId);
		statement.executeUpdate();
		statement.close();
	}
	private String getChildNodeText(CompositeMap node, String childName) {
		if (node == null || childName == null)
			return null;
		CompositeMap childNode = node.getChild(childName);
		if (childNode == null)
			return null;
		return childNode.getText();
	}
	public int registerInterfaceHeader(int idocId, CompositeMap controlNode) throws SQLException {
		if (idocId < 1 || controlNode == null)
			return -1;
		String idoctyp = getChildNodeText(controlNode, IDocFile.IDOCTYP_NODE);
		String cimtyp = getChildNodeText(controlNode, IDocFile.CIMTYP_NODE);
		String templateCode = getTemplateCode(idoctyp, cimtyp);
		String get_interface_header_sql = "select FND_INTERFACE_HEADERS_s.nextval from dual";
		Statement statement = dbConn.createStatement();
		ResultSet rs = statement.executeQuery(get_interface_header_sql);
		int header_id = -1;
		if (rs.next()) {
			header_id = rs.getInt(1);
		} else {
			throw new RuntimeException("Can not create interface header id !");
		}
		rs.close();
		statement.close();
		String insert_sql = "insert into FND_INTERFACE_HEADERS(HEADER_ID,TEMPLET_CODE,ATTRIBUTE_1,CREATED_BY,CREATION_DATE,LAST_UPDATED_BY,LAST_UPDATE_DATE)"
				+ " values(?,?,?,0,sysdate,0,sysdate)";
		PreparedStatement pstatement = dbConn.prepareStatement(insert_sql);
		pstatement.setInt(1, header_id);
		pstatement.setString(2, templateCode);
		pstatement.setString(3, String.valueOf(idocId));
		pstatement.executeUpdate();
		pstatement.close();
		return header_id;

	}
	public String getTemplateCode(String idoctyp, String cimtyp) throws SQLException {
		StringBuffer query_sql = new StringBuffer("select TEMPLATE_CODE from FND_SAP_IDOC_TEMPLATES where IDOCTYP=? ");
		if (cimtyp != null)
			query_sql.append(" and CIMTYP=?");
		PreparedStatement statement = dbConn.prepareStatement(query_sql.toString());
		statement.setString(1, idoctyp);
		if (cimtyp != null)
			statement.setString(2, cimtyp);
		ResultSet rs = statement.executeQuery();
		String templateCode = null;
		if (rs.next()) {
			templateCode = rs.getString(1);
		} else {
			throw new RuntimeException("IDOCTYP:" + idoctyp + " CIMTYP:" + cimtyp + "Can not get template code !");
		}
		rs.close();
		statement.close();
		return templateCode;
	}
	public void registerInterfaceLine(int headerId, CompositeMap contentNode) throws SQLException {
		if (headerId < 1 || contentNode == null)
			return;
		dbConn.setAutoCommit(false);
		handleContentNode(headerId, 0, contentNode);
		dbConn.commit();
		dbConn.setAutoCommit(true);
	}
	private void handleContentNode(int headerId, int parent_id, CompositeMap node) throws SQLException {
		StringBuffer insert_sql = new StringBuffer(
				"insert into FND_INTERFACE_LINES(LINE_ID,HEADER_ID,CREATED_BY,CREATION_DATE,LAST_UPDATED_BY,LAST_UPDATE_DATE,"
						+ " SOURCE_TABLE,PARENT_LINE_ID");
		int line_id = getLineId();
		StringBuffer values_sql = new StringBuffer("values(?,?,0,sysdate,0,sysdate,?,?");
		int index = 1;
		for (int i = 0; i < node.getChilds().size(); i++) {
			CompositeMap child = (CompositeMap) node.getChilds().get(i);
			if (child.getChilds() != null && child.getChilds().size() > 0) {
				handleContentNode(headerId, line_id, child);
				continue;
			}
			insert_sql.append(",ATTRIBUTE_" + (index++));
			values_sql.append(",?");
		}
		insert_sql.append(")").append(values_sql).append(")");
		PreparedStatement statement = dbConn.prepareStatement(insert_sql.toString());
		index = 1;
		statement.setInt(index++, line_id);
		statement.setInt(index++, headerId);
		statement.setString(index++, node.getName());
		statement.setInt(index++, parent_id);
		for (Iterator it = node.getChildIterator(); it.hasNext();) {
			CompositeMap child = (CompositeMap) it.next();
			if (child.getChilds() != null && child.getChilds().size() > 0) {
				continue;
			}
			statement.setString(index++, child.getText());
		}
		statement.executeUpdate();
		statement.close();
	}
	public int getLineId() throws SQLException {
		String query_sql = "select FND_INTERFACE_LINES_s.nextval from dual";
		Statement statement = dbConn.createStatement();
		ResultSet rs = statement.executeQuery(query_sql);
		int lineId = -1;
		if (rs.next()) {
			lineId = rs.getInt(1);
		} else {
			throw new RuntimeException("Can not create idoc id !");
		}
		rs.close();
		statement.close();
		return lineId;
	}
	public void log(String message) {
		if (logger != null) {
			logger.info(message);
		} else {
			System.out.println(message);
		}
	}
	public void updateInterfaceLineStatus(int headerId, int idocId) throws SQLException {
		log("begin updateInterfaceLineStatus headerId:"+headerId+" idocId："+idocId);
		String header_update_sql = "update FND_INTERFACE_HEADERS t set t.status='done' where t.header_id =?";
		String idoc_update_sql = "update fnd_sap_idocs t set t.handled_flag='Y' where t.idoc_id =?";
		PreparedStatement  statement = dbConn.prepareStatement(header_update_sql);
		statement.setInt(1, headerId);
		statement.executeUpdate();
		statement = dbConn.prepareStatement(idoc_update_sql);
		statement.setInt(1, idocId);
		statement.executeUpdate();
		statement.close();
	}
	public void dispose(){
		if(dbConn != null){
			try {
				dbConn.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}
}

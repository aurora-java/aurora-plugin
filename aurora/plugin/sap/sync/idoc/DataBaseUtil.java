package aurora.plugin.sap.sync.idoc;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import uncertain.composite.CompositeMap;
import uncertain.logging.ILogger;
import uncertain.ocm.IObjectRegistry;

import com.sap.conn.idoc.jco.JCoIDocServer;

public class DataBaseUtil {
	private Connection dbConn;
	private ILogger logger;
	public DataBaseUtil(IObjectRegistry registry, ILogger logger) throws ApplicationException {
		dbConn = initConnection(registry);
		this.logger = logger;
	}
	private Connection initConnection(IObjectRegistry registry) throws ApplicationException {
		DataSource ds = (DataSource) registry.getInstanceOfType(DataSource.class);
		try {
			if (ds == null)
				throw new ApplicationException("Can not get DataSource from registry " + registry);
			return ds.getConnection();
		} catch (SQLException e) {
			throw new ApplicationException("Can not get Connection from DataSource", e);
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
			rs.close();
			statement.close();
			String update_sql = "update fnd_sap_servers t set t.status = '',last_update_date=sysdate where t.server_id ="
					+ server_id;
			Statement st = dbConn.createStatement();
			st.executeUpdate(update_sql);
			st.close();
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
		log("unRegisterSapServers where  serverId:" + serverId);
		String delete_sql = "delete from fnd_sap_servers s where s.server_id=" + serverId;
		Statement statement = dbConn.createStatement();
		statement.executeUpdate(delete_sql);
		statement.close();
	}
	public int addIdoc(int serverId, String filePath) throws SQLException, ApplicationException {
		log("addIdoc where  serverId:" + serverId);
		String get_idoc_id_sql = "select fnd_sap_idocs_s.nextval from dual";
		Statement statement = dbConn.createStatement();
		ResultSet rs = statement.executeQuery(get_idoc_id_sql);
		int idoc_id = -1;
		if (rs.next()) {
			idoc_id = rs.getInt(1);
		} else {
			throw new ApplicationException("Can not create idoc id !");
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
		log("updateIdocInfo where  idocId:" + idocId);
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
	public int registerInterfaceHeader(int idocId, CompositeMap controlNode) throws SQLException, ApplicationException {
		log("registerInterfaceHeader where  idocId:" + idocId);
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
			throw new ApplicationException("Can not create interface header id !");
		}
		rs.close();
		statement.close();
		String insert_sql = "insert into FND_INTERFACE_HEADERS(HEADER_ID,TEMPLATE_CODE,ATTRIBUTE_1,CREATED_BY,CREATION_DATE,LAST_UPDATED_BY,LAST_UPDATE_DATE)"
				+ " values(?,?,?,0,sysdate,0,sysdate)";
		PreparedStatement pstatement = dbConn.prepareStatement(insert_sql);
		pstatement.setInt(1, header_id);
		pstatement.setString(2, templateCode);
		pstatement.setString(3, String.valueOf(idocId));
		pstatement.executeUpdate();
		pstatement.close();
		return header_id;

	}
	public String getTemplateCode(String idoctyp, String cimtyp) throws SQLException, ApplicationException {
		log("getTemplateCode where  idoctyp:" + idoctyp + " cimtyp:" + cimtyp);
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
			throw new ApplicationException("IDOCTYP:" + idoctyp + " CIMTYP:" + cimtyp + " Can not get template code !");
		}
		rs.close();
		statement.close();
		return templateCode;
	}
	public IdocType getIdocType(CompositeMap controlNode) {
		String idoctyp = getChildNodeText(controlNode, IDocFile.IDOCTYP_NODE);
		String cimtyp = getChildNodeText(controlNode, IDocFile.CIMTYP_NODE);
		return new IdocType(idoctyp, cimtyp);
	}

	public String getHandleModel(String idoctyp, String cimtyp) throws SQLException, ApplicationException {
		log("getHandleModel where  idoctyp:" + idoctyp + " cimtyp:" + cimtyp);
		StringBuffer query_sql = new StringBuffer("select HANDLE_MODEL from FND_SAP_IDOC_TEMPLATES where IDOCTYP=? ");
		if (cimtyp != null)
			query_sql.append(" and CIMTYP=?");
		PreparedStatement statement = dbConn.prepareStatement(query_sql.toString());
		statement.setString(1, idoctyp);
		if (cimtyp != null)
			statement.setString(2, cimtyp);
		ResultSet rs = statement.executeQuery();
		String handleModel = null;
		if (rs.next()) {
			handleModel = rs.getString(1);
		} else {
			throw new ApplicationException("IDOCTYP:" + idoctyp + " CIMTYP:" + cimtyp
					+ " Can not get handleModel code !");
		}
		rs.close();
		statement.close();
		return handleModel;
	}
	public void registerInterfaceLine(int headerId, CompositeMap contentNode) throws SQLException, ApplicationException {
		log("registerInterfaceLine where  headerId:" + headerId);
		if (headerId < 1 || contentNode == null)
			return;
		dbConn.setAutoCommit(false);
		handleContentNode(headerId, 0, contentNode);
		dbConn.commit();
		dbConn.setAutoCommit(true);
	}
	private void handleContentNode(int headerId, int parent_id, CompositeMap node) throws SQLException,
			ApplicationException {
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
			insert_sql.append(",ATTRIBUTE_" + (getFieldIndex(node.getName(), child.getName())));
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
	public int getLineId() throws SQLException, ApplicationException {
		log("getLineId ");
		String query_sql = "select FND_INTERFACE_LINES_s.nextval from dual";
		Statement statement = dbConn.createStatement();
		ResultSet rs = statement.executeQuery(query_sql);
		int lineId = -1;
		if (rs.next()) {
			lineId = rs.getInt(1);
		} else {
			throw new ApplicationException("Can not create idoc id !");
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
	public void updateInterfaceLineStatus(int headerId, int idocId, String status) throws SQLException {
		log("updateInterfaceLineStatus where headerId:" + headerId + " idocId：" + idocId);
		String header_update_sql = "update FND_INTERFACE_HEADERS t set t.status=? where t.header_id =?";
		String idoc_update_sql = "update fnd_sap_idocs t set t.handled_status=? where t.idoc_id =?";
		PreparedStatement statement = dbConn.prepareStatement(header_update_sql);
		statement.setString(1, status);
		statement.setInt(2, headerId);
		statement.executeUpdate();
		statement = dbConn.prepareStatement(idoc_update_sql);
		statement.setString(1, status);
		statement.setInt(2, idocId);
		statement.executeUpdate();
		statement.close();
	}
	public void updateIdocsStatus(int idocId, String message) throws SQLException {
		log("updateIdocsStatus where headerId:" + " idocId：" + idocId);
		String idoc_update_sql = "update fnd_sap_idocs t set t.handled_status=? where t.idoc_id =?";
		PreparedStatement statement = dbConn.prepareStatement(idoc_update_sql);
		statement.setString(1, message);
		statement.setInt(2, idocId);
		statement.executeUpdate();
		statement.close();
	}
	public String getExecutePkg(int idocId) throws SQLException, ApplicationException {
		log("getExecutePkg from idocId:" + idocId);
		if (idocId < 1)
			return null;
		String query_sql = "select i.idoctyp,i.cimtyp from fnd_sap_idocs i where i.idoc_id = " + idocId;
		Statement statement = dbConn.createStatement();
		ResultSet rs = statement.executeQuery(query_sql);
		String idoctyp = null;
		String cimtyp = null;
		if (rs.next()) {
			idoctyp = rs.getString(1);
			cimtyp = rs.getString(2);
		} else {
			throw new ApplicationException("Can not get idoctyp where idoc_id:" + idocId + "!");
		}
		rs.close();
		statement.close();
		String templateCode = getTemplateCode(idoctyp, cimtyp);
		return getExecutePkg(templateCode);
	}
	public String getExecutePkg(String template_code) throws SQLException, ApplicationException {
		log("getExecutePkg from template_code:" + template_code);
		String query_sql = "select execute_pkg from fnd_interface_templates where enabled_flag='Y' and template_code='"
				+ template_code + "'";
		Statement statement = dbConn.createStatement();
		ResultSet rs = statement.executeQuery(query_sql);
		String executePkg = null;
		if (rs.next()) {
			executePkg = rs.getString(1);
		} else {
			throw new ApplicationException("Can not get template_code：" + template_code + "'s execute_pkg !");
		}
		rs.close();
		statement.close();
		return executePkg;
	}
	public String executePkg(String executePkg, int headerId) throws SQLException {
		log("executePkg where executePkg:" + executePkg + " headerId:" + headerId);
		dbConn.setAutoCommit(false);
		CallableStatement proc = dbConn.prepareCall("{call ? := " + executePkg + "(?)}");
		String errorMessage = null;
		proc.registerOutParameter(1, Types.VARCHAR);
		proc.setInt(2, headerId);
		proc.execute();
		errorMessage = proc.getString(1);
		if (errorMessage == null || "".equals(errorMessage)) {
			dbConn.commit();
		} else {
			dbConn.rollback();
		}
		dbConn.setAutoCommit(true);
		return errorMessage;

	}
	public void stopSapServers(int serverId) throws SQLException {
		log("stopSapServers where serverId:" + serverId);
		String delete_sql = "update fnd_sap_servers s set s.status='Error occurred:please check the console or log for details.',last_update_date=sysdate where s.server_id="
				+ serverId;
		Statement statement = dbConn.createStatement();
		statement.executeUpdate(delete_sql);
		statement.close();
	}
	public int getFieldIndex(String segmenttyp, String fieldname) throws SQLException, ApplicationException {
		log("getFieldIndex from segmenttyp:" + segmenttyp + " fieldname:" + fieldname);
		String get_field_Index_sql = "select t.field_index from fnd_sap_fields t where t.segmenttyp ='" + segmenttyp
				+ "' and t.fieldname='" + fieldname + "'";
		Statement statement = dbConn.createStatement();
		ResultSet rs = statement.executeQuery(get_field_Index_sql);
		int fieldIndex = -1;
		if (rs.next()) {
			fieldIndex = rs.getInt(1);
		} else {
			throw new ApplicationException("Can not get fieldIndex ." + " segmenttyp:" + segmenttyp + " fieldname:"
					+ fieldname);
		}
		rs.close();
		statement.close();
		return fieldIndex;
	}
	public void getHistoryIdocs(String program_id, List idocList) throws SQLException, ApplicationException {
		log("getHistoryIdocs from program_id:" + program_id);
		String get_HistoryIdocs_sql = "select i.idoc_id, i.server_id, i.file_path  from fnd_interface_headers t, "
				+ "fnd_sap_idocs i, fnd_sap_servers s  where (t.status is null or t.status<>'done') and t.attribute_1 = i.idoc_id"
				+ " and i.server_id = s.server_id" + " and s.program_id='" + program_id + "' order by i.idoc_id";
		Statement statement = dbConn.createStatement();
		ResultSet rs = statement.executeQuery(get_HistoryIdocs_sql);
		while (rs.next()) {
			int idoc_id = rs.getInt(1);
			int server_id = rs.getInt(2);
			String file_path = rs.getString(3);
			File file = new File(file_path);
			if (!file.exists()) {
				throw new ApplicationException("file :" + file.getAbsolutePath() + " is not exits");
			}
			idocList.add(new IDocFile(file_path, idoc_id, server_id));
		}
		rs.close();
		statement.close();
	}
	public int existHeaders(int idoc_id) throws SQLException {
		log("get existHeaders from idocId:" + idoc_id);
		String get_field_Index_sql = "select t.header_id " + " from fnd_interface_headers t, fnd_sap_idocs i "
				+ " where t.attribute_1 = i.idoc_id and i.idoc_id = " + idoc_id;
		Statement statement = dbConn.createStatement();
		ResultSet rs = statement.executeQuery(get_field_Index_sql);
		int server_id = -1;
		if (rs.next()) {
			server_id = rs.getInt(1);
		}
		rs.close();
		statement.close();
		return server_id;
	}

	public Connection getConnection() {
		return dbConn;
	}
	public void dispose() throws SQLException {
		log("dispose database connection.");
		if (dbConn != null) {
			dbConn.close();
		}
	}
}

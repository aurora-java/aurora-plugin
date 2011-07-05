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
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			String program_id = server.getProgramID();
			String repository_name = server.getRepository().getName();
			String gateway_host = server.getGatewayHost();
			String gateway_service = server.getGatewayService();
			String respository_destination = server.getRepositoryDestination();
			String select_sql = "select t.SERVER_ID from fnd_sap_servers t where t.PROGRAM_ID=? and t.REPOSITORY_NAME=? and t.GATEWAY_HOST=?"
					+ " and t.GATEWAY_SERVICE=?";
			statement = dbConn.prepareStatement(select_sql);
			int index = 1;
			statement.setString(index++, program_id);
			statement.setString(index++, repository_name);
			statement.setString(index++, gateway_host);
			statement.setString(index++, gateway_service);
			rs = statement.executeQuery();
			if (rs.next()) {
				server_id = rs.getInt(1);
				rs.close();
				statement.close();
				String update_sql = "update fnd_sap_servers t set t.status = '',last_update_date=sysdate where t.server_id ="
						+ server_id;
				Statement st = dbConn.createStatement();
				st.executeUpdate(update_sql);
				st.close();
				statement.close();
				return server_id;
			}
			statement = dbConn.prepareStatement("select fnd_sap_servers_s.nextval from dual");
			rs = statement.executeQuery();
			if (rs.next()) {
				server_id = rs.getInt(1);
			}
			rs.close();
			statement.close();
			String insert_sql = "insert into fnd_sap_servers(" + "SERVER_ID,PROGRAM_ID,REPOSITORY_NAME,GATEWAY_HOST,"
					+ "GATEWAY_SERVICE,RESPOSITORY_DESTINATION,STATUS,CREATED_BY,"
					+ "CREATION_DATE,LAST_UPDATED_BY,LAST_UPDATE_DATE" + ") values"
					+ "(?,?,?,?,?,?,?,0,sysdate,0,sysdate)";
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
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (statement != null) {
				statement.close();
			}
		}

		return server_id;
	}
	public void unRegisterSapServers(int serverId) throws SQLException {
		String delete_sql = "delete from fnd_sap_servers s where s.server_id=" + serverId;
		Statement statement = null;
		try {
			statement = dbConn.createStatement();
			statement.executeUpdate(delete_sql);
			statement.close();
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}
	public int addIdoc(int serverId, String filePath) throws SQLException, ApplicationException {
		String get_idoc_id_sql = "select fnd_sap_idocs_s.nextval from dual";
		Statement statement = null;
		PreparedStatement pStatement = null;
		ResultSet rs = null;
		int idoc_id = -1;
		try {
			statement = dbConn.createStatement();
			rs = statement.executeQuery(get_idoc_id_sql);
			if (rs.next()) {
				idoc_id = rs.getInt(1);
			} else {
				throw new ApplicationException("execute sql:" + get_idoc_id_sql + " failed.");
			}
			rs.close();
			statement.close();
			String insert_sql = "insert into fnd_sap_idocs(IDOC_ID,SERVER_ID,FILE_PATH, CREATED_BY,CREATION_DATE,LAST_UPDATED_BY,LAST_UPDATE_DATE) values(?,?,?,0,sysdate,0,sysdate) ";
			pStatement = dbConn.prepareStatement(insert_sql);
			int index = 1;
			pStatement.setInt(index++, idoc_id);
			pStatement.setInt(index++, serverId);
			pStatement.setString(index++, filePath);
			pStatement.executeUpdate();
			pStatement.close();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (statement != null) {
				statement.close();
			}
			if (pStatement != null) {
				pStatement.close();
			}
		}
		return idoc_id;
	}
	public void updateIdocInfo(int idocId, CompositeMap control_node) throws SQLException {
		// log("updateIdocInfo where  idocId:" + idocId);
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
		PreparedStatement statement = null;
		try {
			String update_sql = "update fnd_sap_idocs set tabnam=?, mandt=?, docnum=?, docrel=?, cimtyp=?,"
					+ " status=?, direct=?, outmod=?, idoctyp=? ,mestyp=?, sndpor=? ,sndprt=? ,sndprn=?, rcvpor=?, rcvprt=? ,"
					+ " rcvprn=?,credat=?, cretim=?, serial=?, last_updated_by=0, last_update_date=sysdate where idoc_id = ?";
			statement = dbConn.prepareStatement(update_sql);
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
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
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
		if (idocId < 1 || controlNode == null)
			return -1;
		String idoctyp = getChildNodeText(controlNode, IDocFile.IDOCTYP_NODE);
		String cimtyp = getChildNodeText(controlNode, IDocFile.CIMTYP_NODE);
		String templateCode = getTemplateCode(idoctyp, cimtyp);
		String get_interface_header_sql = "select FND_INTERFACE_HEADERS_s.nextval from dual";
		Statement statement = null;
		PreparedStatement pstatement = null;
		ResultSet rs = null;
		int header_id = -1;
		try {
			statement = dbConn.createStatement();
			rs = statement.executeQuery(get_interface_header_sql);
			if (rs.next()) {
				header_id = rs.getInt(1);
			} else {
				throw new ApplicationException("execute sql:" + get_interface_header_sql + " failed.");
			}
			rs.close();
			statement.close();
			String insert_sql = "insert into FND_INTERFACE_HEADERS(HEADER_ID,TEMPLATE_CODE,ATTRIBUTE_1,CREATED_BY,CREATION_DATE,LAST_UPDATED_BY,LAST_UPDATE_DATE)"
					+ " values(?,?,?,0,sysdate,0,sysdate)";
			pstatement = dbConn.prepareStatement(insert_sql);
			pstatement.setInt(1, header_id);
			pstatement.setString(2, templateCode);
			pstatement.setString(3, String.valueOf(idocId));
			pstatement.executeUpdate();
			pstatement.close();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (statement != null) {
				statement.close();
			}
			if(pstatement != null){
				pstatement.close();
			}
		}
		return header_id;

	}
	public String getTemplateCode(String idoctyp, String cimtyp) throws SQLException, ApplicationException {
		// log("getTemplateCode where  idoctyp:" + idoctyp + " cimtyp:" +
		// cimtyp);
		StringBuffer query_sql = new StringBuffer("select TEMPLATE_CODE from FND_SAP_IDOC_TEMPLATES where IDOCTYP=? ");
		if (cimtyp != null)
			query_sql.append(" and CIMTYP=?");
		PreparedStatement statement = null;
		ResultSet rs = null;
		String templateCode = null;
		try {
			statement = dbConn.prepareStatement(query_sql.toString());
			statement.setString(1, idoctyp);
			if (cimtyp != null)
				statement.setString(2, cimtyp);
			rs = statement.executeQuery();
			if (rs.next()) {
				templateCode = rs.getString(1);
			} else {
				throw new ApplicationException("IDOCTYP:" + idoctyp + " CIMTYP:" + cimtyp
						+ " execute sql:" + query_sql.toString() + " failed.");
			}
			rs.close();
			statement.close();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		return templateCode;
	}
	public IdocType getIdocType(CompositeMap controlNode) {
		String idoctyp = getChildNodeText(controlNode, IDocFile.IDOCTYP_NODE);
		String cimtyp = getChildNodeText(controlNode, IDocFile.CIMTYP_NODE);
		return new IdocType(idoctyp, cimtyp);
	}

	public String getHandleModel(String idoctyp, String cimtyp) throws SQLException, ApplicationException {
		// log("getHandleModel where  idoctyp:" + idoctyp + " cimtyp:" +
		// cimtyp);
		StringBuffer query_sql = new StringBuffer("select HANDLE_MODEL from FND_SAP_IDOC_TEMPLATES where IDOCTYP=? ");
		if (cimtyp != null)
			query_sql.append(" and CIMTYP=?");
		PreparedStatement statement = null;
		ResultSet rs = null;
		String handleModel = null;
		try {
			statement = dbConn.prepareStatement(query_sql.toString());
			statement.setString(1, idoctyp);
			if (cimtyp != null)
				statement.setString(2, cimtyp);
			rs = statement.executeQuery();
			if (rs.next()) {
				handleModel = rs.getString(1);
			} else {
				throw new ApplicationException("IDOCTYP:" + idoctyp + " CIMTYP:" + cimtyp
						+ " execute sql:" + query_sql.toString() + " failed.");
			}
			rs.close();
			statement.close();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		return handleModel;
	}
	public void registerInterfaceLine(int headerId, CompositeMap contentNode) throws SQLException, ApplicationException {
		// log("registerInterfaceLine where  headerId:" + headerId);
		if (headerId < 1 || contentNode == null)
			return;
		handleContentNode(headerId, 0, contentNode);
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
			if(!isSegment(child)){
				insert_sql.append(",ATTRIBUTE_" + (getFieldIndex(node.getName(), child.getName())));
				values_sql.append(",?");
			}
		}
		insert_sql.append(")").append(values_sql).append(")");
		PreparedStatement statement = null;
		try {
			statement = dbConn.prepareStatement(insert_sql.toString());
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
				if(!isSegment(child)){
					statement.setString(index++, child.getText());
				}
			}
			statement.executeUpdate();
			statement.close();
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}
	private boolean isSegment(CompositeMap node){
		if(node == null)
			return false;
		String attribute = "SEGMENT";
		if(node.getString(attribute) != null){
			return true;
		}
		return false;
	}
	public int getLineId() throws SQLException, ApplicationException {
		// log("getLineId ");
		String query_sql = "select FND_INTERFACE_LINES_s.nextval from dual";
		Statement statement = null;
		ResultSet rs = null;
		int lineId = -1;
		try {
			statement = dbConn.createStatement();
			rs = statement.executeQuery(query_sql);
			if (rs.next()) {
				lineId = rs.getInt(1);
			} else {
				throw new ApplicationException(" execute sql:" + query_sql + " failed.");
			}
			rs.close();
			statement.close();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		return lineId;
	}
	public void updateIdocStatus(int headerId, int idocId, String status) throws SQLException {
		String header_update_sql = "update FND_INTERFACE_HEADERS t set t.status=? where t.header_id =?";
		String idoc_update_sql = "update fnd_sap_idocs t set t.handled_status=? where t.idoc_id =?";
		PreparedStatement statement = null;
		try {
			statement = dbConn.prepareStatement(header_update_sql);
			statement.setString(1, status);
			statement.setInt(2, headerId);
			statement.executeUpdate();
			statement.close();
			statement = dbConn.prepareStatement(idoc_update_sql);
			statement.setString(1, status);
			statement.setInt(2, idocId);
			statement.executeUpdate();
			statement.close();
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}
	public void updateIdocsStatus(int idocId, String message) throws SQLException {
		String idoc_update_sql = "update fnd_sap_idocs t set t.handled_status=? where t.idoc_id =?";
		PreparedStatement statement = null;
		try {
			statement = dbConn.prepareStatement(idoc_update_sql);
			statement.setString(1, message);
			statement.setInt(2, idocId);
			statement.executeUpdate();
			statement.close();
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}
	public String getMiddleExecutePkg(int idocId) throws SQLException, ApplicationException {
		// log("getMiddleExecutePkg from idocId:" + idocId);
		if (idocId < 1)
			return null;
		String query_sql = "select i.idoctyp,i.cimtyp from fnd_sap_idocs i where i.idoc_id = " + idocId;
		Statement statement = null;
		ResultSet rs = null;
		String templateCode = null;
		try {
			statement = dbConn.createStatement();
			rs = statement.executeQuery(query_sql);
			String idoctyp = null;
			String cimtyp = null;
			if (rs.next()) {
				idoctyp = rs.getString(1);
				cimtyp = rs.getString(2);
			} else {
				throw new ApplicationException("execute sql:" + query_sql + " failed!");
			}
			rs.close();
			statement.close();
			templateCode = getTemplateCode(idoctyp, cimtyp);
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		return getMiddleExecutePkg(templateCode);
	}
	public String getMiddleExecutePkg(String template_code) throws SQLException, ApplicationException {
		// log("getMiddleExecutePkg from template_code:" + template_code);
		String query_sql = "select execute_pkg from fnd_interface_templates where enabled_flag='Y' and template_code='"
				+ template_code + "'";
		Statement statement = null;
		ResultSet rs = null;
		String executePkg = null;
		try {
			statement = dbConn.createStatement();
			rs = statement.executeQuery(query_sql);

			if (rs.next()) {
				executePkg = rs.getString(1);
			} else {
				throw new ApplicationException("execute sql:" + query_sql + " failed!");
			}
			rs.close();
			statement.close();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		return executePkg;
	}
	public String getFormalExecutePkg(int idocId) throws SQLException, ApplicationException {
		// log("getFormalExecutePkg from idocId:" + idocId);
		if (idocId < 1)
			return null;
		String query_sql = "select i.idoctyp,i.cimtyp from fnd_sap_idocs i where i.idoc_id = " + idocId;
		Statement statement = null;
		ResultSet rs = null;
		String idoctyp = null;
		String cimtyp = null;
		try {
			statement = dbConn.createStatement();
			rs = statement.executeQuery(query_sql);
			if (rs.next()) {
				idoctyp = rs.getString(1);
				cimtyp = rs.getString(2);
			} else {
				throw new ApplicationException("execute sql:" + query_sql + " failed!");
			}
			rs.close();
			statement.close();
		} finally {
			if(rs != null){
				rs.close();
			}
			if(statement != null){
				statement.close();
			}
		}
		return getFormalExecutePkg(idoctyp, cimtyp);
	}
	public String getFormalExecutePkg(String idoctyp, String cimtyp) throws SQLException, ApplicationException {
		// log("getFormalExecutePkg where  idoctyp:" + idoctyp + " cimtyp:" +
		// cimtyp);
		StringBuffer query_sql = new StringBuffer("select execute_pkg from fnd_sap_idoc_transactions where IDOCTYP=? ");
		if (cimtyp != null)
			query_sql.append(" and CIMTYP=?");
		PreparedStatement statement = null;
		ResultSet rs = null;
		String executePkg = null;
		try {
			statement = dbConn.prepareStatement(query_sql.toString());
			statement.setString(1, idoctyp);
			if (cimtyp != null)
				statement.setString(2, cimtyp);
			rs = statement.executeQuery();
			if (rs.next()) {
				executePkg = rs.getString(1);
			} else {
				throw new ApplicationException("IDOCTYP:" + idoctyp + " CIMTYP:" + cimtyp
						+ " execute sql:" + query_sql.toString() + " failed.");
			}
			rs.close();
			statement.close();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		return executePkg;
	}
	public String executePkg(String executePkg, int headerId) throws SQLException {
		String errorMessage = null;
		try {
			dbConn.setAutoCommit(false);
			CallableStatement proc = dbConn.prepareCall("{call ? := " + executePkg + "(?)}");
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
		} finally {
			dbConn.rollback();
			dbConn.setAutoCommit(true);
		}
		return errorMessage;

	}
	public void stopSapServers(int serverId) throws SQLException {
		String delete_sql = "update fnd_sap_servers s set s.status='Error occurred:please check the console or log for details.',last_update_date=sysdate where s.server_id="
				+ serverId;
		Statement statement = null;
		try {
			statement = dbConn.createStatement();
			statement.executeUpdate(delete_sql);
			statement.close();
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}
	public int getFieldIndex(String segmenttyp, String fieldname) throws SQLException, ApplicationException {
		// log("getFieldIndex from segmenttyp:" + segmenttyp + " fieldname:" +
		// fieldname);
		String get_field_Index_sql = "select t.field_index from fnd_sap_fields t where t.segmenttyp ='" + segmenttyp
				+ "' and t.fieldname='" + fieldname + "'";
		Statement statement = null;
		ResultSet rs = null;
		int fieldIndex = -1;
		try {
			statement = dbConn.createStatement();
			rs = statement.executeQuery(get_field_Index_sql);
			if (rs.next()) {
				fieldIndex = rs.getInt(1);
			} else {
				throw new ApplicationException(" execute sql:" + get_field_Index_sql + " failed.");
			}
			rs.close();
			statement.close();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		return fieldIndex;
	}
	public void getHistoryIdocs(String program_id, List idocList) throws SQLException, ApplicationException {
		String get_HistoryIdocs_sql = "select i.idoc_id, i.server_id, i.file_path  from "
				+ " fnd_sap_idocs i, fnd_sap_servers s  where (i.handled_status is null or i.handled_status<>'done') "
				+ " and i.server_id = s.server_id" + " and s.program_id='" + program_id + "' order by i.idoc_id";
		Statement statement = null;
		ResultSet rs = null;
		try {
			statement = dbConn.createStatement();
			rs = statement.executeQuery(get_HistoryIdocs_sql);
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
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (statement != null) {
				statement.close();
			}
		}

	}
	public int existHeaders(int idoc_id) throws SQLException {
		String get_field_Index_sql = "select t.header_id " + " from fnd_interface_headers t, fnd_sap_idocs i "
				+ " where t.attribute_1 = i.idoc_id and i.idoc_id = " + idoc_id;
		Statement statement = null;
		ResultSet rs = null;
		int server_id = -1;
		try {
			statement = dbConn.createStatement();
			rs = statement.executeQuery(get_field_Index_sql);
			if (rs.next()) {
				server_id = rs.getInt(1);
			}
			rs.close();
			statement.close();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		return server_id;
	}

	public Connection getConnection() {
		return dbConn;
	}
	public void dispose() throws SQLException {
		if (dbConn != null) {
			dbConn.close();
		}
	}
}

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
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import uncertain.composite.CompositeMap;

import com.sap.conn.idoc.jco.JCoIDocServer;

public class DataBaseUtil {
	private Connection dbConn;
	public DataBaseUtil(Connection dbConn) throws AuroraIDocException {
		this.dbConn = dbConn;
	}
	public int registerSapServers(JCoIDocServer server) throws AuroraIDocException {
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
		} catch (SQLException e) {
			throw createAuroraIDocException(statement, e);
		} finally {
			closeResultSet(rs);
			closeStatement(statement);
		}
		return server_id;
	}

	public void unRegisterSapServers(int serverId) throws AuroraIDocException {
		String delete_sql = "delete from fnd_sap_servers s where s.server_id=" + serverId;
		Statement statement = null;
		try {
			statement = dbConn.createStatement();
			statement.executeUpdate(delete_sql);
			statement.close();
		} catch (SQLException e) {
			throw createAuroraIDocException(statement, e);
		} finally {
			closeStatement(statement);
		}
	}

	public int addIdoc(int serverId, String filePath) throws SQLException, AuroraIDocException {
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
				throw new AuroraIDocException("execute sql:" + get_idoc_id_sql + " failed.");
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
		} catch (SQLException e) {
			if (pStatement != null)
				throw new AuroraIDocException("execute sql:" + pStatement.toString() + " failed.", e);
			else if (statement != null) {
				throw new AuroraIDocException("execute sql:" + statement.toString() + " failed.", e);
			}
			throw new AuroraIDocException(e);
		} finally {
			closeResultSet(rs);
			closeStatement(statement);
			closeStatement(pStatement);
		}
		return idoc_id;
	}

	public void updateIdocInfo(int idocId, CompositeMap control_node) throws AuroraIDocException {
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
		} catch (SQLException e) {
			throw createAuroraIDocException(statement, e);
		} finally {
			closeStatement(statement);
		}
	}

	private String getSegmentFieldValue(CompositeMap node, String segment, String filed) {
		CompositeMap parentSegment = getParentSegment(node, segment);
		if (parentSegment != null) {
			return getChildNodeText(parentSegment, filed);
		}
		return null;
	}

	private String getChildNodeText(CompositeMap node, String childName) {
		if (node == null || childName == null)
			return null;
		CompositeMap childNode = node.getChild(childName);
		if (childNode == null)
			return null;
		return childNode.getText();
	}

	private CompositeMap getParentSegment(CompositeMap node, String segment) {
		if (node == null || segment == null)
			return null;
		if (segment.equals(node.getName())) {
			return node;
		}
		CompositeMap parentNode = node.getParent();
		if (parentNode == null)
			return null;
		return getParentSegment(parentNode, segment);
	}

	public int registerInterfaceHeader(int idocId, CompositeMap controlNode) throws AuroraIDocException {
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
				throw new AuroraIDocException("execute sql:" + get_interface_header_sql + " failed.");
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
		} catch (SQLException e) {
			throw createAuroraIDocException(new Statement[] { pstatement, statement }, e);
		} finally {
			closeResultSet(rs);
			closeStatement(statement);
			closeStatement(pstatement);
		}
		return header_id;

	}

	public String getTemplateCode(String idoctyp, String cimtyp) throws AuroraIDocException {
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
				throw new AuroraIDocException("IDOCTYP:" + idoctyp + " CIMTYP:" + cimtyp + " execute sql:"
						+ query_sql.toString() + " failed.");
			}
			rs.close();
			statement.close();
		} catch (SQLException e) {
			throw createAuroraIDocException(statement, e);
		} finally {
			closeResultSet(rs);
			closeStatement(statement);
		}
		return templateCode;
	}

	public IDocType getIdocType(CompositeMap controlNode) {
		String idoctyp = getChildNodeText(controlNode, IDocFile.IDOCTYP_NODE);
		String cimtyp = getChildNodeText(controlNode, IDocFile.CIMTYP_NODE);
		return new IDocType(idoctyp, cimtyp);
	}

	public String getHandleModel(String idoctyp, String cimtyp) throws AuroraIDocException {
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
				throw new AuroraIDocException("IDOCTYP:" + idoctyp + " CIMTYP:" + cimtyp + " execute sql:"
						+ query_sql.toString() + " failed.");
			}
			rs.close();
			statement.close();
		} catch (SQLException e) {
			throw createAuroraIDocException(statement, e);
		} finally {
			closeResultSet(rs);
			closeStatement(statement);
		}
		return handleModel;
	}

	public void registerMiddleLine(int headerId, CompositeMap contentNode) throws AuroraIDocException {
		if (headerId < 1 || contentNode == null)
			return;
		insertIntoMiddleTable(headerId, contentNode);
	}

	private void insertIntoMiddleTable(int headerId, CompositeMap node) throws AuroraIDocException {
		PreparedStatement segmentMapsSt = null;
		PreparedStatement fieldMapsSt = null;
		PreparedStatement insertSt = null;
		ResultSet segmentMapsRs = null;
		ResultSet filedMapsRs = null;
		String segment = node.getName();
		String segmentMapsSQL = "select t.header_id, t.table_name from fnd_sap_segment_maps t where t.segment_name = ?";
		String fieldMapsSQL = "select t.segment_name,t.segment_field,t.table_field from fnd_sap_field_maps t where t.header_id = ?";
		try {
			segmentMapsSt = dbConn.prepareStatement(segmentMapsSQL);
			segmentMapsSt.setString(1, segment);
			segmentMapsRs = segmentMapsSt.executeQuery();
			while (segmentMapsRs.next()) {
				int mapHeaderId = segmentMapsRs.getInt(1);
				String tableName = segmentMapsRs.getString(2);
				fieldMapsSt = dbConn.prepareStatement(fieldMapsSQL);
				fieldMapsSt.setInt(1, mapHeaderId);
				filedMapsRs = fieldMapsSt.executeQuery();
				StringBuffer insert_sql = new StringBuffer("insert into " + tableName
						+ " (BATCH_ID,CREATED_BY,CREATION_DATE,LAST_UPDATED_BY,LAST_UPDATE_DATE");
				StringBuffer values_sql = new StringBuffer("values(" + headerId + ",0,sysdate,0,sysdate");
				List values = new LinkedList();
				while (filedMapsRs.next()) {
					String segmentName = filedMapsRs.getString(1);
					String segmentField = filedMapsRs.getString(2);
					String tableField = filedMapsRs.getString(3);
					insert_sql.append("," + tableField);
					String value = getSegmentFieldValue(node, segmentName, segmentField);
//					values_sql.append(",'" + (value != null ? value : "") + "'");
					values_sql.append(",?");
					values.add(value != null ? value : "");
				}
				insert_sql.append(")").append(values_sql).append(")");
				try {
					insertSt = dbConn.prepareStatement(insert_sql.toString());
					int i=1;
					for(Iterator it = values.iterator();it.hasNext();i++){
						insertSt.setString(i, (String)it.next());
					}
					insertSt.executeUpdate();
				} catch (Throwable e) {
					throw new AuroraIDocException("execute sql:" + insert_sql.toString() + " in insertIntoMiddleTable",
							e);
				}
				insertSt.close();
			}
		} catch (SQLException e) {
			throw createAuroraIDocException(new Statement[] { insertSt, fieldMapsSt, segmentMapsSt }, e);
		} finally {
			closeResultSet(segmentMapsRs);
			closeStatement(segmentMapsSt);
			closeResultSet(filedMapsRs);
			closeStatement(fieldMapsSt);
			closeStatement(insertSt);
		}
		for (int i = 0; i < node.getChilds().size(); i++) {
			CompositeMap child = (CompositeMap) node.getChilds().get(i);
			if (child.getChilds() != null && child.getChilds().size() > 0) {
				insertIntoMiddleTable(headerId, child);
			}
		}
	}

	public void registerInterfaceLine(int headerId, CompositeMap contentNode) throws AuroraIDocException {
		if (headerId < 1 || contentNode == null)
			return;
		handleContentNode(headerId, 0, contentNode);
	}

	private void handleContentNode(int headerId, int parent_id, CompositeMap node) throws AuroraIDocException {
		if(!isSegmentDefined(node.getName()))
			return;
		StringBuffer insert_sql = new StringBuffer(
				"insert into FND_INTERFACE_LINES(LINE_ID,LINE_NUMBER,HEADER_ID,CREATED_BY,CREATION_DATE,LAST_UPDATED_BY,LAST_UPDATE_DATE,"
						+ " SOURCE_TABLE,PARENT_LINE_ID");
		int line_id = getLineId();
		StringBuffer values_sql = new StringBuffer("values(?,?,?,0,sysdate,0,sysdate,?,?");
		int index = 1;
		for (int i = 0; i < node.getChilds().size(); i++) {
			CompositeMap child = (CompositeMap) node.getChilds().get(i);
			if (child.getChilds() != null && child.getChilds().size() > 0) {
				handleContentNode(headerId, line_id, child);
				continue;
			}
			if (!isSegment(child)) {
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
			statement.setInt(index++, line_id);
			statement.setInt(index++, headerId);
			statement.setString(index++, node.getName());
			statement.setInt(index++, parent_id);
			for (Iterator it = node.getChildIterator(); it.hasNext();) {
				CompositeMap child = (CompositeMap) it.next();
				if (child.getChilds() != null && child.getChilds().size() > 0) {
					continue;
				}
				if (!isSegment(child)) {
					statement.setString(index++, child.getText());
				}
			}
			statement.executeUpdate();
			statement.close();
		} catch (Throwable e) {
			throw new AuroraIDocException("execute sql:" + insert_sql.toString() + " in handleContentNode", e);
		} finally {
			closeStatement(statement);
		}
	}
	public boolean isSegmentDefined(String segment) throws AuroraIDocException{
		if(segment == null)
			return false;
		StringBuffer query_sql = new StringBuffer("select 1 from fnd_sap_segments t where t.SEGMENTTYP=? ");
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			statement = dbConn.prepareStatement(query_sql.toString());
			statement.setString(1, segment);
			rs = statement.executeQuery();
			if (rs.next()) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			throw createAuroraIDocException(statement, e);
		} finally {
			closeResultSet(rs);
			closeStatement(statement);
		}
	}

	private boolean isSegment(CompositeMap node) {
		if (node == null)
			return false;
		String attribute = "SEGMENT";
		if (node.getString(attribute) != null) {
			return true;
		}
		return false;
	}

	public int getLineId() throws AuroraIDocException {
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
				throw new AuroraIDocException(" execute sql:" + query_sql + " failed.");
			}
			rs.close();
			statement.close();
		} catch (SQLException e) {
			throw new AuroraIDocException("execute sql:" + query_sql.toString() + " in handleContentNode", e);
		} finally {
			closeResultSet(rs);
			closeStatement(statement);
		}
		return lineId;
	}

	public void updateIdocStatus(int headerId, int idocId, String status) throws AuroraIDocException {
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
		} catch (SQLException e) {
			throw createAuroraIDocException(statement, e);
		} finally {
			closeStatement(statement);
		}
	}

	public void updateIdocsStatus(int idocId, String message) throws AuroraIDocException {
		String idoc_update_sql = "update fnd_sap_idocs t set t.handled_status=? where t.idoc_id =?";
		PreparedStatement statement = null;
		try {
			statement = dbConn.prepareStatement(idoc_update_sql);
			statement.setString(1, message);
			statement.setInt(2, idocId);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			throw createAuroraIDocException(statement, e);
		} finally {
			closeStatement(statement);
		}
	}

	public String getMiddleExecutePkg(int idocId) throws AuroraIDocException {
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
				throw new AuroraIDocException("execute sql:" + query_sql + " failed!");
			}
			rs.close();
			statement.close();
			templateCode = getTemplateCode(idoctyp, cimtyp);
		} catch (SQLException e) {
			throw createAuroraIDocException(query_sql, e);
		} finally {
			closeResultSet(rs);
			closeStatement(statement);
		}
		return getMiddleExecutePkg(templateCode);
	}

	public String getMiddleExecutePkg(String template_code) throws AuroraIDocException {
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
				throw new AuroraIDocException("execute sql:" + query_sql + " failed!");
			}
			rs.close();
			statement.close();
		} catch (SQLException e) {
			throw createAuroraIDocException(query_sql, e);
		} finally {
			closeResultSet(rs);
			closeStatement(statement);
		}
		return executePkg;
	}

	public String getFormalExecutePkg(int idocId) throws AuroraIDocException {
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
				throw new AuroraIDocException("execute sql:" + query_sql + " failed!");
			}
			rs.close();
			statement.close();
		} catch (SQLException e) {
			throw createAuroraIDocException(query_sql, e);
		} finally {
			closeResultSet(rs);
			closeStatement(statement);
		}
		return getFormalExecutePkg(idoctyp, cimtyp);
	}

	public String getFormalExecutePkg(String idoctyp, String cimtyp) throws AuroraIDocException {
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
				throw new AuroraIDocException("IDOCTYP:" + idoctyp + " CIMTYP:" + cimtyp + " execute sql:"
						+ query_sql.toString() + " failed.");
			}
			rs.close();
			statement.close();
		} catch (SQLException e) {
			throw createAuroraIDocException(statement, e);
		} finally {
			closeResultSet(rs);
			closeStatement(statement);
		}
		return executePkg;
	}

	public String executePkg(String executePkg, int headerId) throws AuroraIDocException {
		String errorMessage = null;
		CallableStatement proc = null;
		try {
			dbConn.setAutoCommit(false);
			proc = dbConn.prepareCall("{call ? := " + executePkg + "(?)}");
			proc.registerOutParameter(1, Types.VARCHAR);
			proc.setInt(2, headerId);
			proc.execute();
			errorMessage = proc.getString(1);
			if (errorMessage == null || "".equals(errorMessage)) {
				dbConn.commit();
			} else {
				dbConn.rollback();
			}
			proc.close();
			dbConn.setAutoCommit(true);
		} catch (SQLException e) {
			throw createAuroraIDocException(proc, e);
		} finally {
			closeStatement(proc);
			rollbackConnection();
			setConnectionAutoCommit(true);
		}
		return errorMessage;

	}

	public void stopSapServers(int serverId) throws AuroraIDocException {
		String delete_sql = "update fnd_sap_servers s set s.status='Error occurred:please check the console or log for details.',last_update_date=sysdate where s.server_id="
				+ serverId;
		Statement statement = null;
		try {
			statement = dbConn.createStatement();
			statement.executeUpdate(delete_sql);
			statement.close();
		} catch (SQLException e) {
			throw createAuroraIDocException(delete_sql, e);
		} finally {
			closeStatement(statement);
		}
	}

	public int getFieldIndex(String segmenttyp, String fieldname) throws AuroraIDocException {
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
				throw new AuroraIDocException(" execute sql:" + get_field_Index_sql + " failed.");
			}
			rs.close();
			statement.close();
		} catch (SQLException e) {
			throw createAuroraIDocException(get_field_Index_sql, e);
		}finally {
			closeResultSet(rs);
			closeStatement(statement);
		}
		return fieldIndex;
	}

	public void getHistoryIdocs(String program_id, List idocList) throws AuroraIDocException {
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
					throw new AuroraIDocException("file :" + file.getAbsolutePath() + " is not exits");
				}
				idocList.add(new IDocFile(file_path, idoc_id, server_id));
			}
			rs.close();
			statement.close();
		} catch (SQLException e) {
			throw createAuroraIDocException(get_HistoryIdocs_sql, e);
		}finally {
			closeResultSet(rs);
			closeStatement(statement);
		}

	}

	public int existHeaders(int idoc_id) throws AuroraIDocException {
		String get_field_Index_sql = "select t.header_id " + " from fnd_interface_headers t, fnd_sap_idocs i "
				+ " where t.attribute_1 = i.idoc_id and i.idoc_id = " + idoc_id;
		Statement statement = null;
		ResultSet rs = null;
		int header_id = -1;
		try {
			statement = dbConn.createStatement();
			rs = statement.executeQuery(get_field_Index_sql);
			if (rs.next()) {
				header_id = rs.getInt(1);
			}
			rs.close();
			statement.close();
		} catch (SQLException e) {
			throw createAuroraIDocException(get_field_Index_sql, e);
		}finally {
			closeResultSet(rs);
			closeStatement(statement);
		}
		return header_id;
	}

	public Connection getConnection() {
		return dbConn;
	}

	public void dispose(){
		closeConnection(dbConn);
	}

	private AuroraIDocException createAuroraIDocException(String sql, SQLException e) {
		if (sql != null)
			return new AuroraIDocException("execute sql:" + sql + " failed.", e);
		return new AuroraIDocException(e);
	}

	private AuroraIDocException createAuroraIDocException(Statement statement, SQLException e) {
		if (statement != null)
			return new AuroraIDocException("execute sql:" + statement.toString() + " failed.", e);
		return new AuroraIDocException(e);
	}

	private AuroraIDocException createAuroraIDocException(Statement[] statements, SQLException e) {
		if (statements != null) {
			for (int i = 0; i < statements.length; i++) {
				Statement statement = statements[i];
				if (statement != null)
					return new AuroraIDocException("execute sql:" + statement.toString() + " failed.", e);
			}
		}
		return new AuroraIDocException(e);
	}

	public void closeConnection(Connection conn) {
		if (conn == null)
			return;
		try {
			conn.close();
		} catch (SQLException ex) {
			LoggerUtil.getLogger().log(Level.SEVERE, "", ex);
		}
	}

	public void rollbackConnection() {
		if (dbConn == null)
			return;
		try {
			dbConn.rollback();
		} catch (SQLException ex) {
			LoggerUtil.getLogger().log(Level.SEVERE, "", ex);
		}
	}

	public void setConnectionAutoCommit(boolean autoCommit) {
		if (dbConn== null)
			return;
		try {
			dbConn.setAutoCommit(autoCommit);
		} catch (SQLException ex) {
			LoggerUtil.getLogger().log(Level.SEVERE, "", ex);
		}
	}

	public void closeResultSet(ResultSet rs) {
		if (rs == null)
			return;
		try {
			rs.close();
		} catch (SQLException ex) {
			LoggerUtil.getLogger().log(Level.SEVERE, "", ex);
		}
	}

	public void closeStatement(Statement stmt) {
		if (stmt == null)
			return;
		try {
			stmt.close();
		} catch (SQLException ex) {
			LoggerUtil.getLogger().log(Level.SEVERE, "", ex);
		}
	}
}

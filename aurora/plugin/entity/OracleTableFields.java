package aurora.plugin.entity;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

import uncertain.composite.CompositeMap;
import aurora.database.service.DatabaseServiceFactory;
import aurora.plugin.entity.model.DataType;

public class OracleTableFields {
	private final String[] excluedColumns = { "CREATED_BY", "CREATION_DATE",
			"LAST_UPDATED_BY", "LAST_UPDATE_DATE" };
	private static String test_table_sql = "select 1 from user_objects where object_name=? and (object_type='TABLE' or object_type='VIEW')";
	private static String column_infos_sql = "select c.comments, t.column_name, t.data_type, t.data_scale"
			+ "  from user_tab_columns t, all_col_comments c"
			+ " where t.column_name = c.column_name"
			+ "   and t.table_name = c.table_name"
			+ "   and c.table_name = ?"
			+ "   and c.owner = ?";

	public CompositeMap getTableFields(DatabaseServiceFactory dsf,
			String tableName) throws Exception {
		CompositeMap fMap = new CompositeMap("fields");
		Connection conn = dsf.getDataSource().getConnection();

		try {
			DatabaseMetaData metaData = conn.getMetaData();
			tableName = tableName.toUpperCase();
			String userName = metaData.getUserName();
			if (!isTableExists(tableName, userName, conn)) {
				fMap.put("msg", "TABLE_NOT_EXISTS");
				return fMap;
			}
			// ResultSet rs = metaData.getColumns(null, userName, tableName,
			// "%");
			ResultSet pkRs = metaData.getPrimaryKeys(null, userName, tableName);
			String pkName = null;
			if (pkRs.next()) {
				pkName = pkRs.getString("COLUMN_NAME");
				if (pkRs.next()) {
					fMap.put("msg", "PK_NOT_UNIQUE");
					return fMap;
				}
			} else {
				fMap.put("msg", "PK_NOT_EXISTS");
				return fMap;
			}

			List<String> excluedColumnList = Arrays.asList(excluedColumns);
			PreparedStatement ps = conn.prepareStatement(column_infos_sql);
			ps.setString(1, tableName);
			ps.setString(2, userName);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String columnName = rs.getString("COLUMN_NAME");
				if (excluedColumnList.contains(columnName)) {
					continue;
				}
				CompositeMap element = fMap.createChild("record");
				if (columnName.equalsIgnoreCase(pkName)) {
					element.put("ispk", true);
				}
				element.put("name", columnName);
				element.put("prompt", rs.getString("COMMENTS"));
				String dbtype = rs.getString("DATA_TYPE");
				element.put("dbtype", dbtype);
				int precision = rs.getInt("DATA_SCALE");
				element.put("type", getDisplayType(dbtype, precision));
				element.put("javatype", getJavaType(dbtype, precision));
			}
			ps.close();
		} finally {
			conn.close();
		}
		return fMap;
	}

	private boolean isTableExists(String tableName, String userName,
			Connection conn) throws Exception {
		PreparedStatement ps = conn.prepareStatement(test_table_sql);
		ps.setString(1, tableName);
		ResultSet rs = ps.executeQuery();
		boolean res = rs.next();
		ps.close();
		return res;
	}

	private String getDisplayType(String dbType, int precision) {
		String disType = "";
		for (DataType dt : DataType.values()) {
			if (dt.getDbType().equalsIgnoreCase(dbType)) {
				disType = dt.getDisplayType();
				break;
			}
		}
		if (dbType.equals("NUMBER") && precision > 0) {
			disType = DataType.FLOAT.getDisplayType();
		}
		return disType.toUpperCase();
	}

	private String getJavaType(String dbType, int precision) {
		String javaType = "";
		for (DataType dt : DataType.values()) {
			if (dt.getDbType().equalsIgnoreCase(dbType)) {
				javaType = dt.getJavaType();
				break;
			}
		}
		if (dbType.equals("NUMBER") && precision > 0) {
			javaType = DataType.FLOAT.getJavaType();
		}
		return javaType;
	}
}

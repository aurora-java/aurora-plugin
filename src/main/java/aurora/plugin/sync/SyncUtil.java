package aurora.plugin.sync;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import aurora.database.service.DatabaseServiceFactory;
import aurora.database.service.SqlServiceContext;
import uncertain.ocm.IObjectRegistry;

public class SyncUtil {

	public static Connection getConnection(String dataSourceName,
			IObjectRegistry registry) throws SQLException {
		DatabaseServiceFactory databasefactory = (DatabaseServiceFactory) registry
				.getInstanceOfType(DatabaseServiceFactory.class);
		SqlServiceContext sqlServiceContext = databasefactory
				.createContextWithConnection();
		Connection conn = sqlServiceContext.getNamedConnection(dataSourceName);
		if (conn == null) {
			sqlServiceContext.initConnection(registry, dataSourceName);
			conn = sqlServiceContext.getNamedConnection(dataSourceName);
		}
		conn.setAutoCommit(false);
		return conn;
	}

	public static void freeConnection(Connection conn) {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
		}
	}

	public static String getLastSyncDate(Connection conn, String jobName)
			throws SQLException {
		final String getLastSyncDateSql = "hpm_sync_pkg.getLastUpdated(?,?)";
		CallableStatement cstm = null;
		String syncDate = null;
		try {
			cstm = conn.prepareCall("{call " + getLastSyncDateSql + "}");
			cstm.setString(1, jobName);
			cstm.registerOutParameter(2, java.sql.Types.VARCHAR);
			cstm.execute();
			syncDate = cstm.getString(2);
		} finally {
			if (cstm != null)
				cstm.close();
		}
		return syncDate;
	}

	public static void unlockSync(Connection conn, String jobName)
			throws SQLException {
		CallableStatement cstm = null;
		final String unlockSql = "hpm_sync_pkg.unlock_hpm_sync(?)";
		try {
			cstm = conn.prepareCall("{call " + unlockSql + "}");
			cstm.setString(1, jobName);
			cstm.execute();
		} finally {
			if (cstm != null)
				cstm.close();
		}
	}

	public static void syncSuccess(Connection conn, String jobName)
			throws SQLException {
		CallableStatement cstm = null;
		final String unlockSql = "hpm_sync_pkg.sync_success(?)";
		try {
			cstm = conn.prepareCall("{call " + unlockSql + "}");
			cstm.setString(1, jobName);
			cstm.execute();
		} finally {
			if (cstm != null)
				cstm.close();
		}
	}
}

package aurora.plugin.bgtcheck;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import aurora.database.DBUtil;
import aurora.database.FetchDescriptor;
import aurora.database.ResultSetLoader;
import aurora.database.rsconsumer.CompositeMapCreator;
import aurora.database.service.SqlServiceContext;
import aurora.service.ServiceThreadLocal;

import uncertain.composite.CompositeMap;
import uncertain.logging.ILogger;
import uncertain.ocm.IObjectRegistry;

public class DatabaseTool {

	protected IObjectRegistry mRegistry;
	protected ILogger logger;

	public DatabaseTool(IObjectRegistry registry, ILogger logger) {
		this.mRegistry = registry;
		this.logger = logger;
	}

	public static Connection getContextConnection(IObjectRegistry registry) throws SQLException {
		CompositeMap context = ServiceThreadLocal.getCurrentThreadContext();
		if (context == null)
			throw new IllegalStateException("Can not get context from ServiceThreadLocal!");
		SqlServiceContext sqlServiceContext = SqlServiceContext.createSqlServiceContext(context);
		Connection conn = sqlServiceContext.getNamedConnection(null);
		if (conn == null) {
			sqlServiceContext.initConnection(registry, null);
			conn = sqlServiceContext.getNamedConnection(null);
		}
		return conn;
	}

	public CompositeMap sqlQueryWithParas(IObjectRegistry registry, String prepareSQL, PrepareParameter[] prepareParameters) throws Exception {
		ResultSet resultSet = null;
		CompositeMap result = new CompositeMap("result");
		PreparedStatement st = null;
		try {
			Connection conn = getContextConnection(registry);
			st = conn.prepareStatement(prepareSQL);
			if (prepareParameters != null) {
				for (int i = 0; i < prepareParameters.length; i++) {
					PrepareParameter parameter = prepareParameters[i];
					parameter.getDataType().setParameter(st, i + 1, parameter.getValue());
				}
			}
			resultSet = st.executeQuery();
			ResultSetLoader mRsLoader = new ResultSetLoader();
			mRsLoader.setFieldNameCase(Character.LOWERCASE_LETTER);
			FetchDescriptor desc = FetchDescriptor.fetchAll();
			CompositeMapCreator compositeCreator = new CompositeMapCreator(result);
			mRsLoader.loadByResultSet(resultSet, desc, compositeCreator);
		} finally {
			DBUtil.closeStatement(st);
		}
		return result;
	}

	public boolean sqlExecuteWithParas(IObjectRegistry registry, String prepareSQL, PrepareParameter[] prepareParameters) throws SQLException {
		PreparedStatement st = null;
		boolean success = false;
		try {
			Connection conn = getContextConnection(registry);
			st = conn.prepareStatement(prepareSQL);
			if (prepareParameters != null) {
				for (int i = 0; i < prepareParameters.length; i++) {
					PrepareParameter parameter = prepareParameters[i];
					parameter.getDataType().setParameter(st, i + 1, parameter.getValue());
				}
			}
			success = st.execute();
		} finally {
			DBUtil.closeStatement(st);
		}
		return success;
	}
}

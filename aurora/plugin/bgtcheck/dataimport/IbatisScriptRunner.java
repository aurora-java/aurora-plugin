package aurora.plugin.bgtcheck.dataimport;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.Connection;
import java.util.logging.Level;

import org.apache.ibatis.jdbc.ScriptRunner;

import uncertain.core.DirectoryConfig;
import uncertain.core.UncertainEngine;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;
import aurora.datasource.DataSourceConfig;


public class IbatisScriptRunner {

	private IObjectRegistry mObjectRegistry;
	private Connection connection;

	private DataSourceConfig dsConfig;
	private ILogger logger;

	private final static String SCRIPTRUNNER_LOGDIRECTORY = "ScriptRunner";
	private String scriptRunnercLogDirectory;

	public IbatisScriptRunner(IObjectRegistry objectRegistry) throws Exception {
		mObjectRegistry = objectRegistry;
		connection = DatabaseTool.getContextConnection(mObjectRegistry);
		dsConfig = (DataSourceConfig) mObjectRegistry.getInstanceOfType(DataSourceConfig.class);
		if (dsConfig == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(null, DataSourceConfig.class, this.getClass().getCanonicalName());
		logger = LoggingContext.getLogger(this.getClass().getPackage().getName(), mObjectRegistry);
		Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
		initLogDirectory();
	}

	private void initLogDirectory() throws IOException {
		UncertainEngine ue = (UncertainEngine) mObjectRegistry.getInstanceOfType(UncertainEngine.class);
		if (ue == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(null, UncertainEngine.class, this.getClass().getCanonicalName());
		DirectoryConfig dc = ue.getDirectoryConfig();
		if (dc == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(null, DirectoryConfig.class, this.getClass().getCanonicalName());
		String logDirectory = dc.getLogDirectory();
		if (logDirectory == null)
			logDirectory = (new File(System.getProperty("java.io.tmpdir"))).getCanonicalPath();
		scriptRunnercLogDirectory = logDirectory + File.separator + SCRIPTRUNNER_LOGDIRECTORY;
		logger.info("scriptRunnercLogDirectory is " + scriptRunnercLogDirectory + ".");
	}

	public void executeSQL(String sqlFileFullPath) throws Exception {
		File sqlFile = new File(sqlFileFullPath);
		executeSQL(sqlFile);
	}

	public void executeSQL(File sqlFile) throws Exception {
		ScriptRunner runner = new ScriptRunner(connection);
		runner.setAutoCommit(false);
		runner.setStopOnError(true);
		// 日志
		File logFile = new File(scriptRunnercLogDirectory + File.separator + System.currentTimeMillis() + "_" + sqlFile.getName());
		if (!logFile.exists()) {
			File parentFile = logFile.getParentFile();
			if (!parentFile.exists())
				parentFile.mkdirs();
			logFile.createNewFile();
		}
		PrintWriter pw = new PrintWriter(logFile);
		runner.setErrorLogWriter(pw);
		runner.setLogWriter(null);
		FileReader fr = null;
		try {
			fr = new FileReader(sqlFile);
			runner.runScript(fr);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "sqlFile is " + sqlFile + ". Please check it.", e);
			throw new Exception(e);
		} finally {
			closeReader(fr);
			closeWriter(pw);
		}
	}

	private void closeReader(Reader reader) {
		if (reader != null)
			try {
				reader.close();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "", e);
			}
	}

	private void closeWriter(Writer writer) {
		if (writer != null)
			try {
				writer.close();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "", e);
			}
	}
}

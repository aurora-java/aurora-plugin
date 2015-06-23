package aurora.plugin.bgtcheck.dataimport;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;

public class DataImport extends AbstractEntry {

	private IObjectRegistry mRegistry;

	private ILogger logger;
	private IbatisScriptRunner scriptRunner;
	private DataBaseActions dbActions;

	private String sqlFileRootPath;

	public DataImport(IObjectRegistry registry) throws IOException {
		this.mRegistry = registry;
		logger = LoggingContext.getLogger(this.getClass().getPackage().getName(), mRegistry);

	}

	@Override
	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		scriptRunner = new IbatisScriptRunner(mRegistry);
		sqlFileRootPath = TextParser.parse(sqlFileRootPath, context);
		if (sqlFileRootPath == null || "".equals(sqlFileRootPath))
			throw BuiltinExceptionFactory.createAttributeMissing(this, "sqlFileRootPath");
		File rootDirectory = new File(sqlFileRootPath);
		if (!rootDirectory.exists())
			throw new IllegalArgumentException("sqlFileRootPath:" + sqlFileRootPath + " not exists!");
		if (!rootDirectory.isDirectory())
			throw new IllegalArgumentException("sqlFileRootPath:" + sqlFileRootPath + " is not directory!");
		dbActions = new DataBaseActions(mRegistry, logger);

		execute(0, rootDirectory);
	}

	private void execute(int parent_batch_id, File rootDirectory) throws Exception {
		ArrayList<File> sqlFileList = new ArrayList<File>();
		ArrayList<File> subDirList = new ArrayList<File>();
		filterDirectory(rootDirectory, sqlFileList, subDirList);
		// 如果目录下没有SQL文件或目录
		if (sqlFileList.size() == 0 && subDirList.size() == 0)
			return;
		int batch_id = generateBatchId(rootDirectory);
		dbActions.insert_td_batch_hierarchy(batch_id, parent_batch_id);
		handleSQLFile(batch_id, sqlFileList);
		handleSubDir(batch_id, subDirList);

	}

	private int generateBatchId(File directory) throws SQLException {
		if (!directory.isDirectory())
			throw new IllegalArgumentException("file:" + directory + " is not a directory!");
		String fileName = directory.getName();

		Pattern pattern = Pattern.compile("([\\w]*)(.*)");
		Matcher matcher = pattern.matcher(fileName);
		String fileCode = "";
		String fileDesc = "";

		if (matcher.find()) {
			fileCode = matcher.group(1);
			fileDesc = matcher.group(2);
		}
		int batch_id = dbActions.generateBatchId(fileCode, fileDesc, fileName);
		return batch_id;
	}

	private void handleSubDir(int batch_id, ArrayList<File> subDirList) throws Exception {
		for (File subDir : subDirList) {
			execute(batch_id, subDir);
		}

	}

	private void handleSQLFile(int batch_id, ArrayList<File> sqlFileList) throws Exception {
		for (File sqlFile : sqlFileList) {
			String fileName = sqlFile.getName();
			String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));

			Pattern pattern = Pattern.compile("([\\w]*)(.*)");
			Matcher matcher = pattern.matcher(fileNameWithoutExtension);
			String tableName = "";
			String fileDesc = "";

			if (matcher.find()) {
				tableName = matcher.group(1);
				fileDesc = matcher.group(2);
			}
			dbActions.insert_td_batch_tables(batch_id, tableName, fileDesc, fileNameWithoutExtension);
			dbActions.deleteTable(tableName);
			scriptRunner.executeSQL(sqlFile);
			dbActions.copyDataToTdTable(batch_id, tableName);
		}

	}

	private void filterDirectory(File rootDirectory, ArrayList<File> sqlFileList, ArrayList<File> subDirList) {
		File[] files = rootDirectory.listFiles();
		if (files == null)
			return;
		for (File file : files) {
			if (file.isDirectory()) {
				subDirList.add(file);
			} else if (file.getName().toLowerCase().endsWith(".sql")) {
				sqlFileList.add(file);
			}
		}
	}

	public String getSqlFileRootPath() {
		return sqlFileRootPath;
	}

	public void setSqlFileRootPath(String sqlFileRootPath) {
		this.sqlFileRootPath = sqlFileRootPath;
	}
}

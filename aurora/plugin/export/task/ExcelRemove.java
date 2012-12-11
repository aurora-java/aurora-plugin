package aurora.plugin.export.task;



import java.io.File;

import uncertain.composite.TextParser;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;

public class ExcelRemove extends AbstractEntry{
	public final static String EXECL_2003_EXTENSION = ".xls";
	public final static String EXECL_2007_EXTENSION = ".xlsx";
	
	public String fullPath;

	@Override
	public void run(ProcedureRunner runner) throws Exception {
		if(fullPath == null)
			throw BuiltinExceptionFactory.createAttributeMissing(this, "fullPath");
		fullPath = TextParser.parse(fullPath, runner.getContext());
		if (!fullPath.toLowerCase().endsWith(EXECL_2003_EXTENSION) && !fullPath.toLowerCase().endsWith(EXECL_2007_EXTENSION)) {
			throw new IllegalArgumentException("This file '" + fullPath + "' is not an excel file!");
		}
		ILogger logger = LoggingContext.getLogger(runner.getContext(),this.getClass().getCanonicalName());
		File file = new File(fullPath);
		if(!file.exists())
			logger.warning("This file '" + fullPath + "' is not exist!");
		boolean is_success = file.delete();
		if (!is_success) {
			logger.warning("This file '" + fullPath + "' can not be deleted!");
		}
	}
	public String getFullPath() {
		return fullPath;
	}
	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}
}

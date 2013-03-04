package aurora.plugin.export.task;

import java.io.File;

import uncertain.composite.TextParser;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;


//删除本地报表文件
public class ReportRemove extends AbstractEntry{

	public String fullPath;

	@Override
	public void run(ProcedureRunner runner) throws Exception {
		if(fullPath == null)
			throw BuiltinExceptionFactory.createAttributeMissing(this, "fullPath");
		fullPath = TextParser.parse(fullPath, runner.getContext());
		if (!validateFileExtension(fullPath)) {
			throw new IllegalArgumentException("This file '" + fullPath + "' is not an report file!");
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
	//just for excel
	private boolean validateFileExtension(String fileName){
		if(fileName == null)
			return false;
		return fileName.toLowerCase().endsWith(TaskReportServlet.EXECL_2003_EXTENSION) || fileName.toLowerCase().endsWith(TaskReportServlet.EXECL_2007_EXTENSION);
	}
	public String getFullPath() {
		return fullPath;
	}
	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}
}

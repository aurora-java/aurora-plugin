package aurora.plugin.export.task;

import uncertain.composite.CompositeMap;

public interface IReportTask {
	public String getReportDir();
	public CompositeMap getAccessChecker();
	public CompositeMap getReportTaskTemplate();
}

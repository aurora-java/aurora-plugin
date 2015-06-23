package aurora.plugin.export.task;

import uncertain.composite.CompositeMap;

//WEB-INF下report-task.config配置文件接口
public interface IReportTask {
	public String getReportDir();
	public CompositeMap getAccessChecker();
	public CompositeMap getReportTaskTemplate();
}

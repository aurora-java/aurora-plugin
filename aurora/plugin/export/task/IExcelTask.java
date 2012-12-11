package aurora.plugin.export.task;

import uncertain.composite.CompositeMap;

public interface IExcelTask {
	public String getExcelDir();
	public CompositeMap getAccessChecker();
	public CompositeMap getExcelTaskTemplate();
}

package aurora.plugin.export.task;

import uncertain.composite.CompositeMap;
import uncertain.ocm.IConfigurable;


public class ReportTask implements IReportTask,IConfigurable{
	private String reportDir;
	private CompositeMap accessPrivilegeConfig ;
	private CompositeMap reportTaskTemplate ;//send email or just record on database.
	
	public String getReportDir() {
		return reportDir;
	}

	public void setReportDir(String dir) {
		this.reportDir = dir;
	}

	public CompositeMap getAccessChecker() {
		return accessPrivilegeConfig;
	}
	
	public CompositeMap getReportTaskTemplate(){
		return reportTaskTemplate;
	}

	public void beginConfigure(CompositeMap config) {
		 accessPrivilegeConfig = config.getChild("procedure");
		 reportTaskTemplate = config.getChild("async-task");
	}
	
	public void endConfigure() {
		
	}

}

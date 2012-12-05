package aurora.plugin.export.task;

import uncertain.composite.CompositeMap;
import uncertain.ocm.IConfigurable;
import aurora.application.task.excel.IExcelTask;


public class ExcelTask implements IExcelTask,IConfigurable{
	private String svc;
	private String dir;
	private CompositeMap accessPrivilegeConfig ;
	//send email or just record on database.
	private CompositeMap postProcessConfig ;
	
	public String getSvc() {
		return svc;
	}

	public void setSvc(String svc) {
		this.svc = svc;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public CompositeMap getProcedure() {
		return accessPrivilegeConfig;
	}

	public void endConfigure() {
		
	}

	public void beginConfigure(CompositeMap config) {
		 accessPrivilegeConfig = config.getChild("procedure");
		 postProcessConfig = config.getChild("async-task");
	}
	public CompositeMap getPostProcessConfig(){
		return postProcessConfig;
	}
}

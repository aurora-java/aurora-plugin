package aurora.plugin.export.task;

import uncertain.composite.CompositeMap;
import uncertain.ocm.IConfigurable;


public class ExcelTask implements IExcelTask,IConfigurable{
	private String excelDir;
	private CompositeMap accessPrivilegeConfig ;
	private CompositeMap excelTaskTemplate ;//send email or just record on database.
	
	public String getExcelDir() {
		return excelDir;
	}

	public void setExcelDir(String dir) {
		this.excelDir = dir;
	}

	public CompositeMap getAccessChecker() {
		return accessPrivilegeConfig;
	}
	
	public CompositeMap getExcelTaskTemplate(){
		return excelTaskTemplate;
	}

	public void endConfigure() {
		
	}

	public void beginConfigure(CompositeMap config) {
		 accessPrivilegeConfig = config.getChild("procedure");
		 excelTaskTemplate = config.getChild("async-task");
	}

}

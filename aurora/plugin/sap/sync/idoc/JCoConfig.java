package aurora.plugin.sap.sync.idoc;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;

import aurora.plugin.sap.jco3.SapConfig;

public class JCoConfig extends SapConfig {
	
	private  String destinationName;
	
	public JCoConfig(String destinationName){
		this.destinationName = destinationName;
	}
	
	
	public JCoDestination getJCoDestination(String sid) throws Exception{
		if(sid == null || destinationName.equals(sid))
			return JCoDestinationManager.getDestination(destinationName);
		return null;
	}
}

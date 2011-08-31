package aurora.plugin.sap;

import java.util.HashMap;
import java.util.Map;

import uncertain.core.IGlobalInstance;

public class SapConfig implements IGlobalInstance{
	Map<String,InstanceConfig> sapInstanceMap=new HashMap<String,InstanceConfig>();
	InstanceConfig defaultSapInstance;

	public InstanceConfig getSapInstance(String sid){
		return sapInstanceMap.get(sid);
	}
	
	public InstanceConfig getSapInstance(){
		return defaultSapInstance;
	}
	
	public void addInstances(InstanceConfig[] instances) {		
		InstanceConfig instance;
		int l=instances.length;
		for(int i=0;i<l;i++){
			instance=instances[i];		
			sapInstanceMap.put(instance.sid, instance);
		}
		if(l==1)
			defaultSapInstance=instances[0];
	}
}

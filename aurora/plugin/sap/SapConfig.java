package aurora.plugin.sap;

import java.util.HashMap;
import java.util.Map;

import uncertain.core.IGlobalInstance;

public class SapConfig implements IGlobalInstance{
	Map sapInstanceMap=new HashMap();
	ISapInstance defaultSapInstance;
	public ISapInstance getSapInstance(String sid){
		return (ISapInstance)sapInstanceMap.get(sid);
	}
	
	public ISapInstance getSapInstance(){
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

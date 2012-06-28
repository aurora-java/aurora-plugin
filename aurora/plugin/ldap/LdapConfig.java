package aurora.plugin.ldap;

import java.util.HashMap;
import java.util.Map;

public class LdapConfig{
	
	Map<String,LdapServerInstance> ldapInstanceMap=new HashMap<String,LdapServerInstance>();
	public LdapServerInstance getSapInstance(String name){
		return ldapInstanceMap.get(name);
	}
	
	public void addInstances(LdapServerInstance[] instances) {		
		LdapServerInstance instance;
		int l=instances.length;
		for(int i=0;i<l;i++){
			instance=instances[i];		
			ldapInstanceMap.put(instance.getName(), instance);
		}		
	}
}

package aurora.plugin.ldap;

import java.util.LinkedList;
import java.util.List;

public class LdapConfig {

	List<LdapServerInstance> ldapInstanceList=null;
	public LdapConfig(){
		ldapInstanceList = new LinkedList<LdapServerInstance>();
	}
	public List<LdapServerInstance> getInstanceList() {
		return ldapInstanceList;
	}

	public void addInstances(LdapServerInstance[] instances) {
		LdapServerInstance instance;
		int l = instances.length;
		for (int i = 0; i < l; i++) {
			instance = instances[i];
			ldapInstanceList.add(instance);
		}
	}
}

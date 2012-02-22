package aurora.plugin.ntlm;

import java.util.Iterator;
import java.util.Set;

import jcifs.Config;
import uncertain.composite.CompositeMap;
import uncertain.ocm.IObjectRegistry;

public class NtlmConfig {
	public String procedure;
	public String returnPath;
	CompositeMap domainInstances;
	IObjectRegistry mObjectRegistry;
	boolean enableBasic=false;

	public NtlmConfig(IObjectRegistry registry) {
		mObjectRegistry = registry;
		Config.setProperty("jcifs.smb.client.soTimeout", "1800000");
		Config.setProperty("jcifs.netbios.cachePolicy", "1200");
		Config.setProperty("jcifs.smb.lmCompatibility", "0");
		Config.setProperty("jcifs.smb.client.useExtendedSecurity", "false");
	}

	public void onInitialize() {
		mObjectRegistry.registerInstance(NtlmConfig.class, this);
	}
	
	public CompositeMap getDomainInstances(){
		return domainInstances;
	}
	
	public DomainInstance getDomainInstance(String domain) {
		return (DomainInstance) domainInstances.get(domain);
	}
	
	public DomainInstance getDefaultDomainInstance(){
		Set keySet=domainInstances.keySet();
		Iterator iterator =keySet.iterator();
		if(keySet.size()==1){
			while(iterator.hasNext()){
				return (DomainInstance) domainInstances.get(iterator.next());
			}
		}
		return null;
	}

	public void addInstances(DomainInstance[] domainInstance) {
		domainInstances = new CompositeMap();
		int l = domainInstance.length;
		DomainInstance intance = null;
		for (int i = 0; i < l; i++) {
			intance = domainInstance[i];
			domainInstances.put(intance.getDomain().toUpperCase(), intance);
		}
	}

	public String getProcedure() {
		if(procedure==null)
			throw new RuntimeException("procedure is undefined");
		return procedure;
	}

	public void setProcedure(String procedure) {
		this.procedure = procedure;
	}

	public String getReturnPath() {
		if(returnPath==null)
			throw new RuntimeException("returnPath is undefined");
		return returnPath;
	}

	public void setReturnPath(String returnPath) {
		this.returnPath = returnPath;
	}

	public boolean getEnableBasic() {
		return enableBasic;
	}

	public void setEnableBasic(boolean enableBasic) {
		this.enableBasic = enableBasic;
	}	
	
}

package aurora.plugin.sap;

import com.sap.mw.jco.IRepository;
import com.sap.mw.jco.JCO;

public interface ISapInstance {
	public IRepository getRepository();

	public JCO.Client getClient();

	public String getSid();

	public String getUserid();

	public String getPassword();

	public String getServer_ip();

	public String getDefault_lang();

	public int getMax_conn();

	public String getSap_client();

	public String getSystem_number();

}

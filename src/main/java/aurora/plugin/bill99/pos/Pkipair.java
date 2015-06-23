package aurora.plugin.bill99.pos;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;

import aurora.plugin.bill99.Configuration;

public class Pkipair {

	public String signMsg(String signMsg) {

		String base64 = "";
		try {
			String signFile = getValue("pos_cp_sign_pfx_file");
			String key = getValue("pos_cp_sign_pfx_file_key");
			String psd = getValue("pos_cp_sign_pfx_file_password");

			KeyStore ks = KeyStore.getInstance("PKCS12");
			String fStr = Pkipair.class.getClassLoader()
					.getResource(signFile/* "10411004511603390.pfx" */)
					.getPath();
			// fStr = fStr.substring(1);
			FileInputStream ksfis = new FileInputStream(fStr);
			BufferedInputStream ksbufin = new BufferedInputStream(ksfis);
			char[] keyPwd = psd/* "vpos123" */.toCharArray();
			ks.load(ksbufin, keyPwd);
			PrivateKey priK = (PrivateKey) ks.getKey(
					key/* "10411004511603390" */, keyPwd);
			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initSign(priK);
			signature.update(signMsg.getBytes("UTF-8"));

			sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
			base64 = encoder.encode(signature.sign());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return base64;
	}

	private static String getValue(String key) {
		String value = Configuration.getValue(
				Configuration.DEFAULT_CONFIG_FILE, key);
		// String value = Configuration.getInstance().getValue(key);
		return value == null ? "" : value;
	}
}

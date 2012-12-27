package aurora.plugin.bill99;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import aurora.plugin.bill99.pos.CerEncode;


public class SendReceivePKipair {

	// 配置文件名
	private String configFile;
	

	public SendReceivePKipair(String configFile) {
		super();
		this.configFile = configFile;
	}

	public String signMsg(String signMsg) {

		String base64 = "";
		try {
			String signFile = getValue("send_sign_pfx_file");
			String key = getValue("send_sign_pfx_file_key");
			String psd = getValue("send_sign_pfx_file_password");

			KeyStore ks = KeyStore.getInstance("PKCS12");
			String fStr = SendReceivePKipair.class.getClassLoader()
					.getResource(signFile/* "10411004511603390.pfx" */)
					.getPath().replaceAll("%20", " ");
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

	private  String getValue(String key) {
		String value = Configuration.getValue(configFile, key);
//				.getInstance().getValue(key);
		return value == null ? "" : value;
	}

	
	public boolean enCodeByCer(String val, String msg) {
		boolean flag = false;
		try {
			String verifyFile = getValue("receive_verify_cer_file");
			String fStr = CerEncode.class.getClassLoader().getResource(verifyFile/*mgw.cer*/).getPath();
//			fStr = fStr.substring(1);
			InputStream inStream = new FileInputStream(fStr);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) cf
					.generateCertificate(inStream);
			PublicKey pk = cert.getPublicKey();

			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initVerify(pk);
			signature.update(val.getBytes());
			
			sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();

			flag = signature.verify(decoder.decodeBuffer(msg));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}
}

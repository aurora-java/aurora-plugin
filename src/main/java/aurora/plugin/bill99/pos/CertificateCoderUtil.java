package aurora.plugin.bill99.pos;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

import aurora.plugin.bill99.Configuration;

public class CertificateCoderUtil {
	public static final String KEY_STORE = "PKCS12";
	public static final String X509 = "X.509";

	/**
	 * 
	 * 
	 * @param keyStorePath
	 * @param alias
	 * @param password
	 * @return
	 * @throws Exception
	 */
	private static PrivateKey getPrivateKey(String keyStorePath, String alias,
			String password) throws Exception {
		KeyStore ks = getKeyStore(keyStorePath, password);
		PrivateKey key = (PrivateKey) ks.getKey(alias, password.toCharArray());
		return key;
	}

	/**
	 * 
	 * 
	 * @param certificatePath
	 * @return
	 * @throws Exception
	 */
	private static PublicKey getPublicKey(String certificatePath)
			throws Exception {
		Certificate certificate = getCertificate(certificatePath);
		PublicKey key = certificate.getPublicKey();
		return key;
	}

	/**
	 * Certificate
	 * 
	 * @param certificatePath
	 * @return
	 * @throws Exception
	 */
	private static Certificate getCertificate(String certificatePath)
			throws Exception {
		CertificateFactory certificateFactory = CertificateFactory
				.getInstance(X509);
		FileInputStream in = new FileInputStream(certificatePath);

		Certificate certificate = certificateFactory.generateCertificate(in);
		in.close();

		return certificate;
	}

	/**
	 * Certificate
	 * 
	 * @param keyStorePath
	 * @param alias
	 * @param password
	 * @return
	 * @throws Exception
	 */
	private static Certificate getCertificate(String keyStorePath,
			String alias, String password) throws Exception {
		KeyStore ks = getKeyStore(keyStorePath, password);
		Certificate certificate = ks.getCertificate(alias);

		return certificate;
	}

	/**
	 * KeyStore
	 * 
	 * @param keyStorePath
	 * @param password
	 * @return
	 * @throws Exception
	 */
	private static KeyStore getKeyStore(String keyStorePath, String password)
			throws Exception {
		FileInputStream is = new FileInputStream(keyStorePath);
		KeyStore ks = KeyStore.getInstance(KEY_STORE);
		ks.load(is, password.toCharArray());
		is.close();
		return ks;
	}

	/**
	 * 
	 * 
	 * @param data
	 * @param keyStorePath
	 * @param alias
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptByPrivateKey(byte[] data, String keyStorePath,
			String alias, String password) throws Exception {
		//
		PrivateKey privateKey = getPrivateKey(keyStorePath, alias, password);

		//
		Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);

		return cipher.doFinal(data);

	}

	/**
	 * 
	 * 
	 * @param data
	 * @param keyStorePath
	 * @param alias
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptByPrivateKey(byte[] data, String keyStorePath,
			String alias, String password) throws Exception {
		PrivateKey privateKey = getPrivateKey(keyStorePath, alias, password);

		Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, privateKey);

		return cipher.doFinal(data);

	}

	/**
	 * 
	 * 
	 * @param data
	 * @param certificatePath
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptByPublicKey(byte[] data, String certificatePath)
			throws Exception {

		PublicKey publicKey = getPublicKey(certificatePath);

		Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);

		return cipher.doFinal(data);

	}

	/**
	 * 
	 * 
	 * @param data
	 * @param certificatePath
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptByPublicKey(byte[] data, String certificatePath)
			throws Exception {

		PublicKey publicKey = getPublicKey(certificatePath);

		Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, publicKey);

		return cipher.doFinal(data);

	}

	/**
	 * Certificate
	 * 
	 * @param certificatePath
	 * @return
	 */
	public static boolean verifyCertificate(String certificatePath) {
		return verifyCertificate(new Date(), certificatePath);
	}

	/**
	 * Certificate
	 * 
	 * @param date
	 * @param certificatePath
	 * @return
	 */
	public static boolean verifyCertificate(Date date, String certificatePath) {
		boolean status = true;
		try {
			//
			Certificate certificate = getCertificate(certificatePath);
			//
			status = verifyCertificate(date, certificate);
		} catch (Exception e) {
			status = false;
		}
		return status;
	}

	/**
	 * 
	 * 
	 * @param date
	 * @param certificate
	 * @return
	 */
	private static boolean verifyCertificate(Date date, Certificate certificate) {
		boolean status = true;
		try {
			X509Certificate x509Certificate = (X509Certificate) certificate;
			x509Certificate.checkValidity(date);
		} catch (Exception e) {
			status = false;
		}
		return status;
	}

	/**
	 * 
	 * 
	 * @param keyStorePath
	 * @param alias
	 * @param password
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String sign(byte[] sign, String keyStorePath, String alias,
			String password) throws Exception {
		//
		KeyStore ks = getKeyStore(keyStorePath, password);
		//
		PrivateKey privateKey = (PrivateKey) ks.getKey(alias,
				password.toCharArray());

		//
		Signature signature = Signature.getInstance("SHA1WithRSA");
		signature.initSign(privateKey);
		signature.update(sign);

		//
		return encryptBASE64(signature.sign()).replaceAll("\r\n", "");
	}

	/**
	 * 
	 * 
	 * @param keyStorePath
	 * @param alias
	 * @param password
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String sign(byte[] sign) throws Exception {
		String signFile = getValue("pos_cp_sign_pfx_file");
		String key = getValue("pos_cp_sign_pfx_file_key");
		String psd = getValue("pos_cp_sign_pfx_file_password");

		return sign(sign, CertificateCoderUtil.class.getClassLoader()
				.getResource(signFile).getPath(), key, psd);// 10411004511603390
		// return sign(sign, CertificateCoderUtil.class.getClassLoader()
		// .getResource("10411004511700190.pfx").getPath(),
		// "10411004511700190", "vpos123");// 10411004511603390
	}

	private static String getValue(String key) {
		String value = Configuration.getValue(Configuration.DEFAULT_CONFIG_FILE, key);
//		String value = Configuration.getInstance().getValue(key);
		return value == null ? "" : value;
	}

	/**
	 * 
	 * 
	 * @param data
	 * @param sign
	 * @param certificatePath
	 * @return
	 * @throws Exception
	 */
	public static boolean verify(byte[] data, String sign,
			String certificatePath) throws Exception {

		X509Certificate x509Certificate = (X509Certificate) getCertificate(certificatePath);

		PublicKey publicKey = x509Certificate.getPublicKey();

		Signature signature = Signature.getInstance("SHA1WithRSA");
		signature.initVerify(publicKey);
		signature.update(data);

		return signature.verify(decryptBASE64(sign));

	}

	public static boolean verify(byte[] data, String sign) throws Exception {
		String verifyFile = getValue("pos_cp_verify_cer_file");

		// return verify(data, sign, CertificateCoderUtil.class.getClassLoader()
		// .getResource("mgw.cer").getPath());
		return verify(data, sign, CertificateCoderUtil.class.getClassLoader()
				.getResource(verifyFile).getPath());
	}

	/**
	 * Certificate
	 * 
	 * @param keyStorePath
	 * @param alias
	 * @param password
	 * @return
	 */
	public static boolean verifyCertificate(Date date, String keyStorePath,
			String alias, String password) {
		boolean status = true;
		try {
			Certificate certificate = getCertificate(keyStorePath, alias,
					password);
			status = verifyCertificate(date, certificate);
		} catch (Exception e) {
			status = false;
		}
		return status;
	}

	/**
	 * Certificate
	 * 
	 * @param keyStorePath
	 * @param alias
	 * @param password
	 * @return
	 */
	public static boolean verifyCertificate(String keyStorePath, String alias,
			String password) {
		return verifyCertificate(new Date(), keyStorePath, alias, password);
	}

	/**
	 * BASE64
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptBASE64(String key) throws Exception {
		return Base64.decodeBase64(key.getBytes("gb2312"));
	}

	/**
	 * BASE64
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String encryptBASE64(byte[] key) throws Exception {
		// 使用这些类库是有风险的。
		// http://java.sun.com/products/jdk/faq/faq-sun-packages.html
		// 使用非eclipse环境自带的jdk可以解决编译错误。
		// return Base64.encodeBase64String(key);
		return (new sun.misc.BASE64Encoder()).encodeBuffer(key);
	}
}

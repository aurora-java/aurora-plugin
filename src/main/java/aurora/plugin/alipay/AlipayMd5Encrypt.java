package aurora.plugin.alipay;

import java.io.UnsupportedEncodingException;
import java.security.SignatureException;

import org.apache.commons.codec.digest.DigestUtils;



public class AlipayMd5Encrypt {
	/**
     * 对字符串进行MD5签名
     * 
     * @param text 明文
     * 
     * @return 密文
     */
	public static String md5(String text){
		return DigestUtils.md5Hex(getContentBytes(text, AlipayConfig.input_charset));
	}
	
	/**
     * 将String按照charset的编码方式，编码为byte序列
     * @param content
     * @param charset
     */
    private static byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }

        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }
}

package aurora.plugin.sharepoint;

import java.net.Authenticator;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import sun.misc.BASE64Decoder;
import uncertain.composite.CompositeMap;
import uncertain.logging.ILogger;

import com.microsoft.schemas.sharepoint.CopySoap;
import com.microsoft.schemas.sharepoint.FieldInformationCollection;

public class Download {

	private SharePointConfig spConfig;
	private String fileFullPath;
	private ILogger logger;

	public Download(SharePointConfig spConfig, String fileFullPath) {
		this.spConfig = spConfig;
		this.fileFullPath = fileFullPath;
		logger = spConfig.getLogger(this.getClass().getCanonicalName());
	}

	public byte[] execute() throws Exception {
		if (spConfig.isUseJax()) {
			java.net.CookieManager cm = new java.net.CookieManager();
			java.net.CookieHandler.setDefault(cm);
			Authenticator.setDefault(new SharepointAuthenticator(spConfig));
			return getFileByJAX(fileFullPath);
		} else {
			return getFile(fileFullPath);
		}
	}

	private byte[] getFileByJAX(String remotefilePath) throws Exception {
		String user_name = spConfig.getUserName();
		String pass_word = spConfig.getPassword();
		String copy_asmx_url = spConfig.getCopyOperationFullPath();

		// copy_asmx_url = "http://localhost:1234/sites/Doc/_vti_bin/copy.asmx";

		byte[] fileBytes = null;
		CopySoap copySoap = spConfig.getCopySoap();
		BindingProvider bp = (BindingProvider) copySoap;
		bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, user_name);
		bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, pass_word);
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, copy_asmx_url);
		FieldInformationCollection fields = new FieldInformationCollection();
		byte[] a = null;
		Holder<byte[]> stream = new Holder<byte[]>(a);
		Holder<FieldInformationCollection> fields1 = new Holder<FieldInformationCollection>(fields);
		Holder<Long> getItemResult = new Holder<Long>(new Long(-1));

		copySoap.getItem(remotefilePath, getItemResult, fields1, stream);

		fileBytes = ((byte[]) (stream.value));

		return fileBytes;
	}

	private byte[] getFile(String remotefilePath) throws Exception {

		byte[] fileBytes = null;
		String user_name = spConfig.getUserName();
		String pass_word = spConfig.getPassword();
		String copy_asmx_url = spConfig.getCopyOperationFullPath();

		WebServiceUtil webServiceUtil = new WebServiceUtil(user_name, pass_word);
		CompositeMap requestBody = GetItem.downloadFile(fileFullPath);
		logger.config("request:"+requestBody.toXML());
		CompositeMap response_node = webServiceUtil.request(copy_asmx_url, GetItem.SOAP_ACTION, requestBody);
		// /GetItemResponse/Stream/
		CompositeMap stream_node = (CompositeMap) response_node.getObject("Stream");
		if (stream_node == null)
			return null;
		String fileContent = stream_node.getText();
		if(fileContent == null)
			return null;
		BASE64Decoder base64 = new BASE64Decoder();
		fileBytes = base64.decodeBuffer(fileContent);

		return fileBytes;
	}
}

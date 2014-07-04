package aurora.plugin.sharepoint;

import java.net.Authenticator;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import com.microsoft.schemas.sharepoint.CopySoap;
import com.microsoft.schemas.sharepoint.FieldInformationCollection;

public class Download {

	private SharePointConfig spConfig;
	private String fileFullPath;

	public Download(SharePointConfig spConfig, String fileFullPath) {
		this.spConfig = spConfig;
		this.fileFullPath = fileFullPath;
	}

	public byte[] execute() throws Exception {
		java.net.CookieManager cm = new java.net.CookieManager();
		java.net.CookieHandler.setDefault(cm);
		Authenticator.setDefault(new SharepointAuthenticator(spConfig));
		return getFile(fileFullPath);
	}

	private byte[] getFile(String remotefilePath) throws Exception {
		String user_name = spConfig.getUserName();
		String pass_word = spConfig.getPassword();
		String copy_asmx_url = spConfig.getCopyOperationFullPath();

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
}

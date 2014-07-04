package aurora.plugin.sharepoint;

import java.net.Authenticator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import uncertain.logging.ILogger;

import com.microsoft.schemas.sharepoint.CopyErrorCode;
import com.microsoft.schemas.sharepoint.CopyResult;
import com.microsoft.schemas.sharepoint.CopyResultCollection;
import com.microsoft.schemas.sharepoint.CopySoap;
import com.microsoft.schemas.sharepoint.DestinationUrlCollection;
import com.microsoft.schemas.sharepoint.FieldInformation;
import com.microsoft.schemas.sharepoint.FieldInformationCollection;
import com.microsoft.schemas.sharepoint.FieldType;
import com.microsoft.schemas.sharepoint.ListsSoap;
import com.microsoft.schemas.sharepoint.UpdateListItems.Updates;
import com.microsoft.schemas.sharepoint.UpdateListItemsResponse.UpdateListItemsResult;

public class Upload {

	private SharePointConfig spConfig;
	private SharePointFile spFile;
	private ILogger logger;

	private byte[] fileContent;
	private String sourceSystemUser;
	private String sourceSystem;
	private List<String> folders = new LinkedList<String>();

	public Upload(SharePointConfig spConfig, SharePointFile spFile, byte[] fileContent, String sourceSystem, String sourceSystemUser) {
		this.spConfig = spConfig;
		this.spFile = spFile;
		logger = spConfig.getLogger(this.getClass().getCanonicalName());
		this.fileContent = fileContent;
		this.sourceSystem = sourceSystem;
		this.sourceSystemUser = sourceSystemUser;
	}

	public void execute() throws Exception {
		java.net.CookieManager cm = new java.net.CookieManager();
		java.net.CookieHandler.setDefault(cm);
		Authenticator.setDefault(new SharepointAuthenticator(spConfig));
		String folderPath = spFile.getFolderPath();
		createFolder(folderPath);
		String fileFullPath = spFile.getFileFullPath();
		uploadFile(fileFullPath, fileContent, sourceSystem, sourceSystemUser);
	}

	private void createFolder(String folderPath) throws Exception {
		if (spConfig.isFolderExists(folderPath))
			return;
		String listName = spFile.getListName();
		createFolder(listName, folderPath);
	}

	public void createFolder(String listName, String folderPath) throws Exception {
		String user_name = spConfig.getUserName();
		String pass_word = spConfig.getPassword();
		String list_asmx_url = spConfig.getListsOperationFullPath();

		String[] nextFolder = folderPath.split("/");

		ListsSoap port = spConfig.getListsSoap();
		BindingProvider bp = (BindingProvider) port;
		bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, user_name);
		bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, pass_word);
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, list_asmx_url);

		String currentFolder = "";
		Updates u = new Updates();

		StringBuffer createfolder = new StringBuffer("<Batch OnError=\"Continue\" PreCalc=\"TRUE\" \n" + "ListVersion=\"0\" >\n");
		for (int i = 0; i < nextFolder.length; i++) {
			if (i == 0) {
				currentFolder = currentFolder + nextFolder[i];
			} else {
				currentFolder = currentFolder + "/" + nextFolder[i];
			}
			createfolder.append("   <Method ID=\"1\" Cmd=\"New\">\n" + "      <Field Name=\"ID\">New</Field>\n" + "      <Field Name=\"FSObjType\">1</Field>\n"
					+ "      <Field Name=\"BaseName\">" + currentFolder + "</Field>\n" + "   </Method>\n");

			folders.add(currentFolder);
		}
		createfolder.append("</Batch>");
		u.getContent().add(SharePointConfig.createSharePointCAMLNode(createfolder.toString()));
		UpdateListItemsResult result = port.updateListItems(listName, u);
		printResponse(result);
	}

	private void printResponse(UpdateListItemsResult result) throws Exception {
		List<Object> resultList = result.getContent();
		if (resultList == null || resultList.size() == 0)
			return;
		Object content = resultList.get(0);
		logger.info("createFolder:" + spConfig.parseResult(content));
	}

	private void uploadFile(String fileFullPath, byte[] fileContent, String sourceSystem, String sourceSystemUser) throws Exception {

		String user_name = spConfig.getUserName();
		String pass_word = spConfig.getPassword();
		String copy_asmx_url = spConfig.getCopyOperationFullPath();

		CopySoap copySoap = spConfig.getCopySoap();
		BindingProvider bp = (BindingProvider) copySoap;
		bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, user_name);
		bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, pass_word);
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, copy_asmx_url);

		DestinationUrlCollection destinationUrlCollection = new DestinationUrlCollection();
		destinationUrlCollection.getString().add(fileFullPath);
		FieldInformationCollection fields = new FieldInformationCollection();

		FieldInformation sourceInfo = new FieldInformation();
		sourceInfo.setDisplayName("来源系统");
		sourceInfo.setType(FieldType.TEXT);
		sourceInfo.setValue(sourceSystem);
		fields.getFieldInformation().add(sourceInfo);

		FieldInformation userInfo = new FieldInformation();
		userInfo.setDisplayName("用户名");
		userInfo.setType(FieldType.TEXT);
		userInfo.setValue(sourceSystemUser);
		fields.getFieldInformation().add(userInfo);

		CopyResultCollection results = new CopyResultCollection();
		Holder<CopyResultCollection> resultHolder = new Holder<CopyResultCollection>(results);
		Holder<Long> longHolder = new Holder<Long>(new Long(-1));

		// make the call to upload
		// 第一个参数无意义
		copySoap.copyIntoItems("S", destinationUrlCollection, fields, fileContent, longHolder, resultHolder);
		spConfig.addAllFolder(folders);
		folders.clear();
		// do something meaningful here
		for (CopyResult copyResult : resultHolder.value.getCopyResult()) {
			if (copyResult.getErrorCode() != CopyErrorCode.SUCCESS)
				throw new Exception("Upload failed for: " + copyResult.getDestinationUrl() + " Message: " + copyResult.getErrorMessage() + " Code: "
						+ copyResult.getErrorCode());
		}

	}
}

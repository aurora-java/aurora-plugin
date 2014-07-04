package aurora.plugin.sharepoint;

import java.net.Authenticator;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.xml.ws.BindingProvider;

import org.w3c.dom.Element;

import uncertain.logging.ILogger;

import com.microsoft.schemas.sharepoint.GetListItems;
import com.microsoft.schemas.sharepoint.GetListItemsResponse;
import com.microsoft.schemas.sharepoint.ListsSoap;
import com.microsoft.schemas.sharepoint.UpdateListItems.Updates;
import com.microsoft.schemas.sharepoint.UpdateListItemsResponse.UpdateListItemsResult;

public class Delete {

	private SharePointConfig spConfig;
	private SharePointFile spFile;
	private ILogger logger;

	String list_wsdl_url;
	String user_name;
	String pass_word;
	String list_asmx_url;

	public Delete(SharePointConfig spConfig, SharePointFile spFile) {
		this.spConfig = spConfig;
		this.spFile = spFile;
		logger = spConfig.getLogger(this.getClass().getCanonicalName());
		list_wsdl_url = spConfig.getListsWsdlFullPath();
		user_name = spConfig.getUserName();
		pass_word = spConfig.getPassword();
		list_asmx_url = spConfig.getListsOperationFullPath();
	}

	public void execute() throws Exception {
		java.net.CookieManager cm = new java.net.CookieManager();
		java.net.CookieHandler.setDefault(cm);
		Authenticator.setDefault(new SharepointAuthenticator(spConfig));
		deleteListItem(spFile);
	}

	private void deleteListItem(SharePointFile spFile) throws Exception {

		String listName = spFile.getListName();
		String folderPath = spFile.getFolderPath();
		String fileName = spFile.getFileName();
		String folderPathWithListUrl = spFile.getListUrl() + "/" + folderPath;
		logger.info("listName:" + listName);
		logger.info("folderPathWithListUrl:" + folderPathWithListUrl);

		ListsSoap port = spConfig.getListsSoap();
		BindingProvider bp = (BindingProvider) port;
		bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, user_name);
		bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, pass_word);
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, list_asmx_url);

//		List<Element> l = querySharePointFolder(listName, folderPathWithListUrl);
//		String delete = "<Batch OnError=\"Continue\" ListVersion=\"1\" >\n" + "	<Method ID=\"1\" Cmd=\"Delete\">\n" + "      <Field Name=\"ID\">%1s</Field>\n"
//				+ "	  <Field Name=\"FileRef\" target=\"blank\">%2s</Field>\n" + "	</Method>			   \n" + "</Batch>";

//		String appLocation = spConfig.getAppLocation();
//		String appSite = getAppSite(appLocation);
//		logger.info("appSite:" + appSite);
//		logger.info("querySharePointFolder size:" + l.size());
//		for (int i = 0; i < l.size(); i++) {
//			String fileNameRemote = l.get(i).getAttributes().getNamedItem("ows_LinkFilename").getNodeValue();
//			if (fileName.equals(fileNameRemote)) {
//				String listId = l.get(i).getAttributes().getNamedItem("ows_ID").getNodeValue();
//				String fileRefRelativePath = l.get(i).getAttributes().getNamedItem("ows_FileRef").getNodeValue();
//				String fileRef = appSite + fileRefRelativePath.split("#")[1];
//				logger.info("fileRef:" + fileRef);
//				String deleteFormatted = String.format(delete, listId, fileRef);
//				Updates u = new Updates();
//				u.getContent().add(SharePointConfig.createSharePointCAMLNode(deleteFormatted));
//				UpdateListItemsResult result = port.updateListItems(listName, u);
//				printResponse(result);
//			}
//		}
		String delete = "<Batch OnError=\"Continue\" ListVersion=\"1\" >\n" + "	<Method ID=\"1\" Cmd=\"Delete\">\n" + "      <Field Name=\"ID\">1</Field>\n"
		+ "	  <Field Name=\"FileRef\" target=\"blank\">%1s</Field>\n" + "	</Method>			   \n" + "</Batch>";
		String fileFullPath = spFile.getFileFullPath();
		logger.info("fileRef:" + fileFullPath);
		String deleteFormatted = String.format(delete,fileFullPath);
		logger.info("deleteFormatted:" + deleteFormatted);
		Updates u = new Updates();
		u.getContent().add(SharePointConfig.createSharePointCAMLNode(deleteFormatted));
		UpdateListItemsResult result = port.updateListItems(listName, u);
		printResponse(result);
	}
	private void printResponse(UpdateListItemsResult result) throws Exception {
		List<Object> resultList = result.getContent();
		if (resultList == null || resultList.size() == 0)
			return;
		Object content = resultList.get(0);
		logger.info("Delete:" + spConfig.parseResult(content));
	}

	private String getAppSite(String appLocation) throws Exception {
		String appSite = null;
		URI app = new URI(appLocation);
		String appPath = app.getPath();
		if (appPath == null || "".equals(appPath)) {
			appSite = appLocation;
			if (!appSite.endsWith("/")) {
				appSite = appSite + "/";
			}
		} else {
			int pathIndex = appLocation.indexOf(appPath) + 1;
			appSite = appLocation.substring(0, pathIndex);
		}
		return appSite;
	}

	public List<Element> querySharePointFolder(String listName, String folderPathWithListUrl) throws Exception {

		ListsSoap port = spConfig.getListsSoap();
		BindingProvider bp = (BindingProvider) port;
		bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, user_name);
		bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, pass_word);
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, list_asmx_url);
		GetListItems.QueryOptions msQueryOptions = new GetListItems.QueryOptions();
		msQueryOptions.getContent().add(
				SharePointConfig.createSharePointCAMLNode("<QueryOptions><Folder>" + folderPathWithListUrl + "</Folder></QueryOptions>"));

		GetListItemsResponse.GetListItemsResult result = port.getListItems(listName, "", null, null, null, msQueryOptions, null);

		List<Element> itemList = new LinkedList<Element>();

		List<Object> content = result.getContent();
		if (content == null || content.size() == 0)
			return null;
		Element element = (Element) content.get(0);
		logger.info("querySharePointFolder:"+spConfig.parseResult(element));
		SharePointConfig.findAllElementsByTagName(element, "z:row", itemList);
		return itemList;

	}
}

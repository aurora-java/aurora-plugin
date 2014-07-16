package aurora.plugin.sharepoint;

import java.net.Authenticator;
import java.util.List;

import javax.xml.ws.BindingProvider;


import uncertain.composite.CompositeMap;
import uncertain.logging.ILogger;

import com.microsoft.schemas.sharepoint.ListsSoap;
import com.microsoft.schemas.sharepoint.UpdateListItems.Updates;
import com.microsoft.schemas.sharepoint.UpdateListItemsResponse.UpdateListItemsResult;

public class Delete {

	public static String DELETE_FILE_SUCCESS = "0x00000000";
	
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
		if(spConfig.isUseJax()){
			java.net.CookieManager cm = new java.net.CookieManager();
			java.net.CookieHandler.setDefault(cm);
			Authenticator.setDefault(new SharepointAuthenticator(spConfig));
			deleteListItemByJAX(spFile);
		}else{
			deleteListItem(spFile);
		}
	}

	private void deleteListItemByJAX(SharePointFile spFile) throws Exception {

		String listName = spFile.getListName();
		logger.info("listName:" + listName);
		ListsSoap port = spConfig.getListsSoap();
		BindingProvider bp = (BindingProvider) port;
		bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, user_name);
		bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, pass_word);
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, list_asmx_url);

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
	
	private void deleteListItem(SharePointFile spFile) throws Exception {

		String listName = spFile.getListName();
		String fileFullPath = spFile.getFileFullPath();
		WebServiceUtil webServiceUtil = new WebServiceUtil(user_name, pass_word);

		CompositeMap requestBody = UpdateListItems.deleteFile(listName, fileFullPath);
		logger.config("request:"+requestBody.toXML());
		CompositeMap response_node = webServiceUtil.request(list_asmx_url, UpdateListItems.SOAP_ACTION, requestBody);
		logger.config("response:"+response_node.toXML());
		// /UpdateListItemsResponse/UpdateListItemsResult/Results/
		CompositeMap results = (CompositeMap) response_node.getObject("UpdateListItemsResult/Results");
		if (results == null)
			return;
		List<CompositeMap> resultList = results.getChilds();
		if (resultList == null)
			return;
		int length = resultList.size();
		if (length <= 0)
			return;
		CompositeMap lastResult = resultList.get(length - 1);
		
		CompositeMap errorCode_node = lastResult.getChild("ErrorCode");
		String errorCode = errorCode_node.getText();
		logger.info("errorCode:" + errorCode_node);

		if (DELETE_FILE_SUCCESS.equals(errorCode))
			return;
		CompositeMap errorText_node = lastResult.getChild("ErrorText");

		String errorMessage = errorText_node.getText();

		throw new RuntimeException(errorMessage);
	}
	
	private void printResponse(UpdateListItemsResult result) throws Exception {
		List<Object> resultList = result.getContent();
		if (resultList == null || resultList.size() == 0)
			return;
		Object content = resultList.get(0);
		logger.info("Delete:" + spConfig.parseResult(content));
	}
}

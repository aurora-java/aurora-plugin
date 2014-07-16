package aurora.plugin.sharepoint;

import uncertain.composite.CompositeMap;

public class GetItem {
/*
 * 
 * <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soap="http://schemas.microsoft.com/sharepoint/soap/">
   <soapenv:Body>
      <soap:GetItem>
         <soap:Url>http://moss15/sites/Doc/PRO_DOC/ok/lin/1.txt</soap:Url>
      </soap:GetItem>
   </soapenv:Body>
</soapenv:Envelope>
 * 
 * 
 */
	public static String SOAP_ACTION="http://schemas.microsoft.com/sharepoint/soap/GetItem";
	
	public static  CompositeMap downloadFile(String fileFullPath){
		 CompositeMap getItem_node = createCompositeMap("GetItem");
		 CompositeMap url_node = createCompositeMap("Url");
		 url_node.setText(fileFullPath);
		 getItem_node.addChild(url_node);
		 return getItem_node;
	}
	private static CompositeMap createCompositeMap(String name){
		return new CompositeMap("","http://schemas.microsoft.com/sharepoint/soap/",name);
	}
	
}

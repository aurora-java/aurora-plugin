package aurora.plugin.sharepoint;

import uncertain.composite.CompositeMap;

public class CopyIntoItems {
/*
 * 
 * <S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/">
	   <S:Body>
	      <CopyIntoItems xmlns="http://schemas.microsoft.com/sharepoint/soap/">
	         <SourceUrl>S</SourceUrl>
	         <DestinationUrls>
	            <string>http://moss15/sites/Doc/PRO_DOC/yes/lin/3.txt</string>
	         </DestinationUrls>
	         <Fields>
	            <FieldInformation Type="Text" DisplayName="用户名" Value="linjinxiao"/>
	            <FieldInformation Type="Text" DisplayName="来源系统" Value="Aurora"/>
	         </Fields>
	         <Stream>dGVzdDEy</Stream>
	      </CopyIntoItems>
	   </S:Body>
  </S:Envelope>
 * 
 * 
 */
	public static String SOAP_ACTION="http://schemas.microsoft.com/sharepoint/soap/CopyIntoItems";
	
	public static  CompositeMap uploadFile(String destinationUrl,String sourceSystem,String sourceSystemUser,String stream){
		 CompositeMap copyIntoItems_node = createCompositeMap("CopyIntoItems");
		 CompositeMap sourceUrl_node = createCompositeMap("SourceUrl");
		 sourceUrl_node.setText("S");
		 copyIntoItems_node.addChild(sourceUrl_node);
		 
		 CompositeMap destinationUrls_node = createDestinationUrls(destinationUrl);
		 copyIntoItems_node.addChild(destinationUrls_node);
		 
		 CompositeMap fields_node = createFields(sourceSystem,sourceSystemUser);
		 copyIntoItems_node.addChild(fields_node);
		 
		 CompositeMap stream_node = createStream(stream);
		 copyIntoItems_node.addChild(stream_node);
		 return copyIntoItems_node;
	}
	private static CompositeMap createDestinationUrls(String destinationUrl){
		 CompositeMap destinationUrls_node = createCompositeMap("DestinationUrls");
		 CompositeMap string_node = createCompositeMap("string");
		 string_node.setText(destinationUrl);
		 destinationUrls_node.addChild(string_node);
		 return destinationUrls_node;
	}
	private static CompositeMap createFields(String sourceSystem,String sourceSystemUser){
		 CompositeMap fields_node = createCompositeMap("Fields");
		 CompositeMap ssFieldInformation_node = createSourceSystemFieldInformation(sourceSystem);
		 CompositeMap ssuFieldInformation_node = createSourceSystemUserFieldInformation(sourceSystemUser);
		 fields_node.addChild(ssFieldInformation_node);
		 fields_node.addChild(ssuFieldInformation_node);
		 return fields_node;
	}
	private static CompositeMap createSourceSystemFieldInformation(String value){
		CompositeMap fieldInformation_node = createCompositeMap("FieldInformation");
		fieldInformation_node.put("Type", "Text");
		fieldInformation_node.put("DisplayName", "来源系统");
		fieldInformation_node.put("Value", value);
		return fieldInformation_node;
	}
	private static CompositeMap createSourceSystemUserFieldInformation(String value){
		CompositeMap fieldInformation_node = createCompositeMap("FieldInformation");
		fieldInformation_node.put("Type", "Text");
		fieldInformation_node.put("DisplayName", "用户名");
		fieldInformation_node.put("Value", value);
		return fieldInformation_node;
	}
	private static CompositeMap createStream(String stream){
		CompositeMap stream_node = createCompositeMap("Stream");
		stream_node.setText(stream);
		return stream_node;
	}
	private static CompositeMap createCompositeMap(String name){
		return new CompositeMap("","http://schemas.microsoft.com/sharepoint/soap/",name);
	}
	
}

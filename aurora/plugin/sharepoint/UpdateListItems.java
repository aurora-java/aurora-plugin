package aurora.plugin.sharepoint;

import java.util.LinkedList;

import uncertain.composite.CompositeMap;

public class UpdateListItems {

/*
 * createFolders:
 * 
 * <S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/">
   <S:Body>
      <UpdateListItems xmlns="http://schemas.microsoft.com/sharepoint/soap/">
         <listName>PRO_DOC</listName>
         <updates>
            <Batch ListVersion="0" OnError="Continue" PreCalc="TRUE">
               <Method Cmd="New" ID="1">
                  <Field Name="ID">New</Field>
                  <Field Name="FSObjType">1</Field>
                  <Field Name="BaseName">c1</Field>
               </Method>
               <Method Cmd="New" ID="1">
                  <Field Name="ID">New</Field>
                  <Field Name="FSObjType">1</Field>
                  <Field Name="BaseName">c1/c2</Field>
               </Method>
            </Batch>
         </updates>
      </UpdateListItems>
   </S:Body>
</S:Envelope>
 * 
 * deleteFile:
 * <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soap="http://schemas.microsoft.com/sharepoint/soap/">
   <soapenv:Header/>
   <soapenv:Body>
      <soap:UpdateListItems>
         <soap:listName>PRO_DOC</soap:listName>
         <soap:updates>
            <Batch OnError="Continue" ListVersion="1">
               <Method ID="1" Cmd="Delete">
                  <Field Name="ID">1</Field>
                  <Field Name="FileRef" target="blank">http://moss15/sites/Doc/PRO_DOC/yes/lin/2.txt</Field>
               </Method>
            </Batch>
         </soap:updates>
      </soap:UpdateListItems>
   </soapenv:Body>
</soapenv:Envelope>
 * 
 */
	
	public static String SOAP_ACTION = "http://schemas.microsoft.com/sharepoint/soap/UpdateListItems";
	
	public static CompositeMap createFolders(String listName,LinkedList<String> baseNames){
		if(baseNames == null)
			return null;
		
//		CompositeMap updateListItems_node = new CompositeMap("","http://schemas.microsoft.com/sharepoint/soap/","UpdateListItems");
		
		CompositeMap updateListItems_node = createCompositeMap("UpdateListItems");
		
		CompositeMap listName_node = createCompositeMap("listName");
		listName_node.setText(listName);
		updateListItems_node.addChild(listName_node);
		
		CompositeMap updates_node = createCompositeMap("updates");
		
		CompositeMap batch_node = new CompositeMap("Batch");
		batch_node.put("ListVersion", "0");
		batch_node.put("OnError", "Continue");
		batch_node.put("PreCalc", "TRUE");
		for(String baseName:baseNames){
			CompositeMap modethod_node = createFolderMethod(baseName);
			batch_node.addChild(modethod_node);
		}
		
		updates_node.addChild(batch_node);
		updateListItems_node.addChild(updates_node);
		
		return updateListItems_node;
	}
	
	public static CompositeMap deleteFile(String listName,String fileFullPath){
		if(fileFullPath == null)
			return null;
		
		CompositeMap updateListItems_node = createCompositeMap("UpdateListItems");
		
		CompositeMap listName_node = createCompositeMap("listName");
		listName_node.setText(listName);
		updateListItems_node.addChild(listName_node);
		
		CompositeMap updates_node = createCompositeMap("updates");
		
		CompositeMap batch_node = new CompositeMap("Batch");
		batch_node.put("ListVersion", "1");
		batch_node.put("OnError", "Continue");
			CompositeMap method_node = deleteFileMethod(fileFullPath);
			batch_node.addChild(method_node);
		
		updates_node.addChild(batch_node);
		updateListItems_node.addChild(updates_node);
		
		return updateListItems_node;
	}
	private  static CompositeMap deleteFileMethod(String fileFullPath){
		CompositeMap method_node = new CompositeMap("Method");
		method_node.put("Cmd", "Delete");
		method_node.put("ID", "1");
		
		
		CompositeMap idField_node = new CompositeMap("Field");
		idField_node.put("Name", "ID");
		idField_node.setText("1");
		
		CompositeMap fileRefField_node = new CompositeMap("Field");
		fileRefField_node.put("Name", "FileRef");
		fileRefField_node.put("target", "blank");
		fileRefField_node.setText(fileFullPath);
		
		
		method_node.addChild(idField_node);
		method_node.addChild(fileRefField_node);
		
		return method_node;
	}
	
	private  static CompositeMap createFolderMethod(String baseName){
		CompositeMap method_node = new CompositeMap("Method");
		method_node.put("Cmd", "New");
		method_node.put("ID", "1");
		
		CompositeMap idField_node = new CompositeMap("Field");
		idField_node.put("Name", "ID");
		idField_node.setText("New");
		
		CompositeMap fSObjTypeField_node = new CompositeMap("Field");
		fSObjTypeField_node.put("Name", "FSObjType");
		fSObjTypeField_node.setText("1");
		
		CompositeMap baseName_node = new CompositeMap("Field");
		baseName_node.put("Name", "BaseName");
		baseName_node.setText(baseName);
		
		method_node.addChild(idField_node);
		method_node.addChild(fSObjTypeField_node);
		method_node.addChild(baseName_node);
		
		return method_node;
	}
	
	private static CompositeMap createCompositeMap(String name){
		return new CompositeMap("","http://schemas.microsoft.com/sharepoint/soap/",name);
	}
}

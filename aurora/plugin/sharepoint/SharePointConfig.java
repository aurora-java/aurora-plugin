package aurora.plugin.sharepoint;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import uncertain.composite.CompositeMap;
import uncertain.core.ILifeCycle;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.AbstractLocatableObject;
import uncertain.ocm.IObjectRegistry;
import aurora.service.ServiceThreadLocal;

import com.microsoft.schemas.sharepoint.Copy;
import com.microsoft.schemas.sharepoint.CopySoap;
import com.microsoft.schemas.sharepoint.Lists;
import com.microsoft.schemas.sharepoint.ListsSoap;

public class SharePointConfig extends AbstractLocatableObject implements ILifeCycle {

	public static String COPY_WSDL = "/_vti_bin/copy.asmx?wsdl";
	public static String COPY_OPERATION = "/_vti_bin/Copy.asmx";
	public static String LISTS_WSDL = "/_vti_bin/lists.asmx?wsdl";
	public static String LISTS_OPERATION = "/_vti_bin/Lists.asmx";

	public static QName COPY_QNAME = new QName("http://schemas.microsoft.com/sharepoint/soap/", "Copy");
	public static QName LISTS_QNAME = new QName("http://schemas.microsoft.com/sharepoint/soap/", "Lists");

	public static String DEFAULT_SYSTEM = "Aurora";
	private IObjectRegistry objectRegisty;

	private String userName;
	private String password;
	private String appLocation;

	private String copyWsdlFullPath;
	private String copyOperationFullPath;

	private String listsWsdlFullPath;
	private String listsOperationFullPath;
	private String sourceSystem;

	private static ListsSoap listsSoap;
	private static CopySoap copySoap;

	private ConcurrentLinkedQueue<String> existsFolder = new ConcurrentLinkedQueue<String>();
	private long cacheFolderLength = 1000;
	private CompositeMap spLists = new CompositeMap();
	
	public SharePointConfig(IObjectRegistry registry){
		this.objectRegisty = registry;
	}

	@Override
	public boolean startup() {
		if (appLocation == null)
			throw BuiltinExceptionFactory.createAttributeMissing(this, "appLocation");
		if (copyWsdlFullPath == null)
			copyWsdlFullPath = appLocation + COPY_WSDL;
		if (copyOperationFullPath == null)
			copyOperationFullPath = appLocation + COPY_OPERATION;
		if (listsWsdlFullPath == null)
			listsWsdlFullPath = appLocation + LISTS_WSDL;
		if (listsOperationFullPath == null)
			listsOperationFullPath = appLocation + LISTS_OPERATION;
		if (sourceSystem == null)
			sourceSystem = DEFAULT_SYSTEM;
		return true;
	}

	@Override
	public void shutdown() {

	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAppLocation() {
		return appLocation;
	}

	public void setAppLocation(String appLoc) {
		if (appLoc.endsWith("/"))
			appLoc = appLoc.substring(0, appLoc.length() - 1);
		appLocation = appLoc;
	}

	public String getCopyWsdlFullPath() {
		return copyWsdlFullPath;
	}

	public void setCopyWsdlFullPath(String copyWsdlFullPath) {
		this.copyWsdlFullPath = copyWsdlFullPath;
	}

	public String getCopyOperationFullPath() {
		return copyOperationFullPath;
	}

	public void setCopyOperationFullPath(String copyOperationFullPath) {
		this.copyOperationFullPath = copyOperationFullPath;
	}

	public String getListsWsdlFullPath() {
		return listsWsdlFullPath;
	}

	public void setListsWsdlFullPath(String listsWsdlFullPath) {
		this.listsWsdlFullPath = listsWsdlFullPath;
	}

	public String getListsOperationFullPath() {
		return listsOperationFullPath;
	}

	public void setListsOperationFullPath(String listsOperationFullPath) {
		this.listsOperationFullPath = listsOperationFullPath;
	}

	public String getSourceSystem() {
		return sourceSystem;
	}

	public void setSourceSystem(String sourceSystem) {
		this.sourceSystem = sourceSystem;
	}

	public long getCacheFolderLength() {
		return cacheFolderLength;
	}

	public void setCacheFolderLength(long cacheFolderLength) {
		this.cacheFolderLength = cacheFolderLength;
	}

	public byte[] fileToBytes(File file) throws IOException {

		ByteArrayOutputStream ous = null;
		InputStream ios = null;
		try {
			byte[] buffer = new byte[4096];
			ous = new ByteArrayOutputStream();
			ios = new FileInputStream(file);
			int read = 0;
			while ((read = ios.read(buffer)) != -1)
				ous.write(buffer, 0, read);
		} finally {
			close(ous);
			close(ios);
		}
		return ous.toByteArray();
	}

	public byte[] inputStreamToBytes(InputStream ios) throws IOException {
		ByteArrayOutputStream ous = null;
		try {
			byte[] buffer = new byte[4096];
			ous = new ByteArrayOutputStream();
			int read = 0;
			while ((read = ios.read(buffer)) != -1)
				ous.write(buffer, 0, read);
		} finally {
			close(ous);
			close(ios);
		}
		return ous.toByteArray();
	}

	public static Node createSharePointCAMLNode(String theXML) throws Exception {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setValidating(false);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(new InputSource(new StringReader(theXML)));
		Node node = document.getDocumentElement();
		return node;
	}

	public static void findAllElementsByTagName(Element el, String tagName, List<Element> elementList) {
		if (tagName.equals(el.getTagName())) {
			elementList.add(el);
		}
		Element elem = getFirstElement(el);
		while (elem != null) {
			findAllElementsByTagName(elem, tagName, elementList);
			elem = getNextElement(elem);
		}
	}

	public static Element getNextElement(Element el) {
		Node nd = el.getNextSibling();
		while (nd != null) {
			if (nd.getNodeType() == Node.ELEMENT_NODE) {
				return (Element) nd;
			}
			nd = nd.getNextSibling();
		}
		return null;
	}

	public static Element getFirstElement(Node parent) {
		Node n = parent.getFirstChild();
		while (n != null && Node.ELEMENT_NODE != n.getNodeType()) {
			n = n.getNextSibling();
		}
		if (n == null) {
			return null;
		}
		return (Element) n;
	}

	public static void close(Closeable resource) {
		if (resource == null)
			return;
		try {
			resource.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void checkCacheFolderLength(){
		while(existsFolder.size()>cacheFolderLength){
			existsFolder.poll();
		}
	}

	public void addFolder(String folder) {
		if(existsFolder.contains(folder))
			return;
		checkCacheFolderLength();
		existsFolder.add(folder);
	}

	public void addAllFolder(Collection<String> c) {
		checkCacheFolderLength();
		existsFolder.addAll(c);
	}

	public void removeFolder(String folder) {
		existsFolder.remove(folder);
	}

	public boolean isFolderExists(String folder) {
		return existsFolder.contains(folder);
	}

	public CompositeMap getLists() {
		return spLists;
	}

	public void setLists(CompositeMap lists) {
		if (lists == null)
			return;
		List<CompositeMap> childs = lists.getChilds();
		if (childs == null)
			return;
		for (CompositeMap list : childs) {
			spLists.put(list.getString("url"), list.getString("name"));
		}
	}

	public String getListName(String listUrl) {
		return spLists.getString(listUrl);
	}

	public ListsSoap getListsSoap() throws Exception {
		if (listsSoap == null) {
			synchronized (this) {
				Lists service = new Lists(new URL(listsWsdlFullPath), LISTS_QNAME);
				listsSoap = service.getListsSoap();
			}
		}
		return listsSoap;
	}
	
	public CopySoap getCopySoap() throws Exception {
		if (copySoap == null) {
			synchronized (this) {
				Copy service = new Copy(new URL(copyWsdlFullPath), COPY_QNAME);
				copySoap = service.getCopySoap();
			}
		}
		return copySoap;
	}
	
	public ILogger getLogger(String topic){
		CompositeMap context = ServiceThreadLocal.getCurrentThreadContext();
		return LoggingContext.getLogger(context,topic);
	}
	public void writeResult(Object result, OutputStream stream) throws Exception {
		if (result == null) {
			return;
		}
		if (!(result instanceof Element)) {
			return;
		}
		Element e = (Element) result;
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.transform(new DOMSource(e.getOwnerDocument()), new StreamResult(new OutputStreamWriter(stream, "UTF-8")));
	}
	public String parseResult(Object response) throws Exception {
		if (response == null)
			return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream pw = new PrintStream(baos);
		writeResult(response,pw);
		pw.close();
		return baos.toString();
	}

}

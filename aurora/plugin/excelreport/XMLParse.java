package aurora.plugin.excelreport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import uncertain.composite.CompositeMap;

public class XMLParse extends DefaultHandler {

	CompositeMap current_node = null;
	DynamicContent dynamicContent;
	String rawName;	

	public String getRawName() {
		return rawName;
	}

	public void setRawName(String rawName) {
		this.rawName = rawName;
	}

	SAXParserFactory parser_factory = SAXParserFactory.newInstance();
	
	public DynamicContent getDynamicContent() {
		return dynamicContent;
	}

	public void setDynamicContent(DynamicContent dynamicContent) {
		this.dynamicContent = dynamicContent;
	}

	void addAttribs(CompositeMap node, Attributes attribs) {
		for (int i = 0; i < attribs.getLength(); i++) {
			String attrib_name = attribs.getQName(i);
			node.put(attrib_name, attribs.getValue(i));
		}
	}

	/** handles for SAX */

	public void startElement(String namespaceURI, String localName,
			String rawName, Attributes atts) throws SAXException {
		current_node = new CompositeMap(rawName);
		addAttribs(current_node, atts);		
		if(rawName!=null&&rawName.equals(this.getRawName()))
			getDynamicContent().createRecord(current_node);
	}

	public void parseStream(InputStream stream) throws SAXException,
			IOException {
		SAXParser parser = null;
		try {
			parser = parser_factory.newSAXParser();
		} catch (ParserConfigurationException ex) {
			throw new SAXException("error when creating SAXParser", ex);
		}
		InputSource inputSource = new InputSource(stream);		
		inputSource.setEncoding("UTF-8"); 
		parser.parse(inputSource, this);		
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		XMLParse p = new XMLParse();
		File file = new File(
				"/Users/zoulei/Downloads/excel6419944798375892263.xml");
		InputStream is = new FileInputStream(file);
		p.parseStream(is);
	}

}

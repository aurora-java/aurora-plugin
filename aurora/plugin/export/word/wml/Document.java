package aurora.plugin.export.word.wml;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "doc")
@XmlAccessorType(XmlAccessType.FIELD)
public class Document {
	
	@XmlAttribute
	private String watermark;
	
	private String pageSize = "A4";
	
	private Boolean landscape = false;
	
	private Header header;
	
	private Footer footer;
	
	private Body body;

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}
	
//	public static void main(String[] args) { 
//        try { 
//            JAXBContext jaxbContext = JAXBContext.newInstance(Document.class); 
// 
//            
//            File file = new File("C:/Users/znjqolf/Desktop/docx4j/template/test.xml");
//            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller(); 
//            Document doc = (Document)unmarshaller.unmarshal(new FileInputStream(file)); 
// 
//        } catch (Exception e) { 
//            e.printStackTrace(); 
//        } 
//    }

	public String getWatermark() {
		return watermark;
	}

	public void setWatermark(String watermark) {
		this.watermark = watermark;
	}

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public Footer getFooter() {
		return footer;
	}

	public void setFooter(Footer footer) {
		this.footer = footer;
	}

	public String getPageSize() {
		return pageSize;
	}

	public void setPageSize(String pageSize) {
		this.pageSize = pageSize;
	}

	public Boolean getLandscape() {
		return landscape;
	}

	public void setLandscape(Boolean landscape) {
		this.landscape = landscape;
	} 
}

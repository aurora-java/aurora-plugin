package aurora.plugin.export.word.wml;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;

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
	
	@XmlAttribute
	private Boolean debugger = false;
	
	@XmlAttribute
	private Boolean readOnly = false;
	
	@XmlAttribute
	private String pageSize = "A4";
	
	@XmlAttribute(name="header")
	private Float headerSize = new Float(1.5);
	
	@XmlAttribute(name="footer")
	private Float footerSize = new Float(1.75);
	
	@XmlAttribute
	private Float top = new Float(2.54);
	
	@XmlAttribute
	private Float bottom = new Float(2.54);
	
	@XmlAttribute
	private Float left = new Float(3.17);
	
	@XmlAttribute
	private Float right = new Float(3.17);
	
	@XmlAttribute
	private Boolean landscape = false;
	
	@XmlAttribute
	private BigInteger pgSzCode;
	
	@XmlAttribute
	private BigInteger pgSzH = new BigInteger("16838");
	
	@XmlAttribute
	private BigInteger pgSzW = new BigInteger("11906");
	
	@XmlAttribute
	private String docGridType;
	
	@XmlAttribute
	private BigInteger docGridLinePitch;
	
	private Settings settings;
	
	private Header header;
	
	private Footer footer;
	
	private Body body;
	
	private NumberingChunk numberingChunk;

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}
	
	public static void main(String[] args) { 
        try { 
            JAXBContext jaxbContext = JAXBContext.newInstance(Document.class); 
 
            
            File file = new File("C:/Users/znjqolf/Desktop/docx4j/template/test.xml");
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller(); 
            Document doc = (Document)unmarshaller.unmarshal(new FileInputStream(file)); 
            System.out.println(doc); 
 
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
    }

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

	public Float getTop() {
		return top;
	}

	public void setTop(Float top) {
		this.top = top;
	}

	public Float getBottom() {
		return bottom;
	}

	public void setBottom(Float bottom) {
		this.bottom = bottom;
	}

	public Float getLeft() {
		return left;
	}

	public void setLeft(Float left) {
		this.left = left;
	}

	public Float getRight() {
		return right;
	}

	public void setRight(Float right) {
		this.right = right;
	}

	public Boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}

	public Boolean getDebugger() {
		return debugger;
	}

	public void setDebugger(Boolean debugger) {
		this.debugger = debugger;
	}

	public NumberingChunk getNumberingChunk() {
		return numberingChunk;
	}

	public void setNumberingChunk(NumberingChunk numberingChunk) {
		this.numberingChunk = numberingChunk;
	}

	public Float getHeaderSize() {
		return headerSize;
	}

	public void setHeaderSize(Float headerSize) {
		this.headerSize = headerSize;
	}

	public Float getFooterSize() {
		return footerSize;
	}

	public void setFooterSize(Float footerSize) {
		this.footerSize = footerSize;
	}

	public String getDocGridType() {
		return docGridType;
	}

	public void setDocGridType(String docGridType) {
		this.docGridType = docGridType;
	}

	public BigInteger getDocGridLinePitch() {
		return docGridLinePitch;
	}

	public void setDocGridLinePitch(BigInteger docGridLinePitch) {
		this.docGridLinePitch = docGridLinePitch;
	}

	public Settings getSettings() {
		return settings;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	public BigInteger getPgSzCode() {
		return pgSzCode;
	}

	public void setPgSzCode(BigInteger pgSzCode) {
		this.pgSzCode = pgSzCode;
	}

	public BigInteger getPgSzH() {
		return pgSzH;
	}

	public void setPgSzH(BigInteger pgSzH) {
		this.pgSzH = pgSzH;
	}

	public BigInteger getPgSzW() {
		return pgSzW;
	}

	public void setPgSzW(BigInteger pgSzW) {
		this.pgSzW = pgSzW;
	}
}

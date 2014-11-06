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
	private Double headerSize = 1.5;
	
	@XmlAttribute(name="footer")
	private Double footerSize = 1.75;
	
	@XmlAttribute
	private Double top = 2.54;
	
	@XmlAttribute
	private Double bottom = 2.54;
	
	@XmlAttribute
	private Double left = 3.17;
	
	@XmlAttribute
	private Double right = 3.17;
	
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

	public Double getTop() {
		return top;
	}

	public void setTop(Double top) {
		this.top = top;
	}

	public Double getBottom() {
		return bottom;
	}

	public void setBottom(Double bottom) {
		this.bottom = bottom;
	}

	public Double getLeft() {
		return left;
	}

	public void setLeft(Double left) {
		this.left = left;
	}

	public Double getRight() {
		return right;
	}

	public void setRight(Double right) {
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

	public Double getHeaderSize() {
		return headerSize;
	}

	public void setHeaderSize(Double headerSize) {
		this.headerSize = headerSize;
	}

	public Double getFooterSize() {
		return footerSize;
	}

	public void setFooterSize(Double footerSize) {
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

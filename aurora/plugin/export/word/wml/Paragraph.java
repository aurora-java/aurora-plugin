package aurora.plugin.export.word.wml;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "p")
@XmlAccessorType(XmlAccessType.FIELD)
public class Paragraph {
	
	
	
	@XmlAttribute
	private Float indLeft;
	
	@XmlAttribute
	private Float indFirstLine;
	
	@XmlAttribute
	private Float indFirstLineChars;
	
	@XmlAttribute
	private String align;
	
	@XmlAttribute
	private Long numId = null;
	
	@XmlAttribute
	private Long ilvl = null;
	
	@XmlAttribute
	private Boolean toc = false;
	
	@XmlAttribute
	private String after = "0";
	
	@XmlAttribute
	private BigInteger beforeLines;
	
	@XmlAttribute
	private BigInteger afterLines;
	
	@XmlAttribute
	private String line;// = "400";
	
	@XmlAttribute
	private String lineRule = "atLeast";
	
	@XmlAttribute
	private String orientation;
	
	@XmlAttribute
	private String tocTitle;	
	
	@XmlAttribute
	private BigInteger colsNum;
	
	@XmlAttribute
	private String sectPrType;
	
	@XmlAttribute
	private Double colsSpace;
	
	@XmlAttribute
	private String pageBreakBefore = "false";
	
	@XmlElementRefs({
        @XmlElementRef(name = "t", type = Text.class),
        @XmlElementRef(name = "img",type = Image.class),
        @XmlElementRef(name = "qr-code",type = QRCode.class),
        @XmlElementRef(name = "ptab",type = PTab.class),
        @XmlElementRef(name = "permStart",type = PermStart.class),
        @XmlElementRef(name = "permEnd",type = PermEnd.class)
    })
	private List<Object> objects = new ArrayList<Object>();

	@XmlTransient
	private String tocBookMark;

	
	public Float getIndLeft() {
		return indLeft;
	}

	public void setIndLeft(Float indLeft) {
		this.indLeft = indLeft;
	}

	public Float getIndFirstLine() {
		return indFirstLine;
	}

	public void setIndFirstLine(Float indFirstLine) {
		this.indFirstLine = indFirstLine;
	}

	public String getAlign() {
		return align;
	}

	public void setAlign(String align) {
		this.align = align;
	}

	public Long getNumId() {
		return numId;
	}

	public void setNumId(Long numId) {
		this.numId = numId;
	}

	public Long getIlvl() {
		return ilvl;
	}

	public void setIlvl(Long ilvl) {
		this.ilvl = ilvl;
	}

	public List<Object> getObjects() {
		return objects;
	}

	public void addObject(Object obj) {
		this.objects.add(obj);
	}
	
	
	
//	public static void main(String[] args) { 
//        try { 
//            JAXBContext jaxbContext = JAXBContext.newInstance(Paragraph.class); 
//            Paragraph para = new Paragraph(); 
//            para.setIndFirstLine(1.0);
//            
//            para.addObject(new Text("测试"));
//            
//            para.addObject(new Text("测试2"));
// 
//            Marshaller marshaller = jaxbContext.createMarshaller(); 
//            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); 
// 
//            StringWriter writer = new StringWriter(); 
//            marshaller.marshal(para, writer); 
//            String xml = writer.getBuffer().toString(); 
//            System.out.println(xml); 
// 
//            
//            File file = new File("C:/Users/znjqolf/Desktop/docx4j/template/text.xml");
//            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller(); 
//            Paragraph paragraph = (Paragraph)unmarshaller.unmarshal(new FileInputStream(file)); 
//            System.out.println(paragraph.getObjects().size()); 
// 
//        } catch (Exception e) { 
//            e.printStackTrace(); 
//        } 
//    }

	public String getTocTitle() {
		return tocTitle;
	}

	public void setTocTitle(String tocTitle) {
		this.tocTitle = tocTitle;
	}

	public String getTocBookMark() {
		return tocBookMark;
	}

	public void setTocBookMark(String tocBookMark) {
		this.tocBookMark = tocBookMark;
	}

	public Boolean getToc() {
		return toc;
	}

	public void setToc(Boolean toc) {
		this.toc = toc;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public String getLineRule() {
		return lineRule;
	}

	public void setLineRule(String lineRule) {
		this.lineRule = lineRule;
	}

	public void setObjects(List<Object> objects) {
		this.objects = objects;
	}

	public String getOrientation() {
		return orientation;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public String getAfter() {
		return after;
	}

	public void setAfter(String after) {
		this.after = after;
	}

	public Float getIndFirstLineChars() {
		return indFirstLineChars;
	}

	public void setIndFirstLineChars(Float indFirstLineChars) {
		this.indFirstLineChars = indFirstLineChars;
	}

	public BigInteger getBeforeLines() {
		return beforeLines;
	}

	public void setBeforeLines(BigInteger beforeLines) {
		this.beforeLines = beforeLines;
	}

	public BigInteger getAfterLines() {
		return afterLines;
	}

	public void setAfterLines(BigInteger afterLines) {
		this.afterLines = afterLines;
	}

	public BigInteger getColsNum() {
		return colsNum;
	}

	public void setColsNum(BigInteger colsNum) {
		this.colsNum = colsNum;
	}

	public Double getColsSpace() {
		return colsSpace;
	}

	public void setColsSpace(Double colsSpace) {
		this.colsSpace = colsSpace;
	}

	public String getSectPrType() {
		return sectPrType;
	}

	public void setSectPrType(String sectPrType) {
		this.sectPrType = sectPrType;
	}

	public String getPageBreakBefore() {
		return pageBreakBefore;
	}

	public void setPageBreakBefore(String pageBreakBefore) {
		this.pageBreakBefore = pageBreakBefore;
	}
}

package aurora.plugin.export.word.wml;

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
	private Double indLeft;
	
	@XmlAttribute
	private Double indFirstLine;
	
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
	private String line = "400";
	
	@XmlAttribute
	private String lineRule = "exact";
	
	@XmlAttribute
	private String orientation;
	
	@XmlAttribute
	private String tocTitle;	
	
	@XmlElementRefs({
        @XmlElementRef(name = "t", type = Text.class),
        @XmlElementRef(name = "img",type = Image.class),
        @XmlElementRef(name = "qr-code",type = QRCode.class),
        @XmlElementRef(name = "ptab",type = PTab.class),
        @XmlElementRef(name = "pbdr",type = PBdr.class)
    })
	private List<Object> objects = new ArrayList<Object>();

	@XmlTransient
	private String tocBookMark;

	
	public Double getIndLeft() {
		return indLeft;
	}

	public void setIndLeft(Double indLeft) {
		this.indLeft = indLeft;
	}

	public Double getIndFirstLine() {
		return indFirstLine;
	}

	public void setIndFirstLine(Double indFirstLine) {
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
}

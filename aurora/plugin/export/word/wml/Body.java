package aurora.plugin.export.word.wml;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "body")
@XmlAccessorType(XmlAccessType.FIELD)
public class Body {
	
	@XmlElementRefs({
        @XmlElementRef(name = "p", type = Paragraph.class),
        @XmlElementRef(name = "br",type = Break.class),
        @XmlElementRef(name = "toc",type = Toc.class),
        @XmlElementRef(name = "tbl",type = Table.class),
        @XmlElementRef(name = "chunk",type = AltChunk.class)
    })
	private List<Object> paras = new ArrayList<Object>();

	public List<Object> getParas() {
		return paras;
	}

	public void addPara(Object para) {
		this.paras.add(para);
	}
	
	
	public static void main(String[] args) { 
        try { 
            JAXBContext jaxbContext = JAXBContext.newInstance(Body.class); 
 
            
            File file = new File("C:/Users/znjqolf/Desktop/docx4j/template/text.xml");
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller(); 
            Body body = (Body)unmarshaller.unmarshal(new FileInputStream(file)); 
            System.out.println(body);  
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
    }

}

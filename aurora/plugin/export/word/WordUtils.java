package aurora.plugin.export.word;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.docx4j.XmlUtils;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.PageSizePaper;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTDocProtect;
import org.docx4j.wml.CTHeight;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.CTTabStop;
import org.docx4j.wml.CTVerticalJc;
import org.docx4j.wml.Color;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.FooterReference;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STAlgClass;
import org.docx4j.wml.STAlgType;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.STCryptProv;
import org.docx4j.wml.STDocProtect;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.STHeightRule;
import org.docx4j.wml.STHint;
import org.docx4j.wml.STLineSpacingRule;
import org.docx4j.wml.STPTabAlignment;
import org.docx4j.wml.STPTabLeader;
import org.docx4j.wml.STPTabRelativeTo;
import org.docx4j.wml.STShd;
import org.docx4j.wml.STTabJc;
import org.docx4j.wml.STTabTlc;
import org.docx4j.wml.STVerticalJc;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Tabs;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.Tr;
import org.docx4j.wml.TrPr;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.docx4j.wml.CTTblPrBase.TblStyle;
import org.docx4j.wml.P.Hyperlink;
import org.docx4j.wml.PPrBase.NumPr;
import org.docx4j.wml.PPrBase.NumPr.Ilvl;
import org.docx4j.wml.PPrBase.NumPr.NumId;
import org.docx4j.wml.R.Ptab;
import org.docx4j.wml.SectPr.PgMar;
import org.docx4j.wml.TcPrInner.GridSpan;

import aurora.plugin.export.word.wml.Body;
import aurora.plugin.export.word.wml.Break;
import aurora.plugin.export.word.wml.Document;
import aurora.plugin.export.word.wml.Footer;
import aurora.plugin.export.word.wml.Header;
import aurora.plugin.export.word.wml.Image;
import aurora.plugin.export.word.wml.PBdr;
import aurora.plugin.export.word.wml.PTab;
import aurora.plugin.export.word.wml.Paragraph;
import aurora.plugin.export.word.wml.Table;
import aurora.plugin.export.word.wml.TableTc;
import aurora.plugin.export.word.wml.TableTr;
import aurora.plugin.export.word.wml.Text;
import aurora.plugin.export.word.wml.Toc;


@SuppressWarnings("unchecked")
public class WordUtils {
	
	public static final int TWIP_CENTIMETER = 567;
	
	private static final String KEY_RUN_NUMID = "KEY_RUN_NUMID";
	private static final String KEY_TEMPLATE_FILE = "KEY_TEMPLATE_FILE";
	private static final String KEY_NUMBERING_DEFINITION_PART = "KEY_NUMBERING_DEFINITION_PART";
	
	public static final ThreadLocal threadLocal = new ThreadLocal();
	
	
	private static Object getObject(String key){
		Map map = (Map)threadLocal.get();		
		return map.get(key);
	}
	
	private static void putObject(String key,Object value){
		Map map = (Map)threadLocal.get();
		map.put(key, value);
	}
	
	public static WordprocessingMLPackage createWord(Document doc,File templateFile) throws Exception{
		
		threadLocal.set(new HashMap());
		putObject(KEY_TEMPLATE_FILE,templateFile);
		WordprocessingMLPackage wordMLPackage = WordUtils.createWordprocessingMLPackage(doc);
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		ObjectFactory factory = Context.getWmlObjectFactory();		
				
		
		SectPr docSectPr = wordMLPackage.getMainDocumentPart().getJaxbElement().getBody().getSectPr();
		PgMar pg = factory.createSectPrPgMar();
		Double top = doc.getTop()*TWIP_CENTIMETER;
		pg.setTop(BigInteger.valueOf(top.intValue()));
		Double bottom = doc.getBottom()*TWIP_CENTIMETER;
		pg.setBottom(BigInteger.valueOf(bottom.intValue()));
		Double left = doc.getLeft()*TWIP_CENTIMETER;
		pg.setLeft(BigInteger.valueOf(left.intValue()));
		Double right = doc.getRight()*TWIP_CENTIMETER;
		pg.setRight(BigInteger.valueOf(right.intValue()));
		docSectPr.setPgMar(pg);
		
		
		HeaderPart hp = null;
		Header header = doc.getHeader();
		if(header!=null && header.getPara()!=null){
			hp = WordUtils.addHeader(factory, wordMLPackage, header.getPara());
		}
		
		Footer footer = doc.getFooter();
		if(footer!=null){
			WordUtils.addFooter(factory, wordMLPackage);
		}
		
		String watermark = doc.getWatermark();
		if(watermark!=null){
			WordUtils.addWaterMark(hp,factory, wordMLPackage, watermark);
		}
		
		Body body = doc.getBody();
		List<Object> paras = body.getParas();
		
		int indexOfToc = WordUtils.findToc(paras);
		List<Map> tocs = new ArrayList();
		
		for (Object obj : paras) {
			Object p = null;
			if(obj instanceof Paragraph) {
				Paragraph paragraph = (Paragraph)obj;
				Boolean toc = paragraph.getToc();					
				if(toc){
					String tocTitle = "untitle";
					List objs = paragraph.getObjects();
					if(objs!=null && objs.size() > 0) {
						Object t = objs.get(0);
						if(t instanceof Text){
							tocTitle = ((Text)t).getText();		
						}										
					}
					Map bmk = new HashMap();
					String bookmark = UUID.randomUUID().toString();
					paragraph.setTocBookMark(bookmark);
					bmk.put(Toc.TOC_TITLE, tocTitle);
					bmk.put(Toc.TOC_BOOKMARK, bookmark);
					tocs.add(bmk);
				}
				p = WordUtils.createPara(wordMLPackage,factory,(Paragraph)obj);
			} else if(obj instanceof Break){
				p = WordUtils.createPageBreak(factory);
			}else if(obj instanceof Table){
				Table table = (Table)obj;
				p = WordUtils.createTable(wordMLPackage, factory, table);
			}
			if(p!=null) mdp.getJaxbElement().getBody().getContent().add(p);
		}
		
		//create TOC
		if(indexOfToc!= -1 && !tocs.isEmpty()){	
			mdp.getJaxbElement().getBody().getContent().add(indexOfToc++,WordUtils.createTOCHead(factory));	
			mdp.getJaxbElement().getBody().getContent().add(indexOfToc++,WordUtils.createTOCStart(factory));				
			for (Map toc : tocs) {
				mdp.getJaxbElement().getBody().getContent().add(indexOfToc++,WordUtils.createTOC(factory,toc));					
			}
			mdp.getJaxbElement().getBody().getContent().add(indexOfToc++,WordUtils.createTOCEnd(factory));
			mdp.getJaxbElement().getBody().getContent().add(indexOfToc++,WordUtils.createPageBreak(factory));
		}
		WordUtils.hideSpellAndGrammaticalErrors(wordMLPackage, factory);
		
		if(doc.getReadOnly()) {
			setReadOnly(wordMLPackage, true);
		}
		
		if(doc.getDebugger()){
			System.out.println(XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true));	
		}		
		return wordMLPackage;
	}
	/**
	 * 
	 * @param xml
	 * @param templateFile
	 * @return
	 * @throws Exception
	 */
	public static WordprocessingMLPackage createWord(String xml,File templateFile) throws Exception{		
		Document doc = unmarshalXML(xml);		
		return createWord(doc,templateFile);
		
		
	}
	
	
	private static Document unmarshalXML(String xml) throws JAXBException,FileNotFoundException {
		JAXBContext jaxbContext = JAXBContext.newInstance(Document.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		Document doc = (Document) unmarshaller.unmarshal(new StringReader(xml));
		return doc;
	}

	
	/**
	 * 功能描述：创建文档处理包对象
	 * @return  返回值：返回文档处理包对象
	 * @throws Exception
	 */
	public static WordprocessingMLPackage createWordprocessingMLPackage(Document doc) throws Exception {
		WordprocessingMLPackage wordMLPackage =  WordprocessingMLPackage.createPackage(PageSizePaper.valueOf(doc.getPageSize()),doc.getLandscape());
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();		
		wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
		ndp.setJaxbElement( (Numbering) XmlUtils.unmarshalString(initialNumbering));
		putObject(KEY_NUMBERING_DEFINITION_PART, ndp);
		return wordMLPackage;
	}
	
	/**
	 * 功能描述：获取文档的可用宽度
	 * @param wordPackage 文档处理包对象
	 * @return  返回值：返回值文档的可用宽度
	 * @throws Exception
	 */
	public static int getWritableWidth(WordprocessingMLPackage wordPackage)throws Exception{
		return wordPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips();
	}
	
	
	/**
	 * hideSpellAndGrammaticalErrors
	 * 
	 * @param wordPackage
	 * @param factory
	 * @throws InvalidFormatException
	 */
	public static void hideSpellAndGrammaticalErrors(WordprocessingMLPackage wordMLPackage,ObjectFactory factory) throws InvalidFormatException{
		DocumentSettingsPart ds = wordMLPackage.getMainDocumentPart().getDocumentSettingsPart();  
        if(ds == null){  
            ds = new DocumentSettingsPart();
        }  
        CTSettings cs = factory.createCTSettings();
        cs.setHideSpellingErrors(Context.getWmlObjectFactory().createBooleanDefaultTrue());
        cs.setHideGrammaticalErrors(Context.getWmlObjectFactory().createBooleanDefaultTrue());
        ds.setJaxbElement(cs);
        wordMLPackage.getMainDocumentPart().addTargetPart(ds); 
	}
	
	
	/**
	 * createTable
	 * 
	 * @param wordMLPackage
	 * @param factory
	 * @param table
	 * @return
	 * @throws Exception 
	 */
	public static Tbl createTable(WordprocessingMLPackage wordMLPackage, ObjectFactory factory, Table table) throws Exception{
		
		
//		SectPr docSectPr = wordMLPackage.getMainDocumentPart().getJaxbElement().getBody().getSectPr();
//		int pageWidth = docSectPr.getPgSz().getW().intValue();
		Tbl tbl = factory.createTbl();
		
		TblPr tblPr = factory.createTblPr();
		tbl.setTblPr(tblPr);
		
		Double indLeft = table.getIndLeft();
		if(indLeft!=null){
			indLeft = indLeft * TWIP_CENTIMETER;
			TblWidth w = factory.createTblWidth();
			w.setType("dxa");
			w.setW(BigInteger.valueOf(indLeft.intValue()));
			tblPr.setTblInd(w);
		}
		
		Double tblWidth = table.getWidth();
		if(tblWidth!=null){
			tblWidth = tblWidth * TWIP_CENTIMETER;
			TblWidth w = factory.createTblWidth();
			w.setType("dxa");
			w.setW(BigInteger.valueOf(tblWidth.intValue()));
			tblPr.setTblW(w);
		}
		
		
		Jc jc = factory.createJc();
		jc.setVal(JcEnumeration.fromValue(table.getAlign()));
		tblPr.setJc(jc);
		
		Boolean isBorder = table.getBorder();
		if(isBorder) {			
			TblStyle tblStyle = factory.createCTTblPrBaseTblStyle();
			tblStyle.setVal("TableGrid");
			tblPr.setTblStyle(tblStyle);			
		}
		
		for (TableTr tblTr : table.getTrs()) {
			Tr tr = factory.createTr();
			tbl.getContent().add(tr);
			TrPr trPr = factory.createTrPr();
			
			CTHeight ctHeight = new CTHeight();
			Double height = tblTr.getHeight()*TWIP_CENTIMETER;
			ctHeight.setVal(BigInteger.valueOf(height.intValue()));
			ctHeight.setHRule(STHeightRule.AT_LEAST);
			JAXBElement<CTHeight> trHeightElement = factory.createCTTrPrBaseTrHeight(ctHeight);
			trPr.getCnfStyleOrDivIdOrGridBefore().add(trHeightElement);			
			tr.setTrPr(trPr);
			
			for (TableTc tblTc : tblTr.getTcs()) {
				Tc tc = factory.createTc();
				tr.getContent().add(tc);	
				
				
				TcPr tcPr = factory.createTcPr();
				Double width = tblTc.getWidth();
				if(width!=null){	
					width = width*TWIP_CENTIMETER;
					tc.setTcPr(tcPr);
					TblWidth cellWidth = factory.createTblWidth();
					tcPr.setTcW(cellWidth);
					cellWidth.setType("dxa");
					cellWidth.setW(BigInteger.valueOf(width.intValue()));			
				}
				String fill = tblTc.getFill();
				if(fill!=null){
					CTShd shd = factory.createCTShd();
					shd.setVal(STShd.CLEAR);
					shd.setColor("auto");
					shd.setFill(fill);
					tcPr.setShd(shd);					
				}
				
				Integer span = tblTc.getSpan();
				if(span!=null){
					GridSpan gs = factory.createTcPrInnerGridSpan();
					gs.setVal(new BigInteger(span.toString()));
					tcPr.setGridSpan(gs);
				}
				
				String vAlign = tblTc.getVAlign();
				if(vAlign!=null){
					CTVerticalJc vjc = factory.createCTVerticalJc();
					vjc.setVal(STVerticalJc.fromValue(vAlign));
					tcPr.setVAlign(vjc);				
				}	
				
				tc.setTcPr(tcPr);
				for (Paragraph para : tblTc.getParas()) {
					tc.getContent().add(WordUtils.createPara(wordMLPackage,factory,para));
				}
			}			
		}		
		return tbl;
	}
	
	
	public static int findToc(List<Object> paras) {
		int i = 0;
		for (Object obj : paras) {
			if(obj instanceof Toc) {
				return i;
			}
			i++;
		}
		return -1;
	}
	
	
	public static R createImage(WordprocessingMLPackage wordprocessingMLPackage, ObjectFactory factory, HeaderPart part, File file) throws Exception {
		
		InputStream is = null;
		long length = file.length();
		if (length > Integer.MAX_VALUE) {
			System.out.println("File too large!!");
		}
		byte[] bytes = new byte[(int)length];
		int offset = 0,numRead = 0;
		try {
			is = new FileInputStream(file);
	        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	            offset += numRead;
	        }
	        // Ensure all the bytes have been read in
	        if (offset < bytes.length) {
	            System.out.println("Could not completely read file "+file.getName());
	        }
		} finally{
			if(is!=null) is.close();
		}
		
		BinaryPartAbstractImage imagePart;
		if(part!=null) {
			imagePart = BinaryPartAbstractImage.createImagePart(wordprocessingMLPackage,part, bytes);
		}else {
			imagePart = BinaryPartAbstractImage.createImagePart(wordprocessingMLPackage, bytes);
		}
		
			
        Inline inline = imagePart.createImageInline(null, null, 0, 1, false);
        
        // Now add the inline in w:p/w:r/w:drawing
		org.docx4j.wml.R  run = factory.createR();      
		org.docx4j.wml.Drawing drawing = factory.createDrawing();		
		run.getContent().add(drawing);		
		drawing.getAnchorOrInline().add(inline);		
		return run;
	}
	
	public static JAXBElement getWrappedFldChar(FldChar fldchar) {
		return new JAXBElement(new QName(Namespaces.NS_WORD12, "fldChar"),FldChar.class, fldchar);
	}
	
	public static Hyperlink createHyperlink(String name, String anchor) throws JAXBException {
		String hpl = "<w:hyperlink xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" w:anchor=\""+anchor+"\"><w:r><w:t>"+name+"</w:t></w:r><w:r> <w:tab /> </w:r><w:fldSimple w:instr=\"PAGEREF "+anchor+"\"> <w:r> <w:t></w:t> </w:r> </w:fldSimple> </w:hyperlink>";
		return (Hyperlink)XmlUtils.unmarshalString(hpl);
	}
	
	public static P createTOCHead(ObjectFactory factory){
		P  p = factory.createP();
		org.docx4j.wml.Text  t = factory.createText();
		t.setValue("目    录");
		R  run = factory.createR();
		run.getContent().add(t);
		
		org.docx4j.wml.Jc jc = factory.createJc();
		jc.setVal(JcEnumeration.CENTER);
		PPr ppr = factory.createPPr();
		ppr.setJc(jc);
		
		p.setPPr(ppr);
		RPr rPr = factory.createRPr();
		
		org.docx4j.wml.RFonts rf = new org.docx4j.wml.RFonts();
		rf.setHint(STHint.EAST_ASIA);
		rf.setAscii("宋体");
		rf.setHAnsi("宋体");
		rPr.setRFonts(rf);
		
		BooleanDefaultTrue bdt = Context.getWmlObjectFactory().createBooleanDefaultTrue();
		rPr.setBCs(bdt);
		rPr.setB(bdt);
		run.setRPr(rPr);			
		p.getContent().add(run);
		return p;
	}
	
	public static P createTOCStart(ObjectFactory factory){
		P p1 = factory.createP();
		R r11 = factory.createR();
		FldChar fldchar = factory.createFldChar();
		fldchar.setFldCharType(STFldCharType.BEGIN);
		r11.getContent().add(WordUtils.getWrappedFldChar(fldchar));
		p1.getContent().add(r11);

		R r12 = factory.createR();
		org.docx4j.wml.Text txt = new org.docx4j.wml.Text();
		txt.setSpace("preserve");
		txt.setValue("TOC \\o \"1-3\" \\h \\z \\u \\h");
		r12.getContent().add(factory.createRInstrText(txt));
		p1.getContent().add(r12);

		R r13 = factory.createR();
		FldChar fldchar2 = factory.createFldChar();
		fldchar2.setFldCharType(STFldCharType.SEPARATE);
		r13.getContent().add(WordUtils.getWrappedFldChar(fldchar2));
		p1.getContent().add(r13);
		return p1;
	}
	
	public static P createTOCEnd(ObjectFactory factory){
		P p3 = factory.createP();
		R r3 = factory.createR();
		FldChar fldchar3 = factory.createFldChar();
		fldchar3.setFldCharType(STFldCharType.END);
		r3.getContent().add(WordUtils.getWrappedFldChar(fldchar3));
		p3.getContent().add(r3);
		return p3;
	}
	
	public static P createTOC(ObjectFactory factory, Map<String,String> map) throws JAXBException {

		P p = factory.createP();		
		PPr ppr = factory.createPPr();
		Tabs tabs = factory.createTabs();
		CTTabStop cttab = factory.createCTTabStop();
		cttab.setVal(STTabJc.RIGHT);
		cttab.setLeader(STTabTlc.DOT);
		cttab.setPos(new BigInteger("9350"));//TODO:page width ?
		tabs.getTab().add(cttab);
		ppr.setTabs(tabs);
		p.setPPr(ppr);
		
		String title = map.get(Toc.TOC_TITLE);
		String bookmark = map.get(Toc.TOC_BOOKMARK);			
		Hyperlink link = createHyperlink(title,bookmark);
		p.getContent().add(link);
		return p;
	}
	
	
	
	public static P createPageBreak(ObjectFactory factory){
		Br breakObj = new Br();  
        breakObj.setType(STBrType.PAGE);  
        P p = factory.createP();  
        p.getContent().add(breakObj); 
        return p;
	}
	
	
	/**
	 * Surround the specified r in the specified p with a bookmark (with specified name and id)
	 */
	private static void bookmarkRun(P p, R r, String name, int id) {

		// Find the index
		int index = p.getContent().indexOf(r);

		if (index < 0) {
			System.out.println("P does not contain R!");
			return;
		}

		ObjectFactory factory = Context.getWmlObjectFactory();
		BigInteger ID = BigInteger.valueOf(id);

		// Add bookmark end first
		CTMarkupRange mr = factory.createCTMarkupRange();
		mr.setId(ID);
		JAXBElement<CTMarkupRange> bmEnd = factory.createBodyBookmarkEnd(mr);
		p.getContent().add(index + 1, bmEnd);

		// Next, bookmark start
		CTBookmark bm = factory.createCTBookmark();
		bm.setId(ID);
		bm.setName(name);
		JAXBElement<CTBookmark> bmStart = factory.createBodyBookmarkStart(bm);
		p.getContent().add(index, bmStart);
	}
	
	
	public static P createPara(WordprocessingMLPackage wordprocessingMLPackage, ObjectFactory factory, Paragraph para) throws Exception{
		return createPara(wordprocessingMLPackage,factory,null,para);		
	}
	
	
	public static P createPara(WordprocessingMLPackage wordprocessingMLPackage, ObjectFactory factory, HeaderPart part, Paragraph para) throws Exception{
		P  p = factory.createP();
		Double indFirstLine = para.getIndFirstLine();
		Double indLeft = para.getIndLeft();
		String align = para.getAlign();
		PPr ppr = factory.createPPr();
		
		
		PPrBase.Spacing spacing = factory.createPPrBaseSpacing();
		spacing.setAfter(new BigInteger(para.getAfter()));		
		ppr.setSpacing(spacing);
		
		//左悬挂 首行悬挂
		if(indFirstLine!=null || indLeft !=null){
			PPrBase.Ind ind = factory.createPPrBaseInd();
			if(indLeft != null ) {
				indLeft = indLeft * TWIP_CENTIMETER;
				ind.setLeft(BigInteger.valueOf(indLeft.intValue()));
			}
			if(indFirstLine != null ) {
				indFirstLine = indFirstLine * TWIP_CENTIMETER;
				ind.setFirstLine(BigInteger.valueOf(indFirstLine.intValue()));			
			}
			ppr.setInd(ind);
		}	
		
		
		SectPr docSectPr = wordprocessingMLPackage.getMainDocumentPart().getJaxbElement().getBody().getSectPr();	
		BigInteger pageWidth = docSectPr.getPgSz().getW();
		BigInteger pageHeight = docSectPr.getPgSz().getH();
		String orientation = para.getOrientation();
		SectPr sectPr = factory.createSectPr();
		sectPr.getEGHdrFtrReferences().addAll(docSectPr.getEGHdrFtrReferences());
		
		
		SectPr.PgSz pgSz = factory.createSectPrPgSz();
		if("portrait".equals(orientation)){			
			pgSz.setW(pageWidth);
			pgSz.setH(pageHeight);
//			pgSz.setOrient(STPageOrientation.fromValue("portrait"));
			sectPr.setPgSz(pgSz);
			ppr.setSectPr(sectPr);
		}else if("landscape".equals(orientation)){
			pgSz.setW(pageHeight);
			pgSz.setH(pageWidth);
//			pgSz.setOrient(STPageOrientation.fromValue("landscape"));
			sectPr.setPgSz(pgSz);
			ppr.setSectPr(sectPr);
		}
		
		
		Long numId = para.getNumId();
		Long ilvl = para.getIlvl();
		if(numId!=null && ilvl !=null){			
			Long runNumId = (Long)getObject(KEY_RUN_NUMID);
			if(runNumId==null){
				runNumId = numId;
				putObject(KEY_RUN_NUMID, runNumId);
			}
			
			if(numId > runNumId){				
				NumberingDefinitionsPart ndp = (NumberingDefinitionsPart)getObject(KEY_NUMBERING_DEFINITION_PART);
				long rid =0;
				for(;rid<numId;){
					rid=ndp.restart(runNumId, 0, 1);
				}
				putObject(KEY_RUN_NUMID, numId);
			}
			
			// Create and add <w:numPr>
		    NumPr numPr =  factory.createPPrBaseNumPr();
		    ppr.setNumPr(numPr);
		    
		    // The <w:ilvl> element
		    Ilvl ilvlElement = factory.createPPrBaseNumPrIlvl();
		    numPr.setIlvl(ilvlElement);
		    ilvlElement.setVal(BigInteger.valueOf(ilvl));
		    	    
		    // The <w:numId> element
		    NumId numIdElement = factory.createPPrBaseNumPrNumId();
		    numPr.setNumId(numIdElement);
		    numIdElement.setVal(BigInteger.valueOf(numId));
		}
		
		
		
		//align
		if(align!=null){
			Jc jc = factory.createJc();
			jc.setVal(JcEnumeration.fromValue(align));
			ppr.setJc(jc);
		}
		p.setPPr(ppr);
		
		List<Object> objs = para.getObjects();
		int i = 0;
		boolean hasImg = false;
		for(Object obj:objs){
			if(obj instanceof Text){
				Text text = (Text)obj;
				R run = createRun(factory,text);
				p.getContent().add(run);
				if(para.getToc()){
					bookmarkRun(p,run,para.getTocBookMark(),1); 
				}
			}else if(obj instanceof PTab) {
				PTab ptab = (PTab)obj;
				R run = factory.createR();				
				Ptab pt = factory.createRPtab();
				pt.setRelativeTo(STPTabRelativeTo.fromValue(ptab.getRelativeTo()));
				pt.setAlignment(STPTabAlignment.fromValue(ptab.getAlignment()));
				pt.setLeader(STPTabLeader.fromValue(ptab.getLeader()));
				run.getContent().add(pt);				
				p.getContent().add(run);
			}else if(obj instanceof PBdr) {
				PBdr pbdr = (PBdr)obj;
				org.docx4j.wml.PPrBase.PBdr pdr = factory.createPPrBasePBdr();
				ppr.setPBdr(pdr);
				createPDBorder(factory,pdr,pbdr.getTop());
				createPDBorder(factory,pdr,pbdr.getBottom());
				createPDBorder(factory,pdr,pbdr.getRight());
				createPDBorder(factory,pdr,pbdr.getLeft());
			}else if(obj instanceof Image){
				hasImg = true;
				Image img = (Image)obj;
				File file;
				if(Image.PATH_TYPE_RELATIVE.equals(img.getType())){
					File folder = (File)getObject(KEY_TEMPLATE_FILE);
					file = folder == null ? new File(img.getSrc()) : new File(folder.getParent(),img.getSrc());
				}else {
					file = new File(img.getSrc());
				}
				p.getContent().add(WordUtils.createImage(wordprocessingMLPackage, factory,part, file));
			}
			
			i++;
		}
		if(!hasImg){
			String line = para.getLine();
			String lineRule = para.getLineRule();
			if(line!=null || lineRule !=null){
				spacing.setLine(new BigInteger(line));
				spacing.setLineRule(STLineSpacingRule.fromValue(lineRule));
			}
		}
		return p;
	}
	
	
	private static void createPDBorder(ObjectFactory factory,org.docx4j.wml.PPrBase.PBdr pdr, Object obj){
		if(obj==null)return;
		CTBorder ctborder = factory.createCTBorder();
		PBdr.Border border = (PBdr.Border)obj;
		ctborder.setVal(STBorder.fromValue(border.getValue()));
		ctborder.setColor(border.getColor());
		ctborder.setSz(new BigInteger(border.getSz()));
		ctborder.setSpace(new BigInteger(border.getSpace()));
		if(border instanceof PBdr.Top){
			pdr.setTop(ctborder);
		}else if(border instanceof PBdr.Left) {
			pdr.setLeft(ctborder);
		}else if(border instanceof PBdr.Right) {
			pdr.setRight(ctborder);
		}else if(border instanceof PBdr.Bottom) {
			pdr.setBottom(ctborder);
		}
	}
	
	
	public static R createRun(ObjectFactory factory, Text text){
		R run = factory.createR();
		RPr rpr = factory.createRPr();;
		Boolean isBold = text.isBold();
		
		if(isBold){
			BooleanDefaultTrue bdt = Context.getWmlObjectFactory().createBooleanDefaultTrue();
			rpr.setBCs(bdt);
			rpr.setB(bdt);
		}
		
		String fontFamily = text.getFontFamily();
		RFonts rf = new RFonts();
		rf.setHint(STHint.EAST_ASIA);
		rf.setAscii(fontFamily);
		rf.setHAnsi(fontFamily);
		rpr.setRFonts(rf);
		
		Boolean italic = text.isItalic();
		if(italic){
			BooleanDefaultTrue bdt = Context.getWmlObjectFactory().createBooleanDefaultTrue();
			rpr.setI(bdt);
		}
		
		Color color = new Color();
		color.setVal(text.getFontColor());
		rpr.setColor(color);
		
		
		HpsMeasure sz = new HpsMeasure();
		sz.setVal(new BigInteger(text.getFontSize()));
		rpr.setSz(sz);
		rpr.setSzCs(sz);
		
		
		String underline = text.getUnderline();
		if(underline!=null){
			U u = factory.createU();
			u.setVal(UnderlineEnumeration.fromValue(underline));
			rpr.setU(u);
		}
		
		run.setRPr(rpr);
		
		org.docx4j.wml.Text t = factory.createText();
		String space = text.getSpace();
		if(space != null){
			t.setSpace(space);
		}		
		t.setValue(text.getText());
		run.getContent().add(t);
		
		return run;
	}
	
//	private static PPr createPPr(ObjectFactory factory,PPr ppr){
//		if(ppr == null){
//			ppr = factory.createPPr();
//		}
//		return ppr;
//	}
	
	/**
	 * add Header
	 * 
	 * @param factory
	 * @param wordprocessingMLPackage
	 * @param para
	 * @throws Exception
	 */
	public static HeaderPart addHeader(ObjectFactory factory,WordprocessingMLPackage wordprocessingMLPackage, Paragraph para) throws Exception{
		HeaderPart headerPart = new HeaderPart();
		headerPart.setPackage(wordprocessingMLPackage); 
    	Hdr hdr = factory.createHdr();
        hdr.getContent().add(createPara(wordprocessingMLPackage,factory,headerPart,para));
        headerPart.setJaxbElement(hdr); 
    	
		Relationship relationship = wordprocessingMLPackage.getMainDocumentPart().addTargetPart(headerPart);
		List<SectionWrapper> sections = wordprocessingMLPackage.getDocumentModel().getSections();
 
        SectPr sectPr = sections.get(sections.size() - 1).getSectPr();
        if (sectPr==null ) {
            sectPr = factory.createSectPr();
            wordprocessingMLPackage.getMainDocumentPart().addObject(sectPr);
            sections.get(sections.size() - 1).setSectPr(sectPr);
        }
 
        HeaderReference headerReference = factory.createHeaderReference();
        headerReference.setId(relationship.getId());
        headerReference.setType(HdrFtrRef.DEFAULT);
        sectPr.getEGHdrFtrReferences().add(headerReference);
        return headerPart;
	}
	
	
	/**
	 * add Footer
	 * 
	 * @param factory
	 * @param wordprocessingMLPackage
	 * @param para
	 * @throws Exception
	 */
	public static void addFooter(ObjectFactory factory,WordprocessingMLPackage wordprocessingMLPackage) throws Exception{
		FooterPart footer = new FooterPart(new PartName("/word/myfooter.xml"));
		footer.setPackage(wordprocessingMLPackage); 
		Ftr ftr = factory.createFtr();
    	
		P p = factory.createP();
		addFieldBegin(factory,p);
		
		
    	org.docx4j.wml.Jc jc = factory.createJc();
		jc.setVal(JcEnumeration.CENTER);
		PPr ppr = factory.createPPr();
		ppr.setJc(jc);
		p.setPPr(ppr);
    	
        R run = factory.createR();
        org.docx4j.wml.Text txt = new org.docx4j.wml.Text();
        txt.setSpace("preserve");
        txt.setValue(" PAGE   \\* MERGEFORMAT ");
        run.getContent().add(factory.createRInstrText(txt));
        p.getContent().add(run);
        addFieldEnd(factory,p);
    	
        ftr.getContent().add(p);
        footer.setJaxbElement(ftr); 
    	
		Relationship relationship = wordprocessingMLPackage.getMainDocumentPart().addTargetPart(footer);
		List<SectionWrapper> sections = wordprocessingMLPackage.getDocumentModel().getSections();
 
        SectPr sectPr = sections.get(sections.size() - 1).getSectPr();
        if (sectPr==null ) {
            sectPr = factory.createSectPr();
            wordprocessingMLPackage.getMainDocumentPart().addObject(sectPr);
            sections.get(sections.size() - 1).setSectPr(sectPr);
        }
 
        FooterReference footerReference = factory.createFooterReference();
        footerReference.setId(relationship.getId());
        footerReference.setType(HdrFtrRef.DEFAULT);
        sectPr.getEGHdrFtrReferences().add(footerReference);
	}
	
    /**
     * Every fields needs to be delimited by complex field characters. This method
     * adds the delimiter that precedes the actual field to the given paragraph.
     * @param paragraph
     */
    private static void addFieldBegin(ObjectFactory factory,P paragraph) {
        R run = factory.createR();
        FldChar fldchar = factory.createFldChar();
        fldchar.setFldCharType(STFldCharType.BEGIN);
        run.getContent().add(fldchar);
        paragraph.getContent().add(run);
    }
 
    /**
     * Every fields needs to be delimited by complex field characters. This method
     * adds the delimiter that follows the actual field to the given paragraph.
     * @param paragraph
     */
    private static void addFieldEnd(ObjectFactory factory,P paragraph) {
        FldChar fldcharend = factory.createFldChar();
        fldcharend.setFldCharType(STFldCharType.END);
        R run3 = factory.createR();
        run3.getContent().add(fldcharend);
        paragraph.getContent().add(run3);
    }
	
	
	/**
	 * Add watermark
	 * @param factory
	 * @param wordprocessingMLPackage
	 * @param watermark
	 * @throws Exception
	 */
	public static void addWaterMark(HeaderPart headerPart, ObjectFactory factory,WordprocessingMLPackage wordprocessingMLPackage,String watermark) throws Exception{
		
		boolean createHeaderPart = headerPart == null;
		Relationship relationship = null;
		Hdr hdr = null;
		if(createHeaderPart){
			headerPart = new HeaderPart();
			hdr = factory.createHdr();
			headerPart.setJaxbElement(hdr); 
			relationship =  wordprocessingMLPackage.getMainDocumentPart().addTargetPart(headerPart);	
		}else {
			hdr = headerPart.getJaxbElement();			
		}
		addWaterMarkToP(hdr,factory,wordprocessingMLPackage, headerPart,watermark);
		List<SectionWrapper> sections = wordprocessingMLPackage.getDocumentModel().getSections();
		   
		SectPr sectPr = sections.get(sections.size() - 1).getSectPr();
		// There is always a section wrapper, but it might not contain a sectPr
		if (sectPr==null ) {
			sectPr = factory.createSectPr();
			wordprocessingMLPackage.getMainDocumentPart().addObject(sectPr);
			sections.get(sections.size() - 1).setSectPr(sectPr);
		}
		if(createHeaderPart){
			HeaderReference headerReference = factory.createHeaderReference();
			headerReference.setId(relationship.getId());
			headerReference.setType(HdrFtrRef.DEFAULT);
			sectPr.getEGHdrFtrReferences().add(headerReference);
		}
	}	
	
	/**
	 * 功能描述：设置文档是否只读，包括内容和样式
	 * @param wordPackage  文档处理包对象
	 * @param isReadOnly   是否只读
	 * @throws Exception
	 * 
	 */
	public static void setReadOnly(WordprocessingMLPackage wordPackage , boolean isReadOnly)throws Exception{
		byte[] bt = "".getBytes();
		if(isReadOnly){
			bt = "123456".getBytes();
		}
		ObjectFactory factory = Context.getWmlObjectFactory();
		//创建设置文档对象
		DocumentSettingsPart ds = wordPackage.getMainDocumentPart().getDocumentSettingsPart();
		if(ds == null){
			ds = new DocumentSettingsPart();
		}
		CTSettings cs = ds.getJaxbElement();
		if(cs == null){
			cs = factory.createCTSettings();
		}
		//创建文档保护对象
		CTDocProtect cp = cs.getDocumentProtection();
		if(cp == null){
			cp = new CTDocProtect();
		}
		//设置加密方式
		cp.setCryptProviderType(STCryptProv.RSA_FULL);
		cp.setCryptAlgorithmClass(STAlgClass.HASH);
		//设置任何用户
		cp.setCryptAlgorithmType(STAlgType.TYPE_ANY);
		cp.setCryptAlgorithmSid(new BigInteger("4"));
		cp.setCryptSpinCount(new BigInteger("50000"));
		//只读
		if(isReadOnly){
			cp.setEdit(STDocProtect.READ_ONLY);
			cp.setHash(bt);
			cp.setSalt(bt);
			//设置内容不可编辑
			cp.setEnforcement(true);
			//设置格式不可编辑
			cp.setFormatting(true);
		}else{
			cp.setEdit(STDocProtect.NONE);
			cp.setHash(null);
			cp.setSalt(null);
			//设置内容不可编辑
			cp.setEnforcement(false);
			//设置格式不可编辑
			cp.setFormatting(false);
		}
		
		cs.setDocumentProtection(cp);
		ds.setJaxbElement(cs);
		//添加到文档主体中
		wordPackage.getMainDocumentPart().addTargetPart(ds);
	}
	
	
	
	private static void addWaterMarkToP(Hdr hdr, ObjectFactory factory, WordprocessingMLPackage wordprocessingMLPackage,Part sourcePart,String watermark) throws Exception {
		String openXML = "<w:p xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:w10=\"urn:schemas-microsoft-com:office:word\">"
//	            + "<w:pPr>"
//	                  + "<w:pStyle w:val=\"Header\"/>"
//	            +"</w:pPr>" 
	            + "<w:sdt>" 
	            +   "<w:sdtPr>"
	                  + "<w:id w:val=\"-1589924921\"/>"
	                  + "<w:lock w:val=\"sdtContentLocked\"/>"
	                  + "<w:docPartObj>"
	                        + "<w:docPartGallery w:val=\"Watermarks\"/>"
	                        + "<w:docPartUnique/>"
	                  +"</w:docPartObj>"
	            +"</w:sdtPr>"
	            + "<w:sdtEndPr/>"
	            + "<w:sdtContent>"
	                  + "<w:r>"
	                        + "<w:rPr>"
	                              + "<w:noProof/>"
	                              + "<w:lang w:eastAsia=\"zh-TW\"/>"
	                        +"</w:rPr>"
	                        + "<w:pict>"
	                              + "<v:shapetype adj=\"10800\" coordsize=\"21600,21600\" id=\"_x0000_t136\" o:spt=\"136\" path=\"m@7,l@8,m@5,21600l@6,21600e\">"
	                                    + "<v:formulas>"
	                                          + "<v:f eqn=\"sum #0 0 10800\"/>"
	                                          + "<v:f eqn=\"prod #0 2 1\"/>"
	                                          + "<v:f eqn=\"sum 21600 0 @1\"/>"
	                                          + "<v:f eqn=\"sum 0 0 @2\"/>"
	                                          + "<v:f eqn=\"sum 21600 0 @3\"/>"
	                                          + "<v:f eqn=\"if @0 @3 0\"/>"
	                                          + "<v:f eqn=\"if @0 21600 @1\"/>"
	                                          + "<v:f eqn=\"if @0 0 @2\"/>"
	                                          + "<v:f eqn=\"if @0 @4 21600\"/>"
	                                          + "<v:f eqn=\"mid @5 @6\"/>"
	                                          + "<v:f eqn=\"mid @8 @5\"/>"
	                                          + "<v:f eqn=\"mid @7 @8\"/>"
	                                          + "<v:f eqn=\"mid @6 @7\"/>"
	                                          + "<v:f eqn=\"sum @6 0 @5\"/>"
	                                    +"</v:formulas>"
	                                    + "<v:path o:connectangles=\"270,180,90,0\" o:connectlocs=\"@9,0;@10,10800;@11,21600;@12,10800\" o:connecttype=\"custom\" textpathok=\"t\"/>"
	                                    + "<v:textpath fitshape=\"t\" on=\"t\"/>"
	                                    + "<v:handles>"
	                                          + "<v:h position=\"#0,bottomRight\" xrange=\"6629,14971\"/>"
	                                    +"</v:handles>"
	                                    + "<o:lock shapetype=\"t\" text=\"t\" v:ext=\"edit\"/>"
	                              +"</v:shapetype>"
	                              + "<v:shape fillcolor=\"silver\" id=\"PowerPlusWaterMarkObject357476642\" o:allowincell=\"f\" o:spid=\"_x0000_s2049\" stroked=\"f\" style=\"position:absolute;margin-left:0;margin-top:0;width:527.85pt;height:131.95pt;rotation:315;z-index:-251658752;mso-position-horizontal:center;mso-position-horizontal-relative:margin;mso-position-vertical:center;mso-position-vertical-relative:margin\" type=\"#_x0000_t136\">"
	                                    + "<v:fill opacity=\".5\"/>"
	                                    + "<v:textpath string=\""+watermark+"\" style=\"font-family:&quot;Calibri&quot;;font-size:1pt\"/>"
	                                    + "<w10:wrap anchorx=\"margin\" anchory=\"margin\"/>"
	                              +"</v:shape>"
	                        +"</w:pict>"
	                  +"</w:r>"
	            +"</w:sdtContent>"
	      +"</w:sdt>"
	      + "</w:p>";
			
		P wp = (P) XmlUtils.unmarshalString(openXML);
		List list = hdr.getContent();
		P p = null;
		if(list!=null && list.size()>0){
			p = (P)list.get(0);
		}else {
			p = factory.createP();
			hdr.getContent().add(p);
		} 
		if(p!=null) p.getContent().add(wp.getContent().get(0));
		
	}
	
	
	static final String initialNumbering = "<w:numbering xmlns:ve=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" xmlns:w10=\"urn:schemas-microsoft-com:office:word\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:wne=\"http://schemas.microsoft.com/office/word/2006/wordml\">"
	    + "<w:abstractNum w:abstractNumId=\"0\">"
	    + "<w:nsid w:val=\"2DD860C0\"/>"
	    + "<w:multiLevelType w:val=\"multilevel\"/>"
	    + "<w:tmpl w:val=\"0409001D\"/>"
	    + "<w:lvl w:ilvl=\"0\">"
	        + "<w:start w:val=\"1\"/>"
	        + "<w:numFmt w:val=\"decimal\"/>"
	        + "<w:lvlText w:val=\"%1\"/>"
	        + "<w:lvlJc w:val=\"left\"/>"
	      + "<w:pPr>"
	        + "<w:tabs>"
	          + "<w:tab w:val=\"num\" w:pos=\"420\"/>"
	        + "</w:tabs>"
	        + "<w:ind w:left=\"420\" w:hanging=\"420\"/>"
	      + "</w:pPr>"
	      + "<w:rPr>"
	        + "<w:b/>"
	        + "<w:rFonts w:hint=\"eastAsia\" w:ascii=\"宋体\" w:hAnsi=\"宋体\"/>"
	        + "<w:color w:val=\"auto\"/>"
	        + "<w:sz w:val=\"24\"/>"
	        + "<w:szCs w:val=\"24\"/>"
	      + "</w:rPr>"
	    + "</w:lvl>"
	    + "<w:lvl w:ilvl=\"1\">"
	        + "<w:start w:val=\"1\"/>"
	        + "<w:numFmt w:val=\"decimal\"/>"
	        + "<w:lvlText w:val=\"%1.%2\"/>"
	        + "<w:lvlJc w:val=\"left\"/>"
		      + "<w:pPr>"
		        + "<w:tabs>"
		          + "<w:tab w:val=\"num\" w:pos=\"840\"/>"
		        + "</w:tabs>"
		        + "<w:ind w:left=\"840\" w:hanging=\"840\"/>"
		      + "</w:pPr>"
		      + "<w:rPr>"
		        + "<w:b/>"
		        + "<w:rFonts w:hint=\"eastAsia\" w:ascii=\"宋体\" w:hAnsi=\"宋体\"/>"
		        + "<w:color w:val=\"auto\"/>"
		        + "<w:sz w:val=\"24\"/>"
		        + "<w:szCs w:val=\"24\"/>"
		      + "</w:rPr>"
	    + "</w:lvl>"
	    + "<w:lvl w:ilvl=\"2\">"
	        + "<w:start w:val=\"1\"/>"
	        + "<w:numFmt w:val=\"decimal\"/>"
	        + "<w:lvlText w:val=\"(%3)\"/>"
	        + "<w:lvlJc w:val=\"left\"/>"
		      + "<w:pPr>"
		        + "<w:tabs>"
		          + "<w:tab w:val=\"num\" w:pos=\"1260\"/>"
		        + "</w:tabs>"
		        + "<w:ind w:left=\"1260\"  w:hanging=\"420\"/>"
		      + "</w:pPr>"
	    + "</w:lvl>"
	    + "<w:lvl w:ilvl=\"3\">"
	        + "<w:start w:val=\"1\"/>"
	        + "<w:numFmt w:val=\"lowerLetter\"/>"
	        + "<w:lvlText w:val=\"(%4)\"/>"
	        + "<w:lvlJc w:val=\"left\"/>"
		      + "<w:pPr>"
		        + "<w:tabs>"
		          + "<w:tab w:val=\"num\" w:pos=\"1680\"/>"
		        + "</w:tabs>"
		        + "<w:ind w:left=\"1680\" w:hanging=\"420\"/>"
		      + "</w:pPr>"
	    + "</w:lvl>"
	    + "<w:lvl w:ilvl=\"4\">"
	        + "<w:start w:val=\"1\"/>"
	        + "<w:numFmt w:val=\"lowerRoman\"/>"
	        + "<w:lvlText w:val=\"%5)\"/>"
	        + "<w:lvlJc w:val=\"left\"/>"
		      + "<w:pPr>"
		        + "<w:tabs>"
		          + "<w:tab w:val=\"num\" w:pos=\"2100\"/>"
		        + "</w:tabs>"
		        + "<w:ind w:left=\"2100\" w:hanging=\"420\"/>"
		      + "</w:pPr>"
	    + "</w:lvl>"
	    + "<w:lvl w:ilvl=\"5\">"
	        + "<w:start w:val=\"1\"/>"
	        + "<w:numFmt w:val=\"lowerRoman\"/>"
	        + "<w:lvlText w:val=\"(%6)\"/>"
	        + "<w:lvlJc w:val=\"left\"/>"
		      + "<w:pPr>"
		        + "<w:tabs>"
		          + "<w:tab w:val=\"num\" w:pos=\"2520\"/>"
		        + "</w:tabs>"
		        + "<w:ind w:left=\"2520\" w:hanging=\"420\"/>"
		      + "</w:pPr>"
	    + "</w:lvl>"
	    + "<w:lvl w:ilvl=\"6\">"
	        + "<w:start w:val=\"1\"/>"
	        + "<w:numFmt w:val=\"decimal\"/>"
	        + "<w:lvlText w:val=\"%7.\"/>"
	        + "<w:lvlJc w:val=\"left\"/>"
		      + "<w:pPr>"
		        + "<w:tabs>"
		          + "<w:tab w:val=\"num\" w:pos=\"2940\"/>"
		        + "</w:tabs>"
		        + "<w:ind w:left=\"2940\" w:hanging=\"420\"/>"
		      + "</w:pPr>"
	    + "</w:lvl>"
	    + "<w:lvl w:ilvl=\"7\">"
	        + "<w:start w:val=\"1\"/>"
	        + "<w:numFmt w:val=\"lowerLetter\"/>"
	        + "<w:lvlText w:val=\"%8.\"/>"
	        + "<w:lvlJc w:val=\"left\"/>"
		      + "<w:pPr>"
		        + "<w:tabs>"
		          + "<w:tab w:val=\"num\" w:pos=\"3360\"/>"
		        + "</w:tabs>"
		        + "<w:ind w:left=\"3360\" w:hanging=\"420\"/>"
		      + "</w:pPr>"
	    + "</w:lvl>"
	    + "<w:lvl w:ilvl=\"8\">"
	        + "<w:start w:val=\"1\"/>"
	        + "<w:numFmt w:val=\"lowerRoman\"/>"
	        + "<w:lvlText w:val=\"%9.\"/>"
	        + "<w:lvlJc w:val=\"left\"/>"
		      + "<w:pPr>"
		        + "<w:tabs>"
		          + "<w:tab w:val=\"num\" w:pos=\"3780\"/>"
		        + "</w:tabs>"
		        + "<w:ind w:left=\"3780\" w:hanging=\"420\"/>"
		      + "</w:pPr>"
	    + "</w:lvl>"
	+ "</w:abstractNum>"
	+ "<w:num w:numId=\"1\">"
	    + "<w:abstractNumId w:val=\"0\"/>"
	 + "</w:num>"
	+ "</w:numbering>";
}

package aurora.plugin.poi;

import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.xerces.util.XMLChar;



/**
 * Spreadsheet writer to directly generate OOXML spreadsheets instead of builing them in the Apache
 * POI object model, which eats up tons of memory. Taken from existing example by Yegor Kozlov
 * 
 * @see <a
 *      href="http://svn.apache.org/repos/asf/poi/trunk/src/examples/src/org/apache/poi/xssf/usermodel/examples/BigGridDemo.java">Code
 *      by Yegor Kozlov</a>
 * expand MergedRegion by zoulei1266@hand
 */
public class SpreadsheetWriter {
    private final Writer _out;

    private int _rownum;

    private String xmlEncoding = "UTF-8";
    
    private List mergedRegionList=new LinkedList();
    
    private Map<Integer,Short> columnWidthMap=new TreeMap<Integer,Short>();

    public SpreadsheetWriter(Writer out) {
        _out = out;
    }

    public SpreadsheetWriter(Writer out, String xmlEncoding) {
        _out = out;
        this.xmlEncoding = xmlEncoding;
    }

    public void beginSheet() throws IOException {
        _out.write("<?xml version=\"1.0\" encoding=\"" + xmlEncoding + "\"?>"
                + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        if(!columnWidthMap.isEmpty()){
        	_out.write("<cols>");
	        Set keySet=columnWidthMap.keySet();
	        Iterator iterator=keySet.iterator();
	        while (iterator.hasNext()) {
				Integer col =  (Integer)iterator.next();
				_out.write("<col min=\""+col+"\" max=\""+col+"\" width=\""+columnWidthMap.get(col)+"\" customWidth=\"1\"/>");				
			}
	        _out.write("</cols>");
        }
        _out.write("<sheetData>\n");
    }

    public void endSheet() throws IOException {
        _out.write("</sheetData>");   
        if(!mergedRegionList.isEmpty()){
        	_out.write("<mergeCells>");
        	BigGridUtil.sortList(mergedRegionList);
        	for(int i=0,l=mergedRegionList.size();i<l;i++){
        		_out.write("<mergeCell ref=\""+mergedRegionList.get(i)+"\"/>");
        	}
        	_out.write("</mergeCells>");
        }
        _out.write("</worksheet>");
    }

    /**
     * Insert a new row
     * 
     * @param rownum
     *            0-based row number
     */
    public void insertRow(int rownum) throws IOException {
        _out.write("<row r=\"" + (rownum + 1) + "\">\n");
        this._rownum = rownum;
    }

    /**
     * Insert row end marker
     */
    public void endRow() throws IOException {
        _out.write("</row>\n");
    }

    public void createCell(int columnIndex, String value, int styleIndex) throws IOException {
        String ref = new CellReference(_rownum, columnIndex).formatAsString();
        _out.write("<c r=\"" + ref + "\" t=\"inlineStr\"");
        if (styleIndex != -1)
            _out.write(" s=\"" + styleIndex + "\"");
        _out.write(">");
        _out.write("<is><t>" + santizeForXml(value) + "</t></is>");
        _out.write("</c>");
    }

    public void createCell(int columnIndex, String value) throws IOException {
        createCell(columnIndex, value, -1);
    }

    public void createCell(int columnIndex, double value, int styleIndex) throws IOException {
        String ref = new CellReference(_rownum, columnIndex).formatAsString();
        _out.write("<c r=\"" + ref + "\" t=\"n\"");
        if (styleIndex != -1)
            _out.write(" s=\"" + styleIndex + "\"");
        _out.write(">");
        _out.write("<v>" + value + "</v>");
        _out.write("</c>");
    }

    public void createCell(int columnIndex, double value) throws IOException {
        createCell(columnIndex, value, -1);
    }

    public void createCell(int columnIndex, boolean value) throws IOException {
        createCell(columnIndex, value, -1);
    }

    public void createCell(int columnIndex, boolean value, int styleIndex) throws IOException {
        String ref = new CellReference(_rownum, columnIndex).formatAsString();
        _out.write("<c r=\"" + ref + "\" t=\"b\"");
        if (styleIndex != -1)
            _out.write(" s=\"" + styleIndex + "\"");
        _out.write(">");
        _out.write("<v>" + (value ? 1 : 0) + "</v>");
        _out.write("</c>");
    }

    public void createCell(int columnIndex, Calendar value, int styleIndex) throws IOException {
        createCell(columnIndex, DateUtil.getExcelDate(value, false), styleIndex);
    }

    public void createCell(int columnIndex, Date value, int styleIndex) throws IOException {
        createCell(columnIndex, DateUtil.getExcelDate(value, false), styleIndex);
    }

    private String cData(String str) {
        return "<![CDATA[" + str + "]]>";
    }

    private String santizeForXml(String str) {
        StringBuilder strBuilder = new StringBuilder();

        boolean stringHasSpecial = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!XMLChar.isInvalid(c)) {
                strBuilder.append(c);
                stringHasSpecial = stringHasSpecial || charIsSpecial(c);
            }
        }

        if (stringHasSpecial) {
            return cData(strBuilder.toString());
        }

        return strBuilder.toString();
    }

    private boolean charIsSpecial(char c) {
        if (c == '&' || c == '<' || c == '>')
            return true;
        return false;
    }
    
    public void addMergedRegion(CellRangeAddress range){    	
    	mergedRegionList.add(range.formatAsString());
    }
    
    public void setCellWidth(int columnIndex,short width){
    	this.columnWidthMap.put(Integer.valueOf(columnIndex), Short.valueOf(width));
    }
    
    public void close() throws IOException{
    	if(_out!=null)
    		_out.close();
    }
}
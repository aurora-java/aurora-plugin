package aurora.plugin.poi;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelCellStyles {
    private CellStyle dateStyle;
    private CellStyle headerStyle;
    private CellStyle warningStyle;

    public ExcelCellStyles(Workbook wb) {
        CreationHelper helper = wb.getCreationHelper();
        DataFormat fmt = helper.createDataFormat();

        dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat(fmt.getFormat("yyyy-mm-dd hh:mm:ss"));

        headerStyle = wb.createCellStyle();
        Font headerFont = wb.createFont();
        headerFont.setFontName("宋体");
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);//加粗
        headerFont.setFontHeightInPoints((short) 12);// 字体大小
        headerStyle.setFont(headerFont);        
        headerStyle.setAlignment(CellStyle.ALIGN_CENTER);// 左右居中  
        headerStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);// 上下居中

        warningStyle = wb.createCellStyle();
        Font warningFont = wb.createFont();
        warningFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        warningFont.setColor(Font.COLOR_RED);
        warningStyle.setFont(warningFont);
    }

    public CellStyle getDateStyle() {
        return dateStyle;
    }

    public CellStyle getHeaderStyle() {
        return headerStyle;
    }

    public CellStyle getWarningStyle() {
        return warningStyle;
    }
}
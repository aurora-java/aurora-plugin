package aurora.plugin.poi;

import org.apache.poi.ss.usermodel.Workbook;

public interface IExcelBean {
	public Workbook getNewWorkbook();
	public int getRowLimit();
	public int getColLimit();
	public String getFileExtension();
	public String getMimeType();
}

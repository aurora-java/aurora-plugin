package aurora.plugin.poi;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

public class Excel2003Bean implements IExcelBean{
private int rowLimit;
	
	private int colLimit;
	
	private String fileExtension; 
	
	private String mimeType;
	public static final String KEY_XLS = "xls";
	
	public Excel2003Bean(){
		rowLimit = (int) Math.pow(2, 16); // 65536
        colLimit = (int) Math.pow(2, 8); // 256
        fileExtension = "."+KEY_XLS;
        mimeType = "application/vnd.ms-excel";       
	}
	
	public Workbook getNewWorkbook() {
        return new HSSFWorkbook();
    }
	
	public int getRowLimit() {
		return rowLimit;
	}

	public void setRowLimit(int rowLimit) {
		this.rowLimit = rowLimit;
	}

	public int getColLimit() {
		return colLimit;
	}

	public void setColLimit(int colLimit) {
		this.colLimit = colLimit;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
}

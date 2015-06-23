package aurora.plugin.poi;

import java.io.InputStream;
import java.text.SimpleDateFormat;

import org.apache.poi.hssf.record.ExtSSTRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import uncertain.composite.CompositeMap;

public class ExcelParse {
	public static CompositeMap parseFile(InputStream is,String suffix) throws Exception{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Workbook wb = null;
		Sheet sheet = null;
		Row row = null;
		Cell cell = null;
		CompositeMap excelData = new CompositeMap();
		
		if (".xls".equalsIgnoreCase(suffix)) {
			wb = new HSSFWorkbook(is);
		}else{
			throw new Exception("filetype is undefined");
		}	
//		} else if (".xlsx".equalsIgnoreCase(suffix)) {
//			wb = new XSSFWorkbook(is);
//		}
		for (int i = 0, sheetLength = wb.getNumberOfSheets(); i < sheetLength; i++) {
			sheet = wb.getSheetAt(i);
			CompositeMap sheetData = new CompositeMap("sheet");
			sheetData.put("name", sheet.getSheetName());
			for (int firstRow = sheet.getFirstRowNum(), lastRow = sheet
					.getLastRowNum(); firstRow <= lastRow; firstRow++) {
				row = sheet.getRow(firstRow);
				CompositeMap rowData = new CompositeMap("row");
				if (row != null) {					
					for (int firstCell = row.getFirstCellNum(), lastCell = row
							.getLastCellNum(); firstCell < lastCell; firstCell++) {
						cell = row.getCell(firstCell);
						if (cell != null) {
							if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
								if (DateUtil.isCellDateFormatted(cell))
									rowData.put("C" + firstCell, sdf
											.format(cell.getDateCellValue()));
								else
									rowData.put("C" + firstCell, cell
											.getNumericCellValue());
							} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
								rowData.put("C" + firstCell, cell.toString());
							} else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {

							} else {
								rowData.put("C" + firstCell, "undefined");
							}
						}
					}
					rowData.putLong("maxCell", row.getLastCellNum());
				}
				sheetData.addChild(rowData);
			}
			excelData.addChild(sheetData);
		}
		return excelData;
	}
}

package aurora.plugin.poi.usermodel;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import uncertain.composite.CompositeMap;

import aurora.plugin.dataimport.ImportExcel;

public class XLSXParse {
	DateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public void parseFile(InputStream is, ImportExcel importProcessor)
			throws IOException, SQLException {
		Workbook wb = new XSSFWorkbook(is);
		Sheet sheet = null;
		for (int i = 0, l = wb.getNumberOfSheets(); i < l; i++) {
			sheet = wb.getSheetAt(i);
			parseFile(sheet, importProcessor);
		}
	}

	void parseFile(Sheet sheet, ImportExcel importProcessor)
			throws SQLException {
		Row row;
		Cell cell;
		CompositeMap record;		
		String sheetName=sheet.getSheetName();
		boolean is_write=false;
		for (int i = 0, l = sheet.getLastRowNum(); i <= l; i++) {
			row = sheet.getRow(i);
			record = new CompositeMap("record");
			is_write=false;
			int maxCellNum = row.getLastCellNum();
			record.putInt("maxCell", maxCellNum);
			record.putString("sheetName", sheetName);
			for (int j = 0; j < maxCellNum; j++) {
				String value = null;
				cell = row.getCell(j);
				if (cell != null) {
					if (cell.getCellType() == Cell.CELL_TYPE_STRING)
						value = cell.getRichStringCellValue().toString();
					if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC){						
						
						try{
							Double.parseDouble(cell.toString());
							value=Double.toString(cell.getNumericCellValue());
						}catch(Exception e){								
							if(cell.getDateCellValue()!=null)
								value = df.format(cell.getDateCellValue());
						}										
					}					
					if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN)
						value = Boolean.toString(cell.getBooleanCellValue());
					if (value != null&&!"".equalsIgnoreCase(value)) {
						is_write=true;
						record.putString("C" + j, value);
					}
				}
			}
			if(is_write)
				importProcessor.saveLine(record, i);			
		}
	}
}

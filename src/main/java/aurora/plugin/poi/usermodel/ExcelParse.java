package aurora.plugin.poi.usermodel;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import uncertain.composite.CompositeMap;

import aurora.plugin.dataimport.ImportExcel;

public class ExcelParse {
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	FormulaEvaluator evaluator;

	public void parseFile(InputStream is, ImportExcel importProcessor,
			String suffix) throws IOException, SQLException {
		Workbook wb = null;
		if (ImportExcel.XLS_KEY.equalsIgnoreCase(suffix)) {
			wb = new HSSFWorkbook(is);
		} else if (ImportExcel.XLSX_KEY.equalsIgnoreCase(suffix)) {
			wb = new XSSFWorkbook(is);
		}
		evaluator = wb.getCreationHelper().createFormulaEvaluator();
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
		List<String> cellList = new LinkedList<String>();
		String sheetName = sheet.getSheetName();
		int l = sheet.getLastRowNum();
		if (sheet.getRow(0) == null)
			return;
//		System.out.println("导入文件sheet(" + sheetName + ")最后一行是：" + (l + 1));
		boolean is_write = false;
		boolean is_new = true;
		for (int i = 0; i <= l; i++) {
			row = sheet.getRow(i);
			if (row == null)
				continue;
			record = new CompositeMap("record");
			record.putBoolean("is_new", is_new);
			is_new = false;
			is_write = false;

			if (i == 0) {
				record.putString("sheetName", "sheetName");
			} else
				record.putString("sheetName", sheetName);
			int maxCellNum = row.getLastCellNum();

			for (int j = 0; j < maxCellNum; j++) {
				String value = null;
				cell = row.getCell(j);
				if (cell != null) {
					CellValue cellValue = evaluator.evaluate(cell);
					if (cellValue != null) {
						switch (cellValue.getCellType()) {
						case Cell.CELL_TYPE_BOOLEAN:
							value = Boolean.toString(cellValue
									.getBooleanValue());
							break;
						case Cell.CELL_TYPE_NUMERIC:
							if (DateUtil.isCellDateFormatted(cell)) {
								if (cell.getDateCellValue() != null)
									value = df.format(cell.getDateCellValue());
							} else {
								value = BigDecimal
										.valueOf(cellValue.getNumberValue())
										.stripTrailingZeros().toPlainString();
							}
							break;
						case Cell.CELL_TYPE_STRING:
							value = cellValue.getStringValue();
							break;
						case Cell.CELL_TYPE_BLANK:
							break;
						case Cell.CELL_TYPE_ERROR:
							break;

						// CELL_TYPE_FORMULA will never happen
						case Cell.CELL_TYPE_FORMULA:
							break;
						}
					}
					if (value != null && !"".equalsIgnoreCase(value)) {
						is_write = true;
						record.putString("C" + j, value);
						cellList.add(value);
					} else {
						cellList.add("");
					}
				} else {
					cellList.add("");
				}
			}
			if (i == 0) {
				int indexcount = 0;
				for (int index = cellList.size(); index > 0; index--) {
					String value = cellList.get(index - 1);
					if ("".equals(value)) {
						indexcount++;
						continue;
					} else {
						break;
					}
				}
				maxCellNum = maxCellNum - indexcount;
			}
			record.putInt("maxCell", maxCellNum);
			if (is_write)
				importProcessor.saveLine(record, i);
		}
	}
}

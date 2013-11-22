package aurora.plugin.excelreport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;
import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.core.UncertainEngine;

public class ExcelFactory {
	Map<String, CellStyle> styles;
	public final String KEY_CONTENT = "content";
	public final String KEY_FORMULA = "formula";
	private CreationHelper createHelper;
	private String templatePath;
	private String format;
	UncertainEngine uncertainEngine;

	Workbook wb = null;
	CompositeMap context;

	public Workbook getWorkbook() {
		return wb;
	}

	public CompositeMap getContext() {
		return context;
	}

	public void createExcel(CompositeMap context, ExcelReport excelReport)
			throws Exception {
		if (excelReport.getSheets() == null)
			return;
		this.context = context;
		this.uncertainEngine=excelReport.uncertainEngine;
		this.format = excelReport.getFormat();
		this.setTemplatePath(excelReport.getTemplate());
		if (ExcelReport.KEY_EXCEL2007_SUFFIX.equalsIgnoreCase(format)) {
			if (this.getTemplatePath() != null)
				wb = new XSSFWorkbook(this.getTemplateInputStream());
			else
				wb = new XSSFWorkbook();
		} else {
			if (this.getTemplatePath() != null)
				wb = new HSSFWorkbook(this.getTemplateInputStream());
			else
				wb = new HSSFWorkbook();
		}

		createHelper = wb.getCreationHelper();
		if (excelReport.getStyles() != null)
			styles = createStyles(wb, excelReport);
		int count=0;
		for (SheetWrap sheetObj : excelReport.getSheets()) {
			sheetObj.createSheet(this,count++);			
		}
		if (this.getTemplatePath() != null){
			if(wb instanceof HSSFWorkbook)  
				HSSFFormulaEvaluator.evaluateAllFormulaCells(wb);  
	        else if(wb instanceof XSSFWorkbook)  
	        	XSSFFormulaEvaluator.evaluateAllFormulaCells((XSSFWorkbook)wb); 
		}
		wb.write(excelReport.getOutputStream());
	}

	private Map<String, CellStyle> createStyles(Workbook wb,
			ExcelReport excelReport) {
		Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
		CellStyle style;
		for (CellStyleWrap cellStyleObj : excelReport.getStyles()) {
			style = cellStyleObj.createStyle(wb);
			styles.put(cellStyleObj.getName(), style);
		}
		return styles;
	}

	public static Row createRow(Sheet sheet, int rownum) {
		Row row = sheet.getRow(rownum - 1);
		if (row == null)
			row = sheet.createRow(rownum - 1);
		return row;
	}
	
	public static Cell createCell(Row row,int colIndex){
		Cell cell=row.getCell(colIndex);
		if(cell==null)
			cell=row.createCell(colIndex);
		return cell;
	}

	public static boolean isNotNull(Object value) {
		if (value != null && !"".equals(value))
			return true;
		else
			return false;
	}

	public CellStyle getStyle(String styleName) {
		if (this.styles != null) {
			CellStyle style = this.styles.get(styleName);
			return style;
		} else {
			return null;
		}
	}

	public CreationHelper getCreateHelper() {
		return this.createHelper;
	}

	public void setCellValue(Cell cell, Object value) {
		setCellValue(cell, value, null);
	}

	public void setCellValue(Cell cell, Object value, String dataType) {
		if (value == null)
			return;
		if (ExcelFactory.isNotNull(dataType)) {
			if (CellData.KEY_DATA_TYPE_STRING.equals(dataType))
				cell.setCellValue(getCreateHelper().createRichTextString(
						value.toString()));
			if (CellData.KEY_DATA_TYPE_NUMBER.equals(dataType))
				cell.setCellValue(Double.valueOf(value.toString())
						.doubleValue());
			else
				cell.setCellValue(getCreateHelper().createRichTextString(
						value.toString()));
		} else {
			if (value instanceof String) {
				cell.setCellValue(getCreateHelper().createRichTextString(
						(String) value));
				return;
			}
			if (value instanceof Number) {
				cell.setCellValue(Double.parseDouble(value.toString()));
				return;
			}
			if (value instanceof Date) {
				cell.setCellValue((Date) value);
				return;
			} else {
				cell.setCellValue(value.toString());
				return;
			}
		}
	}

	InputStream getTemplateInputStream() {
		File file = new File(templatePath);
		if (templatePath.lastIndexOf(this.format) > 0) {
			try {
				InputStream is = new FileInputStream(file);
				return is;
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new RuntimeException(
					"templatePath and fileName are  inconsistent");
		}
	}

	public String getTemplatePath() {
		return this.templatePath;
	}

	public void setTemplatePath(String templatePath) {
		if(templatePath==null)return;
		String path=TextParser.parse(templatePath, context);
		File file=new File(path);
		if(!file.exists()){
			ServletContext sc=(ServletContext)this.uncertainEngine.getObjectRegistry().getInstanceOfType(ServletContext.class);
			path=sc.getRealPath(path);			
			file=new File(path);
			if(file.exists()){
				this.templatePath=file.getAbsolutePath();
			}		
		}else{
			this.templatePath = file.getAbsolutePath();
		}		
	}
}

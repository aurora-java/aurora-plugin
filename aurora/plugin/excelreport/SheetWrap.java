package aurora.plugin.excelreport;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;

public class SheetWrap {
	String name;
	Boolean displayGridlines;
	DynamicContent dynamicContent;
	CellData[] staticContent;
	String autoSizeColumns;//0,1

	private int offsetRowIndex = 0;

	private Sheet excelSheet;
	ExcelFactory excelFactory;
	int totalCount = -1;

	public void createSheet(ExcelFactory excelFactory) {
		this.excelSheet=excelFactory.getWorkbook().getSheetAt(0);
		if(this.excelSheet==null)
			this.excelSheet = excelFactory.getWorkbook().createSheet(this.getName());
		else
			excelFactory.getWorkbook().setSheetName(0, this.getName());
		if(this.displayGridlines!=null)
			this.excelSheet.setDisplayGridlines(this.displayGridlines);		
		this.excelFactory = excelFactory;
		if (this.getDynamicContent() != null)
			this.offsetRowIndex = this.getDynamicContent().createContent(excelFactory, this.excelSheet);
		if (this.getStaticContent() != null)
			createStaticContent(excelFactory.getContext());
		autoSizeColumn();
	}
	
	void autoSizeColumn(){
		if(this.autoSizeColumns!=null){
			String[] autoSizeColumnArray= autoSizeColumns.split(",");
			for(String autoSizeColumn:autoSizeColumnArray){
				try{
					int col=new Integer(autoSizeColumn);
					this.excelSheet.autoSizeColumn(col);
				}catch(Exception e){
					e.printStackTrace();
				}
			}		
		}
	}

	void createStaticContent(CompositeMap context) {
		int rowIndex;
		int colIndex;

		Row row;
		Cell cell;
		CellStyle cellStyle;
		
		for (CellData cellConfig : this.getStaticContent()) {
			if (cellConfig.getOffset()) {
				rowIndex = this.offsetRowIndex + cellConfig.getRow();
			} else {
				rowIndex = cellConfig.getRow();
			}

			row = ExcelFactory.createRow(this.excelSheet, rowIndex);
			colIndex = CellReference.convertColStringToIndex(cellConfig
					.getCell());
			cell = ExcelFactory.createCell(row, colIndex);
			cellStyle = this.excelFactory.getStyle(cellConfig.getStyleName());

			if (ExcelFactory.isNotNull(cellStyle)) {
				cell.setCellStyle(cellStyle);
			}
			if (cellConfig.getRange() != null) {
				this.excelSheet.addMergedRegion(CellRangeAddress
						.valueOf(cellConfig.getRange()));
			}
			if (CellData.KEY_FORMULA.equals(cellConfig.getType())) {
				cell.setCellFormula(cellConfig.getValue());
			} else {
				String value = cellConfig.getValue();
				this.excelFactory.setCellValue(cell,
						TextParser.parse(value, context),
						cellConfig.getDataType());
			}
		}
	}	

	public DynamicContent getDynamicContent() {
		return dynamicContent;
	}

	public void addDynamicContent(DynamicContent dynamicContent) {
		this.dynamicContent = dynamicContent;
	}

	public CellData[] getStaticContent() {
		return staticContent;
	}

	public void setStaticContent(CellData[] staticContent) {
		this.staticContent = staticContent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getDisplayGridlines() {
		return displayGridlines;
	}

	public void setDisplayGridlines(boolean displayGridlines) {
		this.displayGridlines = displayGridlines;
	}

	public String getAutoSizeColumns() {
		return autoSizeColumns;
	}

	public void setAutoSizeColumns(String autoSizeColumns) {
		this.autoSizeColumns = autoSizeColumns;
	}	
	
}

package aurora.plugin.excelreport;

import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;
import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;
import uncertain.composite.XMLOutputter;
import uncertain.core.UncertainEngine;
import uncertain.ocm.OCManager;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;

public class ExcelReport extends AbstractEntry {
	String configPath;

	String format;
	UncertainEngine uncertainEngine;
	public final static String KEY_EXCEL_REPORT = "excel-report";
	OutputStream os;
	public CellStyleWrap[] styles;
	public SheetWrap[] sheets;

	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

	public ExcelReport(UncertainEngine uncertainEngine) {
		this.uncertainEngine = uncertainEngine;
	}

	public CellStyleWrap[] getStyles() {
		return styles;
	}

	public void setStyles(CellStyleWrap[] styles) {
		this.styles = styles;
	}

	public SheetWrap[] getSheets() {
		return sheets;
	}

	public void setSheets(SheetWrap[] sheets) {
		this.sheets = sheets;
	}
	
	public void setOutputStream(OutputStream os) {
		this.os = os;
	}

	public void run(ProcedureRunner runner) throws Exception {
		ExcelReport excelReport = null;
		CompositeMap context = runner.getContext();
		if (this.getSheets() == null && this.getConfigPath() != null) {
			OCManager mOCManager = this.uncertainEngine.getOcManager();

			CompositeMap config = (CompositeMap) context.getObject(this
					.getConfigPath());
			config.setNameSpace("dr", "aurora.plugin.excelreport");
			String configStr = XMLOutputter.defaultInstance().toXML(config, false);
			config = CompositeLoader.createInstanceForOCM().loadFromString(configStr,"UTF-8");
			System.out.println(config.toXML());
			excelReport = (ExcelReport) mOCManager.createObject(config);			
		}

		ServiceInstance svc = ServiceInstance.getInstance(context);
		boolean is_http = true;
		format = ".xlsx";
		String fileName = "/Users/zoulai/work/logs/bank" + format;
		excelReport.setFormat(format);
		fileName="excelReport";
		if (is_http) {
			HttpServletResponse response = ((HttpServiceInstance) svc)
					.getResponse();
			if(".xlsx".equals(format)){
				response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			}else{
				response.setContentType("application/vnd.ms-excel");
			}
			response.setCharacterEncoding("GBK");
			response.setHeader("Content-Disposition", "attachment; filename=\""
					+ new String(fileName.getBytes(), "ISO-8859-1") + format+"\"");
			os = response.getOutputStream();			
			excelReport.setOutputStream(os);
		} else {
			os = new FileOutputStream(fileName);
			excelReport.setOutputStream(os);
		}
		ExcelFactory f = new ExcelFactory();
		if (excelReport != null)
			f.createExcel(context, excelReport);
		else
			f.createExcel(context, this);
		if (is_http) {
			runner.stop();
			while (runner.getCaller() != null) {
				runner.getCaller().stop();
				runner = runner.getCaller();
			}
		}
	}

	public OutputStream getOutputStream() {
		return os;
	}

	public String getFormat() {
		return format;
	}
	
	public void setFormat(String format){
		this.format=format;
	}

}

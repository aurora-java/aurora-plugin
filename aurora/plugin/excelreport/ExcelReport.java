package aurora.plugin.excelreport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import javax.servlet.http.HttpServletResponse;

import org.dom4j.IllegalAddException;
import org.xml.sax.SAXException;

import aurora.plugin.export.task.IExcelTask;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;
import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;
import uncertain.composite.XMLOutputter;
import uncertain.core.UncertainEngine;
import uncertain.ocm.IObjectRegistry;
import uncertain.ocm.OCManager;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;

public class ExcelReport extends AbstractEntry {
	String configPath;
	String fileName;
	String format;
	UncertainEngine uncertainEngine;
	public final static String KEY_EXCEL_REPORT = "excel-report";
	public final static String KEY_EXCEL2003_SUFFIX = ".xls";
	public final static String KEY_EXCEL2007_SUFFIX = ".xlsx";
	public final static String KEY_EXCEL2003_MIME = "application/vnd.ms-excel";
	public final static String KEY_EXCEL2007_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	OutputStream os;
	public CellStyleWrap[] styles;
	public SheetWrap[] sheets;
	CompositeMap configObj;
	boolean enableTask = true;

	public ExcelReport(UncertainEngine uncertainEngine) {
		this.uncertainEngine = uncertainEngine;
	}

	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		ExcelReport excelReport = createExcelReport(context);
		if (excelReport == null)
			return;		
		String filename = excelReport.getFileName();
		if (filename != null) {
			if (filename.endsWith(KEY_EXCEL2007_SUFFIX)) {
				excelReport.setFormat(KEY_EXCEL2007_SUFFIX);
			} else if (filename.endsWith(KEY_EXCEL2003_SUFFIX)) {
				excelReport.setFormat(KEY_EXCEL2003_SUFFIX);
			} else {
				throw new IllegalAddException(filename + " illegal suffix");
			}
		}
		File tempFile = null;
		if (!enableTask) {
			tempFile = File.createTempFile("excelreport",
					excelReport.getFormat());
			os = new FileOutputStream(tempFile);
		} else {
			IObjectRegistry or=this.uncertainEngine.getObjectRegistry();
			IExcelTask excelTask=(IExcelTask)or.getInstanceOfType(IExcelTask.class);
			String fileFullPath = excelTask.getExcelDir()+"/"+excelReport.getFileName();
			os = new FileOutputStream(fileFullPath);
			context.putObject("/parameter/@file_path", fileFullPath,true);
		}
		excelReport.setOutputStream(os);
		try {
			new ExcelFactory().createExcel(context, excelReport);
		} catch (Exception e) {
			throw e;
		}
		if (!enableTask) {
			ServiceInstance svc = ServiceInstance.getInstance(context);
			HttpServletResponse response = ((HttpServiceInstance) svc)
					.getResponse();
			setResponseHeader(response, excelReport);
			transferOutputStream(response.getOutputStream(),
					new FileInputStream(tempFile));
			stopRunner(runner);
		}
	}

	ExcelReport createExcelReport(CompositeMap context) throws IOException,
			SAXException {
		ExcelReport excelReport = null;
		if (this.getSheets() == null && this.getConfigPath() != null) {
			CompositeMap config = (CompositeMap) context.getObject(this
					.getConfigPath());
			if (config != null) {
				String configStr = XMLOutputter.defaultInstance().toXML(config,
						false);
				try {
					setConfigObj(CompositeLoader.createInstanceForOCM()
							.loadFromString(configStr, "UTF-8"));
				} catch (IOException e) {
					throw e;
				} catch (SAXException e) {
					throw e;
				}
			}
			OCManager mOCManager = this.uncertainEngine.getOcManager();
			excelReport = (ExcelReport) mOCManager.createObject(getConfigObj());
		} else {
			excelReport = this;
		}
		return excelReport;
	}

	void stopRunner(ProcedureRunner runner) {
		runner.stop();
		while (runner.getCaller() != null) {
			runner.getCaller().stop();
			runner = runner.getCaller();
		}
	}

	void transferOutputStream(OutputStream os, InputStream is)
			throws IOException {
		ReadableByteChannel rbc = Channels.newChannel(is);
		WritableByteChannel wbc = Channels.newChannel(os);
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		try {
			while (rbc.read(buffer) != -1) {
				buffer.flip();
				wbc.write(buffer);
				buffer.compact();
			}
		} catch (IOException e) {
			throw e;
		} finally {
			wbc.close();
			rbc.close();
			is.close();
		}
	}

	void setResponseHeader(HttpServletResponse response, ExcelReport excelReport)
			throws UnsupportedEncodingException {
		if (KEY_EXCEL2007_SUFFIX.equals(format)) {
			response.setContentType(KEY_EXCEL2007_MIME);
		} else {
			response.setContentType(KEY_EXCEL2003_MIME);
		}
		try {
			response.setHeader("Content-Disposition", "attachment; filename=\""
					+ new String(excelReport.getFileName().getBytes(),
							"ISO-8859-1") + "\"");
		} catch (UnsupportedEncodingException e) {
			throw e;
		}
	}

	public OutputStream getOutputStream() {
		return os;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getConfigPath() {
		return configPath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
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

	public CompositeMap getConfigObj() {
		return configObj;
	}

	public void setConfigObj(CompositeMap configObj) {
		this.configObj = configObj;
	}

	public boolean getEnableTask() {
		return enableTask;
	}

	public void setEnableTask(boolean enableTask) {
		this.enableTask = enableTask;
	}	
}

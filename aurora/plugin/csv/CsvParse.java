package aurora.plugin.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.sql.SQLException;
import aurora.plugin.dataimport.ImportExcel;
import uncertain.composite.CompositeMap;

public class CsvParse {
	public void parseFile(InputStream is,ImportExcel importProcessor) throws IOException, SQLException{
		BufferedReader br=new BufferedReader(new InputStreamReader(is,"GBK"));
		String s;
		CompositeMap rowData=null;
		int rownum=0;
		while((s = br.readLine()) != null){
			rowData = new CompositeMap("record");
			String[] cells=s.split(importProcessor.getSeparator());
			int l=cells.length;
			for(int i=0;i<l;i++){
				rowData.putString("C"+i, cells[i]);
			}
			rowData.putLong("maxCell",l);
			importProcessor.saveLine(rowData, rownum++);
		}		
	}		
}

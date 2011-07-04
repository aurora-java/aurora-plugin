package aurora.plugin.cvs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import uncertain.composite.CompositeMap;

public class CsvParse {
	public static CompositeMap parseFile(InputStream is,String separator) throws IOException{
		BufferedReader br=new BufferedReader(new InputStreamReader(is,"GBK"));
		String s;
		CompositeMap excelData = new CompositeMap();
		CompositeMap sheetData = new CompositeMap("sheet");
		CompositeMap rowData=null;
		while((s = br.readLine()) != null){
			rowData = new CompositeMap("row");
			String[] cells=s.split(separator);
			int l=cells.length;
			for(int i=0;i<l;i++){
				rowData.putString("C"+i, cells[i]);
			}
			rowData.putLong("maxCell",l);
			sheetData.addChild(rowData);
		}
		excelData.addChild(sheetData);		
		return excelData;
	}
	public static void main(String[] args){
		String pathname="/Users/zoulei/Desktop/t.csv";
		File file=new File(pathname);		
		InputStream is=null;
		try {
			is = new FileInputStream(file);
			CompositeMap data=CsvParse.parseFile(is, "  ");			
			System.out.print(data.toXML());
		} catch (Exception e) {		
			e.printStackTrace();
		}		
	}
}

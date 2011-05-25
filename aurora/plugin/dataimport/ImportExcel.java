package aurora.plugin.dataimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;

import aurora.database.service.SqlServiceContext;
import aurora.plugin.poi.ParseExcel;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

public class ImportExcel extends AbstractEntry{
	public static final String DEFAULT_SUCCESS_FLAG = "/parameter/@ImportSuccess";
	public String header_id;
	public String user_id;
	public String job_id;
	public String template_code;
	public String attribute1;
	public String attribute2;
	public String attribute3;
	public String attribute4;
	public String attribute5;
	public String status_field=DEFAULT_SUCCESS_FLAG;	

	public void run(ProcedureRunner runner) throws Exception {
		int result=-1;		
		CompositeMap context = runner.getContext();
		validatePara(context);
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance.getInstance(context);
		
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload up = new ServletFileUpload(factory);
		SqlServiceContext sqlServiceContext = SqlServiceContext.createSqlServiceContext(context);
		Connection conn = sqlServiceContext.getConnection();
		List items = up.parseRequest(serviceInstance.getRequest());
		Iterator i = items.iterator();
		while (i.hasNext()) {
			FileItem fileItem = (FileItem) i.next();
			if (!fileItem.isFormField()) {
				ParseExcel parseExcel=new ParseExcel();
				String fileName=fileItem.getName();
				String suffix=fileName.substring(fileName.lastIndexOf("."));
				CompositeMap data=parseExcel.parseXls(fileItem.getInputStream(), suffix);				
	            result=save(conn,data);
			}
		}
        context.putObject(status_field, result,true);              
	}
	
	void validatePara(CompositeMap context){
		header_id=TextParser.parse(header_id, context);
		if(header_id==null&&"".equals(header_id))
			throw new IllegalArgumentException("header_id is undefined");
		user_id=TextParser.parse(user_id, context);
		if(user_id==null&&"".equals(user_id))
			throw new IllegalArgumentException("user_id is undefined");
		job_id=TextParser.parse(job_id, context);
		template_code=TextParser.parse(template_code, context);
		attribute1=TextParser.parse(attribute1, context);
		attribute2=TextParser.parse(attribute2, context);
		attribute3=TextParser.parse(attribute3, context);
		attribute4=TextParser.parse(attribute4, context);
		attribute5=TextParser.parse(attribute5, context);		
	}
	int save(Connection conn,CompositeMap data) throws SQLException {
		int is_success=0;
		PreparedStatement pstm = null;
		CallableStatement cstm=null;
		ResultSet rs = null;
		try{
			cstm=conn.prepareCall("{call fnd_interface_load_pkg.ins_fnd_interface_headers(?,?,?,?,?,?,?,?,?,?)}");		
			cstm.setLong(1, new Long(header_id));
			if(job_id==null)
				cstm.setNull(2, java.sql.Types.NUMERIC);
			else
				cstm.setLong(2, new Long(job_id));
			cstm.setString(3, "NEW");
			cstm.setLong(4, new Long(user_id));
			if(template_code==null)
				cstm.setNull(5, java.sql.Types.VARCHAR);
			else
				cstm.setString(5, template_code);
			if(attribute1==null)
				cstm.setNull(6, java.sql.Types.VARCHAR);
			else
				cstm.setString(6, attribute1);
			if(attribute2==null)
				cstm.setNull(7, java.sql.Types.VARCHAR);
			else
				cstm.setString(7, attribute2);
			if(attribute3==null)
				cstm.setNull(8, java.sql.Types.VARCHAR);
			else
				cstm.setString(8, attribute3);
			if(attribute4==null)
				cstm.setNull(9, java.sql.Types.VARCHAR);
			else
				cstm.setString(9, attribute4);
			if(attribute5==null)
				cstm.setNull(10, java.sql.Types.VARCHAR);
			else
				cstm.setString(10, attribute5);
			cstm.execute();
			saveLines(conn,data);
			if(template_code!=null){
				pstm = conn.prepareStatement("select t.execute_pkg from fnd_interface_templates t where t.enabled_flag='Y' and t.template_code='"+template_code.trim()+"'");
				rs=pstm.executeQuery();			
				if (rs.next()){
					String execute_pkg=rs.getString(1);
					if(execute_pkg!=null){
						cstm=conn.prepareCall("{call "+execute_pkg+"(?,?)}");
						cstm.setLong(1, new Long(header_id));
						cstm.registerOutParameter(2, java.sql.Types.NUMERIC);
						cstm.execute();
						Long result=cstm.getLong(2);
						is_success=result.intValue();
					}
				}
			}
		}catch (SQLException e) {
			throw e;
		}finally{
			if(cstm!=null)
				cstm.close();
			if(pstm!=null)
				pstm.close();
		}
		return is_success;
	}
	
	void saveLines(Connection conn,CompositeMap data) throws SQLException{
		Iterator it=null;
		Iterator iterator=data.getChildIterator();
		while (iterator.hasNext()) {
			CompositeMap sheet = (CompositeMap) iterator.next();
			it=sheet.getChildIterator();
			while (it.hasNext()) {
				CompositeMap row = (CompositeMap) it.next();
				saveLine(conn,row);
			}
		}		
	}
	void saveLine(Connection conn,CompositeMap data) throws SQLException{
		if(data.getLong("maxCell")==null)return;
		int maxcell=data.getLong("maxCell").intValue();	
		StringBuffer stringBuffer=new StringBuffer("fnd_interface_load_pkg.ins_fnd_interface_lines(?,?,?,?,?,?,?");
		for(int i=0;i<maxcell;i++){
			stringBuffer.append(",?");
		}
		stringBuffer.append(")");
		CallableStatement cstm=null;
		try{
			cstm=conn.prepareCall("{call "+stringBuffer+"}");			
			cstm.setLong(1, new Long(header_id));
			cstm.setNull(2,java.sql.Types.VARCHAR);
			cstm.setNull(3,java.sql.Types.VARCHAR);		
			cstm.setLong(4, new Long(user_id));
			cstm.setNull(5,java.sql.Types.NUMERIC);	
			cstm.setNull(6,java.sql.Types.VARCHAR);
			cstm.setNull(7,java.sql.Types.NUMERIC);		
			String valueString;
			for(int i=0;i<maxcell;i++){
				valueString=data.getString("C"+i);
				if(valueString==null)
					cstm.setNull(8+i,java.sql.Types.VARCHAR);	
				else 
					cstm.setString(8+i,valueString);				
			}
			cstm.execute();
		}catch (SQLException e) {
			throw e;
		}finally{
			if(cstm!=null)
				cstm.close();
		}		
	}
	public String getHeader_id() {
		return header_id;
	}

	public void setHeader_id(String header_id) {
		this.header_id = header_id;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getJob_id() {
		return job_id;
	}

	public void setJob_id(String job_id) {
		this.job_id = job_id;
	}

	public String getTemplate_code() {
		return template_code;
	}

	public void setTemplate_code(String template_code) {
		this.template_code = template_code;
	}

	public String getAttribute1() {
		return attribute1;
	}

	public void setAttribute1(String attribute1) {
		this.attribute1 = attribute1;
	}

	public String getAttribute2() {
		return attribute2;
	}

	public void setAttribute2(String attribute2) {
		this.attribute2 = attribute2;
	}

	public String getAttribute3() {
		return attribute3;
	}

	public void setAttribute3(String attribute3) {
		this.attribute3 = attribute3;
	}

	public String getAttribute4() {
		return attribute4;
	}

	public void setAttribute4(String attribute4) {
		this.attribute4 = attribute4;
	}

	public String getAttribute5() {
		return attribute5;
	}

	public void setAttribute5(String attribute5) {
		this.attribute5 = attribute5;
	}

	public String getStatus_field() {
		return status_field;
	}
	public void setStatus_field(String status_field) {
		if(status_field==null)
			status_field=DEFAULT_SUCCESS_FLAG;
		this.status_field = status_field;
	}
	
	public static void main(String[] args){
		String pathname="/Users/zoulei/Desktop/11.xls";
		File file=new File(pathname);
		ParseExcel parseExcel=new ParseExcel();
		InputStream is=null;
		try {
			is = new FileInputStream(file);
			CompositeMap data=parseExcel.parseXls(is, ".xls");			
			System.out.print(data.toXML());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}

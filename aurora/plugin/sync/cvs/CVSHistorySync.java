package aurora.plugin.sync.cvs;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.log.RlogCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.MessageEvent;
import org.quartz.JobExecutionContext;

import uncertain.composite.CompositeMap;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;

import aurora.plugin.quartz.AuroraJobDetail;
import aurora.plugin.quartz.SchedulerConfig;
import aurora.plugin.sync.SyncUtil;

public class CVSHistorySync extends CVSAdapter {
	String connectionString;
	String timezone = "PST";
	String dataSourceName;
	String jobName;

	IObjectRegistry registry;
	ILogger mLogger;
	StringBuffer comments;

	Connection conn;

	boolean is_success = true;
	
	boolean debug=false;

	final static String TIME_ZONE_KEY = "timezone";
	final static String DATASOURCE_NAME_KEY = "datasourcename";
	
	String headFrom;
	String headTo;
	String fileName;

	CompositeMap dataMap;
	Map<String, String> tagMap;
	CompositeMap rowMap;
	boolean is_begin = false;
	
	final String queryHistorySql = "select t.file_name,t.head,t.old_head from sys_cvs_file_temp t";
	final String saveSql = "sys_cvs_sync_pkg.save_sync_history(?,?,?,?,?,?,?,?,?,?)";

	public CVSHistorySync() {
		registry = SchedulerConfig.getObjectRegistry();
		mLogger = LoggingContext.getLogger("aurora.plugin.cvs.sync", registry);
	}

	public void execute(JobExecutionContext context) throws Exception {
		AuroraJobDetail detail = (AuroraJobDetail) context.getJobDetail();
		CompositeMap config = detail.getConfig();
		
		jobName = config.getString("name");
		dataSourceName = config.getString(DATASOURCE_NAME_KEY);
		timezone=config.getString(TIME_ZONE_KEY);
		debug=config.getBoolean("debug",false);	
		
		processSync();
	}
	
	public void processSync() throws Exception{
		CVSClient cvsclient = null;
		try {
			conn =SyncUtil.getConnection(dataSourceName, registry);			
			CVSLogSync logSync = new CVSLogSync();
			logSync.setJobName(jobName);
			logSync.setDebug(debug);
			logSync.syncLog(conn);
			connectionString = logSync.getConnectionString();
			syncHistory();

			if (!is_success) {
				throw new RuntimeException();
			}
			try {
				if (conn != null){
					SyncUtil.syncSuccess(conn, jobName);
					conn.commit();
				}
			} catch (SQLException e) {
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == 54) {
				mLogger.log(Level.WARNING, jobName
						+ " failed,ErrorCode:ORA-00054");
				return;
			}
			try {
				if (conn != null)
					conn.rollback();
			} catch (SQLException sqlException) {
			}
			mLogger.log(Level.SEVERE, jobName+":Exception",e);
			throw e;
		} catch (Exception e) {
			try {
				if (conn != null)
					conn.rollback();
			} catch (SQLException sqlException) {
			}			
			mLogger.log(Level.SEVERE, jobName+":Exception",e);
			throw e;
		} finally {
			SyncUtil.unlockSync(conn, jobName);
			SyncUtil.freeConnection(conn);			
			if (cvsclient != null)
				try {
					cvsclient.closeConnection();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	void syncHistory() throws SQLException, AuthenticationException,
			IOException, CommandException {
		Statement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(queryHistorySql);
			while (resultSet.next()) {
				fileName = resultSet.getString(1);
				headTo = resultSet.getString(2);
				headFrom = resultSet.getString(3);
				if (headFrom == null)
					headFrom = "";
				dataMap = new CompositeMap();
				tagMap = new HashMap<String, String>();
				executeHistoryCommand();
				processSyncHistory();
			}
		} finally {
			if (stmt != null)
				stmt.close();
			if (resultSet != null)
				resultSet.close();
		}
	}

	void executeHistoryCommand() throws AuthenticationException, IOException,
			CommandException {
		CVSClient cvsclient = new CVSClient(connectionString);
		cvsclient.addCVSListener(this);
		try {
			cvsclient.openConnection();
			RlogCommand command = new RlogCommand();
			command.setModule(fileName);
			command.setRevisionFilter(this.headFrom + ":" + this.headTo);
			cvsclient.excute(command);
		} finally {
			cvsclient.closeConnection();
		}
	}	

	// CVS获取数据的时候，触发这个messageSent方法，由于无法区分当前取到的数据为什么内容，因此加入syncStatus字段进行判断
	public void messageSent(MessageEvent event) {
		// 由于继承的该方法不能抛出异常，所以用is_success字段记录写入数据库是否成功
		this.is_success = !event.isError();
		String line = event.getMessage();
		PrintStream stream = event.isError() ? System.err : System.out;
		StringBuffer taggedLine = new StringBuffer();
		if (event.isTagged()) {
			String message = MessageEvent.parseTaggedMessage(taggedLine,
					"Daniel Six");
			// if we get back a non-null line, we have something
			// to output. Otherwise, there is more to come and we
			// should do nothing yet.
			if (message != null) {
				stream.println(message);
			}
		} else {
			if(debug)
				stream.println(line);
			dataConvert(line);
		}
	}

	void processSyncHistory() throws SQLException {
		CallableStatement cstm = null;
		try {
			Iterator it = dataMap.getChildIterator();
			if(it==null)
				System.out.println(dataMap.toXML());
			cstm = conn.prepareCall("{call " + saveSql + "}");
			while (it!=null&&it.hasNext()) {
				CompositeMap row = (CompositeMap) it.next();
				String head = row.getString("revision");
				String tag = null;
				if (tagMap.containsKey(head))
					tag = tagMap.get(head);				
				cstm.setString(1, jobName);
				cstm.setString(2, fileName);
				cstm.setString(3, row.getString("author"));
				cstm.setString(4, head);
				cstm.setString(5, row.getString("date"));
				cstm.setString(6, this.timezone);
				cstm.setString(7, row.getString("state"));
				cstm.setString(8, row.getString("lines"));
				cstm.setString(9, tag);
				cstm.setString(10, row.getString("comments"));
				cstm.execute();
			}
		} finally {
			if (cstm != null)
				cstm.close();
		}
	}

	void dataConvert(String line) {
		if(line.startsWith("RCS")){
			is_begin = true;
			rowMap=null;
		}
		if (line.startsWith("-----")) {			
			if (!is_begin)
				rowMap.putString("comments", comments.toString());
			rowMap = dataMap.createChild("record");
			is_begin = false;
			comments = new StringBuffer();	
			return;
		}
		if (line.startsWith("=====")) {
			rowMap.putString("comments", comments.toString());
			return;
		}
		
		if(rowMap!=null&&rowMap.containsKey("date")){
			if(comments.length()==0)
				comments.append(line);
			else{
				comments.append("\n");
				comments.append(line);
			}
				
			return;
		}

		if (line.startsWith("revision")){
			rowMap.putString("revision", line.split(" ")[1].trim());
			return;
		}
		if (line.startsWith("date")) {
			String[] array = line.split(";");
			for (String en : array) {
				if (en.startsWith("date")) {
					rowMap.putString("date", en.substring(5).trim());
				} else {
					rowMap.putString(en.split(":")[0].trim(),
							en.split(":")[1].trim());
				}
			}
			return;
		}
		if (line.startsWith("symbolic names")) {
			dataMap.put("symbolicNames", true);
			return;
		}
		if (dataMap.containsKey("symbolicNames") && line.startsWith("\t")) {			
			int index=line.lastIndexOf(":");			
			String key=line.substring(index+1).trim();
			String value=line.substring(0,index).trim();
			if (!tagMap.containsKey(key))
				tagMap.put(key, value);
			else{
				String tag=tagMap.get(key);
				tag+=","+value;
				tagMap.put(key, tag);
			}				
			return;
		}		
	}	
}

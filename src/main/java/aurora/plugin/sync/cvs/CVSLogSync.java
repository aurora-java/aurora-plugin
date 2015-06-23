package aurora.plugin.sync.cvs;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import java.util.Map;
import java.util.logging.Level;

import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.log.RlogCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.MessageEvent;

import aurora.plugin.quartz.SchedulerConfig;

import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;

public class CVSLogSync extends CVSAdapter {
	String syncModule;
	String jobName;
	String connectionString;
	String repositoryPath;
	boolean debug=false;
	
	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	boolean is_success = true;

	IObjectRegistry registry;
	ILogger mLogger;

	Connection conn;

	Map<String, String> dataMap = new HashMap<String, String>();

	final String getSyncInfoSql = "sys_cvs_sync_pkg.get_cvs_sync_info(?,?,?,?)";
	final String saveSql = "sys_cvs_sync_pkg.save_sync_log(?,?,?,?,?)";
	
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getConnectionString() {
		return connectionString;
	}

	public void setConnectionString(String connectionString) {
		this.connectionString = connectionString;
	}	

	public CVSLogSync() {
		registry = SchedulerConfig.getObjectRegistry();
		mLogger = LoggingContext.getLogger("aurora.plugin.cvs.sync", registry);
	}

	public void syncLog(Connection conn) throws SQLException,
			CommandAbortedException, AuthenticationException, IOException,
			CommandException {
		this.conn = conn;
		setSyncInfo();
		executeRlogCommand();
		if (!is_success)
			throw new RuntimeException("CVSLogSync");
	}

	void setSyncInfo() throws SQLException {
		CallableStatement cstm = null;
		try {
			cstm = conn.prepareCall("{call " + getSyncInfoSql + "}");
			cstm.setString(1, jobName);
			cstm.registerOutParameter(2, java.sql.Types.VARCHAR);
			cstm.registerOutParameter(3, java.sql.Types.VARCHAR);
			cstm.registerOutParameter(4, java.sql.Types.VARCHAR);
			cstm.execute();
			this.syncModule = cstm.getString(2);
			this.connectionString = cstm.getString(3);
			this.repositoryPath = cstm.getString(4);
		} finally {
			if (cstm != null)
				cstm.close();
		}
	}

	void executeRlogCommand() throws CommandAbortedException,
			AuthenticationException, IOException, CommandException {
		CVSClient cvsclient = new CVSClient(connectionString);
		cvsclient.addCVSListener(this);
		try {
			RlogCommand command = new RlogCommand();
			command.setModule(syncModule);
			command.setCVSCommand('N', null);
			command.setCVSCommand('h', null);
			cvsclient.excute(command);
		} finally {
			if (cvsclient != null)
				cvsclient.closeConnection();
		}
	}

	void setConnection(Connection conn) {
		this.conn = conn;
	}

	public void messageSent(MessageEvent event) {
		String line = event.getMessage().trim();
		if(event.isError()&&!line.startsWith("cvs rlog: Logging")){
			this.is_success = !event.isError();
		}		
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
			if (!"".equals(line.trim()) && !"No records selected.".equals(line))
				dataConvert(line);
			if (line.startsWith("=======")) {
				try {
					processSyncLog();
					dataMap = new HashMap<String, String>();
				} catch (SQLException e) {
					this.is_success = false;
					mLogger.log(Level.SEVERE, null, e);
				}
			}
		}
	}

	void processSyncLog() throws SQLException {
		CallableStatement cstm = null;
		try {
			cstm = conn.prepareCall("{call " + saveSql + "}");
			cstm.setString(1, jobName);
			cstm.setString(2, dataMap.get("fileName"));
			cstm.setString(3, dataMap.get("head"));			
			cstm.setString(4, dataMap.get("locks"));
			cstm.setString(5, dataMap.get("totalRevisions"));
			cstm.execute();
		} finally {
			if (cstm != null)
				cstm.close();
		}
	}

	void dataConvert(String line) {
		if (line.startsWith("RCS file")) {
			String[] array = line.split(":");
			String fileName = array[1];		
			fileName=fileName.substring(this.repositoryPath.length() + 2);	
			fileName=fileName.substring(0, fileName.lastIndexOf(","));
			dataMap.put("fileName",fileName.trim());
			return;
		}
		if (line.startsWith("head")) {
			dataMap.put("head",
					line.split(":").length > 1 ? line.split(":")[1].trim() : "");
			return;
		}
		if (line.startsWith("branch")) {
			dataMap.put("branch",
					line.split(":").length > 1 ? line.split(":")[1].trim() : "");
			return;
		}
		if (line.startsWith("locks")) {
			dataMap.put("locks",
					line.split(":").length > 1 ? line.split(":")[1].trim() : "");
			return;
		}
		if (line.startsWith("total revisions")) {
			dataMap.put("totalRevisions",
					line.split(":").length > 1 ? line.split(":")[1].trim() : "");
			return;
		}
	}

}

package aurora.plugin.sync.cvs;

import java.io.IOException;

import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.connection.ConnectionFactory;
import org.netbeans.lib.cvsclient.event.CVSListener;

public class CVSClient {
	/** Cvs clinet instance used to communicate with cvs server */
	private Client cvsclient;
	/** Cvs connect string */
	private CVSRoot cvsroot;
	/** Connection instance to keep connect with cvs server */
	private Connection connection;	

	/**
	 * Breaks the string representation of CVSClient into it's components:
	 * 
	 * The valid format (from the cederqvist) is:
	 * 
	 * :method:[[user][:password]@]hostname[:[port]]/path/to/repository
	 * 
	 * e.g. :pserver;username=anonymous;hostname=localhost:/path/to/repository
	 */
	public CVSClient(String connectionString) {
		cvsroot = CVSRoot.parse(connectionString);
		connection = ConnectionFactory.getConnection(cvsroot);
		cvsclient = new Client(connection, new StandardAdminHandler());		
	}
	
	/**
	 * Open connection to the cvs server <br>
	 * 
	 * @return connection to cvs server
	 * @throws AuthenticationException
	 * @throws CommandAbortedException
	 */
	public Connection openConnection() throws AuthenticationException,
			CommandAbortedException {		
		connection.open();
		return connection;
	}

	/**
	 * Close connection to the cvs server <br>
	 * */
	public void closeConnection() throws IOException {
		connection.close();
	}
	
	/**
	 * <p>
	 * Excute cvs command
	 * </p>
	 * 
	 * @param command
	 *            to be excute by the cliet
	 * @throws AuthenticationException
	 * @throws CommandAbortedException
	 * @throws IOException
	 * @throws CommandException
	 */
	public void excute(Command command) throws AuthenticationException,
			CommandAbortedException, IOException, CommandException {		
		// put the command to the console
		System.out.println("***Command***" + command.getCVSCommand());
		cvsclient.executeCommand(command, new GlobalOptions());				
	}
	
	public void addCVSListener(CVSListener listener){
		cvsclient.getEventManager().addCVSListener(listener);
	}
}

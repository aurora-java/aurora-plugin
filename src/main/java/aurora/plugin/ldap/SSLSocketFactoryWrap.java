package aurora.plugin.ldap;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class SSLSocketFactoryWrap extends SocketFactory {
	private static SSLSocketFactoryWrap defFactory;
	private SocketFactory factory;

	private SSLSocketFactoryWrap() {
		this.factory = SSLSocketFactory.getDefault();
	}

	public static synchronized SocketFactory getDefault() {
		if (defFactory == null)
			defFactory = new SSLSocketFactoryWrap();
		return defFactory;
	}

	public Socket createSocket(String host, int port) throws IOException,
			UnknownHostException {		
		Socket socket = this.factory.createSocket(host, port);
		return socket;
	}

	public Socket createSocket(String host, int port, InetAddress localHost,
			int localPort) throws IOException, UnknownHostException {		
		Socket socket = this.factory.createSocket(host, port, localHost,
				localPort);
		return socket;
	}

	public Socket createSocket(InetAddress host, int port) throws IOException {		
		Socket socket = this.factory.createSocket(host, port);
		return socket;
	}

	public Socket createSocket(InetAddress address, int port,
			InetAddress localAddress, int localPort) throws IOException {		
		Socket socket = this.factory.createSocket(address, port, localAddress,
				localPort);
		return socket;
	}

}

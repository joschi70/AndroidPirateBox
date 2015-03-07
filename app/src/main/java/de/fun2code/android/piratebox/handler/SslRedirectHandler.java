package de.fun2code.android.piratebox.handler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.fun2code.android.pawserver.listener.ServiceListener;
import de.fun2code.android.piratebox.PirateBoxService;
import de.fun2code.android.piratebox.util.NetworkUtil;

/**
 * SSL Redirect handler
 * 
 * This will set up a server socket which listens to port 443.
 * If a client connects it will be redirected to http://pirate.box.
 * This is not likely to work well, because of wrong certificates for the
 * requested site. Most browsers will reject the connection.
 * 
 * 
 * Note:
 * There are too many browser problems due to wrong certificate.
 * Another problem is that Android seems to be unable to close the SSL
 * socket correctly.
 * It is not recommended to use this hander!
 * 
 * @author joschi
 *
 */
public class SslRedirectHandler implements Handler, ServiceListener {
	private int PORT;
	private String PASSPHRASE = "android";
	private String KEYSTORE = null;
	
	// Variables
	private static final String CERT = "cert"; // cert file variable
	
	private static String MESSAGE = "<html><head><title>Moved</title></head><body><h1>Moved</h1>" +
			"<p>This page has moved to <a href=\"http://pirate.box/\">http://pirate.box/</a>.</p>" +
			"</body></html>";
	
	private static String REDIRECT = "HTTP/1.1 301 Moved Permanently\r\n"
				+ "Location: http://pirate.box/\r\n"
				+ "Content-Type: text/html\r\n"
				+ "Content-Length: " + MESSAGE.length() + "\r\n\r\n" + MESSAGE;

	private String prefix;
	private Server server;
	private String apIp;
	private NetworkUtil netUtil;
	private ServerSocket ssocket;
	private SharedPreferences preferences;
	private AcceptThread acceptThread;

	@Override
	public boolean init(Server server, String prefix) {
		try {
			this.prefix = prefix;
			this.server = server;
			
			preferences = PreferenceManager.getDefaultSharedPreferences(PirateBoxService.getService());
			
			
			// Only initializes if SSL redirect is enabled in settings
			/*
			if(!preferences.getBoolean(Constants.PREF_SSL_REDIRECT, false)) {
				return false;
			}
			*/
	
			PORT = Integer.valueOf(PirateBoxService.getService().getPawServer().port) + 1;
			KEYSTORE = server.props.getProperty(prefix + CERT, PirateBoxService.pawHome + "conf/certs/pawKeystore");
			
			netUtil = new NetworkUtil(PirateBoxService.getService());
			apIp = netUtil.getApIp(2000);

		
			SSLContext ctx = SSLContext.getInstance("TLS");

			KeyManagerFactory kmf = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			// BKS
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

			ks.load(new FileInputStream(KEYSTORE),
					PASSPHRASE.toCharArray());
			kmf.init(ks, PASSPHRASE.toCharArray());
			ctx.init(kmf.getKeyManagers(), null, null);

			SSLServerSocketFactory ssf = ctx.getServerSocketFactory();
			ssocket = ssf.createServerSocket(PORT);
			acceptThread = new AcceptThread();
			acceptThread.start();
			
			PirateBoxService.registerServiceListener(this);

			netUtil.redirectPort(NetworkUtil.IpTablesAction.IP_TABLES_ADD, apIp, NetworkUtil.PORT_HTTPS, PORT);
		} catch (Exception e) {
			e.printStackTrace();
			server.log(Server.LOG_ERROR, prefix, e.getMessage());
			return false;
		}

		return true;
	}

	@Override
	public boolean respond(Request request) throws IOException {
		// Always false
		return false;
	}

	@Override
	public void onServiceStart(boolean success) {
		// Do nothing

	}

	@Override
	public void onServiceStop(boolean success) {
		// Shutdown the server socket
		server.log(Server.LOG_INFORMATIONAL, prefix,
				"Server was stopped ... shutting down SSL socket");
		
		// Clear redirection
		netUtil.redirectPort(NetworkUtil.IpTablesAction.IP_TABLES_DELETE, apIp, NetworkUtil.PORT_HTTPS, PORT);
		
		// Close server socket
		PirateBoxService.unregisterServiceListener(this);

		if(acceptThread != null) {
			acceptThread.closeSockets();
		}
	}

	/**
	 * Thread that accepts the connections
	 * 
	 * @author joschi
	 *
	 */
	class AcceptThread extends Thread {
		// Keep track of all sockets
		List<Socket> sockets = new ArrayList<Socket>();
		
		@Override
		public void run() {
			try {
				while (!ssocket.isClosed()) {
					Socket socket = ssocket.accept();
					server.log(Server.LOG_ERROR, prefix, "SSL Connection ... redirecting");
					sockets.add(socket);

					new SocketThread(socket, sockets).start();
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/**
		 * Close all sockets and the server socket
		 */
		public void closeSockets() {
			for(Socket sock : sockets) {
				try {
					sock.close();
				} catch (IOException e) {}
			}
			
			try {
				ssocket.close();
			} catch (IOException e) {
				e.printStackTrace();
				server.log(Server.LOG_ERROR, prefix, e.getMessage());
			}
		}
	}
	
	/**
	 * Thread handling the socket connection
	 * 
	 * @author joschi
	 *
	 */
	class SocketThread extends Thread {
		private Socket socket;
		private List<Socket> sockets;
		
		public SocketThread(Socket socket, List<Socket> sockets) {
			this.socket = socket;
			this.sockets = sockets;
		}
		
		@Override
		public void run() {
			try {
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();
			
			
				os.write(REDIRECT.getBytes());
				os.flush();
				os.close();
				is.close();
				socket.close();
				// Remove socket from sockets list
				sockets.remove(socket);
			}
			catch(Exception e) {
				server.log(Server.LOG_ERROR, prefix, e.getMessage());
			}
		}
	}

}

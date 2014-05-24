package de.fun2code.android.piratebox.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import android.content.Intent;
import android.util.Log;
import de.fun2code.android.piratebox.Constants;
import de.fun2code.android.piratebox.PirateBoxService;


/**
 * Handler that counts user sessions and connections (MAC addresses).
 * To count user sessions the session cookie is used that is provided by the
 * {@code sunlabs.brazil.handler.CookieSessionHandler}
 * 
 * To get the MAC address that corresponds to an IP number, the file
 * {@literal /proc/net/arp} is inspected.
 * 
 * @author joschi
 *
 */
public class ConnectionCountHandler implements Handler {
	private String prefix;
	private static String COOKIE_NAME = "cookie";
	private static final String ARP_FILE = "/proc/net/arp";
	
	private static Set<String> cookies = new HashSet<String>();
	private static Set<String> macAddresses = new HashSet<String>();

	@Override
	public boolean init(Server server, String prefix) {
		this.prefix = prefix;
		cookies.clear();
		macAddresses.clear();
		
		return true;
	}

	@Override
	public boolean respond(Request request) throws IOException {
		String cookie = getSessionCookie(request);
		
		/*
		 * If cookie is not in list of cookies the file /proc/net/arp
		 * has to be inspected.
		 */
		if(cookie != null && !cookies.contains(cookie)) {
			cookies.add(cookie);
			// Send new session broadcast
			Intent intentSession = new Intent(Constants.BROADCAST_INTENT_CONNECTION);
			intentSession.putExtra(Constants.INTENT_CONNECTION_EXTRA_NUMBER, cookies.size());
			PirateBoxService.getService().sendBroadcast(intentSession);
			
			String mac = getMacAddress(request);
			
			/*
			 * If MAC address is not in list, add it an send a broadcast
			 */
			if(mac != null && !macAddresses.contains(mac)) {
				macAddresses.add(mac);
				Intent intentConnection = new Intent(Constants.BROADCAST_INTENT_SESSION);
				intentConnection.putExtra(Constants.INTENT_SESSION_EXTRA_NUMBER, macAddresses.size());
				PirateBoxService.getService().sendBroadcast(intentConnection);
			}
		}
		return false;
	}
	
	/**
	 * Returns the cookie that belongs to the request.
	 * If cookie was not found {@code null} is returned.
	 * 
	 * @param request	Request to use
	 * @return			cookie or {@code null} if no cookie was found
	 */
	private String getSessionCookie(Request request) {
		String cookieRes = null;
		
		if(request.headers.get("Cookie") != null) {
			cookieRes = request.headers.get("Cookie").replaceAll("^.*?" + COOKIE_NAME + "=([^;]*).*$", "$1");
		}
		
		return cookieRes != null && !cookieRes.equals(request.headers.get("Cookie")) ? cookieRes: null;
	}
	
	/**
	 * Retrieves the IP number of the request and returns the matching MAC
	 * address by inspecting /proc/net/arp.
	 * If no MAC address can be found {@code null} is returned.
	 * 
	 * @param request	Request to use
	 * @return			MAC address or {@code null} if the MAC address could not be found
	 */
	private String getMacAddress(Request request) {
		String mac = null;
		BufferedReader br = null;
		try {
			String ip = request.sock.getInetAddress().toString().replaceAll("[^1-9.]", "");
			File file = new File(ARP_FILE);
			br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
			  if(line.matches("^" + ip + "\\s+.*$")) {
			     mac = line.split("\\s+")[3];
			     break;
			  }
			}
		}
		catch(Exception e) {
			Log.e(Constants.TAG, "Can't get MAC address: " + e.toString());
		}
		finally {
			if(br != null) {
				try {
					br.close();
				} catch (IOException e) {
					Log.e(Constants.TAG, e.toString());
				}
			}
		}
		return mac;
	}
	
	/**
	 * Returns the current total connection count
	 * 
	 * @return		number of total connections
	 */
	public static int getConnectionCount() {
		return macAddresses.size();
	}
	
	/**
	 * Returns the current total session count
	 * 
	 * @return		number of total sessions
	 */
	public static int getSessionCount() {
		return cookies.size();
	}

}

package de.fun2code.android.piratebox.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import de.fun2code.android.pawserver.PawServerActivity;
import de.fun2code.android.pawserver.util.Utils;
import de.fun2code.android.piratebox.Constants;

public class NetworkUtil {
	
	private Context context;
	private WifiManager wifiMgr;
	private ConnectivityManager conMgr;
	
	public static int PORT_HTTP = 80;
	public static int PORT_HTTPS = 443;
	public static int PORT_DROOPY = 8080;
	public static final String WIFI_INTERFACE = "wl0.1";
	public static String DNSMASQ_BIN = "/system/bin/dnsmasq";
	public static String DNSMASQ_BIN_BACKUP = DNSMASQ_BIN + ".pb.backup";
	public static String IPTABLES_BIN = "iptables";
	
	public static enum WrapResult {
		NO_BACKUP,
		OK
	}
	
	// IP tables actions
	public enum IpTablesAction {
		IP_TABLES_ADD,
		IP_TABLES_DELETE
	}
	
	/**
	 * Constucts an NetWorkUtil object
	 * 
	 * @param context	Context to use
	 */
	public NetworkUtil(Context context) {
		this.context = context;
		wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	
	/**
	 * Returns the WiFi state
	 * 
	 * @return	{@code true} if WiFi is enabled, otherwise @{code false}
	 */
	public boolean isWifiEnabled() {
		return wifiMgr.isWifiEnabled();
	}
	
	/**
	 * Enables/disables WiFi
	 * 
	 * @param enable	{@code true} if WiFi should be enabled, otherwise @{code false}
	 * @return			{@code true} if the operation succeeded
	 */
	public boolean setWifiEnabled(boolean enable) {
		return wifiMgr.setWifiEnabled(enable);
	}
	
	/**
	 * Returns the current WiFi configuration
	 * 
	 * @return		current WiFi configuration
	 */
	public WifiConfiguration getWifiApConfiguration() {
		try {
			Method getWifiApConfiguration = WifiManager.class.getMethod("getWifiApConfiguration", new Class[] {});
			Object obj = getWifiApConfiguration.invoke(wifiMgr, new Object[] {});
			return obj != null ? (WifiConfiguration) obj : null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Enables/disables WiFi access point with the given WiFi configuration
	 * 
	 * @param config	WiFi configuratin to use
	 * @param enable	{@code true} to enable the access point, otherwise {@code false}
	 * @return	{@code true} if the operation succeeded
	 */
	public boolean setWifiApEnabled(WifiConfiguration config, boolean enable) {
		try {
			Method setWifiApEnabled = WifiManager.class.getMethod("setWifiApEnabled", new Class[] { WifiConfiguration.class, boolean.class });
			Object obj = setWifiApEnabled.invoke(wifiMgr, new Object[] { config, enable });
			return obj != null ? (Boolean) obj : false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Sets the WiFi access point configurtion
	 * 
	 * @param config	configuration to use
	 * @return			{@code true} if the operation succeeded
	 */
	public boolean setWifiApConfiguration(WifiConfiguration config) {
		try {
			Method setWifiApConfiguration = WifiManager.class.getMethod("setWifiApConfiguration", new Class[] { WifiConfiguration.class });
			Object obj = setWifiApConfiguration.invoke(wifiMgr, new Object[] { config });
			return obj != null ? (Boolean) obj : false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Creates the configuration for an open access point
	 * 
	 * @param ssid		SSID name to use
	 * @return			the created WiFi configuration
	 */
	public WifiConfiguration createOpenAp(String ssid) {
		WifiConfiguration config = new WifiConfiguration();
		config.SSID = ssid;
		config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
		
		return config;
	}
	
	/**
	 * Retrieves the IP address of the access point
	 * 
	 * @param timeout	timeout to use in millis
	 * @return		the access point's IP number of {@code null} if it could not
	 * 				be fetches withinn the specified timeout
	 */
	public String getApIp(long timeout) {
		long until = System.currentTimeMillis() + timeout;
		
		String ip;
		while((ip = Utils.getLocalIpAddress()) == null && System.currentTimeMillis() < until) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return ip;
	}
	
	/**
	 * Checks if mobile date is enabled
	 * 
	 * @return		{@code true} if mobile data is enabled, otherwise {@code false}
	 */
	public boolean getMobileDataEnabled() {
		try {
			Method getMobileDataEnabled = ConnectivityManager.class.getMethod("getMobileDataEnabled", new Class[] {});
			Object obj = getMobileDataEnabled.invoke(conMgr, new Object[] {});
			return obj != null ? (Boolean) obj : false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Turns mobile data on or off
	 * 
	 * @param enable	{@code true} to enable mobile data, otherwise {@code false}
	 * @return			{@code true} if the operation succeeded
	 */
	public boolean setMobileDataEnabled(boolean enable) {
		try {
			Method setMobileDataEnabled = ConnectivityManager.class.getMethod("setMobileDataEnabled", new Class[] { boolean.class });
			setMobileDataEnabled.invoke(conMgr, new Object[] { enable });
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Redirects a port by using iptables command
	 * 
	 * @param actionType		action type to use, can be {@code IP_TABLES_ADD}
	 * 							or {@code IP_TABLES_DELETE}
	 * @param apIp				access point IP number
	 * @param redirectFrom		redirect from port
	 * @param redirectTo		redirect to port
	 * @return					{@code true} if the operation succeeded
	 */
	public boolean redirectPort(IpTablesAction actionType, String apIp, int redirectFrom, int redirectTo) {
		  String action = "-" + (actionType == IpTablesAction.IP_TABLES_ADD ? "A" : "D");

		  String[] iptablesCmds = new String[] {
				  IPTABLES_BIN + " -t nat " + action + " OUTPUT -d 127.0.0.1 -p tcp --dport " + redirectFrom + " -j REDIRECT --to-ports " + redirectTo,
				  IPTABLES_BIN + " -t nat " + action + " OUTPUT -d " + apIp + " -p tcp --dport " + redirectFrom + " -j REDIRECT --to-ports " + redirectTo,
				  IPTABLES_BIN + " -t nat " + action + " PREROUTING -d " + apIp + " -p tcp --dport " + redirectFrom + " -j REDIRECT --to-ports " + redirectTo
		  };

		  ShellUtil shellUtil = new ShellUtil();
		  return shellUtil.execRootShell(iptablesCmds);
		}
	
	/**
	 * Wraps the /system/bin/dnsmasq command with a shell script that calls
	 * the backup dnsmasq file with --address parameter
	 * 
	 * @param apIp	Acess Point IP address to use
	 * @return		On success {@code WrapResult.OK} is returned, otherwise there was an error
	 */
	public WrapResult wrapDnsmasq(String apIp) {
		// Check if backup exists
		if(!new File(NetworkUtil.DNSMASQ_BIN_BACKUP).exists()) {
			return WrapResult.NO_BACKUP;
		}
		
		ShellUtil shellUtil = new ShellUtil();
		shellUtil.remountSystem("rw");
		
		String[] cmd = new String[] { "echo '#!/system/bin/sh' > " + NetworkUtil.DNSMASQ_BIN,   "echo 'exec " + NetworkUtil.DNSMASQ_BIN_BACKUP + " --address=/#/"
					+ apIp + " $*' >> " + NetworkUtil.DNSMASQ_BIN};
		shellUtil.execRootShell(cmd);
		
		shellUtil.remountSystem("ro");
		
		return WrapResult.OK;
	}
	
	/**
	 * Restores the original /system/bin/dnsmasq from backup
	 */
	public boolean unwrapDnsmasq() {
		if(!new File(NetworkUtil.DNSMASQ_BIN_BACKUP).exists()) {
			return false;
		}
		
		ShellUtil shellUtil = new ShellUtil();
		shellUtil.remountSystem("rw");
		
		String[] cmd = new String[] {"cp -pr " + NetworkUtil.DNSMASQ_BIN_BACKUP + " " + NetworkUtil.DNSMASQ_BIN};
		shellUtil.execRootShell(cmd);
		
		shellUtil.remountSystem("ro");
		
		return true;
	}
	
	/**
	 * Creates a backup of /system/bin/dnsmasq if necessary
	 */
	public boolean createDnsMasqBackup() {
		if(!new File(NetworkUtil.DNSMASQ_BIN_BACKUP).exists()) {
			ShellUtil shellUtil = new ShellUtil();
			shellUtil.remountSystem("rw");
			
			String[] cmd = new String[] {"cp -pr " + NetworkUtil.DNSMASQ_BIN + " " + NetworkUtil.DNSMASQ_BIN_BACKUP};
			shellUtil.execRootShell(cmd);
			
			shellUtil.remountSystem("ro");
		}
		
		return new File(NetworkUtil.DNSMASQ_BIN_BACKUP).exists();
	}
	
	/**
	 * Returns the IP address of the access point
	 * 
	 * @return	IP address of access point
	 */
	public static String getApIp(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean autoApStartup = preferences.getBoolean(Constants.PREF_AUTO_AP_STARTUP, true);
		String apIp =  preferences.getString(Constants.PREF_AP_IP, Constants.AP_IP_DEFAULT);
		
		// If AP auto start is enabled return AP IP, otherwise return current WiFi IP
		return autoApStartup ? apIp : getLocalIpAddress();
	}
	
	/**
	 * Returns the current IP Address
	 * 
	 * @return IP address
	 */
	public static String getLocalIpAddress() {
		String ipv4 = null;
		String ipv6 = null;
		
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				
				/*
				 * Skip inactive interfaces.
				 */
				try {
					Method upMethod = intf.getClass().getMethod("isUp", new Class[] {});
					
					if(upMethod != null) {
						Boolean isUp = (Boolean) upMethod.invoke(intf, new Object[] {});

						if(!isUp) {
							continue;
						}
					}
				}
				catch(Exception e) {
					/*
					 *  A NoSuchMethodException may occur.
					 *  isUp() is only available since API level 9.
					 */
				}
				
				boolean ipFound = false;
				
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
						ipv4 = inetAddress.getHostAddress().toString();
						ipFound = true;
					}
					else if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet6Address) {
						ipv6 = inetAddress.getHostAddress().toString();
						ipFound = true;
					}
				}
				
				// Wlan interface has priority. Break if found and IP available.
				if(intf.getName().toLowerCase().startsWith("wlan") && ipFound) {
					break;
				}
			}
		} catch (SocketException ex) {
			Log.e(PawServerActivity.TAG, ex.toString());
		}
		
		// IPv4 has priority
		return ipv4 != null ? ipv4  : ipv6;
	}
}

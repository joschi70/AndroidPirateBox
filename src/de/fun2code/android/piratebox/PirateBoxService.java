package de.fun2code.android.piratebox;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.paw.server.PawServer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;
import de.fun2code.android.pawserver.PawServerService;
import de.fun2code.android.pawserver.listener.ServiceListener;
import de.fun2code.android.piratebox.util.NetworkUtil;
import de.fun2code.android.piratebox.util.NetworkUtil.IpTablesAction;
import de.fun2code.android.piratebox.util.NetworkUtil.WrapResult;
import de.fun2code.android.piratebox.util.ServerConfigUtil;
import de.fun2code.android.piratebox.util.ShellUtil;


/**
 * The PirateBox serice class which handles the access point, network configuration
 * and startup up the web server.
 * 
 * @author joschi
 *
 */
public class PirateBoxService extends PawServerService implements ServiceListener {
	private WifiConfiguration orgApConfig;
	private boolean orgWifiState;
	//private boolean orgMobileDataState; // Not needed
	private NetworkUtil netUtil;
	private ShellUtil shellUtil;
	private PirateBoxService service;
	private SharedPreferences preferences;
	private boolean autoApStartup = true;
	private boolean emulateDroopy = true;
	
	private static List<StateChangedListener> listeners = new ArrayList<StateChangedListener>();;
	private static boolean apRunning, networkRunning, startingUp;
	
	/**
	 * Broadcast receiver which receives access point state change notifications
	 */
	private final BroadcastReceiver apReceiver = new BroadcastReceiver() {
		static final int WIFI_AP_STATE_DISABLING = 10;
		static final int WIFI_AP_STATE_DISABLED = 11;
		static final int WIFI_AP_STATE_ENABLED = 13;
		
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();

	        // If access point state changed
	        if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
	            int state = intent.getIntExtra("wifi_state", WifiManager.WIFI_STATE_UNKNOWN);
	            switch(state) {
	            	// If access point was enabled
	            	case WIFI_AP_STATE_ENABLED:
	            	case WifiManager.WIFI_STATE_ENABLED:
	            		if(!apRunning) {
		            		apRunning = true;
		            		
		            		int pid = shellUtil.waitForProcess(NetworkUtil.DNSMASQ_BIN_BACKUP, 4000);
		            		Log.i(TAG, "Process ID of " + NetworkUtil.DNSMASQ_BIN_BACKUP + ": " + pid);
		            		
		            		// Restore dnsmasq
		            		netUtil.unwrapDnsmasq();
		            		
		            		// Inform about unwrap result
		        			for(StateChangedListener listener : listeners) {
		            			listener.dnsMasqUnWrapped();
		            		}
		            		
		            		// Restore AP state
							netUtil.setWifiApConfiguration(orgApConfig);
		            		
		            		for(StateChangedListener listener : listeners) {
		            			listener.apEnabled(autoApStartup);
		            		}
		            		
		            		Intent apUpIntent = new Intent(Constants.BROADCAST_INTENT_AP);
		            		apUpIntent.putExtra(Constants.INTENT_AP_EXTRA_STATE, true);
		            		sendBroadcast(apUpIntent);
		            		
		            		Intent serviceIntent = new Intent(service,
		            				PirateBoxService.class);
	
		            		startService(serviceIntent);
	            		}
	            			            		
	            		break;
	            	// If access point was disabled
	            	case WIFI_AP_STATE_DISABLING:
	            	case WIFI_AP_STATE_DISABLED:
	            	case WifiManager.WIFI_STATE_DISABLING:
	            	case WifiManager.WIFI_STATE_DISABLED:
						if (apRunning) {
							unregisterReceiver(apReceiver);
							apRunning = false;
	
							for(StateChangedListener listener : listeners) {
								listener.apDisabled(autoApStartup);
							}
							
							Intent apDownIntent = new Intent(Constants.BROADCAST_INTENT_AP);
							apDownIntent.putExtra(Constants.INTENT_AP_EXTRA_STATE, false);
		            		sendBroadcast(apDownIntent);
		            		
		            		/*
		            		 *  If server is running, stop it
		            		 *  AP might have been stopped by external (user or app)
		            		 */
		            		if(PirateBoxService.isRunning()) {
		            			service.stopSelf();
		            		}
													
						}
	            		break;
	            }
	        }
	    }
	};

	@Override
	public void onCreate() {
		super.onCreate();
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		service = this;
		netUtil = new NetworkUtil(this);
		shellUtil = new ShellUtil();
		
		// Save original WiFi state, mobile data state and access point configuration 
		orgWifiState = netUtil.isWifiEnabled();
		//orgMobileDataState = netUtil.getMobileDataEnabled(); // Not needed
		orgApConfig = netUtil.getWifiApConfiguration();
		
		/*
		 * Individual settings.
		 */
		init();
		
		registerServiceListener(this);
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		autoApStartup = preferences.getBoolean(Constants.PREF_AUTO_AP_STARTUP, true);
		emulateDroopy = preferences.getBoolean(Constants.PREF_EMULATE_DROOPY, true);
		
		// If starting the access point is handled by the user
		if(!autoApStartup) {
			apRunning = true;
			
			for(StateChangedListener listener : listeners) {
				listener.apEnabled(autoApStartup);
			}
			
			Log.d(TAG, "Starting service...");
			return super.onStartCommand(intent, flags, startId);
		}
		
		if(apRunning) {
			Log.d(TAG, "Starting service...");
			return super.onStartCommand(intent, flags, startId);
		}
		else {
			Log.d(TAG, "Starting AccessPoint...");
			WrapResult wrapResult = netUtil.wrapDnsmasq(NetworkUtil.getApIp(this));
			
			// Inform about wrap result
			for(StateChangedListener listener : listeners) {
			    listener.dnsMasqWrapped(wrapResult);
			}
								
			startAp();
			
			return START_STICKY;
		}
	}

	@Override
	public WakeLock getWakeLock() {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				Constants.TAG);
		
		return wl;
	}


	@Override
	public WifiLock getWifiLock() {
		return super.getWifiLock();
	}


	@Override
	public void onDestroy() {
		if(autoApStartup) {
			stopAp();
		}
		else {
			apRunning = false;
			for(StateChangedListener listener : listeners) {
				listener.apDisabled(autoApStartup);
			}
		}
		
		teardownNetworking();
		super.onDestroy();
	}
	
	/**
	 * Starts up the access point
	 */
	public void startAp() {	
		startingUp = true;
		netUtil.setWifiEnabled(false);
		//netUtil.setMobileDataEnabled(true); // Not needed
		netUtil.setWifiApEnabled(null, false);
		
		IntentFilter filter = new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED");
		registerReceiver(apReceiver, filter);

		String ssid = preferences.getString(Constants.PREF_SSID_NAME, service.getResources().getString(R.string.pref_ssid_name_default));
		netUtil.setWifiApEnabled(netUtil.createOpenAp(ssid), true);
	}


	/**
	 * Stops the access point
	 */
	public void stopAp() {
		boolean restoreWiFi = true;
		
		// If AP has been stop from external (user/app), do not restore WiFi state
		if(!apRunning) {
			restoreWiFi = false;
			Log.i(TAG, "AP stopped from external ... do not restore WiFi");
		}
		
		netUtil.setWifiApEnabled(null, false);
		
		if(restoreWiFi) {
			netUtil.setWifiEnabled(orgWifiState);
		}
		
		//netUtil.setMobileDataEnabled(orgMobileDataState); // Not needed
	}
	
	
	/*
	 * Service options are:
	 * TAG = Tag name for message logging.
	 * startOnBoot = Indicates if service has been started on boot.
	 * isRuntime = If set to true this will  only allow local connections.
	 * serverConfig = Path to server configuration directory.
	 * pawHome = PAW installation directory.
	 * useWakeLock = Switch wakelock on or off.
	 * hideNotificationIcon = Set to true if no notifications should be shown.
	 * execAutostartScripts = Set to true if scripts inside the autostart directory should be executed onstartup.
	 * showUrlInNotification = Set to true if URL should be shown in notification.
	 * notificationTitle = The notification title.
	 * notificationMessage = The notification message.
	 * appName = Application name"
	 * activityClass = Activity class name.
	 * notificationDrawableId = ID of the notification icon to display.
	 */
	private void init() {
		TAG = "PirateBoxService";
		startedOnBoot = false;
		isRuntime = false;
		serverConfig = Constants.getInstallDir(this) + "/conf/server.xml";
		pawHome = Constants.getInstallDir(this) + "/";
		useWakeLock = preferences.getBoolean(Constants.PREF_KEEP_DEVICE_ON, false);
		useWifiLock = preferences.getBoolean(Constants.PREF_KEEP_DEVICE_ON, false);
		hideNotificationIcon = false;
		execAutostartScripts = false;
		showUrlInNotification = false;
		notificationTitle = getString(R.string.app_name);
		notificationMessage = getString(R.string.notification_message);
		appName = getString(R.string.app_name);
		activityClass = "de.fun2code.android.piratebox.PirateBoxActivity";
		notificationDrawableId = R.drawable.ic_notification;
		
		// Set default mayPost value
		PawServer.DEFAULT_MAX_POST = Constants.DEFAULT_MAX_POST;
		
		/*
		 * Check if maxPost setting is valid
		 */
		String maxPost = ServerConfigUtil.getServerSetting("maxPost", this);
		if(maxPost.length() > 0) {
			try {
        		Long.decode(maxPost).longValue();
        	}
        	catch(NumberFormatException e) {
        		String msg = new MessageFormat(
        			     getString(R.string.msg_max_post_invalid)).format(new Object[] { 
        			    		 Formatter.formatFileSize(this, Constants.DEFAULT_MAX_POST) });
        		 Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        	}
		}
		
		Log.i(TAG, "Home directory: " + Constants.getInstallDir(this));
	}
	
	/**
	 * Changes the dnsmasq configuration by killing the original dnsmasq
	 * process and starting a new one which answers all DNS queries with the IP
	 * of the access point.
	 */
	public void setupDnsmasq() {
		String apIp = NetworkUtil.getApIp(this);
		String baseIp = apIp.substring(0, apIp.lastIndexOf("."));
		
		if (shellUtil.getProcessPid(NetworkUtil.DNSMASQ_BIN) != -1) {
			// Kill dnsmasqd
			shellUtil.killProcessByName(NetworkUtil.DNSMASQ_BIN);

			// Start new dnsmasqd
			String[] dnsmasqCmd = new String[] { NetworkUtil.DNSMASQ_BIN
					+ " --no-resolv --no-poll --dhcp-range=" + baseIp +".2," + baseIp + ".254,1h --address=/#/"
					+ apIp + " --pid-file=" + getFilesDir().getAbsolutePath()
					+ "/dnsmasq.pid"

			};

			shellUtil.execRootShell(dnsmasqCmd);
		}
	}
	
	/**
	 * Redirects port 80 requests to the port of the running server
	 * 
	 * @param action
	 */
	public void doRedirect(IpTablesAction action) {
		// Redirect port 80
		netUtil.redirectPort(action, NetworkUtil.getApIp(this), NetworkUtil.PORT_HTTP, Integer.valueOf(getServerPort()));
		
		// Redirect port 8080 (Droopy support)
		if(emulateDroopy) {
			netUtil.redirectPort(action, NetworkUtil.getApIp(this), NetworkUtil.PORT_DROOPY, Integer.valueOf(getServerPort()));
		}
		
	
		for(StateChangedListener listener : listeners) {
			if(action == IpTablesAction.IP_TABLES_ADD) {
				listener.networkUp();
			}
			else {
				listener.networkDown();
			}
		}
		
		Intent intent = new Intent(Constants.BROADCAST_INTENT_NETWORK);
		intent.putExtra(Constants.INTENT_NETWORK_EXTRA_STATE, action == IpTablesAction.IP_TABLES_ADD ? true : false);
		sendBroadcast(intent);
	}
	
	/**
	 * Stops the PirateBox networking setup
	 */
	public void teardownNetworking() {
		shellUtil.killProcessByName(NetworkUtil.DNSMASQ_BIN);
		
		// Set iptables FORWARD chain to DROP to avoid Internet usage
		NetworkUtil.acceptChain(NetworkUtil.IPTABLES_CHAIN_FORWARD);
		
		doRedirect(IpTablesAction.IP_TABLES_DELETE);
		for(StateChangedListener listener : listeners) {
			listener.networkDown();
		}
		
		networkRunning = false;
	}
	
	/**
	 * Checks if the service is in startup phase
	 * 
	 * @return 		{@code true} if the service is in startup phase, otherwise
	 * 				{@code false}
	 */
	public static boolean isStartingUp() {
		return startingUp;
	}
	
	/**
	 * Checks if the access point has been started
	 * 
	 * @return		{@code true} if the access point has been started, otherwise
	 * 				{@code false}
	 */
	public static boolean isApRunning() {
		return apRunning;
	}
	
	/**
	 * Checks if the PirateBox networking is set up
	 * 
	 * @return		{@code true} if the networking has been set up, otherwise
	 * 				{@code false}
	 */
	public static boolean isNetworkRunning() {
		return networkRunning;
	}
	
	/**
	 * Registers a StateChangedListener which will be informed if the 
	 * state of access point, networking or web service changes
	 * 
	 * @param listener	listener to register
	 */
	public static void registerChangeListener(StateChangedListener listener) {
		if(!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	/**
	 * Unregisters a StateChangedListener
	 * 
	 * @param listener	listener to unregister
	 */
	public static void unregisterChangeListener(StateChangedListener listener) {
		listeners.remove(listener);
	}


	@Override
	public void onServiceStart(boolean success) {
		for(StateChangedListener listener : listeners) {
			listener.serverUp(success);
		}
		
		Intent intent = new Intent(Constants.BROADCAST_INTENT_SERVER);
		intent.putExtra(Constants.INTENT_SERVER_EXTRA_STATE, true);
		sendBroadcast(intent);
		
		doRedirect(IpTablesAction.IP_TABLES_ADD);
		
		// Set iptables FORWARD chain to ACCEPT
		NetworkUtil.dropChain(NetworkUtil.IPTABLES_CHAIN_FORWARD);
		
		networkRunning = true;
		startingUp = false;
	}


	@Override
	public void onServiceStop(boolean success) {
		for(StateChangedListener listener : listeners) {
			listener.serverDown(success);
		}
		
		Intent intent = new Intent(Constants.BROADCAST_INTENT_SERVER);
		intent.putExtra(Constants.INTENT_SERVER_EXTRA_STATE, false);
		sendBroadcast(intent);
		
		unregisterServiceListener(this);
	}
	
}

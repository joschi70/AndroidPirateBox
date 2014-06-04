package de.fun2code.android.piratebox;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

/**
 * Constants that are used throughout the app
 * 
 * @author joschi
 *
 */
public class Constants {
	private static String INSTALL_DIR;
	
	public static final String TAG = "PirateBox";
	public static final String PREF_START_ON_BOOT = "startOnBoot";
	public static final String PREF_SSID_NAME = "ssidName";
	public static final String PREF_AP_IP = "apIp";
	public static final String PREF_STORAGE_DIR = "storageDir";
	public static final String PREF_IOS_WISPR_SUPPORT = "iosWispr";
	public static final String PREF_WP_NCSI_SUPPORT = "wpNcsi";
	public static final String PREF_VERSION = "version";
	public static final String PREF_UPDATE = "update";
	public static final String PREF_AUTO_AP_STARTUP = "autoApStartup";
	public static final String PREF_DEV_CONTENT_SD = "devContentSd";
	public static final String PREF_DEV_INFO_PIRATEBOX_VERSION = "infoPirateBoxVersion";
	public static final String PREF_DEV_INFO_PAW_VERSION = "infoPawVersion";
	public static final String PREF_DEV_INFO_AP_IP_ADDRESS = "infoApIpAddress";
	public static final String PREF_DEV_INFO_IP_ADDRESS = "infoIpAddress";
	public static final String PREF_DEV_INFO_LOCAL_PORT = "infoLocalPort";
	public static final String PREF_DEV_INFO_UPLOADS = "infoUploads";
	public static final String PREF_DEV_INFO_MESSAGES = "infoMessages";
	public static final String PREF_DEV_INFO_CONNECTIONS = "infoConnections";
	public static final String PREF_DEV_RESTORE_DNSMASQ = "restoreDnsMasq";
	public static final String PREF_DEV_RESET_NETWORKING = "resetNetworking";
	public static final String PREF_EMULATE_DROOPY = "emulateDroopy";
	public static final String PREF_KEEP_DEVICE_ON = "keepDeviceOn";
	public static final String PREF_ENABLE_STATISTICS = "enableStatistics";
	public static final String PREF_CLEAR_STATISTICS = "clearStatistics";
	
	public static final String AP_IP_DEFAULT = "192.168.43.1"; // Default AP IP address
	public static final String NAT_TABLE_NAME = "nat";
	
	public static final int DEFAULT_MAX_POST = 209715200; // Default upload size
	
	// Database constants
	public static final String STATS_DATABASE_NAME = "statistics";
	public static final int STATS_DATABASE_VERSION = 1;
	public static final String STATS_TABLE_VISITORS = "vc_statistics";
	public static final String STATS_TABLE_DOWNLOADS = "dl_statistics";
	
	//public static final String DEV_SWITCH_FILE = Environment.getExternalStorageDirectory().getPath() + "/.piratebox_dev";
	
	public static final String BROADCAST_INTENT_SERVER = "de.fun2code.android.piratebox.broadcast.intent.SERVER";
	public static final String INTENT_SERVER_EXTRA_STATE = "SERVER_STATE";
	
	public static final String BROADCAST_INTENT_AP = "de.fun2code.android.piratebox.broadcast.intent.AP";
	public static final String INTENT_AP_EXTRA_STATE = "AP_STATE";
	
	public static final String BROADCAST_INTENT_NETWORK = "de.fun2code.android.piratebox.broadcast.intent.NETWORK";
	public static final String INTENT_NETWORK_EXTRA_STATE = "NETWORK_STATE";
	
	public static final String BROADCAST_INTENT_SHOUT = "de.fun2code.android.piratebox.broadcast.intent.SHOUT";
	public static final String INTENT_SHOUT_EXTRA_NAME = "SHOUT_NAME";
	public static final String INTENT_SHOUT_EXTRA_TEXT = "SHOUT_TEXT";
	public static final String INTENT_SHOUT_EXTRA_NUMBER = "SHOUT_NUMBER";
	public static final String INTENT_SHOUT_EXTRA_DIR = "SHOUT_DIR";
	
	public static final String BROADCAST_INTENT_UPLOAD = "de.fun2code.android.piratebox.broadcast.intent.UPLOAD";
	public static final String INTENT_UPLOAD_EXTRA_FILE = "UPLOAD_FILE";
	public static final String INTENT_UPLOAD_EXTRA_NUMBER = "UPLOAD_NUMBER";
	public static final String INTENT_UPLOAD_EXTRA_DIR = "UPLOAD_DIR";
	
	public static final String BROADCAST_INTENT_CONNECTION = "de.fun2code.android.piratebox.broadcast.intent.CONNECTION";
	public static final String INTENT_CONNECTION_EXTRA_NUMBER = "CONNECTION_NUMBER";
	
	public static final String BROADCAST_INTENT_STATUS_REQUEST = "de.fun2code.android.piratebox.broadcast.intent.STATUS_REQUEST";
	public static final String BROADCAST_INTENT_STATUS_RESULT = "de.fun2code.android.piratebox.broadcast.intent.STATUS_RESULT";
	
	public static String getInstallDir(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		if(INSTALL_DIR == null) {
			//if(new File(DEV_SWITCH_FILE).exists()) {
			if(preferences.getBoolean(PREF_DEV_CONTENT_SD, false)) {
				// Use external storage
				INSTALL_DIR = Environment.getExternalStorageDirectory().getPath() + "/piratebox";
			}
			else {
				// Use /data/data/... directory
				INSTALL_DIR = context.getFilesDir().getAbsolutePath() + "/piratebox";
			}
		}
		
		return INSTALL_DIR;
	}
}

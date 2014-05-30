package de.fun2code.android.piratebox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;
import de.fun2code.android.piratebox.database.DatabaseHandler;
import de.fun2code.android.piratebox.handler.ConnectionCountHandler;
import de.fun2code.android.piratebox.util.DialogUtil;
import de.fun2code.android.piratebox.util.NetworkUtil;
import de.fun2code.android.piratebox.util.ShellUtil;

/**
 * Main preference Activity
 * 
 * @author joschi
 *
 */
public class PreferencesActivity extends PreferenceActivity {
	private SharedPreferences preferences;
	private Activity activity;
	
	/**
	 * Shared preference listener that is used to update the SSID on the fly
	 */
	private OnSharedPreferenceChangeListener prefChangeListener = new OnSharedPreferenceChangeListener() {
		
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
				String key) {
			// Update SSID by using iwconfig command
			if(key.equals(Constants.PREF_SSID_NAME) && PirateBoxService.isApRunning()) {
				ShellUtil shellUtil = new ShellUtil();
				String[] commands = { "iwconfig " + NetworkUtil.WIFI_INTERFACE + " essid '" +
				sharedPreferences.getString(Constants.PREF_SSID_NAME, activity.getResources().getString(R.string.pref_ssid_name_default)) + 
				"'" };
				shellUtil.execRootShell(commands);
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this;
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		addPreferencesFromResource(R.xml.preferences);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		preferences.registerOnSharedPreferenceChangeListener(prefChangeListener);
		
		/*
		 * Handle the press of the restore dnsmawq backup preference button
		 */
		findPreference(Constants.PREF_DEV_RESTORE_DNSMASQ).setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				NetworkUtil netUtil = new NetworkUtil(activity.getApplicationContext());
				boolean res = netUtil.unwrapDnsmasq();
				
				int resIdTitle = res ? R.string.dialog_title_info : R.string.dialog_title_error;
				int resIdMessage = res ? R.string.dialog_msg_dnsmasq_restore_ok : R.string.dialog_msg_dnsmasq_restore_error;
				DialogUtil.showDialog(activity, resIdTitle, resIdMessage);
				return true;
			}
			
			
		});
		
		/*
		 * Handle the press of the restore networking preference button
		 */
		findPreference(Constants.PREF_DEV_RESET_NETWORKING).setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				NetworkUtil netUtil = new NetworkUtil(activity.getApplicationContext());
				netUtil.flushIpTable(Constants.NAT_TABLE_NAME);
				
				int resIdTitle = R.string.dialog_title_info;
				int resIdMessage = R.string.dialog_msg_network_reset;
				DialogUtil.showDialog(activity, resIdTitle, resIdMessage);
				return true;
			}
				
		});
		
		/*
		 * Handle the press of the clear statistics preference button
		 */
		findPreference(Constants.PREF_CLEAR_STATISTICS).setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				OnClickListener posListener = new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						DatabaseHandler dbHandler = new DatabaseHandler(activity);
						dbHandler.clearTables();
						
						if(PirateBoxService.isRunning()) {
							ConnectionCountHandler.clearConnectionCount();
						}
						DialogUtil.showDialog(activity, R.string.dialog_title_info, R.string.dialog_msg_clear_statistics);
					}
				};
				
				DialogUtil.showDialog(activity, R.string.dialog_title_confirm, R.string.dialog_qst_clear_statistics, 
						android.R.string.yes, android.R.string.no, posListener, null);
				
				return true;
			}
				
		});
	}
	
	@Override
	public void onPause() {
		super.onPause();
		preferences.unregisterOnSharedPreferenceChangeListener(prefChangeListener);
	}
}
package de.fun2code.android.piratebox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;
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
		findPreference("restoreDnsMasq").setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				NetworkUtil netUtil = new NetworkUtil(activity.getApplicationContext());
				boolean res = netUtil.unwrapDnsmasq();
				
				int resIdTitle = res ? R.string.dialog_title_info : R.string.dialog_title_error;
				int resIdMessage = res ? R.string.dialog_msg_dnsmasq_restore_ok : R.string.dialog_msg_dnsmasq_restore_error;
				
				new AlertDialog.Builder(activity)
				.setTitle(resIdTitle)
				.setMessage(resIdMessage)
				.setPositiveButton(getText(android.R.string.ok), null)
				.show();
				
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
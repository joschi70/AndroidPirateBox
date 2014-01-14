package de.fun2code.android.piratebox;

import org.paw.util.FileUploadSplit;

import de.fun2code.android.piratebox.util.FileUtil;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Boot receiver that listens to {@code android.intent.action.BOOT_COMPLETED}
 * Intent actions
 * 
 * 
 * @author joschi
 *
 */
public class BootReceiver extends BroadcastReceiver {
	private static final String TAG = "BootReceiver";
	private static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		if (intent.getAction().equals(ACTION)) {
			if (preferences.getBoolean(Constants.PREF_START_ON_BOOT, false)) {
				Log.i(TAG, "PirateBox started on boot...");
				
				// Wait for external storage
				FileUtil.waitExternalStorageWritable(5, 3000);
				
				Intent serviceIntent = new Intent(context,
						PirateBoxService.class);
				
				// Start the PirateBox service
				context.startService(serviceIntent);
			}
		}
	}
	
	
}

package de.fun2code.android.piratebox.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.fun2code.android.piratebox.Constants;
import de.fun2code.android.piratebox.PirateBoxService;
import de.fun2code.android.piratebox.R;
import de.fun2code.android.piratebox.util.PirateUtil;

/**
 * This BrascastReceiver returns information of the current PirateBox state
 * aus broadcast
 * 
 * To request status broadcast an intent with action 
 * {@code de.fun2code.android.piratebox.broadcast.intent.STATUS_REQUEST} ant 
 * the receiver will respond with a resulting broadcast with action
 * {@code de.fun2code.android.piratebox.broadcast.intent.STATUS_RESULT}
 * 
 * The result broadcast will have the following extras:
 * <dl>
 * <dt>SERVER_STATE<dt>
 * <dd>boolean value: {@code true} if the server is running, otherwise {@code false}</dd>
 * <dt>UPLOAD_NUMBER<dt>
 * <dd>int value: number of uploaded files</dd>
 * <dt>SHOUT_NUMBER<dt>
 * <dd>int value: number of shout/chat messages</dd>
 * <dt>UPLOAD_DIR<dt>
 * <dd>String value: upload directory location</dd>
 * <dt>SHOUT_DIR<dt>
 * <dd>String value: shout/chat directory location</dd>
 * </dl>
 * 
 * 
 * @author joschi
 *
 */
public class StatusRequestReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// If status request
		if (intent != null
				&& intent.getAction().equals(Constants.BROADCAST_INTENT_STATUS_REQUEST)) {
			Intent resultIntent = new Intent(Constants.BROADCAST_INTENT_STATUS_RESULT);
			
			resultIntent.putExtra(Constants.INTENT_SERVER_EXTRA_STATE,
					PirateBoxService.isRunning());
			resultIntent.putExtra(Constants.INTENT_UPLOAD_EXTRA_NUMBER,
					PirateUtil.calculateUploads(context));
			resultIntent.putExtra(Constants.INTENT_SHOUT_EXTRA_NUMBER,
					PirateUtil.calculateMessages(context));
			
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

			String storageDir = preferences.getString(
					Constants.PREF_STORAGE_DIR, context.getResources()
							.getString(R.string.pref_storage_dir_default));
			resultIntent.putExtra(Constants.INTENT_UPLOAD_EXTRA_DIR, storageDir
					+ "/uploads");
			resultIntent.putExtra(Constants.INTENT_SHOUT_EXTRA_DIR, storageDir
					+ "/chat");
			
			context.sendBroadcast(resultIntent);
		}
		
	}

}

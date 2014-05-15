package de.fun2code.android.piratebox.plugin.locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import de.fun2code.android.pawserver.PawServerService;
import de.fun2code.android.piratebox.PirateBoxService;

/**
 * This is the "fire" {@code BroadcastReceiver} for a <i>Locale</i> plug-in setting.
 */
public final class FireReceiver extends BroadcastReceiver
{

	/**
	 * @param context {@inheritDoc}.
	 * @param intent the incoming {@code Intent}. This should always contain the store-and-forward {@code Bundle} that was saved
	 *            by {@link EditActivity_} and later broadcast by <i>Locale</i>.
	 */
	@Override
	public void onReceive(final Context context, final Intent intent)
	{
		/*
		 * Always be sure to be strict on your input parameters! A malicious third-party app could always send your plug-in an
		 * empty or otherwise malformed Intent. And since Locale applies settings in the background, you don't want your plug-in
		 * to crash.
		 */

		if (com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING.equals(intent.getAction()))
		{
			final Bundle bundle = intent.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
			
			if(bundle != null && bundle.containsKey(Constants.INTENT_EXTRA_STATE)) {
				final Boolean stateOn = bundle.getBoolean(Constants.INTENT_EXTRA_STATE, false);
				
				final boolean serverRunning = PirateBoxService.isRunning();
				final boolean serverStartup = PirateBoxService.isStartingUp();
				Intent serviceIntent = new Intent(context, PirateBoxService.class);

				if(Constants.IS_LOGGABLE) {
					Log.i(Constants.LOG_TAG, "Plugin sate flag: " + stateOn);
				}
				
				if(stateOn) {
					if(!serverRunning && !serverStartup) {
						context.startService(serviceIntent);
					}
				}
				else {
					if(serverRunning && !serverStartup) {
						context.stopService(serviceIntent);
					}
				}
				
			}
		}
	}
}
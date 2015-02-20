package de.fun2code.android.piratebox.wear;

import java.io.File;
import java.text.MessageFormat;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import android.util.Log;
import android.webkit.MimeTypeMap;
import de.fun2code.android.piratebox.Constants;
import de.fun2code.android.piratebox.PirateBoxActivity;
import de.fun2code.android.piratebox.R;

public class WearBroadcastReceiver extends BroadcastReceiver {
	
	private static final String TAG = "PirateBoxWear";
	private static final String NOTIFICATION_PB_GROUP = "GROUP_PIRATE_BOX";
	private static int summaryId = (int) System.currentTimeMillis();

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		if (!preferences.getBoolean(Constants.PREF_WEAR_NOTIFICATIONS, false) ||
				(!action.equals(Constants.BROADCAST_INTENT_UPLOAD)
				&& !action.equals(Constants.BROADCAST_INTENT_SHOUT))) {
			return;
		}
		
		/*
		 *  Unique notification ID, notifications for Wear will be grouped
		 *  by NOTIFICATION_*_GROUP
		 */
		int notificationId =  (int) System.currentTimeMillis();
	
		Intent appIntent = new Intent(context, PirateBoxActivity.class);
		PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, 0);
		
		NotificationCompat.Builder notificationBuilder = null;
		String title = "";
		String message = "";
		
		if (intent.getAction().equals(Constants.BROADCAST_INTENT_UPLOAD)) {
			String file = intent.getExtras().getString(
					Constants.INTENT_UPLOAD_EXTRA_FILE);

			Intent openIntent = new Intent();
			openIntent.setAction(android.content.Intent.ACTION_VIEW);
			String mimeType = MimeTypeMap.getSingleton()
					.getMimeTypeFromExtension(
							file.substring(file.lastIndexOf(".") + 1));
			openIntent.setDataAndType(Uri.fromFile(new File(file)),
					mimeType != null ? mimeType : "*/*");
			
			PendingIntent openPendingIntent = PendingIntent.getActivity(context, 0, openIntent, 0);
						
			String actionTitle = context.getString(R.string.wear_notification_upload_open_file);
			title = context.getString(R.string.wear_notification_upload_title);
			message = new File(file).getName();
			
			notificationBuilder = new NotificationCompat.Builder(context)
	        .setContentIntent(appPendingIntent)
	        .addAction(android.R.drawable.ic_menu_set_as, actionTitle,
	        				openPendingIntent)
	        .setGroup(NOTIFICATION_PB_GROUP)
	        .setContentTitle(title)
	        .setContentText(message);
		}
		else if(intent.getAction().equals(Constants.BROADCAST_INTENT_SHOUT)) {
			title = new MessageFormat(context.getString(R.string.wear_notification_shout_title)).
				format(new Object[] { intent.getExtras().getString(Constants.INTENT_SHOUT_EXTRA_NAME) });
			message = intent.getExtras().getString(Constants.INTENT_SHOUT_EXTRA_TEXT);
			
			notificationBuilder = new NotificationCompat.Builder(context)
	        .setContentIntent(appPendingIntent)
	        .setGroup(NOTIFICATION_PB_GROUP)
	        .setContentTitle(title)
	        .setContentText(message);
		}

		if(notificationBuilder != null) {	
			// Get an instance of the NotificationManager service
			NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
			
			try {
				// Build the notification and issues it with notification manager.
				notificationManager.notify(notificationId, notificationBuilder.build());
			}
			 catch(Exception e) {
				 Log.e(TAG, "Unable to send Wear notification: " + e);
			 }
			
			/*
			 *  Summary builder is needed for vibration support
			 *  For more details see:
			 *  http://stackoverflow.com/questions/24807402/android-wear-setvibrate-does-not-work-if-setgroup-called
			 */
			NotificationCompat.Builder summaryBuilder = new     
				        NotificationCompat.Builder(context)
				        .setGroup(NOTIFICATION_PB_GROUP)
				        .setGroupSummary(true)
				        .setContentTitle(context.getString(R.string.wear_summary_notification_title))
				        .setContentText(context.getString(R.string.wear_notifications_available))
				        .setSmallIcon(R.drawable.ic_notification_wear);
			 Notification notification = summaryBuilder.build();
			 notification.defaults |= Notification.DEFAULT_VIBRATE;
			 
			 try {
				 notificationManager.notify(summaryId, notification);
			 }
			 catch(Exception e) {
				 Log.e(TAG, "Unable to send Wear summary notification: " + e);
			 }
		}

	}

}

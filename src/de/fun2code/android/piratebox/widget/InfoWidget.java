package de.fun2code.android.piratebox.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import de.fun2code.android.piratebox.R;

public class InfoWidget extends AppWidgetProvider {

	private static String TAG = "PirateBoxInfoWidget";
	
	private final String BROADCAST_INTENT_SERVER = "de.fun2code.android.piratebox.broadcast.intent.SERVER";
	private final String BROADCAST_INTENT_SHOUT = "de.fun2code.android.piratebox.broadcast.intent.SHOUT";
	private final String BROADCAST_INTENT_UPLOAD = "de.fun2code.android.piratebox.broadcast.intent.UPLOAD";
	private final String BROADCAST_INTENT_STATUS_REQUEST = "de.fun2code.android.piratebox.broadcast.intent.STATUS_REQUEST";
	private final String BROADCAST_INTENT_STATUS_RESULT = "de.fun2code.android.piratebox.broadcast.intent.STATUS_RESULT";
	
	private final String INTENT_SERVER_EXTRA_STATE = "SERVER_STATE";
	private final String INTENT_UPLOAD_EXTRA_NUMBER = "UPLOAD_NUMBER";
	private final String INTENT_SHOUT_EXTRA_NUMBER = "SHOUT_NUMBER";
	
	private final String WIDGET_INTENT_CLICKED = "de.fun2code.android.piratebox.infowidget.intent.clicked";
	
	private static boolean serverStatus = false;
	private static int uploads = 0;
	private static int shouts = 0;
	

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		int[] widgetIds = manager.getAppWidgetIds(new ComponentName(context,
				InfoWidget.class));
		//Toast.makeText(context, intent.getAction(), Toast.LENGTH_SHORT).show();
		if(intent.getAction()
				.equals(BROADCAST_INTENT_SERVER)) {
			serverStatus = intent.getBooleanExtra(INTENT_SERVER_EXTRA_STATE, false);
		}
		else if(intent.getAction()
				.equals(BROADCAST_INTENT_STATUS_RESULT)) {
			serverStatus = intent.getBooleanExtra(INTENT_SERVER_EXTRA_STATE, false);
			uploads = intent.getIntExtra(INTENT_UPLOAD_EXTRA_NUMBER, 0);
			shouts = intent.getIntExtra(INTENT_SHOUT_EXTRA_NUMBER, 0);
		}
		else if(intent.getAction()
				.equals(BROADCAST_INTENT_UPLOAD)) {
			uploads++;
		}
		else if(intent.getAction()
				.equals(BROADCAST_INTENT_SHOUT)) {
			shouts++;
		}
		else if(intent.getAction()
				.equals(WIDGET_INTENT_CLICKED) || 
				intent.getAction()
				.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
			// Send broadcast to request info
			context.sendBroadcast(new Intent(BROADCAST_INTENT_STATUS_REQUEST));
		}
		
		
		showWidget(context, manager, widgetIds);

	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] ints) {
		context.sendBroadcast(new Intent(BROADCAST_INTENT_STATUS_REQUEST));
		showWidget(context, appWidgetManager, ints);
	}

	private void showWidget(Context context, AppWidgetManager manager,
			int[] widgetIds) {
		RemoteViews views = createRemoteViews(context);
		manager.updateAppWidget(widgetIds, views);
	}

	private RemoteViews createRemoteViews(Context context) {
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_info_layout);
		
		//views.setTextViewText(R.id.textStatus, serverStatus ? "up" : "down");
		views.setTextViewText(R.id.textUploads, String.valueOf(uploads));
		views.setTextViewText(R.id.textShouts, String.valueOf(shouts));
		
		Intent msg = new Intent(WIDGET_INTENT_CLICKED);
        PendingIntent intent = PendingIntent.getBroadcast(context, -1 /*not used*/, msg, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.main, intent);

		return views;
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		
		context.sendBroadcast(new Intent(BROADCAST_INTENT_STATUS_REQUEST));
	}

}

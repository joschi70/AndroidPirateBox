package de.fun2code.android.piratebox.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import de.fun2code.android.piratebox.Constants;
import de.fun2code.android.piratebox.R;

public class InfoWidget extends AppWidgetProvider {

	private static String TAG = "PirateBoxInfoWidget";
	
	private final String WIDGET_INTENT_CLICKED = "de.fun2code.android.piratebox.infowidget.intent.clicked";
	
	private static boolean serverRunning = false;
	private static int uploads = 0;
	private static int shouts = 0;
	private static int connections = 0;
	

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		int[] widgetIds = manager.getAppWidgetIds(new ComponentName(context,
				InfoWidget.class));
		//Toast.makeText(context, intent.getAction(), Toast.LENGTH_SHORT).show();
		if(intent.getAction()
				.equals(Constants.BROADCAST_INTENT_SERVER)) {
			serverRunning = intent.getBooleanExtra(Constants.INTENT_SERVER_EXTRA_STATE, false);
		}
		else if(intent.getAction()
				.equals(Constants.BROADCAST_INTENT_STATUS_RESULT)) {
			serverRunning = intent.getBooleanExtra(Constants.INTENT_SERVER_EXTRA_STATE, false);
			uploads = intent.getIntExtra(Constants.INTENT_UPLOAD_EXTRA_NUMBER, 0);
			shouts = intent.getIntExtra(Constants.INTENT_SHOUT_EXTRA_NUMBER, 0);
			connections = intent.getIntExtra(Constants.INTENT_CONNECTION_EXTRA_NUMBER, 0);
		}
		else if(intent.getAction()
				.equals(Constants.BROADCAST_INTENT_UPLOAD)) {
			uploads++;
		}
		else if(intent.getAction()
				.equals(Constants.BROADCAST_INTENT_SHOUT)) {
			shouts++;
		}
		else if(intent.getAction()
				.equals(Constants.BROADCAST_INTENT_CONNECTION)) {
			connections++;
		}
		else if(intent.getAction()
				.equals(WIDGET_INTENT_CLICKED) || 
				intent.getAction()
				.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
			// Send broadcast to request info
			context.sendBroadcast(new Intent(Constants.BROADCAST_INTENT_STATUS_REQUEST));
		}
		
		
		showWidget(context, manager, widgetIds);

	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] ints) {
		context.sendBroadcast(new Intent(Constants.BROADCAST_INTENT_STATUS_REQUEST));
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
		
		CharSequence noValue = context.getText(R.string.pref_dev_info_default_summary);
		
		views.setTextViewText(R.id.textUploads, serverRunning ? String.valueOf(uploads) : noValue);
		views.setTextViewText(R.id.textShouts, serverRunning ? String.valueOf(shouts) : noValue);
		views.setTextViewText(R.id.textConnections, serverRunning ? String.valueOf(connections) : noValue);
		
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
		
		context.sendBroadcast(new Intent(Constants.BROADCAST_INTENT_STATUS_REQUEST));
	}

}

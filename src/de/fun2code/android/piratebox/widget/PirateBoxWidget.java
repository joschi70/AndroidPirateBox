package de.fun2code.android.piratebox.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import de.fun2code.android.piratebox.PirateBoxService;
import de.fun2code.android.piratebox.R;

public class PirateBoxWidget extends AppWidgetProvider {
	public static final String WIDGET_INTENT_UPDATE = "de.fun2code.android.pawserver.widget.intent.update";
    public static final String WIDGET_INTENT_CLICKED = "de.fun2code.android.piratebox.widget.intent.clicked";

	
	@Override
	public void onReceive(Context context, Intent intent) {
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] widgetIds = manager.getAppWidgetIds(new ComponentName(context, PirateBoxWidget.class));
   
        if(intent.getAction().equals(WIDGET_INTENT_CLICKED)) {
        	Intent serviceIntent = new Intent(context, PirateBoxService.class);
        	
        	boolean serverRunning = PirateBoxService.isRunning();
        	boolean startingUp = PirateBoxService.isStartingUp();
        	
        	// Only start if not running an not starting up
        	if(!serverRunning && !startingUp) {
        		showIntermediateImage(context);
        		context.startService(serviceIntent);
        	}
        	// Only stop service, if service is running
        	else if(serverRunning) {
        		showIntermediateImage(context);
        		context.stopService(serviceIntent);
        	}
        }
        else {
        	showWidget(context, manager, widgetIds, PirateBoxService.isRunning());
        }
        
        
	}
	
	/**
	 * Shows the intermediate image
	 * 
	 * @param context	Context to use
	 */
	private void showIntermediateImage(Context context) {
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] widgetIds = manager.getAppWidgetIds(new ComponentName(context, PirateBoxWidget.class));
		
		// Set intermediate image
    	RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setImageViewResource(R.id.widgetCanvas, R.drawable.widget_limbo);
        manager.updateAppWidget(widgetIds, views);
	}
	
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] ints) {
		showWidget(context, appWidgetManager, ints, PirateBoxService.isRunning());

	}
	
	private void showWidget(Context context, AppWidgetManager manager, int[] widgetIds, boolean status) {
        RemoteViews views = createRemoteViews(context, status);
        manager.updateAppWidget(widgetIds, views);
    }

    private RemoteViews createRemoteViews(Context context, boolean status) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        int iconId = status ? R.drawable.widget_on : R.drawable.widget_off;
        views.setImageViewResource(R.id.widgetCanvas, iconId);

        Intent msg = new Intent(WIDGET_INTENT_CLICKED);
        PendingIntent intent = PendingIntent.getBroadcast(context, -1 /*not used*/, msg, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widgetCanvas, intent);
        return views;
    }

}

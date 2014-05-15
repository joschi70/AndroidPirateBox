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
		else if (intent.getAction()
				.equals(WIDGET_INTENT_CLICKED)){
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

//		Location location = CacheStumblerWidgetService.location;
//		CacheInfo cache = CacheStumblerWidgetService.cache;
//
//		if (cache != null && location != null) {
//			views.setTextViewText(R.id.textCacheName,
//					Html.fromHtml(cache.getName()));
//
//			views.setTextViewText(R.id.textType, cache.getType());
//
//			views.setTextViewText(R.id.textAccuracy,
//					(int) Math.round(location.getAccuracy()) + "m");
//			views.setTextViewText(R.id.textDistance,
//					(int) Math.round(CacheStumblerWidgetService.cacheDistance)
//							+ "m");
//			views.setTextViewText(R.id.textProvider, location.getProvider());
//
//			views.setTextViewText(R.id.textDifficultyTerrain,
//					cache.getDifficulty() + "/" + cache.getTerrain());
//
//			float bearing = CacheStumblerWidgetService.cacheBearing;
//			
//			// Hide progessbar
//			views.setViewVisibility(R.id.progressBarWrapper, View.GONE);
//
//			if (bearing != -1
//					&& CacheStumblerWidgetService.cacheDistance > location
//							.getAccuracy()) {
//
//				Options options = new BitmapFactory.Options();
//				// do not scale
//				options.inScaled = false;
//				Bitmap bitmap = BitmapFactory.decodeResource(
//						context.getResources(), R.drawable.direction_small,
//						options);
//				Matrix matrix = new Matrix();
//				// matrix.postRotate(bearing * -1);
//				matrix.postRotate(bearing);
//				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
//						bitmap.getHeight(), matrix, true);
//
//				views.setImageViewBitmap(R.id.imageDirection, bitmap);
//			} else {
//				views.setImageViewResource(R.id.imageDirection,
//						R.drawable.na_large);
//			}
//			
//			// Show image
//			views.setViewVisibility(R.id.imageDirection,
//					View.VISIBLE);
//			
//			views.setViewVisibility(R.id.linearLayoutLoading, View.GONE);
//
//			views.setViewVisibility(R.id.linearLayoutTitle, View.VISIBLE);
//			views.setViewVisibility(R.id.linearLayoutInfoLine1, View.VISIBLE);
//			views.setViewVisibility(R.id.linearLayoutInfoLine2, View.VISIBLE);
//
//			Intent intent = new Intent(context,
//					CacheStumblerWidgetActivity.class);
//			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
//					intent, 0);
//			views.setOnClickPendingIntent(R.id.main, pendingIntent);
//
//		}
//		// Show no value
//		else {
//			// Hide image
//			views.setImageViewResource(R.id.imageDirection, R.drawable.na_large);
//			views.setViewVisibility(R.id.imageDirection, View.GONE);
//
//			// Show progessbar
//			views.setViewVisibility(R.id.progressBarWrapper, View.VISIBLE);
//			
//			views.setViewVisibility(R.id.linearLayoutTitle, View.GONE);
//			views.setViewVisibility(R.id.linearLayoutInfoLine1, View.GONE);
//			views.setViewVisibility(R.id.linearLayoutInfoLine2, View.GONE);
//			
//			views.setViewVisibility(R.id.linearLayoutLoading, View.VISIBLE);
//
//			/*
//			 * Disable the onClick, by using an Intent that will trigger noting
//			 */
//			Intent intent = new Intent(context, InfoWidget.class);
//			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
//					intent, 0);
//			views.setOnClickPendingIntent(R.id.main, pendingIntent);
//		}
//
//		// Check if we have to display special icons
//		if (cache != null
//				&& location != null
//				&& CacheStumblerWidgetService.cacheDistance <= location
//						.getAccuracy()
//				&& location.getAccuracy() <= MIN_ACCURACY) {
//			views.setImageViewResource(R.id.imageDirection,
//					R.drawable.circle_large);
//		} else if (cache != null && location != null 
//				&& CacheStumblerWidgetService.cacheDistance <= location
//						.getAccuracy()) {
//			views.setImageViewResource(R.id.imageDirection, R.drawable.na_large);
//		}
		
		Intent msg = new Intent(WIDGET_INTENT_CLICKED);
        PendingIntent intent = PendingIntent.getBroadcast(context, -1 /*not used*/, msg, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.main, intent);

		return views;
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);

//		Intent serviceIntent = new Intent(context,
//				CacheStumblerWidgetService.class);
//		context.stopService(serviceIntent);
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		

//		Intent serviceIntent = new Intent(context,
//				CacheStumblerWidgetService.class);
//		context.startService(serviceIntent);
		/*
		 * // Init currentLocation = getBestLocation();
		 * 
		 * AppWidgetManager manager = AppWidgetManager.getInstance(ctx); int[]
		 * widgetIds = manager.getAppWidgetIds(new ComponentName(ctx,
		 * CacheStumblerWidget.class)); showWidget(ctx, manager, widgetIds);
		 */
	}

}

package com.rehivetech.beeeon.widget.receivers;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rehivetech.beeeon.asynctask.ActorActionTask;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.widget.data.WidgetClockData;
import com.rehivetech.beeeon.widget.service.WidgetService;

/**
 * @author mlyko
 */
public class WidgetBridgeBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = WidgetBridgeBroadcastReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		String receivedAction = intent.getAction();
		Log.d(TAG, "broadcastReceived: " + receivedAction);

		// if time was changed (any way), update clock widgets
		if (receivedAction.equals(Intent.ACTION_TIME_TICK) || receivedAction.equals(Intent.ACTION_TIME_CHANGED) || receivedAction.equals(Intent.ACTION_TIMEZONE_CHANGED)){
			WidgetClockData.onUpdateAllClocks(context);
		}
		else if(receivedAction.equals(Intent.ACTION_LOCALE_CHANGED)){
			WidgetClockData.reloadWeekDays();
		}
		// if screen went on, update clocks + tell the service
		else if(receivedAction.equals(Intent.ACTION_SCREEN_ON)){
			WidgetClockData.onUpdateAllClocks(context);
			context.startService(WidgetService.getIntentStandBy(context, false));
		}
		// if screen went off, tell the service
		else if(receivedAction.equals(Intent.ACTION_SCREEN_OFF)){
			context.startService(WidgetService.getIntentStandBy(context, true));
		}
		// if any actor value was changed, tell the service to refresh widget with that device
		else if(receivedAction.equals(ActorActionTask.ACTION_ACTOR_CHANGED)){
			String adapterId = intent.getStringExtra(ActorActionTask.EXTRA_ACTOR_CHANGED_ADAPTER_ID);
			String actorId = intent.getStringExtra(ActorActionTask.EXTRA_ACTOR_CHANGED_ID);

			if(adapterId == null || adapterId.isEmpty() || actorId == null || actorId.isEmpty()) return;
			context.startService(WidgetService.getIntentActorChangeResult(context, adapterId, actorId));

			/*
			// update location widget if exists
			int[] locationWidgetsIds = WidgetProvider.getAllIdsByClass(context, WidgetLocationListProvider.class);
			if(locationWidgetsIds != null && locationWidgetsIds.length > 0){
				AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
				// TODO
				//widgetManager.notifyAppWidgetViewDataChanged();
			}
			//*/
		}
		/*
		// TODO somehow not getting isNoConnectivity
		else if(receivedAction.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
			boolean isNoConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
			boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
			Log.d(TAG, String.format("IS Connectivity = %b, isFailover = %b", isNoConnectivity, isFailover));
		}
		//*/

		// TODO mit zmenu stavu prihlaseni jako broadcast?
	}
}

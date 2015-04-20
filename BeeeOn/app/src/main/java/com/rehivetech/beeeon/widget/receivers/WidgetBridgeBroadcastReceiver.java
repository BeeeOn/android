package com.rehivetech.beeeon.widget.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.widget.service.WidgetService;

/**
 * @author mlyko
 */
public class WidgetBridgeBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = WidgetBridgeBroadcastReceiver.class.getSimpleName();

	public static String ACTION_TIME_UPDATED = "com.rehivetech.beeeon.ACTION_TIME_UPDATED";
	public static String ACTION_SCREEN_ON = "com.rehivetech.beeeon.ACTION_SCREEN_ON";
	public static String ACTION_SCREEN_OFF = "com.rehivetech.beeeon.ACTION_SCREEN_OFF";
	public static String ACTION_ACTOR_CHANGED = "com.rehivetech.beeeon.ACTION_ACTOR_CHANGED";
	public static String ACTION_LOCALE_CHANGED = "com.rehivetech.beeeon.ACTION_LOCALE_CHANGED";

	// TODO optimize so there is no resending of broadcasts
	@Override
	public void onReceive(Context context, Intent intent) {
		String receivedAction = intent.getAction();
		Log.d(TAG, "broadcastReceived: " + receivedAction);
		Context appContext = context.getApplicationContext();

		// if time was changed (any way), resends broadcast for clock widgets
		if (receivedAction.equals(Intent.ACTION_TIME_TICK) || receivedAction.equals(Intent.ACTION_TIME_CHANGED) || receivedAction.equals(Intent.ACTION_TIMEZONE_CHANGED)){
			sendCustomBroadcast(ACTION_TIME_UPDATED, appContext);
		}
		// if screen went on, resend broadcast + tell the service
		else if(receivedAction.equals(Intent.ACTION_SCREEN_ON)){
			sendCustomBroadcast(ACTION_SCREEN_ON, appContext);
			context.startService(WidgetService.getStandByIntent(context, false));
		}
		// if screen went off, resend broadcast + tell the service
		else if(receivedAction.equals(Intent.ACTION_SCREEN_OFF)){
			sendCustomBroadcast(ACTION_SCREEN_OFF, appContext);
			context.startService(WidgetService.getStandByIntent(context, true));
		}
		// if any actor value was changed, tell the service to refresh widget with that device
		else if(receivedAction.equals(ACTION_ACTOR_CHANGED)){
			// TODO spustit se specifickym intentem
			// TODO xxx ziskat id widgetu (spise aktoru)
			// context.startService(WidgetService.getActorChangeIntent(context xxx));
		}
		else if(receivedAction.equals(Intent.ACTION_LOCALE_CHANGED)){
			sendCustomBroadcast(ACTION_LOCALE_CHANGED, appContext);
		}

		// TODO mit zmenu stavu prihlaseni jako broadcast?
	}

	public static void sendCustomBroadcast(String action, Context context){
		Intent actionIntent = new Intent(action);
		context.sendBroadcast(actionIntent);
		Log.d(TAG, "sendCustomBroadcast() = " + action);
	}
}

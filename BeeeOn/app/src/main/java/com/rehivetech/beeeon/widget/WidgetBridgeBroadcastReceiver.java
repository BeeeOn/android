package com.rehivetech.beeeon.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rehivetech.beeeon.util.Log;

/**
 * @author mlyko
 */
public class WidgetBridgeBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = WidgetBridgeBroadcastReceiver.class.getSimpleName();

	public static String ACTION_TIME_CHANGED = "com.rehivetech.beeeon.ACTION_TIME_CHANGED";
	public static String ACTION_SCREEN_ON = "com.rehivetech.beeeon.ACTION_SCREEN_ON";
	public static String ACTION_SCREEN_OFF = "com.rehivetech.beeeon.ACTION_SCREEN_OFF";

	@Override
	public void onReceive(Context context, Intent intent) {
		String receivedAction = intent.getAction();
		Log.d(TAG, "broadcastReceived: " + receivedAction);
		Context appContext = context.getApplicationContext();

		if (receivedAction.equals(Intent.ACTION_TIME_TICK) || receivedAction.equals(Intent.ACTION_TIME_CHANGED) || receivedAction.equals(Intent.ACTION_TIMEZONE_CHANGED)){
			sendCustomBroadcast(ACTION_TIME_CHANGED, appContext);
		}

		if(receivedAction.equals(Intent.ACTION_SCREEN_ON)){
			sendCustomBroadcast(ACTION_SCREEN_ON, appContext);
			context.startService(WidgetService.getStandByIntent(context, false));
		}

		if(receivedAction.equals(Intent.ACTION_SCREEN_OFF)){
			sendCustomBroadcast(ACTION_SCREEN_OFF, appContext);
			context.startService(WidgetService.getStandByIntent(context, true));
		}

		// TODO mit zmenu stavu prihlaseni jako broadcast?
	}

	public static void sendCustomBroadcast(String action, Context context){
		Intent actionIntent = new Intent(action);
		context.sendBroadcast(actionIntent);
	}
}

package com.rehivetech.beeeon.widget.clock;

import android.content.Context;
import android.content.Intent;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.widget.WidgetBridgeBroadcastReceiver;
import com.rehivetech.beeeon.widget.WidgetProvider;

public class WidgetClockProvider extends WidgetProvider {
	private static final String TAG = WidgetClockProvider.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.d(TAG, action);

		if (action.equals(WidgetBridgeBroadcastReceiver.ACTION_TIME_CHANGED) || action.equals(WidgetBridgeBroadcastReceiver.ACTION_SCREEN_ON)) {
			WidgetClockData.onUpdateClock(context, null, getAllIds(context));
		}
		else if(action.equals(WidgetBridgeBroadcastReceiver.ACTION_LOCALE_CHANGED)){
			WidgetClockData.reloadWeekDays();
		}

		super.onReceive(context, intent);
	}
}


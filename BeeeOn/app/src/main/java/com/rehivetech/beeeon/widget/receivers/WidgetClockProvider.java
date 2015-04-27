package com.rehivetech.beeeon.widget.receivers;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Bundle;

public class WidgetClockProvider extends WidgetProvider {
	private static final String TAG = WidgetClockProvider.class.getSimpleName();

	@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);


	}
}


package com.rehivetech.beeeon.widget.receivers;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.rehivetech.beeeon.widget.data.WidgetData;
import com.rehivetech.beeeon.widget.data.WidgetLocationData;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetLocationListProvider extends WidgetProvider {
	private static final String TAG = WidgetLocationListProvider.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		// open detail activity of chosen module from list
		if (intent.getAction().equals(WidgetLocationData.OPEN_DETAIL_ACTION)) {
			String moduleId = intent.getStringExtra(WidgetLocationData.EXTRA_ITEM_DEV_ID);
			String gateId = intent.getStringExtra(WidgetLocationData.EXTRA_ITEM_GATE_ID);
			context.startActivity(WidgetData.startDetailActivityIntent(context, gateId, moduleId));
		}

		super.onReceive(context, intent);
	}
}
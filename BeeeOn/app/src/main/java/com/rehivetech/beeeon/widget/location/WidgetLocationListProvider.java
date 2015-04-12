package com.rehivetech.beeeon.widget.location;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.SensorDetailActivity;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.WidgetListService;
import com.rehivetech.beeeon.widget.WidgetData;
import com.rehivetech.beeeon.widget.WidgetProvider;
import com.rehivetech.beeeon.widget.WidgetService;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetLocationListProvider extends WidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        // open detail activity of chosen device from list
        if (intent.getAction().equals(WidgetLocationData.OPEN_DETAIL_ACTION)) {
            Intent detailIntent = new Intent(context, SensorDetailActivity.class);
            detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            detailIntent.putExtra(SensorDetailActivity.EXTRA_DEVICE_ID, intent.getStringExtra(WidgetLocationData.EXTRA_ITEM_DEV_ID));
            detailIntent.putExtra(SensorDetailActivity.EXTRA_ADAPTER_ID, intent.getStringExtra(WidgetLocationData.EXTRA_ITEM_ADAPTER_ID));
            context.startActivity(detailIntent);
        }

        super.onReceive(context, intent);
    }
}
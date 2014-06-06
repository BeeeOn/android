package cz.vutbr.fit.iha;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

/**
 * @brief Methods for fixing various compatibility issues
 * @author Robyer
 *
 */
public class Compatibility {

	/**
	 * Set background of View with correct API method
	 * @param view
	 * @param background
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void setBackground(View view, Drawable background) {	
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			view.setBackground(background);
	    } else {
	    	view.setBackgroundDrawable(background);
	    }
	}

	/**
	 * Handle resizing widgets for TouchWiz (Galaxy S3 and similar) on Android 4.1.2
	 * @param widgetProvider
	 * @param context
	 * @param intent
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void handleTouchWizResizing(AppWidgetProvider widgetProvider, Context context, Intent intent) {	
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			// using code from http://stackoverflow.com/questions/17396045/how-to-catch-widget-size-changes-on-devices-where-onappwidgetoptionschanged-not 
			if (intent.getAction().contentEquals("com.sec.android.widgetapp.APPWIDGET_RESIZE")) {
	    	    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

	    	    int appWidgetId = intent.getIntExtra("widgetId", 0);
	    	    int widgetSpanX = intent.getIntExtra("widgetspanx", 0);
	    	    int widgetSpanY = intent.getIntExtra("widgetspany", 0);

	    	    if (appWidgetId > 0 && widgetSpanX > 0 && widgetSpanY > 0) {
	    	        Bundle newOptions = new Bundle();
	    	        // we have to convert these numbers for future use
	    	        newOptions.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, widgetSpanY * 74);
	    	        newOptions.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, widgetSpanX * 74);

	    	        widgetProvider.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
	    	    }
		    }
		}
	}

}

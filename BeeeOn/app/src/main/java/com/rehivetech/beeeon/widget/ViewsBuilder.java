package com.rehivetech.beeeon.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.Compatibility;
import com.rehivetech.beeeon.util.Log;

/**
 * Created by Tomáš on 25. 4. 2015.
 */
public class ViewsBuilder{
	private static final String TAG = ViewsBuilder.class.getSimpleName();

	private Context mContext;
	private RemoteViews mRemoteViews;

	public ViewsBuilder(Context context){
		mContext = context;
	}

	public ViewsBuilder(Context context, int layoutResource){
		this(context);
		loadRootView(layoutResource);
	}

	public void loadRootView(int layoutResource){
		mRemoteViews = new RemoteViews(mContext.getPackageName(), layoutResource);
	}

	public void loadRootView(RemoteViews rootViews){
		mRemoteViews = rootViews;
	}

	public RemoteViews getRoot(){
		return mRemoteViews;
	}

	public void addView(int viewId, RemoteViews child){
		mRemoteViews.addView(viewId, child);
	}

	public void removeAllViews(int viewId){
		mRemoteViews.removeAllViews(viewId);
	}

	public void setTextViewText(int viewId, String text){
		mRemoteViews.setTextViewText(viewId, text);
	}

	public void setTextViewColor(int viewId, int colorResource){
		mRemoteViews.setTextColor(viewId, mContext.getResources().getColor(colorResource));
	}

	public void setTextViewTextSize(int viewId, int unit, float size){
		Compatibility.setTextViewTextSize(mContext, mRemoteViews, viewId, unit, size);
	}

	public void setTextViewTextSize(int viewId, int dimension){
		Compatibility.setTextViewTextSize(mContext, mRemoteViews, viewId, TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(dimension));
	}

	public void setSwitchChecked(boolean state){
		setImage(R.id.widget_switchcompat, state ? R.drawable.switch_on : R.drawable.switch_off);
	}

	public void setSwitchDisabled(boolean isDisabled, boolean isChecked) {
		if (isDisabled) {
			setImage(R.id.widget_switchcompat, isChecked == true ? R.drawable.switch_on_disabled : R.drawable.switch_off_disabled);
		}
		else{
			setImage(R.id.widget_switchcompat, isChecked == true ? R.drawable.switch_on : R.drawable.switch_off);
		}
	}

	public void setTextView(int viewId, String text, int color, int dimension){
		setTextViewText(viewId, text);
		setTextViewColor(viewId, color);
		setTextViewTextSize(viewId, dimension);
	}

	public void setImage(int viewId, int resourceId){
		mRemoteViews.setImageViewResource(viewId, resourceId);
	}

	public void setImage(int viewId, Bitmap bitmap){
		Log.v(TAG, "Bitmap size " + String.valueOf(Compatibility.bitmapGetByteCount(bitmap)) + " bytes");
		mRemoteViews.setImageViewBitmap(viewId, bitmap);
	}

	public void setOnClickListener(int viewId, PendingIntent listener){
		mRemoteViews.setOnClickPendingIntent(viewId, listener);
	}

	public void setViewVisibility(int viewId, int visibility){
		mRemoteViews.setViewVisibility(viewId, visibility);
	}

	public void setRemoteAdapter(int viewId, int widgetId, Intent intent){
		Compatibility.setRemoteAdapter(mRemoteViews, widgetId, intent, viewId);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setEmptyView(int viewId, int emptyViewId){
		mRemoteViews.setEmptyView(viewId, emptyViewId);
	}
}
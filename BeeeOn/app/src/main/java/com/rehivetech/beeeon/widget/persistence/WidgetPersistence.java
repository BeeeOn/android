package com.rehivetech.beeeon.widget.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;

import java.util.List;

/**
 * @author mlyko
 */
public abstract class WidgetPersistence {

	// helpers
	protected UnitsHelper mUnitsHelper;
	protected TimeHelper mTimeHelper;

	// persistence data
	protected int mWidgetId;
	protected final int mOffset;
	protected int mBoundView;

	protected Context mContext;
	protected RemoteViews mParentRemoteViews;
	protected RemoteViews mValueRemoteViews;

	public WidgetPersistence(Context context, int widgetId, int offset, int view, UnitsHelper unitsHelper, TimeHelper timeHelper) {
		mContext = context.getApplicationContext();
		mWidgetId = widgetId;
		mOffset = offset;
		mBoundView = view;
		mUnitsHelper = unitsHelper;
		mTimeHelper = timeHelper;
	}

	public int getOffset() {
		return mOffset;
	}

	public int getBoundView() {
		return mBoundView;
	}

	public abstract String getPrefFileName();

	public abstract void load();
	public abstract void configure(Object obj, Adapter adapter);
	public abstract void save();
	public abstract void change(Object obj, Adapter adapter);

	public abstract void initValueView(RemoteViews parentRV);

	/**
	 * Updates value layout when logged in
	 */
	public void updateValueView(){
		updateValueView("");
	}

	/**
	 * Updates value layout with gotten data (either cached or from UnitsHelper)
	 * @param cachedFormat	Format for specifying what looks like when cached data -> available only %s
	 */
	public abstract void updateValueView(String cachedFormat);

	public RemoteViews getValueViews(){
		return mValueRemoteViews;
	}


	public void delete(){
		getSettings().edit().clear().commit();
	}

	public SharedPreferences getSettings() {
		return mContext.getSharedPreferences(String.format(getPrefFileName(), mWidgetId, mOffset), Context.MODE_PRIVATE);
	}


	// ------ METHODS FOR WORKING WITH MORE OBJECTS AT ONCE ------ //
	public static <T extends WidgetPersistence> void loadAll(List<T> widgetPersistences){
		if(widgetPersistences == null) return;
		for(WidgetPersistence per : widgetPersistences){
			per.load();
		}
	}

	public static <T extends WidgetPersistence> void saveAll(List<T> widgetPersistences){
		if(widgetPersistences == null) return;
		for(WidgetPersistence per : widgetPersistences){
			per.save();
		}
	}

	public static <T extends WidgetPersistence> void deleteAll(List<T> widgetPersistences){
		if(widgetPersistences == null) return;
		for(WidgetPersistence per : widgetPersistences){
			per.delete();
		}
	}
}

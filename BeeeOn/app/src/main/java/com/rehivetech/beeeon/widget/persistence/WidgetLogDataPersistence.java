package com.rehivetech.beeeon.widget.persistence;

import android.content.Context;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.ModuleLog;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Created by Tomáš on 27. 4. 2015.
 */
public class WidgetLogDataPersistence extends WidgetPersistence {

	private static final String PREF_INTERVAL_START = "interval_start";
	private static final String PREF_TYPE = "type";
	private static final String PREF_GAP = "gap";
	private static final String PREF_GAP_RADIO_ID = "gap_radio_id";


	// persistent data
	public long intervalStart;
	public String type;
	public int gap;
	public int gapRadioId;


	public WidgetLogDataPersistence(Context context, int widgetId) {
		super(context, widgetId);
	}

	@Override
	public void load() {
		gapRadioId = mPrefs.getInt(getProperty(PREF_GAP_RADIO_ID), R.id.widget_gap_weekly);
		intervalStart = mPrefs.getLong(getProperty(PREF_INTERVAL_START), DateTime.now(DateTimeZone.UTC).minusWeeks(1).getMillis());
		type = mPrefs.getString(getProperty(PREF_TYPE), ModuleLog.DataType.AVERAGE.getId());
		gap = mPrefs.getInt(getProperty(PREF_GAP), ModuleLog.DataInterval.HOUR.getSeconds());
	}

	@Override
	public void save() {
		mPrefs.edit()
				.putLong(getProperty(PREF_INTERVAL_START), intervalStart)
				.putString(getProperty(PREF_TYPE), type)
				.putInt(getProperty(PREF_GAP), gap)
				.putInt(getProperty(PREF_GAP_RADIO_ID), gapRadioId)
				.apply();
	}

	@Override
	public void delete() {
		mPrefs.edit()
				.remove(getProperty(PREF_INTERVAL_START))
				.remove(getProperty(PREF_TYPE))
				.remove(getProperty(PREF_GAP))
				.remove(getProperty(PREF_GAP_RADIO_ID))
				.apply();
	}

	@Override
	public String getPropertyPrefix() {
		return "log_data_pair";
	}
}

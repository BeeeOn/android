package com.rehivetech.beeeon.widget.persistence;

import android.content.Context;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.DeviceLog;
import com.rehivetech.beeeon.household.device.values.BaseEnumValue;
import com.rehivetech.beeeon.util.Log;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
		type = mPrefs.getString(getProperty(PREF_TYPE), DeviceLog.DataType.AVERAGE.getValue());
		gap = mPrefs.getInt(getProperty(PREF_GAP), DeviceLog.DataInterval.HOUR.getValue());
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

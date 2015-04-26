package com.rehivetech.beeeon.widget.persistence;

import android.content.Context;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;

/**
 * Created by Tomáš on 26. 4. 2015.
 */
public abstract class WidgetBeeeOnPersistence extends WidgetPersistence {

	private static final String PREF_ID = "id";
	private static final String PREF_NAME = "name";
	private static final String PREF_ADAPTER_ID = "adpater_id";
	private static final String PREF_ADAPTER_ROLE = "adapter_role";

	public String id;
	public String name;
	public String adapterId;
	protected String adapterRole;

	WidgetBeeeOnPersistence(Context context, int widgetId, int offset, int boundView, UnitsHelper unitsHelper, TimeHelper timeHelper, WidgetSettings settings) {
		super(context, widgetId, offset, boundView, unitsHelper, timeHelper, settings);
	}

	@Override
	public void load() {
		id = mPrefs.getString(getProperty(PREF_ID), "");
		name = mPrefs.getString(getProperty(PREF_NAME), mContext.getString(R.string.placeholder_not_exists));
		adapterId = mPrefs.getString(getProperty(PREF_ADAPTER_ID), "");
		adapterRole = mPrefs.getString(getProperty(PREF_ADAPTER_ROLE), User.Role.Guest.getValue());
	}

	@Override
	public void save() {
		mPrefs.edit()
				.putString(getProperty(PREF_ID), id)
				.putString(getProperty(PREF_NAME), name)
				.putString(getProperty(PREF_ADAPTER_ID), adapterId)
				.putString(getProperty(PREF_ADAPTER_ROLE), adapterRole)
				.apply();
	}

	@Override
	public void delete() {
		mPrefs.edit()
				.remove(getProperty(PREF_ID))
				.remove(getProperty(PREF_NAME))
				.remove(getProperty(PREF_ADAPTER_ID))
				.remove(getProperty(PREF_ADAPTER_ROLE))
				.apply();
	}

	// ----------------------------------------------------------- //
	// ---------------------- GETTERS ---------------------------- //
	// ----------------------------------------------------------- //
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getAdapterId() {
		return adapterId;
	}
}
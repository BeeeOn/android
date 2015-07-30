package com.rehivetech.beeeon.widget.persistence;

import android.content.Context;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;

/**
 * Created by Tomáš on 26. 4. 2015.
 */
public abstract class WidgetBeeeOnPersistence extends WidgetPersistence {

	private static final String PREF_ID = "id";
	private static final String PREF_NAME = "name";
	private static final String PREF_GATE_ID = "gate_id";
	private static final String PREF_GATE_ROLE = "gate_role";

	public String id;
	public String name;
	public String gateId;
	protected String mGateRole;

	protected User.Role mUserRole;

	WidgetBeeeOnPersistence(Context context, int widgetId, int offset, int boundView, UnitsHelper unitsHelper, TimeHelper timeHelper, WidgetSettings settings) {
		super(context, widgetId, offset, boundView, unitsHelper, timeHelper, settings);
	}

	@Override
	public void load() {
		id = mPrefs.getString(getProperty(PREF_ID), "");
		name = mPrefs.getString(getProperty(PREF_NAME), mContext.getString(R.string.persistence_widget_placeholder_not_exists));
		gateId = mPrefs.getString(getProperty(PREF_GATE_ID), "");
		mGateRole = mPrefs.getString(getProperty(PREF_GATE_ROLE), User.Role.Guest.getId());

		mUserRole = Utils.getEnumFromId(User.Role.class, mGateRole, User.Role.Guest);
	}

	@Override
	public void save() {
		mPrefs.edit()
				.putString(getProperty(PREF_ID), id)
				.putString(getProperty(PREF_NAME), name)
				.putString(getProperty(PREF_GATE_ID), gateId)
				.putString(getProperty(PREF_GATE_ROLE), mGateRole)
				.apply();
	}

	@Override
	public void delete() {
		mPrefs.edit()
				.remove(getProperty(PREF_ID))
				.remove(getProperty(PREF_NAME))
				.remove(getProperty(PREF_GATE_ID))
				.remove(getProperty(PREF_GATE_ROLE))
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

	public String getGateId() {
		return gateId;
	}
}
package com.rehivetech.beeeon.household.watchdog;

import com.rehivetech.beeeon.INameIdentifier;
import com.rehivetech.beeeon.R;

import java.util.ArrayList;

/**
 * Created by ThinkDeep on 8.3.2015.
 */
public class Watchdog implements INameIdentifier {
	public static final int TYPE_SENSOR = 1;
	public static final int TYPE_QUALITY_OF_LIFE = 2; // TODO not sure if usable here (specified on page)
	public static final int TYPE_GEOFENCE = 3;

	// types of possible actions which watchdog cane make
	public static final String ACTION_NOTIFICATION = "notif";
	public static final String ACTION_ACTOR = "act";

	// icons of possible actions
	public static final int[] actionIcons = {
			R.drawable.ic_bell_dark_24dp,
			R.drawable.ic_shutdown
	};

	// types of parameters (TODO should be as enum class in the future)
	public static final int PAR_MODULE_ID = 0;
	public static final int PAR_OPERATOR = 1;
	public static final int PAR_TRESHOLD = 2;
	public static final int PAR_ACTION_TYPE = 3;
	public static final int PAR_ACTION_VALUE = 4;

	private boolean mEnabled = true;
	private int mType = TYPE_SENSOR;
	private WatchdogBaseType mOperatorType;
	private String mId;
	private String mName;

	private String mGateId;

	private String mGeoRegionId;

	private ArrayList<String> mModules;
	private ArrayList<String> mParams;

	public Watchdog(String gateId, String id) {
		mGateId = gateId;
		mId = id;
	}

	public Watchdog(int type) {
		setOperatorType(type);
	}

	public void setOperatorType(int operatorType) {
		mType = operatorType;
		switch (operatorType) {
			default:
			case TYPE_SENSOR:
				mOperatorType = new WatchdogSensorType();
				break;

			case TYPE_GEOFENCE:
				mOperatorType = new WatchdogGeofenceType();
				break;
		}
	}

	public void setOperatorType(WatchdogBaseType type) {
		mOperatorType = type;
	}

	public WatchdogBaseType getOperatorType() {
		return mOperatorType;
	}

	public String getId() {
		return mId;
	}

	public void setId(String Id) {
		mId = Id;
	}

	public String getGateId() {
		return mGateId;
	}

	public void setGateId(String gateId) {
		mGateId = gateId;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public boolean isEnabled() {
		return mEnabled;
	}

	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}

	public int getType() {
		return mType;
	}

	public void setType(int type) {
		mType = type;
	}

	public ArrayList<String> getModules() {
		return mModules;
	}

	public void setModules(ArrayList<String> modules) {
		mModules = modules;
	}

	public void addModules(String module) {
		mModules.add(module);
	}

	public ArrayList<String> getParams() {
		return mParams;
	}

	public void setParams(ArrayList<String> params) {
		mParams = params;
		getOperatorType().setByType(getParam(Watchdog.PAR_OPERATOR));
	}

	public String getParam(int pos) {
		if (this.getParams() == null || this.getParams().size() <= pos) return null;

		return this.getParams().get(pos);
	}


	public String getGeoRegionId() {
		return mGeoRegionId;
	}

	public void setGeoRegionId(String GeoRegionId) {
		this.mGeoRegionId = GeoRegionId;
	}

	/**
	 * Set action type
	 *
	 * @param act ACTION_NOTIFICATION || ACITON_ACTOR
	 */
	public void setAction(String act) {
		this.getParams().set(PAR_ACTION_TYPE, act);
	}

	/**
	 * Returns parameter for action type
	 *
	 * @return default NOTIFICATION
	 */
	public String getAction() {
		String action = getParam(PAR_ACTION_TYPE);
		if (action == null) action = ACTION_NOTIFICATION;

		return action;
	}


	public int getActionIconResource() {
		switch (getAction()) {
			case ACTION_ACTOR:
				return actionIcons[1];

			default:
			case ACTION_NOTIFICATION:
				return actionIcons[0];
		}
	}
}

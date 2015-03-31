package com.rehivetech.beeeon.adapter.watchdog;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.R;

import java.util.ArrayList;

/**
 * Created by ThinkDeep on 8.3.2015.
 */
public class WatchDog implements IIdentifier {
    public static final int TYPE_SENSOR = 1;
    public static final int TYPE_QUALITY_OF_LIFE = 2; // TODO not sure if usable here (specified on page)
    public static final int TYPE_GEOFENCE = 3;

    // types of possible actions which watchdog cane make
    public static final String ACTION_NOTIFICATION = "notif";
    public static final String ACTION_ACTOR = "act";

    // icons of possible actions
    public static final int[] actionIcons = {
        R.drawable.ic_notification,
        R.drawable.ic_shutdown
    };

    // types of parameters (TODO should be as enum class in the future)
    public static final int PAR_DEV_ID = 0;
    public static final int PAR_OPERATOR = 1;
    public static final int PAR_TRESHOLD = 2;
    public static final int PAR_ACTION_TYPE = 3;
    public static final int PAR_ACTION_VALUE = 4;

    private boolean mEnabled = true;
    private int mType = TYPE_SENSOR;
    private String mId;
    private String mName;
    private String mAdapterId;

    private WatchDogBaseType mOperatorType;

	private String mGeoRegionId;
	private String mGeoDirectionType;

    private ArrayList<String> mDevices;
    private ArrayList<String> mParams;

    public WatchDog(int type){
        mType = type;
        switch(mType){
            default:
            case TYPE_SENSOR:
                mOperatorType = new WatchDogSensorType();
                break;
            /*
            case TYPE_GEOFENCE:
                mOperatorType = new WatchDogSensorType();
                break;
            //*/
        }
    }

    public WatchDogBaseType getOperatorType(){
        return mOperatorType;
    }

    public String getId(){
        return mId;
    }

    public void setId(String Id){
        mId = Id;
    }

    public String getAdapterId(){
        return mAdapterId;
    }

    public void setAdapterId(String adapterId){
        mAdapterId = adapterId;
    }

    public String getName(){
        return mName;
    }

    public void setName(String name){
        mName = name;
    }

    public boolean isEnabled(){
        return mEnabled;
    }

    public void setEnabled(boolean enabled){
        mEnabled = enabled;
    }

    public int getType(){
        return mType;
    }

    public void setType(int type){
        mType = type;
    }

    public ArrayList<String> getDevices(){
        return mDevices;
    }

    public void setDevices(ArrayList<String> devices){
        mDevices = devices;
    }

    public void AddDevice(String device){
        mDevices.add(device);
    }

    public ArrayList<String> getParams(){
        return mParams;
    }

    public void setParams(ArrayList<String> params){
        mParams = params;
        getOperatorType().setParams(mParams);
        getOperatorType().setByType(this.getParams().get(WatchDog.PAR_OPERATOR));
    }

    public void AddParam(String param) {
        mParams.add(param);
    }

	public String getGeoDirectionType() {
		return mGeoDirectionType;
	}

	public void setGeoDirectionType(String GeoDirectionType) {
		this.mGeoDirectionType = GeoDirectionType;
	}

	public String getGeoRegionId() {
		return mGeoRegionId;
	}

	public void setGeoRegionId(String GeoRegionId) {
		this.mGeoRegionId = GeoRegionId;
	}

    public String getParam(int pos){
        if(this.getParams().size() <= pos) return null;

        return this.getParams().get(pos);
    }

    public int getActionIconResource(){
        String action = getParam(PAR_ACTION_TYPE);
        if(action == null) action = ACTION_NOTIFICATION;

        switch(action){
            case ACTION_ACTOR:
                return actionIcons[1];

            default:
            case ACTION_NOTIFICATION:
                return actionIcons[0];
        }
    }
}

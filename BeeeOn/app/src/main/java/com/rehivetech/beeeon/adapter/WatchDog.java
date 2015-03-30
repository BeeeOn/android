package com.rehivetech.beeeon.adapter;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.device.Device;

import java.util.ArrayList;

/**
 * Created by ThinkDeep on 8.3.2015.
 */
public class WatchDog implements IIdentifier {

    // TODO make as separated enum table
    public static final String OPERATOR_GT = "gt";
    public static final String OPERATOR_LT = "lt";

    // types of possible actions which watchdog cane make
    public static final int ACTION_UNKNOWN = 0;
    public static final int ACTION_NOTIFICATION = 1;
    public static final int ACTION_ACTOR = 2;

    // types of parameters (TODO should be as enum class in the future)
    public static final int PAR_DEV_ID = 0; // TODO preskocit zatim parametr s ID senzoru
    public static final int PAR_OPERATOR = 1;
    public static final int PAR_TRESHOLD = 2;
    public static final int PAR_ACTION_VALUE = 3;

    public static final int[] actionIcons = {
            R.drawable.ic_unknown,
            R.drawable.ic_notification,
            R.drawable.ic_shutdown
    };

    private boolean mEnabled = true;
    private int mType = 1; // temporary solution
    private String mId;
    private String mName;
    private String mAdapterId;

	private String mGeoRegionId;
	private String mGeoDirectionType;

    private ArrayList<String> mDevices;
    private ArrayList<String> mParams;

    public WatchDog(){}
    public WatchDog(int type){
        mType = type;
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
    }

    public void AddParam(String param) {
        mParams.add(param);
    }

    public String getId(){
        return mId;
    }

    public void setName(String name){
        mName = name;
    }

    public String getName(){
        return mName;
    }

    public String getAdapterId() {
        return mAdapterId;
    }

    public void setAdapterId(String AdapterId) {
        this.mAdapterId = AdapterId;
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

    public int getActionIconResource(){
        if(mType < 0 || mType >= actionIcons.length)
            return actionIcons[0];
        else
            return actionIcons[mType];
    }

    public int getOperatorIconResource(){
        String operator = this.getParams().get(WatchDog.PAR_OPERATOR);
        switch(operator){
            case OPERATOR_LT:
                return R.drawable.ic_action_previous_item;

            default:
            case OPERATOR_GT:
                return R.drawable.ic_action_next_item;
        }

    }
}

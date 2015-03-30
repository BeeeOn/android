package com.rehivetech.beeeon.adapter;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.adapter.device.Device;

import java.util.ArrayList;

/**
 * Created by ThinkDeep on 8.3.2015.
 */
public class WatchDog implements IIdentifier {
    public static enum OperatorType{ SMALLER, GREATER }
    public static enum ActionType{ NOTIFICATION, ACTOR_ACTION }

    private boolean mEnabled = true;
    private int mtype = 1; // temporary solution
    private String mId;
    private String mName;
    private String mAdapterId;

	private String mGeoRegionId;
	private String mGeoDirectionType;

    private ArrayList<String> mDevices;
    private ArrayList<String> mParams;

    public String getAdapterId(){
        return mAdapterId;
    }

    public void setAdapterId(String adapterId){
        mAdapterId = adapterId;
    }

    public boolean isEnabled(){
        return mEnabled;
    }

    public void setEnabled(boolean enabled){
        mEnabled = enabled;
    }

    public int getType(){
        return mtype;
    }

    public void setType(int type){
        mtype = type;
    }

    public ArrayList<String> getDevices(){
        return mDevices;
    }

    public void AddDevice(String device){
        mDevices.add(device);
    }

    public ArrayList<String> getParams(){
        return mParams;
    }

    public void AddParam(String param){
        mParams.add(param);
    }

    public void setDevices(ArrayList<String> devices){
        mDevices = devices;
    }

    public void setParams(ArrayList<String> params){
        mParams = params;
    }

    public void setId(String Id){
        mId = Id;
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

    WatchDog(){};

    public WatchDog(int type){
        mtype = type;
    }



}

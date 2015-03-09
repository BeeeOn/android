package com.rehivetech.beeeon.network.xml;

import com.rehivetech.beeeon.adapter.device.Device;

import java.util.ArrayList;

/**
 * Created by ThinkDeep on 8.3.2015.
 */
public class WatchDog {

    private boolean mEnabled = true;
    private int mtype = 1; // temporary solution
    private String mId;
    private String mName;

    private ArrayList<Device> mDevices;
    private ArrayList<String> mParams;

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

    public ArrayList<Device> getDevices(){
        return mDevices;
    }

    public void AddDevice(Device device){
        mDevices.add(device);
    }

    public ArrayList<String> getParams(){
        return mParams;
    }

    public void AddParam(String param){
        mParams.add(param);
    }

    public void setDevices(ArrayList<Device> devices){
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

    WatchDog(){};

    WatchDog(int type){
        mtype = type;
    }

}

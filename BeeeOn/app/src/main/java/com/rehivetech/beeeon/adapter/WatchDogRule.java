package com.rehivetech.beeeon.adapter;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.INameIdentifier;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.values.BaseValue;

public class WatchDogRule implements IIdentifier, INameIdentifier {
    public static enum OperatorType{ SMALLER, GREATER }
    public static enum ActionType{ NOTIFICATION, ACTOR_ACTION }

    protected String mAdapterId;
    protected String mId = "";
    protected String mName = "";
    protected Device mDevice;
    protected OperatorType mOperator;
    protected ActionType mAction;
    protected BaseValue mTreshold;
    protected boolean mEnabled;

    // TODO udelat constructor bez parametru

    public WatchDogRule(String id, String adapterId, String name, Device dev, OperatorType op, ActionType ac, BaseValue tresh, boolean act) {
        setId(id);
        setAdapterId(adapterId);
        setName(name);
        setDevice(dev);
        setOperator(op);
        setAction(ac);
        setTreshold(tresh);
        setEnabled(act);
    }

    public WatchDogRule(){

    }

    @Override
    public String getId() {
        return mId;
    }
    public void setId(String id) {
        mId = id;
    }

    // getter & setter for Adapter
    public String getAdapterId() {
        return mAdapterId;
    }
    public void setAdapterId(String adapterId) {
        mAdapterId = adapterId;
    }

    // getter & setter for Device
    public Device getDevice() {
        return mDevice;
    }
    public void setDevice(Device device) {
        mDevice = device;
    }

    // getter & setter for Name
    public String getName(){
        return mName;
    }
    public void setName(String name){
        mName = name;
    }

    // getter & setter for Operator
    public OperatorType getOperator() {
        return mOperator;
    }
    public void setOperator(OperatorType mOperator) {
        this.mOperator = mOperator;
    }

    // getter & setter for Action
    public ActionType getAction() {
        return mAction;
    }
    public void setAction(ActionType mAction) {
        this.mAction = mAction;
    }

    // getter & setter for Treshold
    public BaseValue getTreshold() {
        return mTreshold;
    }
    public void setTreshold(BaseValue mTreshold) {
        this.mTreshold = mTreshold;
    }

    // getter & setter for IsActive
    public boolean getEnabled() {
        return mEnabled;
    }
    public void setEnabled(boolean mIsActive) {
        this.mEnabled = mIsActive;
    }
}

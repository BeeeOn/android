package com.rehivetech.beeeon.adapter;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.values.BaseValue;

public class WatchDogRule implements IIdentifier {
    public static enum OperatorType{ SMALLER, GREATER }
    public static enum ActionType{ NOTIFICATION, ACTOR_ACTION }

    protected String mAdapterId;
    protected String mId = "";
    protected String mName = "";
    protected Device mDevice;
    protected OperatorType mOperator;
    protected ActionType mAction;
    protected BaseValue mTreshold;
    protected boolean mIsActive;

    public WatchDogRule(String id, String name, Device dev, OperatorType op, ActionType ac, BaseValue tresh, boolean act) {
        setId(id);
        setName(name);
        setDevice(dev);
        setOperator(op);
        setAction(ac);
        setTreshold(tresh);
        setIsActive(act);
    }

    @Override
    public String getId() {
        return mId;
    }
    public void setId(String id) {
        mId = id;
    }

    // getter & setter for Device
    public void setDevice(Device device) {
        mDevice = device;
    }
    public Device getDevice() {
        return mDevice;
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
    public boolean getIsActive() {
        return mIsActive;
    }
    public void setIsActive(boolean mIsActive) {
        this.mIsActive = mIsActive;
    }
}

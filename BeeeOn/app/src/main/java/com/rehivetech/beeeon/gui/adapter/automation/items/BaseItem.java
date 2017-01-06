package com.rehivetech.beeeon.gui.adapter.automation.items;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Temporary models. After REST implementation will use AutomationModels.
 * Created by Mrnda on 10/14/2016.
 */

public abstract class BaseItem implements Parcelable {

    @SerializedName("name")
    private String mName;

    @SerializedName("gateId")
    private String mGateId;

    @SerializedName("active")
    private boolean mActive;

    public BaseItem(String name, String gateId, boolean active) {
        mName = name;
        mGateId = gateId;
        mActive = active;
    }

    protected BaseItem(Parcel in) {
        mName = in.readString();
        mGateId = in.readString();
        mActive = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mGateId);
        dest.writeInt(mActive ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getGateId() {
        return mGateId;
    }

    public String getName() {
        return mName;
    }

    public boolean isActive(){return mActive;}

    public void setActive(boolean active) {mActive = active;}
}
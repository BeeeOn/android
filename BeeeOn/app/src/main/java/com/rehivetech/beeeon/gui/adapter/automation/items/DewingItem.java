package com.rehivetech.beeeon.gui.adapter.automation.items;

import android.os.Parcel;

/**
 * Created by Mrnda on 10/16/2016.
 */

public class DewingItem extends BaseItem{


    private String mInsideTemperatureAbsoluteModuleId;
    private String mOutsideTemperatureAbsoluteModueId;
    private String mHumidityAbsoluteModuleId;

    public DewingItem(String name,
                      String gateId,
                      boolean active,
                      String insideTemperatureModuleId,
                      String outsideTemperatureModuleId,
                      String humidityModuleId) {
        super(name, gateId, active);
        mInsideTemperatureAbsoluteModuleId = insideTemperatureModuleId;
        mOutsideTemperatureAbsoluteModueId = outsideTemperatureModuleId;
        mHumidityAbsoluteModuleId = humidityModuleId;
    }


    public DewingItem(Parcel in){
        super(in);
        mHumidityAbsoluteModuleId = in.readString();
        mOutsideTemperatureAbsoluteModueId = in.readString();
        mInsideTemperatureAbsoluteModuleId = in.readString();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mHumidityAbsoluteModuleId);
        dest.writeString(mOutsideTemperatureAbsoluteModueId);
        dest.writeString(mInsideTemperatureAbsoluteModuleId);
    }

    public String getInsideTemperatureModuleId() {
        return mInsideTemperatureAbsoluteModuleId;
    }

    public String getOutstideTemeperatureModuleId() {
        return mOutsideTemperatureAbsoluteModueId;
    }

    public String getHumidityModuleId() {
        return mHumidityAbsoluteModuleId;
    }

    public static final Creator<DewingItem> CREATOR = new Creator<DewingItem>() {
        public DewingItem createFromParcel(Parcel source) {
            return new DewingItem(source);
        }

        public DewingItem[] newArray(int size) {
            return new DewingItem[size];
        }
    };
}

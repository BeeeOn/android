package com.rehivetech.beeeon.gui.adapter.automation.items;

/**
 * Created by Mrnda on 10/16/2016.
 */

public class DewingItem extends BaseItem{


    private String mInsideTemperatureAbsoluteModuleId;
    private String mOutstideTemeperatureAbsoluteModuleId;
    private String mHumidityAbsoluteModuleId;

    public DewingItem(String name,
                      String gateId,
                      boolean active,
                      String insideTemperatureModuleId,
                      String outsideTemperatureModuleId,
                      String humidityModuleId) {
        super(name, gateId, active);
        mInsideTemperatureAbsoluteModuleId = insideTemperatureModuleId;
        mOutstideTemeperatureAbsoluteModuleId = outsideTemperatureModuleId;
        mHumidityAbsoluteModuleId = humidityModuleId;
    }

    public String getInsideTemperatureModuleId() {
        return mInsideTemperatureAbsoluteModuleId;
    }

    public String getOutstideTemeperatureModuleId() {
        return mOutstideTemeperatureAbsoluteModuleId;
    }

    public String getHumidityModuleId() {
        return mHumidityAbsoluteModuleId;
    }
}

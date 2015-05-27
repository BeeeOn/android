package data;

import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robert on 23. 5. 2015.
 */
public class Device {

    private final List<Module> mModules = new ArrayList<>();

    private final int mTypeId;

    private final String mTypeName;

    private Translation mName;

    private Translation mManufacturer;

    private Features mFeatures;

    public Device(int typeId, String typeName) {
        mTypeId = typeId;
        mTypeName = typeName;
    }

    public List<Module> getModules() {
        return mModules;
    }

    public void setModules(List<Module> modules) {
        mModules.clear();
        mModules.addAll(modules);
    }

    public int getTypeId() {
        return mTypeId;
    }

    public String getTypeName() {
        return mTypeName;
    }

    public Translation getName() {
        return mName;
    }

    public void setName(Translation name) {
        mName = name;
    }

    public Translation getManufacturer() {
        return mManufacturer;
    }

    public void setManufacturer(Translation manufacturer) {
        mManufacturer = manufacturer;
    }

    public Features getFeatures() {
        return mFeatures;
    }

    public void setFeatures(Features features) {
        mFeatures = features;
    }

    public static class Features {
        private Integer mRefresh;
        private Boolean mBattery;
        private Boolean mLed;

        public boolean hasRefresh() {
            return mRefresh != null && mRefresh > 0;
        }

        @Nullable
        public Integer getDefaultRefresh() {
            return mRefresh;
        }

        public void setRefresh(Integer refresh) {
            mRefresh = refresh;
        }

        public boolean hasBattery() {
            return mBattery != null && mBattery;
        }

        public void setBattery(Boolean battery) {
            mBattery = battery;
        }

        public boolean hasLed() {
            return mLed != null && mLed;
        }

        public void setLed(Boolean led) {
            mLed = led;
        }

        @Override
        public String toString() {
            String res = "";
            if (hasRefresh())
                res += ", refresh(" + getDefaultRefresh() + ")";
            if (hasBattery())
                res += ", battery";
            if (hasLed())
                res += ", led";
            return res.isEmpty() ? "-none-" : res.substring(2);
        }
    }
}

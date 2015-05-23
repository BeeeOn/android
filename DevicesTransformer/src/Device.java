import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robert on 23. 5. 2015.
 */
public class Device {

    private final List<Module> mModules = new ArrayList<>();

    private final String mTypeId;

    private final String mTypeName;

    private Translation mName;

    private Translation mManufacturer;

    private int mRefresh;

    private boolean mLed;

    private boolean mBattery;

    public Device(String typeId, String typeName) {
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

    public String getTypeId() {
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

    public int getRefresh() {
        return mRefresh;
    }

    public void setRefresh(int refresh) {
        mRefresh = refresh;
    }

    public boolean isLed() {
        return mLed;
    }

    public void setLed(boolean led) {
        mLed = led;
    }

    public boolean isBattery() {
        return mBattery;
    }

    public void setBattery(boolean battery) {
        mBattery = battery;
    }
}

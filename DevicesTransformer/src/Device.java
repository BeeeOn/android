/**
 * Created by Robert on 23. 5. 2015.
 */
public class Device {

    private final String mId;

    private final String mName;

    public Device(String id, String name) {
        mId = id;
        mName = name;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

}

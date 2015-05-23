import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robert on 23. 5. 2015.
 */
public class Devices {

    private final String mVersion;

    private final List<Device> mDevices = new ArrayList<>();

    public Devices(String version) {
        mVersion = version;
    }

    public String getVersion() {
        return mVersion;
    }

    public void addDevice(Device device) {
        mDevices.add(device);
    }

    public void printLog(PrintStream stream) {
        /*stream.println("--- LISTING OF DEVICES ---");

        for (Devices.Item item : mItems) {
            String name = item.key;
            String value = item.value;

            System.out.println(String.format("%s = \"%s\"", name, value));
        }*/
    }

    public void printDevicesJava(PrintWriter writer) {
        /*writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        writer.println("<resources>");

        for (Devices.Item item : mItems) {
            String name = item.key;
            String value = item.value;

            writer.println(String.format("\t<string name=\"%s\">%s</string>", name, value));
        }

        writer.println("</resources>");*/
    }
    
}

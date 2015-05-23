package data;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
        stream.println(String.format("--- LISTING OF DEVICES (version %s) ---", mVersion));

        for (Device device : mDevices) {
            stream.println(String.format("[%s] %s",
                    device.getTypeId(),
                    device.getTypeName()));

            stream.println(String.format("\tManufacturer: %s\n\tName: %s\n\tRefresh: %s\n\tLed: %s\n\tBattery: %s",
                    Arrays.toString(device.getManufacturer().getResourceIds()),
                    Arrays.toString(device.getName().getResourceIds()),
                    String.valueOf(device.getRefresh()),
                    String.valueOf(device.isLed()),
                    String.valueOf(device.isBattery())));

            for (Module module : device.getModules()) {
                // TODO
            }

            stream.println("------");
        }
    }

    public void printDevicesJava(PrintWriter writer) {
        /*writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        writer.println("<resources>");

        for (data.Devices.Item item : mItems) {
            String name = item.key;
            String value = item.value;

            writer.println(String.format("\t<string name=\"%s\">%s</string>", name, value));
        }

        writer.println("</resources>");*/
    }
    
}

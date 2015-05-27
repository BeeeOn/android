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
			stream.println(String.format("[%d] %s",
					device.getTypeId(),
					device.getTypeName()));

			stream.println(String.format("\tManufacturer: %s\n\tName: %s\n\tFeatures: %s",
					Arrays.toString(device.getManufacturer().getTranslationIds()),
					Arrays.toString(device.getName().getTranslationIds()),
					getFeaturesString(device.getFeatures())));

			stream.println("\tDevices:");
			for (Module module : device.getModules()) {
				stream.println(String.format("\t\t[%d] \tType: %s\tOffset: %d%s",
						module.getId(),
						module.getType(),
						module.getOffset(),
						module.getOrder() != null ? "\tOrder: " + module.getOrder() : ""
				));

				stream.println(String.format("\t\t\t\tName: %s%s\t",
						module.getGroup() != null ? Arrays.toString(module.getGroup().getTranslationIds()) : "",
						Arrays.toString(module.getName().getTranslationIds())
						));
			}

			stream.println("------");
		}
	}

	private String getFeaturesString(Device.Features features) {
		String res = "";
		if (features != null) {
			if (features.hasRefresh())
				res += ", refresh(" + features.getDefaultRefresh() + ")";
			if (features.hasBattery())
				res += ", battery";
			if (features.hasLed())
				res += ", led";
			if (!res.isEmpty())
				res = res.substring(2);
		}
		return String.format("[%s]", res);
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

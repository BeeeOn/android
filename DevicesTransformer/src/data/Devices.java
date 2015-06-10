package data;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
					device.getManufacturer().getTranslationId(),
					device.getName().getTranslationId(),
					getFeaturesString(device.getFeatures())
			));

			stream.println("\tDevices:");
			for (Module module : device.getModules()) {
				stream.println(String.format("\t\t[%d] \tType: %s\tOffset: %d%s",
						module.getId(),
						module.getType(),
						module.getOffset(),
						module.getOrder() != null ? "\tOrder: " + module.getOrder() : ""
				));

				stream.println(String.format("\t\t\t\tName: %s%s\t",
						module.getGroup() != null ? module.getGroup().getTranslationId() : "",
						module.getName().getTranslationId()
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
		writer.println("/** BEGIN OF GENERATED CONTENT **/");

		Iterator<Device> it = mDevices.iterator();
		while (it.hasNext()) {
			Device device = it.next();

			Device.Features features = device.getFeatures();
			if (features == null) {
				features = new Device.Features();
			}

			// Begin of type definition
			writer.println(String.format("TYPE_%d(\"%d\", \"%s\", %s, %s, new DeviceFeatures(%s, %s, %s)) {",
					device.getTypeId(),
					device.getTypeId(),
					device.getTypeName(),
					device.getName().getResourceId(),
					device.getManufacturer().getResourceId(),
					features.hasRefresh() ? features.getDefaultRefresh().toString() : "null",
					features.hasLed() ? "true" : "false",
					features.hasBattery() ? "true" : "false"
			));

				// Begin of createModules() method
				writer.println("\t@Override\n\tpublic List<Module> createModules(Device device) {");

					// Begin of modules array
					writer.println("\t\treturn Arrays.asList(");

						Iterator<Module> itModule = device.getModules().iterator();
						while (itModule.hasNext()) {
							Module module = itModule.next();
							Translation tgroup = module.getGroup();
							String group = tgroup != null ? tgroup.getResourceId() : "null";

							Translation tname = module.getName();
							String name = tname != null ? tname.getResourceId() : "null";

							writer.print(String.format("\t\t\t\tnew Module(device, \"%d\", %s, %d, %s, %s, %s, %b",
									module.getId(),
									module.getType(),
									module.getOffset(),
									module.getOrder(),
									group,
									name,
									module.isActuator())
							);

							if (!module.getRules().isEmpty()) {
								writer.println(", Arrays.asList(");

								Iterator<Module.Rule> itRule = module.getRules().iterator();
								while (itRule.hasNext()) {
									Module.Rule rule = itRule.next();

									String ids = Arrays.toString(rule.hideModulesIds);
									ids = ids.substring(1, ids.length() - 1);

									writer.print(String.format("\t\t\t\t\t\tnew Module.Rule(%d, new int[] {%s})",
											rule.value,
											ids
									));

									writer.println(itRule.hasNext() ? "," : "");
								}

								writer.print("\t\t\t\t)");
							} else {
								writer.print(", null");
							}

							if (!module.getValues().isEmpty()) {
								writer.println(", Arrays.asList(");

								Iterator<Module.Value> itValue = module.getValues().iterator();
								while (itValue.hasNext()) {
									Module.Value value = itValue.next();

									writer.print(String.format("\t\t\t\t\t\tnew EnumValue.Item(%d, \"%d\", %s)",
											value.id,
											value.id,
											value.translation.getResourceId() // value name resource
									));

									writer.println(itValue.hasNext() ? "," : "");
								}

								writer.print("\t\t\t\t)");
							}

							writer.println(itModule.hasNext() ? ")," : ")");
						}

					writer.println("\t\t);");
					// End of modules array


				writer.println("\t}");
				// End of createModules() method

			writer.println(it.hasNext() ? "}," : "};");
			// End of type definition
		}

		writer.println("/** END OF GENERATED CONTENT **/");
	}

}

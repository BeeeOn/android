package parser;

import com.sun.org.apache.xml.internal.utils.DefaultErrorHandler;
import data.Device;
import data.Devices;
import data.Module;
import data.Translation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robert on 23. 5. 2015.
 */
public class DevicesParser {

	public static Devices parse(File file) throws ParserConfigurationException, IOException, SAXException {
		//Get the DOM Builder Factory
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);

		//Get the DOM Builder
		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setErrorHandler(new DefaultErrorHandler());

		//Load and Parse the XML document
		//document contains the complete XML as a Tree.
		Document document = builder.parse(file);

		//Iterating through the nodes and extracting the data.
		Node devicesNode = document.getDocumentElement();
		if (!(devicesNode instanceof Element)) {
			return null;
		}

		String version = ((Element) devicesNode).getAttribute("version");

		Devices devices = new Devices(version);

		NodeList itemsNodes = devicesNode.getChildNodes();
		for (int i = 0; i < itemsNodes.getLength(); i++) {
			Node node = itemsNodes.item(i);

			if (node instanceof Element) {
				String tag = node.getNodeName();
				if (tag.equals("device")) {
					Device device = parseDevice((Element) node);
					devices.addDevice(device);
				} else {
					throw new IllegalStateException(String.format("Unexpected element '%s' (expected 'device')", tag));
				}
			}
		}

		return devices;
	}

	private static Device parseDevice(Element element) {
		int typeId = Integer.valueOf(element.getAttribute("id"));
		String typeName = element.getAttribute("name");

		Device device = new Device(typeId, typeName);

		NodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			if (node instanceof Element) {
				String tag = node.getNodeName();
				if (tag.equals("name")) {
					device.setName(new Translation(node.getTextContent()));
				} else if (tag.equals("manufacturer")) {
					device.setManufacturer(new Translation(node.getTextContent()));
				} else if (tag.equals("features")) {
					Device.Features features = parseFeatures((Element) node);
					device.setFeatures(features);
				} else if (tag.equals("modules")) {
					List<Module> modules = parseModules((Element) node);
					device.setModules(modules);
				} else {
					throw new IllegalStateException(String.format("Unexpected element '%s' (expected 'name|manufacturer|features|modules')", tag));
				}
			}
		}

		return device;
	}

	private static Device.Features parseFeatures(Element element) {
		Device.Features features = new Device.Features();

		NodeList featuresNodes = element.getChildNodes();
		for (int i = 0; i < featuresNodes.getLength(); i++) {
			Node node = featuresNodes.item(i);

			if (node instanceof Element) {
				String tag = node.getNodeName();
				if (tag.equals("refresh")) {
					String refresh = ((Element) node).getAttribute("default");
					features.setRefresh(Integer.parseInt(refresh));
				} else if (tag.equals("battery")) {
					features.setBattery(Boolean.TRUE);
				} else if (tag.equals("led")) {
					features.setLed(Boolean.TRUE);
				} else {
					throw new IllegalStateException(String.format("Unexpected element '%s' (expected 'refresh|battery|led')", tag));
				}
			}
		}

		return features;
	}

	private static List<Module> parseModules(Element element) {
		List<Module> modules = new ArrayList<>();

		NodeList modulesNodes = element.getChildNodes();
		for (int i = 0; i < modulesNodes.getLength(); i++) {
			Node node = modulesNodes.item(i);

			if (node instanceof Element) {
				String tag = node.getNodeName();
				if (tag.equals("module")) {
					Module module = parseModule((Element) node);
					modules.add(module);
				} else {
					throw new IllegalStateException(String.format("Unexpected element '%s' (expected 'module')", tag));
				}
			}
		}

		return modules;
	}

	private static Module parseModule(Element element) {
		int id = Integer.parseInt(element.getAttribute("id"));
		String type = element.getAttribute("type");
		int offset = Integer.parseInt(element.getAttribute("offset"));

		Module module = new Module(id, type, offset);

		NodeList moduleNodes = element.getChildNodes();
		for (int i = 0; i < moduleNodes.getLength(); i++) {
			Node node = moduleNodes.item(i);

			if (node instanceof Element) {
				String tag = node.getNodeName();
				if (tag.equals("order")) {
					module.setOrder(Integer.parseInt(node.getTextContent()));
				} else if (tag.equals("group")) {
					module.setGroup(new Translation(node.getTextContent()));
				} else if (tag.equals("name")) {
					module.setName(new Translation(node.getTextContent()));
				} else if (tag.equals("is-actuator")) {
					module.setActuator(true);
				} else if (tag.equals("constraints")) {
					Module.Constraints constraints = parseConstraints((Element) node);
					module.setConstraints(constraints);
				} else if (tag.equals("values")) {
					Translation name = new Translation(((Element) node).getAttribute("name"));
					List<Module.Value> values = parseValues((Element) node);
					module.setValues(name, values);
				} else if (tag.equals("rules")) {
					List<Module.Rule> rules = parseRules((Element) node);
					module.setRules(rules);
				} else {
					throw new IllegalStateException(String.format("Unexpected element '%s' (expected 'module')", tag));
				}
			}
		}

		return module;
	}

	private static Module.Constraints parseConstraints(Element element) {
		Module.Constraints constraints = new Module.Constraints();

		NodeList constraintsNodes = element.getChildNodes();
		for (int i = 0; i < constraintsNodes.getLength(); i++) {
			Node node = constraintsNodes.item(i);

			if (node instanceof Element) {
				String tag = node.getNodeName();
				if (tag.equals("min")) {
					constraints.setMin(Double.parseDouble(node.getTextContent()));
				} else if (tag.equals("max")) {
					constraints.setMax(Double.parseDouble(node.getTextContent()));
				} else if (tag.equals("granularity")) {
					constraints.setGranularity(Double.parseDouble(node.getTextContent()));
				} else {
					throw new IllegalStateException(String.format("Unexpected element '%s' (expected 'min|max|granularity')", tag));
				}
			}
		}

		return constraints;
	}

	private static List<Module.Value> parseValues(Element element) {
		List<Module.Value> values = new ArrayList<>();

		NodeList valuesNodes = element.getChildNodes();
		for (int i = 0; i < valuesNodes.getLength(); i++) {
			Node node = valuesNodes.item(i);

			if (node instanceof Element) {
				String tag = node.getNodeName();
				if (tag.equals("value")) {
					int id = Integer.parseInt(((Element) node).getAttribute("id"));
					Translation translation = new Translation(node.getTextContent());
					Module.Value value = new Module.Value(id, translation);
					values.add(value);
				} else {
					throw new IllegalStateException(String.format("Unexpected element '%s' (expected 'value')", tag));
				}
			}
		}

		return values;
	}

	private static List<Module.Rule> parseRules(Element element) {
		List<Module.Rule> rules = new ArrayList<>();

		NodeList rulesNodes = element.getChildNodes();
		for (int i = 0; i < rulesNodes.getLength(); i++) {
			Node node = rulesNodes.item(i);

			if (node instanceof Element) {
				String tag = node.getNodeName();
				if (tag.equals("if")) {
					int value = Integer.parseInt(((Element) node).getAttribute("value"));

					List<Integer> hideModulesIdsList = new ArrayList<>();
					NodeList hideModuleNodes = ((Element) node).getElementsByTagName("hide-module");
					for (int j = 0; j < hideModuleNodes.getLength(); j++) {
						Node hideModuleNode = hideModuleNodes.item(j);
						if (hideModuleNode instanceof Element) {
							Integer id = new Integer(((Element) hideModuleNode).getAttribute("id"));
							hideModulesIdsList.add(id);
						}
					}

					Module.Rule rule = new Module.Rule(value, hideModulesIdsList.toArray(new Integer[hideModulesIdsList.size()]));
					rules.add(rule);
				} else {
					throw new IllegalStateException(String.format("Unexpected element '%s' (expected 'if')", tag));
				}
			}
		}

		return rules;
	}

}

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
                String tag = ((Element) node).getTagName();
                if (tag.equals("device")) {
                    // Parse device
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
        int typeId = Integer.valueOf(element.getAttribute("idtype"));
        String typeName = element.getAttribute("name");

        Device device = new Device(typeId, typeName);

        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String name = node.getNodeName();

            if (node instanceof Element) {
                String content = node.getTextContent();

                if (name.equals("name")) {
                    device.setName(new Translation(content));
                } else if (name.equals("manufacturer")) {
                    device.setManufacturer(new Translation(content));
                } else if (name.equals("refresh")) {
                    device.setRefresh(Integer.parseInt(content));
                } else if (name.equals("led")) {
                    device.setLed(Boolean.parseBoolean(content));
                } else if (name.equals("battery")) {
                    device.setBattery(Boolean.parseBoolean(content));
                } else if (name.equals("modules")) {
                    List<Module> modules = parseModules((Element) node);
                    device.setModules(modules);
                }
            }
        }

        return device;
    }

    private static List<Module> parseModules(Element element) {
        List<Module> modules = new ArrayList<>();

        NodeList modulesNodes = element.getChildNodes();
        for (int i = 0; i < modulesNodes.getLength(); i++) {
            Node node = modulesNodes.item(i);

            if (node instanceof Element) {
                String tag = ((Element) node).getTagName();
                if (tag.equals("module")) {
                    // Parse module
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
        int order = Integer.parseInt(element.getAttribute("order"));
        double min = Double.parseDouble(element.getAttribute("min"));
        double max = Double.parseDouble(element.getAttribute("max"));
        double granularity = Double.parseDouble(element.getAttribute("granularity"));
        boolean enumValues = Boolean.parseBoolean(element.getAttribute("vstrings"));

        Module module = new Module(id, type, offset, order);
        module.setMin(min);
        module.setMax(max);
        module.setGranularity(granularity);
        module.setEnumValues(enumValues);

        // TODO: rest of data

        return module;
    }

}

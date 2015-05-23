import org.w3c.dom.*;
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

        //Get the DOM Builder
        DocumentBuilder builder = factory.newDocumentBuilder();

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
                    throw new IllegalStateException(String.format("Unsupported element '%s'", tag));
                }
            }
        }

        return devices;
    }

    private static Device parseDevice(Element element) {
        String typeId = element.getAttribute("idtype");
        String typeName = element.getAttribute("name");

        Device device = new Device(typeId, typeName);

        NamedNodeMap attributes = element.getAttributes();
        Node nameNode = attributes.getNamedItem("name");
        if (nameNode != null) {
            device.setName(new Translation(nameNode.getTextContent()));
        }

        Node manufacturerNode = attributes.getNamedItem("manufacturer");
        if (manufacturerNode != null) {
            device.setManufacturer(new Translation(manufacturerNode.getTextContent()));
        }

        Node refreshNode = attributes.getNamedItem("refresh");
        if (refreshNode != null) {
            device.setRefresh(Integer.parseInt(refreshNode.getTextContent()));
        }

        Node ledNode = attributes.getNamedItem("led");
        if (ledNode != null) {
            device.setLed(Boolean.parseBoolean(ledNode.getTextContent()));
        }

        Node batteryNode = attributes.getNamedItem("battery");
        if (batteryNode != null) {
            device.setBattery(Boolean.parseBoolean(batteryNode.getTextContent()));
        }

        Node modulesNode = attributes.getNamedItem("modules");
        if (modulesNode == null) {
            throw new IllegalStateException(String.format("Missing 'modules' element in device '%s' ('%s')", typeName, typeId));
        }

        List<Module> modules = parseModules((Element) modulesNode);
        device.setModules(modules);

        return device;
    }

    private static List<Module> parseModules(Element element) {
        /*List<Language.Item> items = new ArrayList<Language.Item>();

        String prefix = element.getAttribute("name").trim();

        NodeList itemsNodes = element.getChildNodes();
        for (int i = 0; i < itemsNodes.getLength(); i++) {
            Node node = itemsNodes.item(i);

            if (node instanceof Element) {
                String name = ((Element) node).getAttribute("name").trim();
                String value = node.getTextContent();

                Language.Item item = new Language.Item(prefix, name, value);
                items.add(item);
            }
        }

        return items;*/
    }

}

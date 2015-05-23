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
        String idType = element.getAttribute("idtype");
        String nameType = element.getAttribute("name");

        Device device = new Device(idType, nameType);

        NamedNodeMap attributes = element.getAttributes();
        Node nameNode = attributes.getNamedItem("name");
        Node refreshNode = attributes.getNamedItem("refresh");
        Node ledNode = attributes.getNamedItem("led");
        Node batteryNode = attributes.getNamedItem("battery");
        Node manufacturerNode = attributes.getNamedItem("manufacturer");

        Node modulesNode = attributes.getNamedItem("modules");
        if (modulesNode == null) {
            throw new IllegalStateException(String.format("Missing 'modules' element in device '%s' ('%s')", nameType, idType));
        }

        Modules modules = parseModules((Element) modulesNode);
        device.setModules(modules);

        return device;
    }

    /*private static Language.Item parseString(Element element) {
        String name = element.getAttribute("name").trim();
        String value = element.getTextContent();

        return new Language.Item(name, value);
    }

    private static List<Language.Item> parseValues(Language language, Element element) {
        List<Language.Item> items = new ArrayList<Language.Item>();

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

        return items;
    }*/

}

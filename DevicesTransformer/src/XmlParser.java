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
public class XmlParser {

    public static Language parseLanguage(File file) throws ParserConfigurationException, IOException, SAXException {

        //Get the DOM Builder Factory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        //Get the DOM Builder
        DocumentBuilder builder = factory.newDocumentBuilder();

        //Load and Parse the XML document
        //document contains the complete XML as a Tree.
        Document document = builder.parse(file);

        //Iterating through the nodes and extracting the data.
        Node languageNode = document.getDocumentElement();
        if (!(languageNode instanceof Element)) {
            return null;
        }

        String code = ((Element) languageNode).getAttribute("code");
        Language language = new Language(code);

        NodeList itemsNodes = languageNode.getChildNodes();
        for (int i = 0; i < itemsNodes.getLength(); i++) {
            Node node = itemsNodes.item(i);

            if (node instanceof Element) {
                String tag = ((Element) node).getTagName();
                if (tag.equals("string")) {
                    // Single string translation
                    Language.Item item = parseString((Element) node);
                    language.addItem(item);
                } else if (tag.equals("values")) {
                    // Values translation
                    List<Language.Item> items = parseValues(language, (Element) node);
                    language.addItems(items);
                } else {
                    throw new IllegalStateException(String.format("Unsupported tag '%s'", tag));
                }
            }
        }
        
        String output = String.format("/values-%s/generated_strings_devices.xml", language.getCode());
        System.out.println(String.format("Output: %s", output));

        System.out.println("--- ITEMS ---");
        for (Language.Item item : language.getItems()) {

            String name = item.key;
            String value = item.value;

            System.out.println(String.format("<string name=\"%s\">%s</string>", name, value));
        }

        return language;
    }

    private static Language.Item parseString(Element element) {
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
    }

}

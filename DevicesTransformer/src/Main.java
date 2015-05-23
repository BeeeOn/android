import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Main {

    public static void main(String[] args) {
        URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
        System.out.println(location.getFile());

        File file = new File("xml/language_cs.xml");

        try {
            Language language = XmlParser.parseLanguage(file);
            language.printLog();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

}

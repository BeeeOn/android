import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;

public class Main {

    public static void main(String[] args) {
        URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
        System.out.println(location.getFile());

        File[] files = new File("xml/languages/").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("language_") && name.endsWith(".xml");
            }
        });

        for (File file : files) {
            try {
                Language language = XmlParser.parseLanguage(file);
                language.printLog(System.out);
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
        }
    }

}

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;

public class Main {

    public static void main(String[] args) {
        URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
        System.out.println(location.getFile());

        processLanguages();
    }


    private static void processLanguages() {
        File[] files = new File("xml/languages/").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("language_") && name.endsWith(".xml");
            }
        });

        for (File file : files) {
            try {
                System.out.println(String.format("Loading translation from '%s'", file.getAbsolutePath()));

                Language language = LanguageParser.parse(file);

                File dir = new File(String.format("xml_exported/values-%s/", language.getCode()));
                dir.mkdirs();

                String name = "generated_strings_devices.xml";
                File output = new File(dir, name);

                System.out.println(String.format("Saving Android's strings XML to '%s'", output.getAbsolutePath()));
                PrintWriter writer = new PrintWriter(output, "UTF-8");
                language.printAndroidXml(writer);
                writer.close();
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

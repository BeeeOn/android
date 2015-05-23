import data.Devices;
import data.Language;
import org.xml.sax.SAXException;
import parser.DevicesParser;
import parser.LanguageParser;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;

public class Main {

    public static void main(String[] args) {
        processDevices();
        processLanguages();
    }


    private static void processDevices() {
        File file = new File("xml/devices.xml");

        try {
            System.out.println(String.format("Loading devices specification from '%s'", file.getAbsolutePath()));

            Devices devices = DevicesParser.parse(file);

            File dir = new File("export/objects/");
            dir.mkdirs();

            String name = "devices.java";
            File output = new File(dir, name);

            System.out.println(String.format("Saving devices objects to '%s'", output.getAbsolutePath()));
            PrintWriter writer = new PrintWriter(output, "UTF-8");

            devices.printDevicesJava(writer);

            writer.close();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
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

                File dir = new File(String.format("export/values-%s/", language.getCode()));
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

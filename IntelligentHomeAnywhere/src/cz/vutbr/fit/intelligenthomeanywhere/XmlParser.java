package cz.vutbr.fit.intelligenthomeanywhere;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.util.Xml;

/**
 * Class for parsing xml file, returning object with parsed data
 * @author ThinkDeep
 *
 */
public class XmlParser {

	private String _filename;
	private Capabilities _capabilities;
	
	/**
	 * Constructor of parser
	 * @param filename string with path to file
	 */
	XmlParser(String filename){
		_filename = filename;
	}
	
	/**
	 * Getter for parsed xml file
	 * @return object Capabilities or null
	 */
	public Capabilities GetResult(){
		return _capabilities;
	}
	
	/**
	 * Method for invoke parsing
	 */
	void Parse(){
		try{
			_capabilities = parse(Loader(_filename));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Open file and return inputstrem
	 * @param filename string with path to file
	 * @return inputstream of file
	 */
	private InputStream Loader(String filename){
		File file = new File(filename);
		InputStream in = null;
		try{
			in = new FileInputStream(file);
		} catch(IOException e){
			e.printStackTrace();
		}
		return in;
	}
	
	/**
	 * Main parse method returning object with data
	 * @param in inputstream with file to parse
	 * @return object Capabilities with parsed xml
	 */
	private Capabilities parse(InputStream in) throws XmlPullParserException, IOException{
		try{
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readAdapter(parser);
		} finally {
			in.close();
		}
	}
	
	/**
	 * Parse method starting with adapter tag and searching for capabilities tag
	 * @param parser control object
	 * @return Capabilites object
	 */
	public Capabilities readAdapter(XmlPullParser parser) throws XmlPullParserException, IOException{
		Capabilities cap = null;
		String ID = null;
		String Version = null;
		
		parser.require(XmlPullParser.START_TAG, null, "adapter");
		ID = parser.getAttributeValue(null, "id");
		
		while(parser.next() != XmlPullParser.END_TAG){
			if(parser.getEventType() != XmlPullParser.START_TAG)
				continue;
			String name = parser.getName();
			
			if(name.equals("capabilities"))
				cap = readCapabilities(parser);
			else if(name.equals("version")){
				Version = readText(parser);
				nextEndTag(parser);
			}
			else
				skip(parser);
		}
		cap.SetId(ID);
		cap.SetVersion(Version);
		return cap;
	}
	
	/**
	 * Parse method that skip actual tag
	 * @param parser control object
	 */
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
	    if (parser.getEventType() != XmlPullParser.START_TAG) {
	        throw new IllegalStateException();
	    }
	    int depth = 1;
	    while (depth != 0) {
	        switch (parser.next()) {
	        case XmlPullParser.END_TAG:
	            depth--;
	            break;
	        case XmlPullParser.START_TAG:
	            depth++;
	            break;
	        }
	    }
	 }
	
	/**
	 * Parse method starting with capabilities tag and searching for device tags
	 * @param parser control object
	 * @return Capabilites object
	 */
	public Capabilities readCapabilities(XmlPullParser parser) throws XmlPullParserException, IOException{
		Capabilities cap = new Capabilities();

		parser.require(XmlPullParser.START_TAG, null, "capabilities");
		while(parser.next() != XmlPullParser.END_TAG){
			if(parser.getEventType() != XmlPullParser.START_TAG)
				continue;
			String name = parser.getName();
			
			if(name.equals("device")){
				Device device = new Device();
				device.SetInit(isInitialized(parser.getAttributeValue(null, "initialized")));
				device.SetType(Integer.parseInt((parser.getAttributeValue(null, "type")).substring(2),16));
				if(!device.GetInit()){
					nextEndTag(parser);
					nextEndTag(parser);
				}else {
					device.SetLocation(readUltimate(parser,"device","location",XmlPullParser.START_TAG));
					device.SetName(readUltimate(parser, "location", "name",XmlPullParser.END_TAG));
				}
				device.SetRefresh(Integer.parseInt(readUltimate(parser, "name", "refresh", XmlPullParser.END_TAG)));
				nextStartTag(parser);
				device.SetBattery(Integer.parseInt(readUltimate(parser, "status", "battery", XmlPullParser.START_TAG)));
				nextStartTag(parser);
				device.SetAddress(readUltimate(parser, "network", "address", XmlPullParser.START_TAG));
				device.SetQuality(Integer.parseInt(readUltimate(parser, "address", "quality", XmlPullParser.END_TAG)));
				nextEndTag(parser);
				switch(device.GetType()){
					case 0: // temperature
						device.deviceDestiny = new Temperature();
						device.deviceDestiny.SetValue(readUltimate(parser, "network", "value", XmlPullParser.END_TAG));
						break;
					case 1: // humidity
						device.deviceDestiny = new Humidity();
						device.deviceDestiny.SetValue(readUltimate(parser, "network", "value", XmlPullParser.END_TAG));
						break;
					case 2: // pressure
						device.deviceDestiny = new Pressure();
						device.deviceDestiny.SetValue(readUltimate(parser, "network", "value", XmlPullParser.END_TAG));
						break;
					case 3: // sensor switch
						device.deviceDestiny = new Switch_s();
						device.deviceDestiny.SetValue(readUltimate(parser, "network", "value", XmlPullParser.END_TAG));
						break;
					case 4: // control switch
						device.deviceDestiny = new Switch_c();
						device.deviceDestiny.SetValue(readUltimate(parser, "network", "value", XmlPullParser.END_TAG));
						break;
					case 5: // illumination
						device.deviceDestiny = new Illumination();
						device.deviceDestiny.SetValue(readUltimate(parser, "network", "value", XmlPullParser.END_TAG));
						break;
					case 6: // noise
						device.deviceDestiny = new Noise();
						device.deviceDestiny.SetValue(readUltimate(parser, "network", "value", XmlPullParser.END_TAG));
						break;
					case 7: // emission
						device.deviceDestiny = new Emission();
						device.deviceDestiny.SetValue(readUltimate(parser, "network", "value", XmlPullParser.END_TAG));
						break;
					default:
						device.deviceDestiny = new UnknownDeviceType();
						device.deviceDestiny.SetValue(readUltimate(parser, "network", "value", XmlPullParser.END_TAG));
				}

				nextStartTag(parser);
				device.deviceDestiny.SetLog(isEnabled(parser.getAttributeValue(null, "enabled")));
				if(device.deviceDestiny.GetLog())
					device.SetLog(readText(parser));

				nextEndTag(parser);
				nextEndTag(parser);
				nextEndTag(parser);
				cap.devices.add(device);
			}else
				skip(parser);
		}
		return cap;
	}
	
	/**
	 * Parse method start with start/end tag arg1 depending on parametr tag, and returning text value of tag arg2
	 * @param parser control object
	 * @param arg1 starting tag
	 * @param arg2 searching tag
	 * @param tag type of starting tag (START_TAG/END_TAG)
	 * @return String with text value of searching tag
	 */
	public String readUltimate(XmlPullParser parser, String arg1, String arg2, int tag) throws XmlPullParserException, IOException{
		parser.require(tag,  null, arg1);
		String result = "";
		while(parser.next() != XmlPullParser.END_TAG){
			if(parser.getEventType() != XmlPullParser.START_TAG)
				continue;
			String name = parser.getName();
			
			if(name.equals(arg2)){
				result = readText(parser);
			}else
				skip(parser);
		}
		
		parser.require(XmlPullParser.END_TAG, null, arg2);
		return result;
	}
	
	/**
	 * Return text value of actual tag
	 * @param parser control object
	 * @return String with tag text
	 */
	private String readText(XmlPullParser parser) throws XmlPullParserException, IOException{
		String result = "";
		if(parser.next() == XmlPullParser.TEXT){
			result = parser.getText();
			//parser.nextTag();
		}
		return result;
	}
	
	/**
	 * Check if string value is 1 or 0, in case of 0 return false, otherwise true
	 * @param value number in string
	 * @return false if 0, else true
	 */
	private boolean isInitialized(String value){
		if(Integer.parseInt(value) != 0)
			return true;
		return false;
	}
	
	/**
	 * Check if string value is 1 or 0, in case of 0 return false, otherwise true
	 * @param value number in string
	 * @return false if 0, else true
	 */
	private boolean isEnabled(String value){
		if(Integer.parseInt(value) != 0)
			return true;
		return false;
	}

	/**
	 * Pushing parser to next start tag
	 * @param parser control object
	 */
	private void nextStartTag(XmlPullParser parser) throws XmlPullParserException, IOException{
		while(parser.next() != XmlPullParser.START_TAG);
	}
	
	/**
	 * Pushing parser to next end tag
	 * @param parser control object
	 */
	private void nextEndTag(XmlPullParser parser) throws XmlPullParserException, IOException{
		while(parser.next() != XmlPullParser.END_TAG);
	}
	
	/**
	 * Parse method starting with location tag, returning his text value
	 * @param parser control object
	 * @return Capabilites object
	 */
	@Deprecated
	public String readLocation(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG,  null, "device");
		String location = "";
		while(parser.next() != XmlPullParser.END_TAG){
			if(parser.getEventType() != XmlPullParser.START_TAG)
				continue;
			String name = parser.getName();
			
			if(name.equals("location")){
				location = readText(parser);
			}else
				skip(parser);
		}
		
		parser.require(XmlPullParser.END_TAG, null, "location");
		return location;
	}
}

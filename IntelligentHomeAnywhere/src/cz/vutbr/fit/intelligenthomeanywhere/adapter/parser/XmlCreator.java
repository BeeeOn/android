package cz.vutbr.fit.intelligenthomeanywhere.adapter.parser;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Capabilities;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.Device;

/**
 * Class for creating xml file from _capabilities object
 * @author ThinkDeep
 *
 */
public class XmlCreator {

	private Capabilities _capabilities;
	
	public XmlCreator(Capabilities cap){
		_capabilities = cap;
	}
	
	/**
	 * Method for creating xml file (string)
	 * @return String contains xml file
	 */
	public String create(){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			serializer.startTag(null, "adapter");
			serializer.attribute(null,"id",_capabilities.getId());
				serializer.startTag(null, "version");
				serializer.text(_capabilities.getVersion());
				serializer.endTag(null, "version");
			
				serializer.startTag(null, "capabilities");
					
				for(Device d : _capabilities.devices){
					serializer.startTag(null, "device");
					serializer.attribute(null, "initialized", (d.getInit() ? "1" : "0"));
					serializer.attribute(null, "type", d.getStringType());
					if(!d.getInit())
						serializer.attribute(null, "involved", d.getInvolveTime());
					
						serializer.startTag(null, "location");
						serializer.text((d.getLocation() != null) ? d.getLocation() : "");
						serializer.endTag(null, "location");
						
						serializer.startTag(null, "name");
						serializer.text((d.getName() != null) ? d.getName() : "");
						serializer.endTag(null, "name");
						
						serializer.startTag(null, "refresh");
						serializer.text(Integer.toString(d.getRefresh()));
						serializer.endTag(null, "refresh");
						
						serializer.startTag(null, "battery");
						serializer.text(Integer.toString(d.getBattery()));
						serializer.endTag(null, "battery");
						
						serializer.startTag(null, "network");
							serializer.startTag(null, "address");
							serializer.text(d.getAddress());
							serializer.endTag(null, "address");
							
							serializer.startTag(null, "quality");
							serializer.text(Integer.toString(d.getQuality()));
							serializer.endTag(null, "quality");
						serializer.endTag(null, "network");
						
						serializer.startTag(null, "value");
						serializer.text(d.deviceDestiny.getValue());
						serializer.endTag(null, "value");
							
						serializer.startTag(null, "logging");
						serializer.attribute(null, "enabled", (d.deviceDestiny.getLog() ? "1" : "0"));
						if(d.deviceDestiny.getLog())
							serializer.text(d.getLog());
						serializer.endTag(null, "logging");
					serializer.endTag(null, "device");
				}
				serializer.endTag(null, "capabilities");
			serializer.endTag(null, "adapter");
			
			serializer.endDocument();
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method saving xml file with filename on a phone to dir folder
	 * @param filename name of new writing xml file
	 * @param dir is path to the file
	 */
	public void saveXml(String dir, String filename){
		try{
			byte[] buffer = this.create().getBytes("UTF-8");
			File file = new File(dir,filename);
			DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
			out.flush();
			out.write(buffer,0,buffer.length);
			out.close();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}

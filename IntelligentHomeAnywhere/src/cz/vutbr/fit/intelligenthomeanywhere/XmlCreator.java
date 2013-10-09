package cz.vutbr.fit.intelligenthomeanywhere;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import org.xmlpull.v1.XmlSerializer;
import android.util.Xml;

/**
 * Class for creating xml file from _capabilities object
 * @author ThinkDeep
 *
 */
public class XmlCreator {

	private Capabilities _capabilities;
	
	XmlCreator(Capabilities cap){
		_capabilities = cap;
	}
	
	/**
	 * Method for creating xml file (string)
	 * @return String contains xml file
	 */
	public String Create(){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			serializer.startTag(null, "adapter");
			serializer.attribute(null,"id",_capabilities.GetId());
				serializer.startTag(null, "version");
				serializer.text(_capabilities.GetVersion());
				serializer.endTag(null, "version");
			
				serializer.startTag(null, "capabilities");
					
				for(Device d : _capabilities.devices){
					serializer.startTag(null, "device");
					serializer.attribute(null, "initialized", (d.GetInit() ? "1" : "0"));
					serializer.attribute(null, "type", d.GetStringType());
					if(!d.GetInit())
						serializer.attribute(null, "involved", d.GetInvolveTime());
					
						serializer.startTag(null, "location");
						serializer.text((d.GetLocation() != null) ? d.GetLocation() : "");
						serializer.endTag(null, "location");
						
						serializer.startTag(null, "name");
						serializer.text((d.GetName() != null) ? d.GetName() : "");
						serializer.endTag(null, "name");
						
						serializer.startTag(null, "refresh");
						serializer.text(Integer.toString(d.GetRefresh()));
						serializer.endTag(null, "refresh");
						
						serializer.startTag(null, "battery");
						serializer.text(Integer.toString(d.GetBattery()));
						serializer.endTag(null, "battery");
						
						serializer.startTag(null, "network");
							serializer.startTag(null, "address");
							serializer.text(d.GetAddress());
							serializer.endTag(null, "address");
							
							serializer.startTag(null, "quality");
							serializer.text(Integer.toString(d.GetQuality()));
							serializer.endTag(null, "quality");
						serializer.endTag(null, "network");
						
						serializer.startTag(null, "value");
						serializer.text(d.deviceDestiny.GetValue());
						serializer.endTag(null, "value");
							
						serializer.startTag(null, "logging");
						serializer.attribute(null, "enabled", (d.deviceDestiny.GetLog() ? "1" : "0"));
						if(d.deviceDestiny.GetLog())
							serializer.text(d.GetLog());
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
	public void SaveXml(String dir, String filename){
		try{
			byte[] buffer = this.Create().getBytes("UTF-8");
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

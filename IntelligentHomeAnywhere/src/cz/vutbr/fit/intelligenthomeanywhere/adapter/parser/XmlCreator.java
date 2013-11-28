/**
 * @brief Package for manipulation with XML and parsers
 */

package cz.vutbr.fit.intelligenthomeanywhere.adapter.parser;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;

/**
 * Class for creating XML file from Adapter object
 * @author ThinkDeep
 *
 */
public class XmlCreator {

	private Adapter mAdapter;
	
	public XmlCreator(Adapter cap){
		mAdapter = cap;
	}
	
	/**
	 * Method for creating XML file (string)
	 * @return String contains XML file
	 */
	public String create(){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			serializer.startTag(null, "adapter");
			serializer.attribute(null,"id", mAdapter.getId());
				serializer.startTag(null, "version");
				serializer.text(mAdapter.getVersion());
				serializer.endTag(null, "version");
			
				serializer.startTag(null, "capabilities");
					
				for(BaseDevice d : mAdapter.devices){
					serializer.startTag(null, "device");
					serializer.attribute(null, "initialized", (d.isInitialized() ? "1" : "0"));
					serializer.attribute(null, "type", "0x" + Integer.toHexString(d.getType()));
					if(!d.isInitialized())
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
						serializer.text(d.getStringValue());
						serializer.endTag(null, "value");
							
						serializer.startTag(null, "logging");
						serializer.attribute(null, "enabled", (d.isLogging() ? "1" : "0"));
						if(d.isLogging())
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
	 * Method saving XML file with filename on a phone to folder
	 * @param filename name of new writing XML file
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

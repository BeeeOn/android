package cz.vutbr.fit.iha;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;


/**
 * Demo data class for create demo XML files
 * @author ThinkDeep
 * @author Leopold Podmolík
 *
 */
public class DemoData {
	private Context mContext;
	
	public DemoData(Context context){
		this.mContext = context;
	}
	
	/**
	 * Check for demo data on SDcard of phone
	 * @return true if everything is OK
	 */
	public boolean checkDemoData(){
		HashMap<String, File> demos = new HashMap<String, File>();
		demos.put(Constants.DEMO_ASSETNAME, new File(mContext.getExternalFilesDir(null).getPath() + "/" + Constants.DEMO_FILENAME));
		demos.put(Constants.DEMO_LOG_ASSETNAME, new File(mContext.getExternalFilesDir(null).getPath() + "/" +  Constants.DEMO_LOG_FILENAME));
		demos.put(Constants.DEMO_LOCATION_ASSETNAME, new File(mContext.getExternalFilesDir(null).getPath() + "/" + Constants.DEMO_LOCATION_FILENAME));
		
		for(Map.Entry<String, File> entry : demos.entrySet()){
			if(!entry.getValue().exists()){
				try {
					AssetManager assetManager = this.mContext.getAssets();
					InputStream in_xml = assetManager.open(entry.getKey());
					OutputStream out_xml = new FileOutputStream(entry.getValue());
					byte[] buf_xml = new byte[1024];
					int len;
					while ((len = in_xml.read(buf_xml)) > 0) {
						out_xml.write(buf_xml,0,len);
					}
					in_xml.close();
					out_xml.close();
					Log.i("DemoData", "Communication file created");
				} catch(Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		return true;
	}

}

package cz.vutbr.fit.intelligenthomeanywhere;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;


/**
 * Demo data class for create demo XML files
 * @author ThinkDeep
 * @author Leopold Podmolík
 *
 */
public class DemoData {
	private Activity mActivity;
	
	public DemoData(Activity aActivity){
		this.mActivity = aActivity;
	}
	
	/**
	 * Check for demo data on SDcard of phone
	 * @return true if everything is OK
	 */
	public boolean checkDemoData(){
		String path = Environment.getExternalStorageDirectory().toString() + "/IHA/";
		File dir = new File(path);
		if(!dir.exists()){
			if(!dir.mkdirs()) {
				Log.e("checkDemoData","ERROR: Creation of directory " + path + " on sdcard failed");
				return false;
			} else {
				Log.i("checkDemoData","Created directory " + path + " on sdcard");
			}
		}
		
		if(!(new File(Constants.DEMO_COMMUNICATION)).exists() || !(new File(Constants.DEMO_LOGFILE)).exists()){
			try{
				AssetManager assetManager = this.mActivity.getAssets();
				InputStream in_xml = assetManager.open("komunikace.xml");
				OutputStream out_xml = new FileOutputStream(Constants.DEMO_COMMUNICATION);
				byte[] buf_xml = new byte[1024];
				int len;
				while((len = in_xml.read(buf_xml)) > 0){
					out_xml.write(buf_xml,0,len);
				}
				in_xml.close();
				out_xml.close();
				Log.i("DemoData","Communication file created");
				
				InputStream in_log = assetManager.open("sensor0.log");
				OutputStream out_log = new FileOutputStream(Constants.DEMO_LOGFILE);
				byte[] buf_log = new byte[1024];
				while((len = in_log.read(buf_log)) > 0){
					out_log.write(buf_log, 0, len);
				}
				in_log.close();
				out_log.close();
				Log.i("DemoData","Log file created");
				
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

}

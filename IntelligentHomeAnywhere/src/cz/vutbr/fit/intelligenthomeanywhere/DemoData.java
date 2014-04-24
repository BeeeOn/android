package cz.vutbr.fit.intelligenthomeanywhere;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

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
		File demoFile = new File(mContext.getExternalFilesDir(null).getPath() + "/" + Constants.DEMO_FILENAME);
		File demoLogFile = new File(mContext.getExternalFilesDir(null).getPath() + "/" +  Constants.DEMO_LOG_FILENAME);
		
		if (!demoFile.exists() || !demoLogFile.exists()) {
			try {
				AssetManager assetManager = this.mContext.getAssets();
				InputStream in_xml = assetManager.open(Constants.DEMO_ASSETNAME);
				OutputStream out_xml = new FileOutputStream(demoFile);
				byte[] buf_xml = new byte[1024];
				int len;
				while ((len = in_xml.read(buf_xml)) > 0) {
					out_xml.write(buf_xml,0,len);
				}
				in_xml.close();
				out_xml.close();
				Log.i("DemoData", "Communication file created");
				
				InputStream in_log = assetManager.open(Constants.DEMO_LOG_ASSETNAME);
				OutputStream out_log = new FileOutputStream(demoLogFile);
				byte[] buf_log = new byte[1024];
				while((len = in_log.read(buf_log)) > 0){
					out_log.write(buf_log, 0, len);
				}
				in_log.close();
				out_log.close();
				Log.i("DemoData", "Log file created");
				
			} catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

}

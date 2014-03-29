package cz.vutbr.fit.intelligenthomeanywhere.persistence;

import java.util.ArrayList;

import android.content.Context;
import cz.vutbr.fit.intelligenthomeanywhere.NotImplementedException;
import cz.vutbr.fit.intelligenthomeanywhere.User;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.widget.WidgetData;

/**
 * Persistence service that handles caching data on this device.
 * 
 * @author Robyer
 */
public class Persistence {

	private final Context mContext;
	
	/**
	 * Constructor.
	 * @param context
	 */
	public Persistence(Context context) {
		mContext = context;
	}
	
	public void saveLastUser(User user) {
		throw new NotImplementedException();
	}
	
	public User loadLastUser() {
		throw new NotImplementedException();
	}


	public ArrayList<Adapter> loadAdapters() {
		throw new NotImplementedException();
	}
	
	public boolean saveAdapters(ArrayList<Adapter> adapters) {
		throw new NotImplementedException();
	}
	
	
	public ArrayList<BaseDevice> loadDevices(String adapterId) {
		throw new NotImplementedException();
	}
	
	public boolean saveDevices(ArrayList<BaseDevice> devices) {
		throw new NotImplementedException();
	}
	
	public BaseDevice loadDevice(String deviceId) {
		throw new NotImplementedException();
	}
	
	public boolean saveDevice(BaseDevice device) {
		throw new NotImplementedException();
	}
	
	
	public WidgetData loadWidgetData(String widgetId) {
		throw new NotImplementedException();
	}
	
	public boolean saveWidgetData(WidgetData data) {
		throw new NotImplementedException();
	}
	
	
	public User loadLoggedUser() {
		throw new NotImplementedException();
	}
	
	public boolean saveLoggedUser(User user) {
		throw new NotImplementedException();
	}

	
	/*public Settings loadSettings() {
		throw new NotImplementedException();
	}
	
	public boolean saveSettings(Settings settings) {
		throw new NotImplementedException();
	}*/
	
	
}

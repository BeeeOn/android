package cz.vutbr.fit.iha.persistence;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.exception.NotImplementedException;
import cz.vutbr.fit.iha.household.ActualUser;
import cz.vutbr.fit.iha.household.User;
import cz.vutbr.fit.iha.widget.WidgetData;

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
	
	public SharedPreferences getSettings() {
		return mContext.getSharedPreferences(Constants.PERSISTENCE_PREF_FILENAME, 0);
	}
	
	public void saveLastEmail(String email) {
		Editor settings = getSettings().edit();
		
		if (email == null)
			settings.remove(Constants.PERSISTENCE_PREF_LAST_USER);
		else
			settings.putString(Constants.PERSISTENCE_PREF_LAST_USER, email);
		
		settings.commit();
	}
	
	public String loadLastEmail() {
		return getSettings().getString(Constants.PERSISTENCE_PREF_LAST_USER, "");
	}
	
	public void saveActiveAdapter(String adapterId) {
		Editor settings = getSettings().edit();
		settings.putString(Constants.PERSISTENCE_PREF_ACTIVE_ADAPTER, adapterId);
		settings.commit();
	}
	
	public String loadActiveAdapter() {
		return getSettings().getString(Constants.PERSISTENCE_PREF_ACTIVE_ADAPTER, "");
	}

	public List<Adapter> loadAdapters() {
		throw new NotImplementedException();
	}
	
	public boolean saveAdapters(List<Adapter> adapters) {
		throw new NotImplementedException();
	}
	
	
	public List<BaseDevice> loadDevices(String adapterId) {
		throw new NotImplementedException();
	}
	
	public boolean saveDevices(List<BaseDevice> devices) {
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
	
	public boolean saveLoggedUser(ActualUser user) {
		throw new NotImplementedException();
	}

	
	/*public Settings loadSettings() {
		throw new NotImplementedException();
	}
	
	public boolean saveSettings(Settings settings) {
		throw new NotImplementedException();
	}*/
	
	
}

package cz.vutbr.fit.intelligenthomeanywhere.listing;

import java.util.ArrayList;
import java.util.List;

import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;

public class SimpleListing {

	private List<BaseDevice> mDevices = new ArrayList<BaseDevice>();
	
	public SimpleListing() {}
	
	public SimpleListing(List<BaseDevice> devices) {
		mDevices = devices;
	}
	
	public void setDevices(final List<BaseDevice> devices) {
		mDevices = devices;
	}
	
	public List<BaseDevice> getDevices() {
		return mDevices;
	}
	
	public BaseDevice getById(String id) {
		for (BaseDevice device : mDevices) {
			if (device.getId().equals(id)) {
				return device;
			}
		}
		
		return null;
	}
	
}

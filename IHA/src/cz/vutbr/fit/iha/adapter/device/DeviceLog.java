package cz.vutbr.fit.iha.adapter.device;

import java.util.Date;
import java.util.List;

import cz.vutbr.fit.iha.exception.NotImplementedException;

/**
 * Represents history of values for device. 
 */
public class DeviceLog {

	public DeviceLog() {
		throw new NotImplementedException();
	}
	
	public List<String> getValues() {
		throw new NotImplementedException();
	}
	
	public List<String> getValues(Date start, Date end) {
		throw new NotImplementedException();
	}

}

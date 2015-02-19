package cz.vutbr.fit.iha.pair;

import java.util.EnumSet;

import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.Device;
import cz.vutbr.fit.iha.adapter.device.Device.SaveDevice;
import cz.vutbr.fit.iha.household.User;

/**
 * Represents pair of device and location for saving it to server
 */
public class AddUserPair {
	public final Adapter adapter;
	public final User user;

	public AddUserPair(final Adapter adapter, final User user) {
		this.adapter = adapter;
		this.user = user;
	}
}
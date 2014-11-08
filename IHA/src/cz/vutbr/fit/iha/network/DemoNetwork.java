package cz.vutbr.fit.iha.network;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import android.content.Context;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.Device;
import cz.vutbr.fit.iha.adapter.device.Device.SaveDevice;
import cz.vutbr.fit.iha.adapter.device.DeviceLog;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataInterval;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataType;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.adapter.device.values.BaseEnumValue;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.household.ActualUser;
import cz.vutbr.fit.iha.household.User;
import cz.vutbr.fit.iha.household.User.Gender;
import cz.vutbr.fit.iha.household.User.Role;
import cz.vutbr.fit.iha.network.xml.CustomViewPair;
import cz.vutbr.fit.iha.network.xml.XmlParsers;
import cz.vutbr.fit.iha.network.xml.action.ComplexAction;
import cz.vutbr.fit.iha.network.xml.condition.Condition;
import cz.vutbr.fit.iha.pair.LogDataPair;

/**
 * Network service that handles communication in demo mode.
 * 
 * @author Robyer
 */
public class DemoNetwork implements INetwork {

	public static final String DEMO_EMAIL = "demo";

	private Context mContext;
	private ActualUser mUser;

	private class AdapterHolder {
		public final Adapter adapter;
		public final Map<String, Location> locations = new HashMap<String, Location>();
		public final Map<String, Facility> facilities = new HashMap<String, Facility>();

		public AdapterHolder(Adapter adapter) {
			this.adapter = adapter;
		}
	}

	Map<String, AdapterHolder> mAdapters = new HashMap<String, AdapterHolder>();

	public DemoNetwork(Context context) {
		mContext = context;

		try {
			XmlParsers parser = new XmlParsers();

			String assetName = Constants.ASSET_ADAPTERS_FILENAME;
			for (Adapter adapter : parser.getDemoAdaptersFromAsset(mContext, assetName)) {
				mAdapters.put(adapter.getId(), new AdapterHolder(adapter));
			}

			for (AdapterHolder holder : mAdapters.values()) {
				assetName = String.format(Constants.ASSET_LOCATIONS_FILENAME, holder.adapter.getId());

				for (Location location : parser.getDemoLocationsFromAsset(mContext, assetName)) {
					holder.locations.put(location.getId(), location);
				}

				assetName = String.format(Constants.ASSET_ADAPTER_DATA_FILENAME, holder.adapter.getId());
				for (Facility facility : parser.getDemoFacilitiesFromAsset(mContext, assetName)) {
					holder.facilities.put(facility.getId(), facility);
				}

				// Set last update time to time between (-26 hours, now>
				for (Facility facility : holder.facilities.values()) {
					facility.setLastUpdate(DateTime.now(DateTimeZone.UTC).minusSeconds(new Random().nextInt(60 * 60 * 26)));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isAdapterAllowed(String adapterId) {
		return getRandomForAdapter(adapterId).nextBoolean();
	}

	public Random getRandomForAdapter(String adapterId) {
		try {
			int id = Integer.parseInt(adapterId);
			return new Random(id);
		} catch (NumberFormatException e) {
			return new Random();
		}
	}

	@Override
	public void setUser(ActualUser user) {
		mUser = user;
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public boolean signIn(String email, String gcmid) {
		mUser.setName("John Doe");
		mUser.setEmail(DEMO_EMAIL);
		mUser.setGender(Gender.Male);
		mUser.setPicture(null);
		mUser.setPictureUrl("");
		mUser.setSessionId("123456789");

		return true;
	}

	@Override
	public boolean signUp(String email) {
		return true;
	}

	@Override
	public boolean addAdapter(String adapterId, String adapterName) {
		if (!isAdapterAllowed(adapterId)) {
			return false;
		}

		Random rand = getRandomForAdapter(adapterId);

		Adapter adapter = new Adapter();
		adapter.setId(adapterId);
		adapter.setName(adapterName);

		// Use random role
		Role[] roles = Role.values();
		adapter.setRole(roles[rand.nextInt(roles.length)]);

		// Use random offset
		adapter.setUtcOffset(rand.nextInt(24 * 60) - 12 * 60);

		return true;
	}

	@Override
	public List<Adapter> getAdapters() {
		List<Adapter> adapters = new ArrayList<Adapter>();

		for (AdapterHolder holder : mAdapters.values()) {
			adapters.add(holder.adapter);
		}

		return adapters;
	}

	@Override
	public List<Facility> initAdapter(String adapterId) {
		List<Facility> facilities = new ArrayList<Facility>();

		AdapterHolder holder = mAdapters.get(adapterId);
		if (holder != null) {
			// Set last update time to time between (-26 hours, now>
			for (Facility facility : holder.facilities.values()) {
				facility.setLastUpdate(DateTime.now(DateTimeZone.UTC).minusSeconds(new Random().nextInt(60 * 60 * 26)));
				facilities.add(facility);
			}
		}

		return facilities;
	}

	@Override
	public boolean reInitAdapter(String oldId, String newId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateFacilities(String adapterId, List<Facility> facilities, EnumSet<SaveDevice> toSave) {
		for (Facility facility : facilities) {
			if (!updateFacility(adapterId, facility, toSave)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean updateDevice(String adapterId, Device device, EnumSet<SaveDevice> toSave) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean switchState(String adapterId, Device device) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean prepareAdapterToListenNewSensors(String adapterId) {
		return isAdapterAllowed(adapterId);
	}

	@Override
	public boolean deleteFacility(String adapterId, Facility facility) {
		AdapterHolder holder = mAdapters.get(adapterId);
		if (holder == null) {
			return false;
		}

		return holder.facilities.remove(facility.getId()) != null;
	}

	@Override
	public List<Facility> getFacilities(String adapterId, List<Facility> facilities) {
		List<Facility> result = new ArrayList<Facility>();

		for (Facility facility : facilities) {
			Facility newFacility = getFacility(adapterId, facility);
			if (newFacility != null) {
				result.add(newFacility);
			}
		}

		return result;
	}

	@Override
	public Facility getFacility(String adapterId, Facility facility) {
		AdapterHolder holder = mAdapters.get(adapterId);
		if (holder == null) {
			return null;
		}

		return holder.facilities.get(facility.getId());
	}

	@Override
	public boolean updateFacility(String adapterId, Facility facility, EnumSet<SaveDevice> toSave) {
		AdapterHolder holder = mAdapters.get(adapterId);
		if (holder == null) {
			return false;
		}

		if (!holder.facilities.containsKey(facility.getId())) {
			return false;
		}

		// NOTE: this replaces (or add) whole facility, not only fields marked as toSave
		holder.facilities.put(facility.getId(), facility);
		return true;
	}

	@Override
	public List<Facility> getNewFacilities(String adapterId) {
		// TODO Auto-generated method stub
		return new ArrayList<Facility>();
	}

	@Override
	public DeviceLog getLog(String adapterId, Device device, LogDataPair pair) {
		// Generate random values for log in demo mode
		DeviceLog log = new DeviceLog(DataType.AVERAGE, DataInterval.RAW);

		double lastValue = pair.device.getValue().getDoubleValue();
		double range = 1 + Math.log(device.getFacility().getRefresh().getInterval());

		long start = pair.interval.getStartMillis();
		long end = pair.interval.getEndMillis();

		Random random = new Random();

		if (Double.isNaN(lastValue)) {
			lastValue = random.nextDouble() * 1000;
		}

		int everyMsecs = Math.max(pair.gap.getValue(), device.getFacility().getRefresh().getInterval()) * 1000;

		boolean isBinary = (device.getValue() instanceof BaseEnumValue);

		while (start < end) {
			if (isBinary) {
				lastValue = random.nextBoolean() ? 1 : 0;
			} else {
				double addvalue = random.nextInt((int) range * 1000) / 1000;
				boolean plus = random.nextBoolean();
				lastValue = lastValue + addvalue * (plus ? 1 : -1);
			}

			log.addValue(log.new DataRow(start, (float) lastValue));
			start += everyMsecs;
		}

		return log;
	}

	@Override
	public List<Location> getLocations(String adapterId) {
		List<Location> locations = new ArrayList<Location>();

		AdapterHolder holder = mAdapters.get(adapterId);
		if (holder != null) {
			for (Location location : holder.locations.values()) {
				locations.add(location);
			}
		}

		return locations;
	}

	@Override
	public boolean updateLocations(String adapterId, List<Location> locations) {
		for (Location location : locations) {
			if (!updateLocation(adapterId, location)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean updateLocation(String adapterId, Location location) {
		AdapterHolder holder = mAdapters.get(adapterId);
		if (holder == null) {
			return false;
		}

		if (!holder.locations.containsKey(location.getId())) {
			return false;
		}

		// NOTE: this replaces (or add) whole facility, not only fields marked as toSave
		holder.locations.put(location.getId(), location);
		return true;
	}

	@Override
	public boolean deleteLocation(String adapterId, Location location) {
		AdapterHolder holder = mAdapters.get(adapterId);
		if (holder == null) {
			return false;
		}

		return holder.locations.remove(location.getId()) != null;
	}

	@Override
	public Location createLocation(String adapterId, Location location) {
		AdapterHolder holder = mAdapters.get(adapterId);
		if (holder == null) {
			return null;
		}

		// Create unique location id
		String locationId;
		int i = 0;
		do {
			locationId = String.valueOf(i++);
		} while (holder.locations.containsKey(locationId));

		// Set new location id
		location.setId(locationId);

		return location;
	}

	@Override
	public boolean addView(String viewName, int iconID, List<Device> devices) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<CustomViewPair> getViews() {
		// TODO Auto-generated method stub
		return new ArrayList<CustomViewPair>();
	}

	@Override
	public boolean deleteView(String viewName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateView(String viewName, int iconId, Facility facility, NetworkAction action) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAccounts(String adapterId, ArrayList<User> users) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAccount(String adapterId, User user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteAccounts(String adapterId, List<User> users) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteAccount(String adapterId, User user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public HashMap<String, User> getAccounts(String adapterId) {
		// TODO Auto-generated method stub
		return new HashMap<String, User>();
	}

	@Override
	public boolean updateAccounts(String adapterId, ArrayList<User> users) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateAccount(String adapterId, User user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setTimeZone(String adapterId, int differenceToGMT) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getTimeZone(String adapterId) {
		AdapterHolder holder = mAdapters.get(adapterId);
		if (holder == null) {
			return 0;
		}

		return holder.adapter.getUtcOffsetMillis() / (60 * 1000);
	}

	@Override
	public boolean NotificationsRead(ArrayList<String> msgID) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Condition setCondition(Condition condition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean connectConditionWithAction(String conditionID, String actionID) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Condition getCondition(Condition condition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Condition> getConditions() {
		// TODO Auto-generated method stub
		return new ArrayList<Condition>();
	}

	@Override
	public boolean updateCondition(Condition condition) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteCondition(Condition condition) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ComplexAction setAction(ComplexAction action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ComplexAction> getActions() {
		// TODO Auto-generated method stub
		return new ArrayList<ComplexAction>();
	}

	@Override
	public ComplexAction getAction(ComplexAction action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean updateAction(ComplexAction action) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteAction(ComplexAction action) {
		// TODO Auto-generated method stub
		return false;
	}

}

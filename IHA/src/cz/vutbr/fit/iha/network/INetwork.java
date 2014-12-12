package cz.vutbr.fit.iha.network;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.Device;
import cz.vutbr.fit.iha.adapter.device.Device.SaveDevice;
import cz.vutbr.fit.iha.adapter.device.DeviceLog;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.household.ActualUser;
import cz.vutbr.fit.iha.household.User;
import cz.vutbr.fit.iha.network.xml.CustomViewPair;
import cz.vutbr.fit.iha.network.xml.action.ComplexAction;
import cz.vutbr.fit.iha.network.xml.condition.Condition;
import cz.vutbr.fit.iha.pair.LogDataPair;

public interface INetwork {

	/**
	 * Action of View messages
	 * 
	 * @author ThinkDeep
	 * 
	 */
	public enum NetworkAction {
		REMOVE("0"), //
		ADD("1");

		private final String mAction;

		private NetworkAction(String action) {
			mAction = action;
		}

		public String getValue() {
			return mAction;
		}

		public static NetworkAction fromValue(String value) {
			for (NetworkAction item : values()) {
				if (value.equalsIgnoreCase(item.getValue()))
					return item;
			}
			throw new IllegalArgumentException("Invalid NetworkAction value");
		}
	}

	public void setUser(ActualUser user);
	
	public void setUID(String userId);

	/**
	 * Checks if Internet connection is available.
	 * 
	 * @return true if available, false otherwise
	 */
	public boolean isAvailable();

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////SIGNIN,SIGNUP,ADAPTERS//////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Return actual UID used for communication (= active session)
	 * 
	 * @return UID for actual communication
	 */
	public String getUID();
	
	/**
	 * Method does logging in/registration of user and load communication UID.
	 * You can get actual communication UID by calling getUID()
	 * 
	 * @return true on success, false or throw exception otherwise
	 */
	public boolean loadUID();

	/**
	 * Method register adapter to server
	 * 
	 * @param adapterID
	 *            adapter id
	 * @param adapterName
	 *            adapter name
	 * @return true if adapter has been registered, false otherwise
	 */
	public boolean addAdapter(String adapterID, String adapterName);

	/**
	 * Method ask for list of adapters. User has to be sign in before
	 * 
	 * @return list of adapters or empty list
	 */
	public List<Adapter> getAdapters();

	/**
	 * Method ask for whole adapter data
	 * 
	 * @param adapterID
	 *            of wanted adapter
	 * @return Adapter
	 */
	public List<Facility> initAdapter(String adapterID);

	/**
	 * Method change adapter id
	 * 
	 * @param oldId
	 *            id to be changed
	 * @param newId
	 *            new id
	 * @return true if change has been successfully
	 */
	public boolean reInitAdapter(String oldId, String newId);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////DEVICES,LOGS////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method send updated fields of devices
	 * 
	 * @param devices
	 * @return true if everything goes well, false otherwise
	 */
	public boolean updateFacilities(String adapterID, List<Facility> facilities, EnumSet<SaveDevice> toSave);

	/**
	 * Method send wanted fields of device to server
	 * 
	 * @param adapterID
	 *            id of adapter
	 * @param device
	 *            to save
	 * @param toSave
	 *            ENUMSET specified fields to save
	 * @return true if fields has been updated, false otherwise
	 */
	public boolean updateDevice(String adapterID, Device device, EnumSet<SaveDevice> toSave);

	/**
	 * Method toggle or set actor to new value
	 * 
	 * @param adapterID
	 * @param device
	 * @return
	 */
	public boolean switchState(String adapterID, Device device);

	/**
	 * Method make adapter to special state, when listen for new sensors (e.g. 15s) and wait if some sensors has been
	 * shaken to connect
	 * 
	 * @param adapterID
	 * @return
	 */
	public boolean prepareAdapterToListenNewSensors(String adapterID);

	/**
	 * Method delete facility from server
	 * 
	 * @param adapterID
	 * @param facility
	 *            to be deleted
	 * @return true if is deleted, false otherwise
	 */
	public boolean deleteFacility(String adapterID, Facility facility);

	/**
	 * Method ask for actual data of facilities
	 * 
	 * @param facilities
	 *            list of facilities to which needed actual data
	 * @return list of updated facilities fields
	 */
	public List<Facility> getFacilities(List<Facility> facilities);

	/**
	 * Method ask server for actual data of one facility
	 * 
	 * @param facility
	 * @return
	 */
	public Facility getFacility(Facility facility);

	public boolean updateFacility(String adapterID, Facility facility, EnumSet<SaveDevice> toSave);

	/**
	 * TODO: need to test
	 * 
	 * @param adapterID
	 * @param facilities
	 * @return
	 */
	public List<Facility> getNewFacilities(String adapterID);

	/**
	 * Method ask for data of logs
	 * 
	 * @param deviceId
	 *            id of wanted device
	 * @param pair
	 *            data of log (from, to, type, interval)
	 * @return list of rows with logged data
	 */
	public DeviceLog getLog(String adapterID, Device device, LogDataPair pair);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////ROOMS///////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method call to server for actual list of locations
	 * 
	 * @return List with locations
	 */
	public List<Location> getLocations(String adapterID);

	/**
	 * Method call to server to update location
	 * 
	 * @param locations
	 *            to update
	 * @return true if everything is OK, false otherwise
	 */
	public boolean updateLocations(String adapterID, List<Location> locations);

	/**
	 * Method call to server to update location
	 * 
	 * @param adapterID
	 * @param location
	 * @return
	 */
	public boolean updateLocation(String adapterID, Location location);

	/**
	 * Method call to server and delete location
	 * 
	 * @param location
	 *            to delete
	 * @return true room is deleted, false otherwise
	 */
	public boolean deleteLocation(String adapterID, Location location);

	public Location createLocation(String adapterID, Location location);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////VIEWS///////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method send newly created custom view
	 * 
	 * @param viewName
	 *            name of new custom view
	 * @param iconID
	 *            icon that is assigned to the new view
	 * @param deviceIds
	 *            list of devices that are assigned to new view
	 * @return true if everything goes well, false otherwise
	 */
	public boolean addView(String viewName, int iconID, List<Device> devices);

	/**
	 * Method ask for list of all custom views
	 * 
	 * @return list of defined custom views
	 */
	public List<CustomViewPair> getViews();

	/**
	 * Method delete whole custom view from server
	 * 
	 * @param viewName
	 *            name of view to erase
	 * @return true if view has been deleted, false otherwise
	 */
	public boolean deleteView(String viewName);

	public boolean updateView(String viewName, int iconId, Facility facility, NetworkAction action);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////ACCOUNTS////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	public boolean addAccounts(String adapterID, ArrayList<User> users);

	/**
	 * Method add new user to adapter
	 * 
	 * @param adapterID
	 * @param email
	 * @param role
	 * @return
	 */
	public boolean addAccount(String adapterID, User user);

	/**
	 * Method delete users from actual adapter
	 * 
	 * @param users
	 *            email of user
	 * @return true if all users has been deleted, false otherwise
	 */
	public boolean deleteAccounts(String adapterID, List<User> users);

	/**
	 * Method delete on user from adapter
	 * 
	 * @param adapterID
	 * @param user
	 * @return
	 */
	public boolean deleteAccount(String adapterID, User user);

	/**
	 * Method ask for list of users of current adapter
	 * 
	 * @return Map of users where key is email and value is User object
	 */
	public HashMap<String, User> getAccounts(String adapterID);

	/**
	 * Method update users roles on server on current adapter
	 * 
	 * @param userNrole
	 *            map with email as key and role as value
	 * @return true if all accounts has been changed false otherwise
	 */
	public boolean updateAccounts(String adapterID, ArrayList<User> users);

	/**
	 * Method update users role on adapter
	 * 
	 * @param adapterID
	 * @param user
	 * @param role
	 * @return
	 */
	public boolean updateAccount(String adapterID, User user);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////TIME////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method set wanted time zone to server
	 * 
	 * @NOTE using difference from GMT (UTC+0),
	 *       https://merlin.fit.vutbr.cz/wiki-iot/index.php/Smarthome_cloud#SetTimeZone
	 * @param differenceToGMT
	 * @return
	 */
	public boolean setTimeZone(String adapterID, int differenceToGMT);

	/**
	 * Method call to server to get actual time zone
	 * 
	 * @return integer in range <-12,12>
	 */
	public int getTimeZone(String adapterID);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////NOTIFICATIONS///////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method set read flag to notification on server
	 * 
	 * @param msgID
	 *            id of notification
	 * @return true if server took flag, false otherwise
	 */
	public boolean NotificationsRead(ArrayList<String> msgID);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////CONDITIONS,ACTIONS//////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	public Condition setCondition(Condition condition);

	public boolean connectConditionWithAction(String conditionID, String actionID);

	public Condition getCondition(Condition condition);

	public List<Condition> getConditions();

	public boolean updateCondition(Condition condition);

	public boolean deleteCondition(Condition condition);

	public ComplexAction setAction(ComplexAction action);

	public List<ComplexAction> getActions();

	public ComplexAction getAction(ComplexAction action);

	public boolean updateAction(ComplexAction action);

	public boolean deleteAction(ComplexAction action);

}

/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.coltroller;

import java.util.ArrayList;
import java.util.Date;

import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;

/**
 * MVC class
 * @author ThinkDeep
 *
 */
public class Controller {

	public Controller(){
		//TODO: constructor
	}
	
	/**
	 * Login to the server
	 * @param userId if this parameter is null, try to connect as last user
	 * @return true if is logged in, false otherwise
	 */
	public boolean login(String userId){
		return false;
	}
	
	/**
	 * Log out actual user
	 * @return true if user is logged out, false otherwise
	 */
	public boolean logout(){
		return false;
	}
	
	/**
	 * Method checks if is user logged in
	 * @return true if is logged in, false otherwise
	 */
	public boolean isLoggedIn(){
		return false;
	}
	
	/**
	 * Method return List of adapters which actual user have permissions
	 * @return List of adapters or null
	 */
	public ArrayList<Adapter> getAdapters(){
		return null;
	}
	
	/**
	 * TODO: description -> what does it do?
	 * @param id
	 * @param fromDate
	 * @param toDate
	 * @return TODO: replace Object with LogItem??
	 */
	public ArrayList<Object> getDeviceLog(String id, Date fromDate, Date toDate){
		return null;
	}
	
	/**
	 * Method returns List of rooms in given adapter
	 * @param adapterId id of given adapter
	 * @return List of rooms
	 */
	public ArrayList<String> getRooms(String adapterId){
		return null;
	}
	
	/**
	 * TODO: description -> what does id do?
	 * @param roomId ... room has id?
	 * @return TODO: replace Object with SensorsList
	 */
	public Object getRoom(String roomId){
		return null;
	}
	
	/**
	 * Method returns device by its id
	 * @param id of wanted device
	 * @param forceUpdate if is true then download latest changes of device from server, return cached data otherwise
	 * @return Device
	 */
	public BaseDevice getDevice(String id, boolean forceUpdate){
		return null;
	}
	
	/**
	 * Method save device to cache (server)
	 * @param device to be saved
	 * @return saved device?
	 */
	public BaseDevice saveDevie(BaseDevice device){
		return null;
	}
	
	/**
	 * Method add user to actual adapter
	 * @param user info about user
	 * @return true if user has been added, false otherwise
	 */
	public boolean addUser(ConAccount user){
		return false;
	}
	
	/**
	 * Method delete user account from actual adapter
	 * @param user info about user
	 * @return true if user has been deleted, false otherwise
	 */
	public boolean deleteUser(ConAccount user){
		return false;
	}
	
	/**
	 * Method update user account
	 * @param user info about
	 * @return true if user has been updated, false otherwise
	 */
	public boolean updateUser(ConAccount user){
		return false;
	}
	
	/**
	 * Method return user info by its id (users has id before logged in???)
	 * @param id
	 * @return ConAccount
	 */
	public ConAccount getUser(String id){
		return null;
	}
	
	/**
	 * Method add new custom view for actual user
	 * @param view
	 * @return true if view has been added, false otherwise
	 */
	public boolean addView(CustomView view){
		return false;
	}
	
	/**
	 * Method remove custom view from actual user
	 * @param view
	 * @return true if view has been removed, false otherwise
	 */
	public boolean delView(CustomView view){
		return false;
	}
	
	/**
	 * Method update view for actual user
	 * @param view
	 * @return true if view has been updated, false otherwise
	 */
	public boolean saveView(CustomView view){
		return false;
	}
	
	/**
	 * Method register new Adapter for actual user and make him SUPERUSER 
	 * @param id of adapter
	 * @return true, if adapter has been registered successfully, false otherwise
	 */
	public boolean registrAdapter(String id){
		return false;
	}
	
	/**
	 * Method unregister/remove Adapter from server
	 * @param id of adapter
	 * @return true if adapter has been removed, false otherwise
	 */
	public boolean unregisterAdapter(String id){
		return false;
	}
	
}

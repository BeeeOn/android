/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.controller;

import java.util.ArrayList;

import android.view.View;
import cz.vutbr.fit.intelligenthomeanywhere.NotImplementedException;
import cz.vutbr.fit.intelligenthomeanywhere.User;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.DeviceLog;
import cz.vutbr.fit.intelligenthomeanywhere.network.Network;
import cz.vutbr.fit.intelligenthomeanywhere.persistence.Persistence;

public final class Controller {
	
	private Persistence mPersistence = new Persistence();
	
	private Network mNetwork = new Network();
	
	private User mUser;

	public Controller() {
		
	}

	
	public boolean login() {
		User lastUser = mPersistence.loadLastUser();
		
		if (lastUser != null)
			return login(lastUser.email);
		
		return false; // TODO: throw proper exception
	}

	public boolean login(String userId) {
		if (!mNetwork.isAvailable())
			return false; // TODO: throw proper exception

		// TODO: catch and throw proper exception
		mUser = mNetwork.signIn(userId);
		if (mUser != null) {
			mPersistence.saveLastUser(mUser);
			return true;
		}
		
		return false;
	}
	
	public boolean logout() {
		mUser = null;
		mPersistence.saveLastUser(null);
		
		return true;
	}
	
	public boolean isLoggedIn() {
		return mUser != null;
	}
	
	public ArrayList<Adapter> getAdapters() {
		throw new NotImplementedException();
	}
	
	public DeviceLog getDeviceLog(String id) {
		throw new NotImplementedException();
	}

	public ArrayList<String> getRooms(String adapterId) {
		throw new NotImplementedException();
	}
	
	public ArrayList<BaseDevice> getRoom(String roomId) {
		throw new NotImplementedException();
	}

	
	public ArrayList<String> getLists() {
		throw new NotImplementedException();
	}
	
	public ArrayList<BaseDevice> getList(String listId) {
		throw new NotImplementedException();
	}

	
	public BaseDevice getDevice(String id) {
		return getDevice(id, false);
	}
	
	public BaseDevice getDevice(String id, boolean forceupdate) {
		throw new NotImplementedException();
	}
	
	public BaseDevice saveDevice(BaseDevice device) {
		throw new NotImplementedException();
	}

	
	public boolean addUser(User user) {
		throw new NotImplementedException();
	}
	
	public boolean deleteUser(User user) {
		throw new NotImplementedException();
	}
	
	public boolean saveUser(User user) {
		throw new NotImplementedException();
	}
	
	public User getUser(String id) {
		throw new NotImplementedException();
	}

	
	public boolean addView(View view) {
		throw new NotImplementedException();
	}
	
	public boolean delView(View view) {
		throw new NotImplementedException();
	}
	
	public boolean saveView(View view) {
		throw new NotImplementedException();
	}

	
	public boolean registerAdapter(String id) {
		throw new NotImplementedException();
	}
	
	public boolean unregisterAdapter(String id) {
		throw new NotImplementedException();
	}

}
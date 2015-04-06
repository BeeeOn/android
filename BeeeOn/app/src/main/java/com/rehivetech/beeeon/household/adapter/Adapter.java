/**
 * @brief Package for adapter manipulation
 */
package com.rehivetech.beeeon.household.adapter;

import com.rehivetech.beeeon.INameIdentifier;
import com.rehivetech.beeeon.household.user.User;

/**
 * @brief Class for parsed data from XML file of adapters
 * @author ThinkDeep
 * 
 */
public class Adapter implements INameIdentifier {
	public static final String TAG = Adapter.class.getSimpleName();

	private String mId = "";
	private String mName = "";
	private User.Role mRole;
	private int mUtcOffsetMillis;

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Debug method
	 */
	public String toDebugString() {
		return String.format("Id: %s\nName: %s\nRole: %s", mId, mName, mRole);
	}

	/**
	 * Set name of adapter
	 * 
	 * @param name
	 */
	public void setName(String name) {
		mName = name;
	}

	/**
	 * Get name of adapter
	 * 
	 * @return
	 */
	public String getName() {
		return mName.length() > 0 ? mName : getId();
	}

	/**
	 * Set role of actual user of adapter
	 * 
	 * @param role
	 */
	public void setRole(User.Role role) {
		mRole = role;
	}

	/**
	 * Get role of actual user of adapter
	 * 
	 * @return
	 */
	public User.Role getRole() {
		return mRole;
	}

	/**
	 * Setting id of adapter
	 * 
	 * @param id
	 */
	public void setId(String id) {
		mId = id;
	}

	/**
	 * Returning id of adapter
	 * 
	 * @return id
	 */
	public String getId() {
		return mId;
	}

	/**
	 * Setting UTC offset of adapter
	 * 
	 * @param utcOffsetInMinutes
	 */
	public void setUtcOffset(int offsetInMinutes) {
		mUtcOffsetMillis = offsetInMinutes * 60 * 1000;
	}

	/**
	 * Returning UTC offset of adapter
	 * 
	 * @return UTC offset in milliseconds
	 */
	public int getUtcOffsetMillis() {
		return mUtcOffsetMillis;
	}

}

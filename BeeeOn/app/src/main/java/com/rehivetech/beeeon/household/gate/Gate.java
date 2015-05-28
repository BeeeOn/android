/**
 * @brief Package for gate manipulation
 */
package com.rehivetech.beeeon.household.gate;

import com.rehivetech.beeeon.INameIdentifier;
import com.rehivetech.beeeon.household.user.User;

/**
 * @author ThinkDeep
 * @brief Class for parsed data from XML file of adapters
 */
public class Gate implements INameIdentifier {
	public static final String TAG = Gate.class.getSimpleName();

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
	 * Set name of gate
	 *
	 * @param name
	 */
	public void setName(String name) {
		mName = name;
	}

	/**
	 * Get name of gate
	 *
	 * @return
	 */
	public String getName() {
		return mName.length() > 0 ? mName : getId();
	}

	/**
	 * Set role of actual user of gate
	 *
	 * @param role
	 */
	public void setRole(User.Role role) {
		mRole = role;
	}

	/**
	 * Get role of actual user of gate
	 *
	 * @return
	 */
	public User.Role getRole() {
		return mRole;
	}

	/**
	 * Setting id of gate
	 *
	 * @param id
	 */
	public void setId(String id) {
		mId = id;
	}

	/**
	 * Returning id of gate
	 *
	 * @return id
	 */
	public String getId() {
		return mId;
	}

	/**
	 * Setting UTC offset of gate
	 *
	 * @param utcOffsetInMinutes
	 */
	public void setUtcOffset(int offsetInMinutes) {
		mUtcOffsetMillis = offsetInMinutes * 60 * 1000;
	}

	/**
	 * Returning UTC offset of gate
	 *
	 * @return UTC offset in milliseconds
	 */
	public int getUtcOffsetMillis() {
		return mUtcOffsetMillis;
	}

}

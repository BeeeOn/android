/**
 * @brief Package for adapter manipulation
 */
package cz.vutbr.fit.iha.adapter;

import cz.vutbr.fit.iha.household.User;

/**
 * @brief Class for parsed data from XML file of adapters
 * @author ThinkDeep
 * 
 */
public class Adapter {
	public static final String TAG = Adapter.class.getSimpleName();

	private String mId = "";
	private String mName = "";
	private User.Role mRole;
	private int mUtcOffset;

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
	 * Setting utc offset of adapter
	 * 
	 * @param utcOffset in minutes
	 */
	public void setUtcOffset(int offset) {
		mUtcOffset = offset;
	}

	/**
	 * Returning id of adapter
	 * 
	 * @return utcOffset in minutes
	 */
	public int getUtcOffset() {
		return mUtcOffset;
	}

}

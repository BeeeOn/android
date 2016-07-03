package com.rehivetech.beeeon.household.gate;

import android.support.annotation.NonNull;

import com.rehivetech.beeeon.INameIdentifier;
import com.rehivetech.beeeon.household.user.User;

public class Gate implements INameIdentifier {

	protected final String mId;
	protected String mName;
	protected User.Role mRole = User.Role.Guest;
	protected int mUtcOffsetInMinutes;

	public Gate(String id, String name) {
		mId = id;
		mName = name;
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * @param name of gate
	 */
	public void setName(String name) {
		mName = name;
	}

	/**
	 * @return name of gate, or id if name is empty (check it with hasName())
	 */
	public String getName() {
		return hasName() ? mName : getId();
	}

	public boolean hasName() {
		return mName != null && !mName.isEmpty();
	}

	/**
	 * @param role of actual user of gate
	 */
	public void setRole(@NonNull User.Role role) {
		mRole = role;
	}

	/**
	 * @return role of actual user of gate
	 */
	@NonNull
	public User.Role getRole() {
		return mRole;
	}

	/**
	 * @return id
	 */
	public String getId() {
		return mId;
	}

	/**
	 * @param offsetInMinutes
	 */
	public void setUtcOffset(int offsetInMinutes) {
		mUtcOffsetInMinutes = offsetInMinutes;
	}

	/**
	 * @return UTC offset of gate in minutes
	 */
	public int getUtcOffset() {
		return mUtcOffsetInMinutes;
	}
}

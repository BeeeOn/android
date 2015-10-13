package com.rehivetech.beeeon.household.user;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.INameIdentifier;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.Utils;

import java.util.HashMap;

/**
 * Represents single person.
 */
public class User implements INameIdentifier {

	private String mId = "";

	private String mName = "";

	private String mSurname = "";

	private String mEmail = "";

	private Role mRole = Role.Guest;

	private Gender mGender = Gender.UNKNOWN;

	private Bitmap mPicture = null;

	private String mPictureUrl = "";

	private HashMap<String, String> mJoinedProviders = new HashMap<>();

	public User() {
	}

	public User(final String id, final String name, final String surname, final String email, final Gender gender, final Role role) {
		mId = id;
		mName = name;
		mSurname = surname;
		mEmail = email;
		mGender = gender;
		mRole = role;
	}

	public User(final String id, final String email, final Role role) {
		mId = id;
		mEmail = email;
		mRole = role;
	}

	public enum Role implements IIdentifier {
		Guest("guest", R.string.user_role_guest), // can only read gate and devices' data
		User("user", R.string.user_role_user), // = guest + can switch state of switch devices
		Admin("admin", R.string.user_role_admin), // = user + can change devices' settings (rename, logging, refresh,...)
		Owner("owner", R.string.user_role_owner); // = admin + can change whole gate's settings (devices, users,...)

		private final String mRole;
		private final int mStringRes;

		Role(String role, int stringRes) {
			mRole = role;
			mStringRes = stringRes;
		}

		public String getId() {
			return mRole;
		}
		
		public int getStringResource() {
			return mStringRes;
		}
	}

	public enum Gender implements IIdentifier {
		UNKNOWN("unknown"),
		MALE("male"),
		FEMALE("female");

		String mValue;

		Gender(String value) {
			mValue = value;
		}

		public String getId() {
			return mValue;
		}
	}

	public String getId() {
		if (!mId.isEmpty())
			return mId;
		return getEmail();
	}

	public void setId(String id) {
		this.mId = id;
	}

	public HashMap<String, String> getJoinedProviders() {
		return mJoinedProviders;
	}

	public void setJoinedProviders(HashMap<String, String> joinedProviders) {
		mJoinedProviders = joinedProviders;
	}

	public String getFullName() {
		return String.format("%s %s", mName, mSurname).trim();
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getSurname() {
		return mSurname;
	}

	public void setSurname(String surname) {
		this.mSurname = surname;
	}

	public String getEmail() {
		return mEmail;
	}

	public void setEmail(String email) {
		mEmail = email;
	}

	public Role getRole() {
		return mRole;
	}

	public void setRole(Role role) {
		mRole = role;
	}

	public Gender getGender() {
		return mGender;
	}

	public void setGender(Gender gender) {
		mGender = gender;
	}

	public boolean isEmpty() {
		return mId.isEmpty() || mEmail.isEmpty() || mName.isEmpty() || (mPicture == null && !mPictureUrl.isEmpty());
	}

	/**
	 * @return picture url or empty string
	 */
	public String getPictureUrl() {
		return mPictureUrl;
	}

	public void setPictureUrl(String url) {
		mPictureUrl = url;
	}

	/**
	 * @return user picture bitmap or null
	 */
	@Nullable
	public Bitmap getPicture() {
		return mPicture;
	}

	public void setPicture(@Nullable Bitmap picture) {
		mPicture = Utils.getRoundedShape(picture);
	}

	public String toDebugString() {
		return String.format("Id: %s\nEmail: %s\nRole: %s\nName: %s\nGender: %s", mId, mEmail, mRole, mName, mGender);
	}

	/**
	 * Represents pair of user and gate for saving it to server
	 */
	public static class DataPair {
		public final User user;
		public final String gateId;

		public DataPair(final User user, final String gateId) {
			this.user = user;
			this.gateId = gateId;
		}
	}

}

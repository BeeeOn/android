package com.rehivetech.beeeon.household.user;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.INameIdentifier;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.Utils;

/**
 * Represents single person.
 */
public class User implements INameIdentifier {

	private String mId = "";

	private String mName = "";

    private String mSurname = "";

	private String mEmail = "";

	private Role mRole = Role.Guest;

	private Gender mGender = Gender.Unknown;

	private Bitmap mPicture = null;

	private String mPictureUrl = "";

	private boolean mDefaultPicture = true;

	public User() {}

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

	public enum Role {
		Guest("guest", R.string.user_role_guest), // can only read adapter and devices' data
		User("user", R.string.user_role_user), // = guest + can switch state of switch devices
		Admin("admin", R.string.user_role_admin), // = user + can change devices' settings (rename, logging, refresh,...)
		Superuser("superuser", R.string.user_role_superuser); // = admin + can change whole adapter's settings (devices, users,...)

		private final String mRole;
		private final int mStringRes;

		private Role(String role, int stringRes) {
			mRole = role;
			mStringRes = stringRes;
		}

		public String getValue() {
			return mRole;
		}

		public int getStringResource() { return mStringRes; }

		public static Role fromString(final String role) {
			if (role.equalsIgnoreCase("superuser")) {
				return Superuser;
			}
			if (role.equalsIgnoreCase("admin")) {
				return Admin;
			}
			if (role.equalsIgnoreCase("user")) {
				return User;
			}
			return Guest;
		}
	}

	public enum Gender {
		Unknown, Male, Female;
		
		public static Gender fromString(String value) {
			if (value.equalsIgnoreCase("male"))
				return Male;
			else if (value.equalsIgnoreCase("female"))
				return Female;
			else
				return Unknown;
		}
	}

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		this.mId = id;
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
	 * @param context
	 * @return bitmap with default silhouette
	 */
	public Bitmap getDefaultPicture(Context context) {
		return BitmapFactory.decodeResource(context.getResources(), R.drawable.person_silhouette);
	}

	/**
	 * @return user picture bitmap or null
	 */
	public Bitmap getPicture() {
		return mPicture;
	}

	public void setPicture(Bitmap picture) {
		mPicture = Utils.getRoundedShape(picture);
	}

	public String toDebugString() {
		return String.format("Id: %s\nEmail: %s\nRole: %s\nName: %s\nGender: %s", mId, mEmail, mRole, mName, mGender);
	}

}
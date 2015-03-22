package com.rehivetech.beeeon.household;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.User.Role;

/**
 * Represents single person.
 */
public class User {

	protected String mName = "";

	protected String mEmail = "";

	protected Role mRole = Role.Guest;

	protected Gender mGender = Gender.Unknown;

	public User() {
	}

	public User(final String name, final String email, final Role role, final Gender gender) {
		mName = name;
		mEmail = email;
		mRole = role;
		mGender = gender;
	}

	public User(final String email, final Role role) {
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

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
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

	public String toDebugString() {
		return String.format("Email: %s\nRole: %s\nName: %s\nGender: %s", mEmail, mRole, mName, mGender);
	}

}

package cz.vutbr.fit.intelligenthomeanywhere;

/**
 * Represents single person.
 */
public class User {

	private final String mName;
	
	private final String mEmail;
	
	private final Role mRole;
	
	public User(final String name, final String email, final Role role) {
		mName = name;
		mEmail = email;
		mRole = role;
	}

	public enum Role {
		Guest,		// can only read adapter and devices' data
		User,		// = guest + can switch state of switch devices
		Admin,		// = user + can change devices' settings (rename, logging, refresh,...) 
		Superuser;	// = admin + can change whole adapter's settings (devices, users,...)
		
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
	};
	
	public String getName() {
		return mName;
	}
	
	public String getEmail() {
		return mEmail;
	}
	
	public Role getRole() {
		return mRole;
	}

}

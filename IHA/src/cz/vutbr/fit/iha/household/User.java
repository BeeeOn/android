package cz.vutbr.fit.iha.household;

/**
 * Represents single person.
 */
public class User {

	private String mName;
	
	private String mEmail;
	
	private Role mRole;
	
	private Gender mGender;
	
	public User() { }
	
	public User(final String name, final String email, final Role role, final Gender gender) {
		mName = name;
		mEmail = email;
		mRole = role;
		mGender = gender;
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
	}
	
	public enum Gender {
		Unknown,
		Male,
		Female
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

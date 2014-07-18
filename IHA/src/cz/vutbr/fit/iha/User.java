package cz.vutbr.fit.iha;

/**
 * Represents single person.
 */
public class User {

	private String mName;
	
	private final String mEmail;
	
	private final Role mRole;
	
	private final Gender mGender;
	
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
	
	public void setName(String name){
		mName = name;
	}
	
	public String getEmail() {
		return mEmail;
	}
	
	public Role getRole() {
		return mRole;
	}
	
	public Gender getGender() {
		return mGender;
	}

	public String toDebugString(){
		String result = "";

		result += "Email: " + mEmail + "\n";
		result += "Role: " + mRole + "\n";
		result += "Name: " + mName + "\n";
		result += "Gender: " + mGender + "\n";
		
		return result;
	}

}

package cz.vutbr.fit.intelligenthomeanywhere;

/**
 * Represents single person.
 */
public class User {

	public String name;
	
	public String email;
	
	public Role role = Role.Guest;
	
	public User(String name, String email, Role role) {
		this.name = name;
		this.email = email;
		this.role = role;
	}

	public enum Role {
		Guest, User, Admin, Superadmin
	};

}

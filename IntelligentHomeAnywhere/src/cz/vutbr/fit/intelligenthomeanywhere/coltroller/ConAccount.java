/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.coltroller;

/**
 * Class for info about user
 * @author ThinkDeep
 *
 */
public class ConAccount {
	
	private String mEmail;
	private Role mRole;
	
	private static String GUEST = "guest";
	private static String USER = "user";
	private static String ADMIN = "admin";
	private static String SUPERUSER = "superuser";

	/**
	 * Constructor
	 */
	public ConAccount() {
		mEmail = null;
		mRole = Role.guest;
	}
	
	/**
	 * Constructor
	 * @param email of user
	 * @param role of user
	 */
	public ConAccount(String email, String role){
		mEmail = email;
		mRole = getRoleFromString(role);
	}
	
	/**
	 * Constructor
	 * @param email of user
	 * @param role of user
	 */
	public ConAccount(String email, Role role){
		mEmail = email;
		mRole = role;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public String getEmail(){
		return mEmail;
	}
	
	/**
	 * Setter
	 * @param email
	 */
	public void setEmail(String email){
		mEmail = email;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public String getStringRole(){
		return getRoleAsString(mRole);
	}
	
	/**
	 * Getter
	 * @return
	 */
	public Role getRole(){
		return mRole;
	}
	
	/**
	 * Setter
	 * @param role
	 */
	public void setRole(Role role){
		mRole = role;
	}
	
	/**
	 * Setter
	 * @param role
	 */
	public void setRole(String role){
		mRole = getRoleFromString(role);
	}
	
	//TODO: maybe move to Constants class
	/**
	 * Method convert role as string to enum
	 * @param role string value of role
	 * @return one of Role enum or null
	 */
	public static Role getRoleFromString(String role){
		if(role.equals(GUEST))
			return Role.guest;
		else if(role.equals(USER))
				return Role.user;
		else if(role.equals(ADMIN))
				return Role.admin;
		else if(role.equals(SUPERUSER))
				return Role.superuser;
		else
			return null;
	}
	
	//TODO: maybe move to Constants class
	/**
	 * Method convert role to string
	 * @param role of user
	 * @return role as String
	 */
	public static String getRoleAsString(Role role){
		switch(role){
			case guest:
				return GUEST;
			case admin:
				return ADMIN;
			case superuser:
				return SUPERUSER;
			case user:
				return USER;
			default:
				return null;
		}
	}
	
	//TODO: maybe move to Constants class
	public enum Role{
		guest, user, admin, superuser
	}

}

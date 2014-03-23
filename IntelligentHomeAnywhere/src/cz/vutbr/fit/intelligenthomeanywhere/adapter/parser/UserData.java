/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.adapter.parser;

/**
 * @author ThinkDeep
 *
 */
public class UserData {
	
	public static final String SUPERUSER = "superuser";
	public static final String ADMIN = "admin";
	public static final String USER = "user";
	public static final String GUEST = "guest";

	private String mEmail;
	private ROLE mRole;
	private String mName;
	private String mSurname;
	//false is woman/female
	private boolean mGender;
	
	/**
	 * Constructor
	 */
	public UserData() {}
	
	/**
	 * Constructor
	 * @param email
	 * @param role
	 * @param name
	 * @param surname
	 * @param gender where false is woman/female, and true is man/male
	 */
	public UserData(String email, String role, String name, String surname, boolean gender){
		mEmail = email;
		mRole = parseRole(role);
		mName = name;
		mSurname = surname;
		mGender = gender;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public String getEmail(){
		return mEmail;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public String getRole(){
		return parseRole(mRole);
	}
	
	/**
	 * Getter
	 * @return
	 */
	public ROLE getRoleRaw(){
		return mRole;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public String getName(){
		return mName;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public String getSurname(){
		return mSurname;
	}
	
	/**
	 * Getter of gender where false is woman/female and true is man/male
	 * @return
	 */
	public boolean getGender(){
		return mGender;
	}
	
	/**
	 * Setter
	 * @param email
	 */
	public void setEmail(String email){
		mEmail = email;
	}
	
	/**
	 * Setter
	 * @param email
	 */
	public void setRole(String role){
		mRole = parseRole(role);
	}
	
	/**
	 * Setter
	 * @param email
	 */
	public void setRole(ROLE role){
		mRole = role;
	}
	
	/**
	 * Setter
	 * @param email
	 */
	public void setName(String name){
		mName = name;
	}
	
	/**
	 * Setter
	 * @param email
	 */
	public void setSurname(String surname){
		mSurname = surname;
	}
	
	/**
	 * Setter
	 * @param email
	 */
	public void setGender(boolean gender){
		mGender = gender;
	}
	
	/**
	 * Setter
	 * @param email
	 */
	/**
	 * Method convert string to enum
	 * @param role string role
	 * @return ROLE enum
	 */
	public static ROLE parseRole(String role){
		if(role.equals(SUPERUSER))
			return ROLE.eSUPERUSER;
		else if(role.equals(ADMIN))
			return ROLE.eADMIN;
		else if(role.equals(USER))
			return ROLE.eUSER;
		else if(role.equals(GUEST))
			return ROLE.eGUEST;
		else
			return null;
	}
	
	/**
	 * Method convert enum to string
	 * @param role ROLE enum
	 * @return string role
	 */
	public static String parseRole(ROLE role){
		switch(role){
		case eADMIN:
			return ADMIN;
		case eGUEST:
			return GUEST;
		case eSUPERUSER:
			return SUPERUSER;
		case eUSER:
			return USER;
		default:
			return null;
		}
	}
	
	/**
	 * Roles of users
	 * @author ThinkDeep
	 *
	 */
	public static enum ROLE{
		eSUPERUSER,
		eADMIN,
		eUSER,
		eGUEST
	}
	
	/**
	 * Method emulate toString for debugging
	 * @return
	 */
	public String debugString(){
		String result = "";
		
		result += "Email: " + mEmail + "\n";
		result += "Role: " + parseRole(mRole) + "\n";
		result += "Name: " + (( mName == null ) ? "Unknown" : mName) + "\n";
		result += "Surname: " + (( mSurname == null ) ? "Unknown" : mSurname) + "\n";
		result += "Gender: " + (( mGender ) ? "Male" : "Female" ) + "\n";
		
		return result;
	}
}

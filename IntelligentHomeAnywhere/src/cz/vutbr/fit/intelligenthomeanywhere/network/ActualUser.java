/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.network;

import cz.vutbr.fit.intelligenthomeanywhere.User;

/**
 * @author ThinkDeep
 *
 */
public class ActualUser extends User {

	private static ActualUser mActualUser;
	private String mToken;
	private String mPicture;
	private String mSessionId;
	
	/**
	 * @param name
	 * @param email
	 * @param role
	 * @param gender
	 */
	private ActualUser(String name, String email, Role role, Gender gender) {
		super(name, email, role, gender);
		mSessionId = "0";
	}
	
	public static void setActualUser(String name, String email) {
		mActualUser = new ActualUser(name, email, Role.Guest, Gender.Male);
	}
	
	public static ActualUser getActualUser(){
		if (mActualUser == null) {
			setActualUser("", "");
		}

		return mActualUser;
	}

	/**
	 * @return the mToken
	 */
	public String getToken() {
		return mToken;
	}

	/**
	 * @param mToken the mToken to set
	 */
	public void setToken(String mToken) {
		this.mToken = mToken;
	}

	/**
	 * @return the mPicture
	 */
	public String getPicture() {
		return mPicture;
	}

	/**
	 * @param mPicture the mPicture to set
	 */
	public void setPicture(String mPicture) {
		this.mPicture = mPicture;
	}

	/**
	 * @return the mSessionId
	 */
	public String getSessionId() {
		return mSessionId;
	}

	/**
	 * @param mSessionId the mSessionId to set
	 */
	public void setSessionId(String mSessionId) {
		this.mSessionId = mSessionId;
	}
	
	

}

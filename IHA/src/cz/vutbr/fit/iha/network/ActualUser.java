/**
 * 
 */
package cz.vutbr.fit.iha.network;

import android.graphics.Bitmap;
import cz.vutbr.fit.iha.User;

/**
 * @author ThinkDeep
 *
 */
public class ActualUser extends User {

	private static ActualUser mActualUser;
	private String mToken;
	private String mPictureURL;
	private Bitmap mPictureIMG;
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
	public String getPictureURL() {
		return mPictureURL;
	}

	/**
	 * @param mPicture the mPicture to set
	 */
	public void setPicture(String Picture) {
		this.mPictureURL = Picture;
	}

	/**
	 * @return the mPictureIMG
	 */
	public Bitmap getPictureIMG() {
		return mPictureIMG;
	}

	/**
	 * @param mPictureIMG the mPictureIMG to set
	 */
	public void setPicture(Bitmap PictureIMG) {
		this.mPictureIMG = PictureIMG;
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

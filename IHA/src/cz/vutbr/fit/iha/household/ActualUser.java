/**
 * 
 */
package cz.vutbr.fit.iha.household;

import cz.vutbr.fit.iha.Utils;
import android.graphics.Bitmap;

/**
 * @author ThinkDeep
 *
 */
public class ActualUser extends User
{
	private Bitmap mPicture;
	private String mPictureUrl;	
	private String mSessionId = "";

	public ActualUser() {
		super();
	}
	
	/**
	 * @return the mPicture
	 */
	public String getPictureURL() {
		return mPictureUrl;
	}

	/**
	 * @param mPicture the mPicture to set
	 */
	public void setPictureUrl(String url) {
		mPictureUrl = url;
	}

	/**
	 * @return the mPictureIMG
	 */
	public Bitmap getPicture() {
		return mPicture;
	}

	/**
	 * @param mPictureIMG the mPictureIMG to set
	 */
	public void setPicture(Bitmap picture) {
		mPicture = Utils.getRoundedShape(picture);
	}

	/**
	 * @return the mSessionId
	 */
	public String getSessionId() {
		return mSessionId;
	}

	/**
	 * @param sessionId the mSessionId to set
	 */
	public void setSessionId(String sessionId) {
		mSessionId = sessionId;
	}
	
	public boolean isLoggedIn() {
		return mSessionId.length() > 0;
	}
	
	public void logout() {
		mSessionId = "";
	}

}

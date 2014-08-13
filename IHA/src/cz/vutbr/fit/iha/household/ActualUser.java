/**
 * 
 */
package cz.vutbr.fit.iha.household;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.util.Utils;

/**
 * @author ThinkDeep
 * 
 */
public class ActualUser extends User {
	private String mId;
	private Bitmap mPicture;
	private String mPictureUrl;
	private String mSessionId = "";	

	public ActualUser() {
		super();
	}

	public String getId() {
		if (mId == null) {
			mId = getEmail();
		}
		return mId;
	}
	
	public void setId(String id) {
		mId = id;
	}
	
	/**
	 * @return picture url or empty string
	 */
	public String getPictureURL() {
		return mPictureUrl;
	}

	/**
	 * @param String picture url set
	 */
	public void setPictureUrl(String url) {
		mPictureUrl = url;
	}

	/**
	 * Get user picture
	 * @param context
	 * @return user picture or default silhouette
	 */
	public Bitmap getPicture(Context context) {
		if (mPicture == null) {
			setPicture(getDefaultPicture(context));
		}
		return mPicture;
	}

	/**
	 * Get bitmap of default silhouette
	 * @param context
	 * @return bitmap with default silhouette
	 */
	private Bitmap getDefaultPicture(Context context) {
		return BitmapFactory.decodeResource(context.getResources(), R.drawable.person_silhouette);
	}
	
	/**
	 * Set user picture 
	 * @param picture
	 */
	public void setPicture(Bitmap picture) {
		mPicture = Utils.getRoundedShape(picture);
	}

	/**
	 * @return sessionId
	 */
	public String getSessionId() {
		return mSessionId;
	}

	/**
	 * @param sessionId
	 */
	public void setSessionId(String sessionId) {
		mSessionId = sessionId;
	}

	/**
	 * Checks if user is logged in (has sessionId)
	 * @return true if user is logged in, false otherwise
	 */
	public boolean isLoggedIn() {
		return mSessionId.length() > 0;
	}

	/**
	 * Logout user (erases his sessionId)
	 */
	public void logout() {
		mSessionId = "";
	}

}

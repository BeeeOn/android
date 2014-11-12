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
	private Bitmap mPicture;
	private String mPictureUrl;
	private String mUserId = "";
	private boolean mDefaultPicture = true;

	public ActualUser() {
		super();
	}

	/**
	 * @return picture url or empty string
	 */
	public String getPictureURL() {
		return mPictureUrl;
	}

	/**
	 * @param String
	 *            picture url set
	 */
	public void setPictureUrl(String url) {
		mPictureUrl = url;
	}

	public boolean isPictureDefault() {
		return mDefaultPicture;
	}

	/**
	 * Get user picture
	 * 
	 * @param context
	 * @return user picture or default silhouette
	 */
	public Bitmap getPicture(Context context) {
		if (mPicture == null) {
			mDefaultPicture = true;
			setPicture(getDefaultPicture(context));
		} else
			mDefaultPicture = false;
		return mPicture;
	}

	/**
	 * Get bitmap of default silhouette
	 * 
	 * @param context
	 * @return bitmap with default silhouette
	 */
	private Bitmap getDefaultPicture(Context context) {
		return BitmapFactory.decodeResource(context.getResources(), R.drawable.person_silhouette);
	}

	/**
	 * Set user picture
	 * 
	 * @param picture
	 */
	public void setPicture(Bitmap picture) {
		mPicture = Utils.getRoundedShape(picture);
	}

	/**
	 * @return sessionId
	 */
	public String getUserId() {
		return mUserId;
	}

	/**
	 * @param sessionId
	 */
	public void setUserId(String sessionId) {
		mUserId = sessionId;
	}

	/**
	 * Checks if user is logged in (has sessionId)
	 * 
	 * @return true if user is logged in, false otherwise
	 */
	public boolean isLoggedIn() {
		return mUserId.length() > 0;
	}

	/**
	 * Logout user (erases his sessionId)
	 */
	public void logout() {
		mUserId = "";
	}

}

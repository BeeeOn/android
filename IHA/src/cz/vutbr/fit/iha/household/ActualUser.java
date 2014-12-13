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
	private boolean mDefaultPicture = true;

	public ActualUser() {
		super();
	}
	
	public boolean isEmpty() {
		return mEmail.isEmpty() || mName.isEmpty() || (mPicture == null && !mPictureUrl.isEmpty());
	}

	/**
	 * @return picture url or empty string
	 */
	public String getPictureUrl() {
		return mPictureUrl;
	}

	/**
	 * @param url
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
	 * @return user picture or null
	 */
	public Bitmap getPicture() {
		return mPicture;
	}

	/**
	 * Get bitmap of default silhouette
	 * 
	 * @param context
	 * @return bitmap with default silhouette
	 */
	public Bitmap getDefaultPicture(Context context) {
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

}

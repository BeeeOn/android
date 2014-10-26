package cz.vutbr.fit.iha.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.LoginActivity;
import cz.vutbr.fit.iha.thread.ToastMessageThread;
import cz.vutbr.fit.iha.util.Log;

/**
 * Class communicate with Google server Get basic user info and picture if is possible
 * 
 * @author Leopold Podmolik
 */
public class GoogleAuth extends AsyncTask<Void, Void, GoogleAuthState> {
	private static final String TAG = GoogleAuth.class.getSimpleName();
	private static GoogleAuth mThis;

	private static String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email";

	private LoginActivity mActivity;
	private String mEmail;
	private String mToken;
	private String mUserName;
	private String mPictureUrl;
	private Bitmap mPicture;

	// ////////////////////////////////////////////////////////////////////////////////////
	// ///////////////// Constructors ////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor
	 * 
	 * @param mActivity
	 * @param mEmail
	 */
	public GoogleAuth(LoginActivity activity, String Email) {
		this.mActivity = activity;
		this.mEmail = Email;
		GoogleAuth.mThis = this;
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// ///////////////// Get-Set METHODS /////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////

	/**
	 * @return the mEmail
	 */
	public String getEmail() {
		return mEmail;
	}

	/**
	 * @param mEmail
	 *            the mEmail to set
	 */
	public void setEmail(String Email) {
		this.mEmail = Email;
	}

	/**
	 * @return the mUserName
	 */
	public String getUserName() {
		return mUserName;
	}

	/**
	 * @param mUserName
	 *            the mUserName to set
	 */
	public void setUserName(String UserName) {
		this.mUserName = UserName;
	}

	/**
	 * @return the mPicture
	 */
	public String getPicture() {
		return mPictureUrl;
	}

	/**
	 * @param mPicture
	 *            the mPicture to set
	 */
	public void setPicture(String Picture) {
		this.mPictureUrl = Picture;
	}

	/**
	 * @return the mPicture
	 */
	public Bitmap getPictureIMG() {
		return mPicture;
	}

	/**
	 * @param mPicture
	 *            the mPicture to set
	 */
	public void setPictureIMG(Bitmap PictureIMG) {
		this.mPicture = PictureIMG;
	}

	/**
	 * Getter of token
	 * 
	 * @return
	 */
	protected String getToken() {
		if (this.mToken != null)
			return this.mToken;
		return "";
	}

	public void setDebugToken(String token) {
		mToken = token;
	}

	public void invalidateToken() {
		GoogleAuthUtil.invalidateToken(mActivity, mToken);
		mToken = "";
	}

	/**
	 * Singleton-like method, but not initializing
	 * 
	 * @return static object
	 * @throws Exception
	 */
	public static GoogleAuth getGoogleAuth() throws Exception {
		if (mThis != null) {
			return mThis;
		} else
			throw new Exception("Not initialized");
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// ///////////////// Override METHODS ///////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected GoogleAuthState doInBackground(Void... params) {
		try {
			mToken = GoogleAuthUtil.getToken(mActivity, mEmail, SCOPE);
			Log.d(TAG, "Token");

			fetchInfoFromProfileServer(mToken);

			return GoogleAuthState.eOK;
		} catch (UserRecoverableAuthException userRecoverableException) {
			mActivity.startActivityForResult(userRecoverableException.getIntent(), LoginActivity.USER_RECOVERABLE_AUTH);
			return GoogleAuthState.eRecorver;
		} catch (IOException e) {
			return GoogleAuthState.eNoConnection;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return GoogleAuthState.eUnknown;
	}

	@Override
	protected void onPostExecute(final GoogleAuthState result) {
		super.onPostExecute(result);

		switch (result) {
		case eOK:
			mActivity.progressDismiss();
			break;
		case eRecorver:
			mActivity.progressDismiss();
			new ToastMessageThread(mActivity, R.string.toast_google_auth).start();
			break;
		case eUnknown:
			mActivity.progressDismiss();
			new ToastMessageThread(mActivity, R.string.toast_something_wrong).start();
			break;
		case eNoConnection:
			mActivity.progressDismiss();
			new ToastMessageThread(mActivity, R.string.toast_check_your_connection_via_browser).start();
			break;
		default:
			break;
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// ///////////////// NON-THREAD METHODS (need parent thread) /////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////

	public boolean doInForeground(boolean fetchPhoto) {
		try {
			mToken = GoogleAuthUtil.getToken(mActivity, mEmail, SCOPE);
			Log.d(TAG, "Token");

			if (fetchPhoto)
				fetchInfoFromProfileServer(mToken);

			return true;
		} catch (UserRecoverableAuthException userRecoverableException) {
			mActivity.startActivityForResult(userRecoverableException.getIntent(), LoginActivity.USER_RECOVERABLE_AUTH);
			mActivity.progressChangeText(mActivity.getString(R.string.progress_google));
			new ToastMessageThread(mActivity, R.string.toast_google_auth).start();
			return true;
		} catch (IOException e) {
			mActivity.progressDismiss();
			new ToastMessageThread(mActivity, R.string.toast_check_your_connection_via_browser).start();
			e.printStackTrace();
		} catch (Exception e) {
			mActivity.progressDismiss();
			new ToastMessageThread(mActivity, R.string.toast_internet_connection).start();
			e.printStackTrace();
		}
		return false;
	}

	/****************************************************************************************/
	/* Prevzato z SDK - EXTRAS - SAMPLE - AUTH */
	/****************************************************************************************/

	/**
	 * Method download name and picture URL
	 * 
	 * @param token
	 * @throws IOException
	 * @throws JSONException
	 */
	private void fetchInfoFromProfileServer(String token) throws IOException, JSONException {
		URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + token);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setConnectTimeout(10000);
		int sc = con.getResponseCode();
		if (sc == 200) {
			InputStream is = con.getInputStream();
			String respond = readResponse(is);

			mUserName = getName(respond);
			mPictureUrl = getPicture(respond);

			Log.d(TAG, String.format("Hello %s!", mUserName));
			Log.i(TAG, mPictureUrl);
			is.close();
			if (mPictureUrl != null && mPictureUrl.length() > 0) {
				fetchPictureFromProfileServer(mPictureUrl);
			}

		} else if (sc == 401) {
			GoogleAuthUtil.invalidateToken(mActivity, token);
		}
	}

	/**
	 * Reads the response from the input stream and returns it as a string.
	 */
	private String readResponse(InputStream is) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] data = new byte[2048];
		int len = 0;
		while ((len = is.read(data, 0, data.length)) >= 0) {
			bos.write(data, 0, len);
		}
		return new String(bos.toByteArray(), "UTF-8");
	}

	/**
	 * Parses the response and returns the name of the user.
	 * 
	 * @throws JSONException
	 *             if the response is not JSON or if first name does not exist in response
	 */
	private String getName(String jsonResponse) throws JSONException {
		JSONObject profile = new JSONObject(jsonResponse);
		return profile.getString("name");
	}

	/**
	 * Parses the response and returns the url of picture of the user.
	 * 
	 * @throws JSONException
	 *             if the response is not JSON or if first name does not exist in response
	 */
	private String getPicture(String jsonResponse) throws JSONException {
		JSONObject profile = new JSONObject(jsonResponse);
		boolean result = profile.isNull("picture");
		return (!result) ? profile.getString("picture") : null;
	}

	/**
	 * Method download picture
	 * 
	 * @param urlPicture
	 * @throws IOException
	 * @throws JSONException
	 */
	private void fetchPictureFromProfileServer(String urlPicture) throws IOException, JSONException {
		try {
			URL imageURL = new URL(urlPicture);
			HttpURLConnection connection = (HttpURLConnection) imageURL.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream inputStream = connection.getInputStream();

			setPictureIMG(BitmapFactory.decodeStream(inputStream));// Convert to bitmap
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}

}

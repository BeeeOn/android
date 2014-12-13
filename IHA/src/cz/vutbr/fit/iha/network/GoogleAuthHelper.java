package cz.vutbr.fit.iha.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import cz.vutbr.fit.iha.exception.IhaException;
import cz.vutbr.fit.iha.exception.NetworkError;
import cz.vutbr.fit.iha.household.User.Gender;
import cz.vutbr.fit.iha.util.Log;
import cz.vutbr.fit.iha.util.Utils;

public class GoogleAuthHelper {
	
	public static final String RECOVERABLE_INTENT = "recoverable_intent";
	
	private static final String TAG = GoogleAuthHelper.class.getSimpleName();
	
	private static String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email";
	
	public static class GoogleUserInfo {
		public final String email;
		public final String id;
		public final String token;
		public final String name;
		public final String pictureUrl;
		public final boolean emailVerified;
		public final Gender gender;
		public final String profileUrl;
		public final String locale;
		
		public GoogleUserInfo(String id, String email, String token, String name, String pictureUrl, boolean emailVerified, Gender gender, String profileUrl, String locale) {			
			this.id = id;
			this.email = email;
			this.token = token;
			this.name = name;
			this.pictureUrl = pictureUrl;
			this.emailVerified = emailVerified;
			this.gender = gender;
			this.profileUrl = profileUrl;
			this.locale = locale;
		}
	}
	
	public static String getToken(Context context, String email) throws IhaException {
		try {
			String token = GoogleAuthUtil.getToken(context, email, SCOPE);
			Log.d(TAG, String.format("Google token: %s", token));
			return token;
		} catch (UserRecoverableAuthException e) {
			// Rethrowing this exception with correct intent so LoginActivity could get it and handle it
			throw IhaException.wrap(e, NetworkError.GOOGLE_TRY_AGAIN).set(RECOVERABLE_INTENT, e.getIntent());
		} catch (Exception e) {
			throw IhaException.wrap(e, NetworkError.GOOGLE_TOKEN);	
		}
	}
	
	public static void invalidateToken(Context context, String token) {
		GoogleAuthUtil.invalidateToken(context, token);
	}
	
	/**
	 * Method download name and picture URL
	 * 
	 * This CAN'T be called on UI thread.
	 * 
	 * @param token
	 * @return GoogleUserInfo or null
	 */
	public static GoogleUserInfo fetchInfoFromProfileServer(String token) throws IhaException {
		String requestUrl = "https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + token;
		String response = "";
		
		try {
			URL url = new URL(requestUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			try {
				if (con.getResponseCode() == 401) {
					// Our token is invalid
					// GoogleAuthUtil.invalidateToken(activity, token); // let invalidation to caller of this method...
					throw new IhaException("Response 401 Not Authorized", NetworkError.GOOGLE_TOKEN);
				}

				InputStream in = con.getInputStream();
				response = Utils.getUtf8StringFromInputStream(in);
			} finally {
				con.disconnect();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		try {
			JSONObject profile = new JSONObject(response);
			if (profile.isNull("error")) {
				String id = profile.optString("id");
				String email = profile.optString("email");
				boolean emailVerified = profile.optBoolean("verified_email");
				String name = profile.optString("name"); // or "given_name" + "family_name"
				String profileUrl = profile.optString("link"); // Google+ page
				String pictureUrl = profile.optString("picture");
				Gender gender = Gender.fromString(profile.optString("gender")); // "male" / "female"
				String locale = profile.optString("locale"); // e.g. "cs"
				
				Log.i(TAG, String.format("Loaded info about user: %s <%s>", name, email));
				return new GoogleUserInfo(id, email, token, name, pictureUrl, emailVerified, gender, profileUrl, locale);
			} else {
				// Some error, lets load what happened
				JSONObject error = profile.getJSONObject("error");
				int errorCode = error.optInt("code");
				String errorMessage = error.optString("message");
				
				if (errorCode == 401) {
					// Our token is invalid
					// GoogleAuthUtil.invalidateToken(activity, token); // let invalidation to caller of this method...
					throw new IhaException(errorMessage, NetworkError.GOOGLE_TOKEN);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}

}

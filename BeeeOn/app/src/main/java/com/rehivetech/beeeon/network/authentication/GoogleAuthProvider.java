package com.rehivetech.beeeon.network.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.activity.LoginActivity;
import com.rehivetech.beeeon.activity.WebLoginActivity;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GoogleAuthProvider implements IAuthProvider {
	private static final String TAG = GoogleAuthProvider.class.getSimpleName();

	// This ID must be unique amongst all providers
	public static final int PROVIDER_ID = 201;

	public static final String PROVIDER_NAME = "google";
	public static final String PARAMETER_TOKEN = "token";

	private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email";

	private static Map<String, String> mParameters = new HashMap<>();

	private String mEmail = "";

	public GoogleAuthProvider() {}

	public GoogleAuthProvider(String token) {
		mParameters.put(PARAMETER_TOKEN, token);
	}

	public void setLoginEmail(String email) {
		mEmail = email;
	}

	@Override
	public String getProviderName() {
		return PROVIDER_NAME;
	}

	@Override
	public Map<String, String> getParameters() {
		return mParameters;
	}

	@Override
	public String getPrimaryParameter() {
		return "";
	}

	@Override
	public void setPrimaryParameter(String parameter) {
		// nothing to do here
	}

	@Override
	public void prepareAuth(LoginActivity activity) {
		if (!Utils.isGooglePlayServicesAvailable(activity))
			webloginAuth(activity);
		else
			androidAuth(activity);
	}

	/**
	 * Helper for invalidating Google authentication token.
	 *
	 * @param context
	 */
	public void invalidateToken(Context context) {
		GoogleAuthUtil.invalidateToken(context, mParameters.get(PARAMETER_TOKEN));
	}

	/**
	 * Method mine users account names
	 *
	 * @return array of names to choose
	 */
	private String[] getAccountNames(Context context) {
		AccountManager mAccountManager = AccountManager.get(context);
		Account[] accounts = mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
		String[] names = new String[accounts.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = accounts[i].name;
		}
		return names;
	}

	private void webloginAuth(LoginActivity activity) {
		Log.d(TAG, "Start webloginAuth");

		final String redirect = "http://localhost";
		final String tokenUrl = "https://accounts.google.com/o/oauth2/token";

		StringBuilder url = new StringBuilder();
		url.append("https://accounts.google.com/o/oauth2/auth?client_id=");
		url.append(Utils.uriEncode(Constants.WEB_LOGIN_CLIENT_ID));
		url.append("&scope=openid%20email%20profile");
		url.append("&redirect_uri=");
		url.append(Utils.uriEncode(redirect));
		url.append("&state=foobar");
		url.append("&response_type=code");

		final Intent intent = new Intent(activity, WebLoginActivity.class);
		intent.putExtra(WebLoginActivity.LOGIN_URL, url.toString());
		intent.putExtra(WebLoginActivity.TOKEN_URL, tokenUrl);
		intent.putExtra(WebLoginActivity.CLIENT_ID, Constants.WEB_LOGIN_CLIENT_ID);
		intent.putExtra(WebLoginActivity.CLIENT_SECRET, Constants.WEB_LOGIN_SECRET);
		intent.putExtra(WebLoginActivity.REDIRECT_URI, redirect);
		intent.putExtra(WebLoginActivity.GRANT_TYPE, "authorization_code");

		// Start activity and let user login via web
		activity.startActivityForResult(intent, PROVIDER_ID);
	}

	private void androidAuth(LoginActivity activity) {
		Log.d(TAG, "Start androidAuth");

		if (mEmail.isEmpty()) {
			String[] accounts = this.getAccountNames(activity);
			Log.d(TAG, String.format("Found number of accounts on this device: %d", accounts.length));

			if (accounts.length == 1) {
				// Set the only one email account used
				mEmail = accounts[0];
			} else {
				// Start activity and let user choose the email account to use
				Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, false, null, null, null, null);
				activity.startActivityForResult(intent, PROVIDER_ID);
				return; // we need to finish initialization in activity's onResult
			}
		}

		if (!mEmail.isEmpty()) {
			try {
				// Load token from Google server and save it
				String token = GoogleAuthUtil.getToken(activity, mEmail, SCOPE);
				mParameters.put(PARAMETER_TOKEN, token);

				Intent data = new Intent();
				data.putExtra(PARAMETER_TOKEN, token);

				// Report success to caller
				activity.onActivityResult(PROVIDER_ID, LoginActivity.RESULT_AUTH, data);
			} catch (UserRecoverableAuthException e) {
				Intent intent = e.getIntent();
				if (intent != null) {
					activity.startActivityForResult(intent, PROVIDER_ID);
				}
			} catch (GoogleAuthException | IOException e) {
				// Return error to caller
				activity.onActivityResult(PROVIDER_ID, LoginActivity.RESULT_ERROR, null);
			}
			return;
		}

		// Return error to LoginActivity
		activity.onActivityResult(PROVIDER_ID, LoginActivity.RESULT_ERROR, null);
	}
}

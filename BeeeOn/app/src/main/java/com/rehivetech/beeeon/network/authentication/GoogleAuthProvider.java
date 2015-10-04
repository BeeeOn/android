package com.rehivetech.beeeon.network.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.gui.activity.LoginActivity;
import com.rehivetech.beeeon.gui.activity.WebAuthActivity;
import com.rehivetech.beeeon.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GoogleAuthProvider implements IAuthProvider {
	private static final String TAG = GoogleAuthProvider.class.getSimpleName();

	// This ID must be unique amongst all providers
	public static final int PROVIDER_ID = 201;

	private static final String PROVIDER_NAME = "google";

	private static final String PARAMETER_TOKEN = "token";

	private static final String AUTH_INTENT_DATA_TOKEN = "token";

	private static final String AUTH_INTENT_DATA_EMAIL = "email";

	private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email";

	private static Map<String, String> mParameters = new HashMap<>();

	private String mEmail = "";

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
		return mEmail;
	}

	@Override
	public void setPrimaryParameter(String parameter) {
		mEmail = parameter;
	}

	@Override
	public boolean loadAuthIntent(Intent data) {
		String email = data.getStringExtra(AUTH_INTENT_DATA_EMAIL);
		if (email != null)
			mEmail = email;

		String token = data.getStringExtra(AUTH_INTENT_DATA_TOKEN);
		if (token == null)
			return false;

		mParameters.put(PARAMETER_TOKEN, token);
		return true;
	}

	@Override
	public void prepareAuth(final LoginActivity activity) {
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

	private void webloginAuth(final LoginActivity activity) {
		Log.d(TAG, "Start webloginAuth");

		final Intent intent = new Intent(activity, WebAuthActivity.class);
		intent.putExtra(WebAuthActivity.EXTRA_PROVIDER_ID, PROVIDER_ID);

		// Start activity and let user login via web
		activity.startActivityForResult(intent, PROVIDER_ID);
	}

	private void androidAuth(final LoginActivity activity) {
		Log.d(TAG, "Start androidAuth");

		String[] accounts = this.getAccountNames(activity);
		if (!mEmail.isEmpty()) {
			// Check if this e-mail still exists on this device
			boolean found = false;
			for (String account : accounts) {
				if (account.equalsIgnoreCase(mEmail)) {
					found = true;
					break;
				}
			}

			// If this email was not found, delete it and let user choose the one again
			if (!found)
				mEmail = "";
		}

		if (mEmail.isEmpty()) {

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
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						// Load token from Google server and save it
						String token = GoogleAuthUtil.getToken(activity, mEmail, SCOPE);

						Intent data = new Intent();
						data.putExtra(AUTH_INTENT_DATA_TOKEN, token);

						// Remember also primary email this provider was started with
						data.putExtra(AUTH_INTENT_DATA_EMAIL, mEmail);

						// Report success to caller
						activity.onActivityResult(PROVIDER_ID, IAuthProvider.RESULT_AUTH, data);
					} catch (UserRecoverableAuthException e) {
						Intent intent = e.getIntent();
						if (intent != null) {
							activity.startActivityForResult(intent, PROVIDER_ID);
						}
					} catch (GoogleAuthException | IOException e) {
						// Return error to caller
						activity.onActivityResult(PROVIDER_ID, IAuthProvider.RESULT_ERROR, null);
					}
				}
			}).start();

			return;
		}

		// Return error to LoginActivity
		activity.onActivityResult(PROVIDER_ID, IAuthProvider.RESULT_ERROR, null);
	}

	public static class GoogleWebViewClient extends WebViewClient implements IWebAuthProvider {
		private static final String REDIRECT_URL = "http://localhost";
		private static final String TOKEN_URL = "https://accounts.google.com/o/oauth2/token";

		private final WebAuthActivity mActivity;

		private boolean done = false;

		private FinishLoginTask mFinishLoginTask;

		public GoogleWebViewClient(final WebAuthActivity activity, final WebView webView) {
			mActivity = activity;

			webView.setWebViewClient(this);

			StringBuilder url = new StringBuilder();
			url.append("https://accounts.google.com/o/oauth2/auth?client_id=");
			url.append(Utils.uriEncode(Constants.WEB_LOGIN_CLIENT_ID));
			url.append("&scope=openid%20email%20profile");
			url.append("&redirect_uri=");
			url.append(Utils.uriEncode(REDIRECT_URL));
			url.append("&state=foobar");
			url.append("&response_type=code");

			webView.loadUrl(url.toString());
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			boolean isRedirectPage = url.startsWith(REDIRECT_URL);

			// Hide webView when it is redirect page or user is done with logging in
			view.setVisibility(isRedirectPage || done ? View.INVISIBLE : View.VISIBLE);

			if (isRedirectPage && !done) {
				// This is page we're looking for
				done = true;
				finishWebLoginAuth(url);
			}
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			// On any error (either expected or unexpected) we are closing this activity, so we hide webView immediately
			view.setVisibility(View.INVISIBLE);

			if (!done) {
				done = true;

				if ((errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT) && failingUrl.startsWith(REDIRECT_URL)) {
					Log.w(TAG, String.format("ignoring errorCode: %d and failingUrl: %s", errorCode, failingUrl));
					finishWebLoginAuth(failingUrl);
				} else {
					Log.e(TAG, String.format("received errorCode: %d and failingUrl: %s\ndescription: %s", errorCode, failingUrl, description));

					// Report error to caller
					mActivity.setResult(IAuthProvider.RESULT_ERROR);
					mActivity.finish();
				}
			}
		}

		@Override
		public void onActivityStop() {
			if (mFinishLoginTask != null) {
				mFinishLoginTask.cancel(true);
			}
		}

		private void finishWebLoginAuth(final String url) throws AppException {
			final Uri parsed = Uri.parse(url);

			final String error = parsed.getQueryParameter("error");
			if (error != null)
				Log.e(TAG, String.format("received error: %s", error));

			final String code = parsed.getQueryParameter("code");
			if (code == null) {
				mActivity.setResult(IAuthProvider.RESULT_ERROR);
				mActivity.finish();
				return;
			}

			mFinishLoginTask = new FinishLoginTask(code);
			mFinishLoginTask.execute();
		}

		private class FinishLoginTask extends AsyncTask<Void, Void, String> {
			private final String mCode;

			public FinishLoginTask(final String code) {
				mCode = code;
			}

			@Override
			protected String doInBackground(Void... nothing) {
				// Prepare parameters for Google request
				final Map<String, String> params = new HashMap<>(8);
				params.put("code", mCode);
				params.put("client_id", Constants.WEB_LOGIN_CLIENT_ID);
				params.put("client_secret", Constants.WEB_LOGIN_SECRET);
				params.put("redirect_uri", REDIRECT_URL);
				params.put("grant_type", "authorization_code");

				String token = "";
				try {
					JSONObject tokenJson = Utils.fetchJsonByPost(TOKEN_URL, params);
					Log.d(TAG, String.format("received: %s", tokenJson.toString()));
					token = tokenJson.getString("access_token");
				} catch (IOException | JSONException e) {
					e.printStackTrace();
				}
				return token;
			}

			@Override
			protected void onPostExecute(String token) {
				if (!token.isEmpty()) {
					final Intent data = new Intent();
					data.putExtra(GoogleAuthProvider.AUTH_INTENT_DATA_TOKEN, token);

					// Report success to caller
					mActivity.setResult(IAuthProvider.RESULT_AUTH, data);
				} else {
					// Report error to caller
					mActivity.setResult(IAuthProvider.RESULT_ERROR);
				}

				mActivity.finish();
			}

		}

	}

}

package com.rehivetech.beeeon.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.base.BaseActivity;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.network.authentication.GoogleAuthProvider;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WebLoginActivity extends BaseActivity {

	public static final String LOGIN_URL = "login.url";
	public static final String TOKEN_URL = "token.url";
	public static final String CLIENT_ID = "client.id";
	public static final String CLIENT_SECRET = "client.secret";
	public static final String REDIRECT_URI = "redirect.uri";
	public static final String GRANT_TYPE = "grant.type";

	private static final String TAG = WebLoginActivity.class.getSimpleName();

	public WebLoginActivity() {
	}

	private String getExtraNonNull(String key) {
		if (getIntent().hasExtra(key))
			return getIntent().getStringExtra(key);

		throw new NullPointerException(String.format("Missing key '%s' for web login", key));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final String redirect = getExtraNonNull(REDIRECT_URI);
		final String loginUrl = getExtraNonNull(LOGIN_URL);
		final WebView webview = new WebView(this);
		final Context context = getBaseContext();

		setResult(RESULT_CANCELED);

		setContentView(webview);
		webview.setVisibility(View.VISIBLE);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setNetworkAvailable(true);

		Log.d(TAG, String.format("loading URL %s", loginUrl));
		webview.loadUrl(loginUrl.toString());

		webview.setWebViewClient(new WebViewClient() {
			boolean done = false;

			@Override
			public void onPageFinished(WebView view, String url) {
				if (url.startsWith(redirect) && !done) {
					done = true;
					finishWebLoginAuth(context, view, url);
				}
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				if (!done) {
					done = true;

					if ((errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT) && failingUrl.startsWith(redirect)) {
						Log.w(TAG, String.format("ignoring errorCode: %d and failingUrl: %s", errorCode, failingUrl));
						finishWebLoginAuth(context, view, failingUrl);
					} else {
						Log.e(TAG, String.format("received errorCode: %d and failingUrl: %s\ndescription: %s", errorCode, failingUrl, description));

						// Report error to caller
						setResult(LoginActivity.RESULT_ERROR);
						finish();
					}
				}
			}
		});
	}

	private void finishWebLoginAuth(final Context context, final WebView view, final String url) throws AppException {
		final Uri parsed = Uri.parse(url);

		final String error = parsed.getQueryParameter("error");
		if (error != null)
			Log.e(TAG, String.format("received error: %s", error));

		final String code = parsed.getQueryParameter("code");
		if (code == null) {
			setResult(LoginActivity.RESULT_ERROR);
			finish();
			return;
		}

		final String clientId = getExtraNonNull(CLIENT_ID);
		final String clientSecret = getExtraNonNull(CLIENT_SECRET);
		final String tokenUrl = getExtraNonNull(TOKEN_URL);
		final String redirectUri = getExtraNonNull(REDIRECT_URI);
		final String grantType = getExtraNonNull(GRANT_TYPE);

		final Map<String, String> params = new HashMap<String, String>(8);
		params.put("code", code);
		params.put("client_id", clientId);
		params.put("client_secret", clientSecret);
		params.put("redirect_uri", redirectUri);
		params.put("grant_type", grantType);

		view.setVisibility(View.INVISIBLE);

		final FinishLoginTask tokenTask = new FinishLoginTask(context, tokenUrl, params);
		tokenTask.setListener(new CallbackTask.CallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					final Intent data = new Intent();
					data.putExtra(GoogleAuthProvider.PARAMETER_TOKEN, tokenTask.mToken);

					// Report success to caller
					setResult(LoginActivity.RESULT_AUTH, data);
				} else {
					// Report error to caller
					setResult(LoginActivity.RESULT_ERROR);
				}
				finish();
			}
		});

		tokenTask.execute(this);
	}

	class FinishLoginTask extends CallbackTask<Activity> {
		final String url;
		final Map<String, String> params;

		private String mToken = "";

		public FinishLoginTask(final Context context, final String url, Map<String, String> params) {
			super(context);
			this.url = url;
			this.params = params;
		}

		@Override
		protected Boolean doInBackground(Activity activity) {
			try {
				JSONObject tokenJson = Utils.fetchJsonByPost(url, params);
				Log.d(TAG, String.format("received: %s", tokenJson.toString()));
				mToken = tokenJson.getString("access_token");
				return true;
			}  catch (IOException | JSONException e) {
				e.printStackTrace();
			}
			return false;
		}
	}

}

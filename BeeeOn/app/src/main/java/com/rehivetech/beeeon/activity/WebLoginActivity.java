package com.rehivetech.beeeon.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.base.BaseActivity;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jviki on 3/19/15.
 */
public class WebLoginActivity extends BaseActivity {

	public static final String TOKEN_VALUE = "token.value";
	public static final String LOGIN_URL = "login.url";
	public static final String CLIENT_ID = "client.id";
	public static final String CLIENT_SECRET = "client.secret";
	public static final String REDIRECT_URI = "redirect.uri";
	public static final String GRANT_TYPE = "grant.type";

	private static final String TAG = LoginActivity.class.getSimpleName();

	public WebLoginActivity() {
	}

	class FinishLoginTask extends CallbackTask<Activity> {
		final String url;
		final Map<String, String> params;

		public FinishLoginTask(final Context context, final String url, Map<String, String> params) {
			super(context);
			this.url = url;
			this.params = params;
		}

		@Override
		protected Boolean doInBackground(Activity activity) {
			JSONObject tokenJson = null;
			final String token;

			try {
				tokenJson = Utils.fetchJsonByPost(url, params);
				Log.d(TAG, "received: " + tokenJson.toString());
				token = tokenJson.getString("access_token");
			}  catch (Exception e) {
				throw new AppException(e, NetworkError.GOOGLE_TOKEN);
			}

			final Intent data = new Intent();
			data.putExtra(TOKEN_VALUE, token);
			activity.setResult(RESULT_OK, data);
			activity.finish();

			return true;
		}
	}

	private String getExtraNonNull(String key) {
		if(getIntent().hasExtra(key))
			return getIntent().getStringExtra(key);

		throw new NullPointerException("Missing key '" + key + "' for web login");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final String loginUrl = getExtraNonNull(LOGIN_URL);
		final WebView webview = new WebView(this);
		final Context context = getBaseContext();

		setResult(RESULT_CANCELED);

		setContentView(webview);
		webview.setVisibility(View.VISIBLE);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setNetworkAvailable(true);

		Log.d(TAG, "loading URL " + loginUrl);
		webview.loadUrl(loginUrl.toString());

		webview.setWebViewClient(new WebViewClient() {
			boolean done = false;

			@Override
			public void onPageFinished(WebView view, String url) {
				if(url.startsWith(redirect) && !done) {
					done = true;
					finishWebLoginAuth(context, view, url);
				}
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				if(!done) {
					done = true;

					if(errorCode == -2 && failingUrl.startsWith(redirect)) {
						Log.e(TAG, "ignoring errorCode: " + errorCode + " and failingUrl: " + failingUrl);
						finishWebLoginAuth(context, view, failingUrl);
					}
					else {
						Log.e(TAG, "received errorCode: " + errorCode + " and failingUrl: " + failingUrl);
						Log.e(TAG, "description: " + description);
						finish();
					}
				}
			}
		});
	}

	private void finishWebLoginAuth(final Context context, final WebView view, final String url) throws AppException {
		final Uri parsed = Uri.parse(url);
		final String code = parsed.getQueryParameter("code");

		final String clientId = getExtraNonNull(CLIENT_ID);
		final String clientSecret = getExtraNonNull(CLIENT_SECRET);

		final Map<String, String> params = new HashMap<String, String>(8);
		params.put("code", code);
		params.put("client_id", clientId);
		params.put("client_secret", clientSecret);
		params.put("redirect_uri", getExtraNonNull(REDIRECT_URI));
		params.put("grant_type", getExtraNonNull(GRANT_TYPE));

		Log.d(TAG, "url: " + url);
		Log.d(TAG, "code: " + code);
		Log.d(TAG, "client_id: " + clientId);
		Log.d(TAG, "client_secret: " + clientSecret);

		view.setVisibility(View.INVISIBLE);

		final FinishLoginTask tokenTask = new FinishLoginTask(context, tokenUrl, params);
		tokenTask.setListener(new CallbackTask.CallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				finish();
			}
		});

		tokenTask.execute(this);
	}

}

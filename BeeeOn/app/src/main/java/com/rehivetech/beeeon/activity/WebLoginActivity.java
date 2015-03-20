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
	public static final String TOKEN_URL = "token.url";
	public static final String CLIENT_ID = "client.id";
	public static final String CLIENT_SECRET = "client.secret";
	public static final String REDIRECT_URI = "redirect.uri";
	public static final String GRANT_TYPE = "grant.type";

	private static final String TAG = WebLoginActivity.class.getSimpleName();

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
				Log.d(TAG, String.format("received: %s", tokenJson.toString()));
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
				if(url.startsWith(redirect) && !done) {
					done = true;
					finishWebLoginAuth(context, view, url);
				}
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				if(!done) {
					done = true;

					if((errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT) && failingUrl.startsWith(redirect)) {
						Log.w(TAG, String.format("ignoring errorCode: %d and failingUrl: %s", errorCode, failingUrl));
						finishWebLoginAuth(context, view, failingUrl);
					}
					else {
						Log.e(TAG, String.format("received errorCode: %d and failingUrl: %s\ndescription: %s", errorCode, failingUrl, description));
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
		final String tokenUrl = getExtraNonNull(TOKEN_URL);

		final Map<String, String> params = new HashMap<String, String>(8);
		params.put("code", code);
		params.put("client_id", clientId);
		params.put("client_secret", clientSecret);
		params.put("redirect_uri", getExtraNonNull(REDIRECT_URI));
		params.put("grant_type", getExtraNonNull(GRANT_TYPE));

		Log.d(TAG, String.format("url: %s", url));
		Log.d(TAG, String.format("code: %s", code));
		Log.d(TAG, String.format("client_id: %s", clientId));
		Log.d(TAG, String.format("client_secret: %s", clientSecret));

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

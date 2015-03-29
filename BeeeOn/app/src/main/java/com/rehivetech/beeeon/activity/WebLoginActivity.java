package com.rehivetech.beeeon.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.rehivetech.beeeon.base.BaseActivity;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.network.authentication.GoogleAuthProvider;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
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

	private FinishLoginTask mFinishLoginTask;

	private String getExtraNonNull(String key) {
		if (getIntent().hasExtra(key))
			return getIntent().getStringExtra(key);

		throw new IllegalStateException(String.format("Missing key '%s' for web login", key));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final String redirect = getExtraNonNull(REDIRECT_URI);
		final String loginUrl = getExtraNonNull(LOGIN_URL);

		WebView webView = initWebView(redirect, loginUrl);
		ProgressBar progressBar = initProgressBar();

		FrameLayout layout = new FrameLayout(this);
		layout.addView(progressBar);
		layout.addView(webView);
		setContentView(layout);

		setResult(RESULT_CANCELED);
	}

	private ProgressBar initProgressBar() {
		ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER);

		progressBar.setLayoutParams(params);
		progressBar.setIndeterminate(true);
		progressBar.setVisibility(View.VISIBLE);

		return progressBar;
	}

	private WebView initWebView(final String redirect, final String loginUrl) {
		WebView webView = new WebView(this);

		webView.setVisibility(View.INVISIBLE);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setNetworkAvailable(true);

		Log.d(TAG, String.format("loading URL %s", loginUrl));
		webView.loadUrl(loginUrl.toString());

		webView.setWebViewClient(new WebViewClient() {
			boolean done = false;

			@Override
			public void onPageFinished(WebView view, String url) {
				boolean isRedirectPage = url.startsWith(redirect);

				// Hide webView when it is redirect page or user is done with logging in
				view.setVisibility(isRedirectPage || done ? View.INVISIBLE : View.VISIBLE);

				if (isRedirectPage && !done) {
					// This is page we're looking for
					done = true;
					finishWebLoginAuth(WebLoginActivity.this, url);
				}
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				// On any error (either expected or unexpected) we are closing this activity, so we hide webView immediately
				view.setVisibility(View.INVISIBLE);

				if (!done) {
					done = true;

					if ((errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT) && failingUrl.startsWith(redirect)) {
						Log.w(TAG, String.format("ignoring errorCode: %d and failingUrl: %s", errorCode, failingUrl));
						finishWebLoginAuth(WebLoginActivity.this, failingUrl);
					} else {
						Log.e(TAG, String.format("received errorCode: %d and failingUrl: %s\ndescription: %s", errorCode, failingUrl, description));

						// Report error to caller
						setResult(IAuthProvider.RESULT_ERROR);
						finish();
					}
				}
			}
		});

		return webView;
	}

	private void finishWebLoginAuth(final Context context, final String url) throws AppException {
		final Uri parsed = Uri.parse(url);

		final String error = parsed.getQueryParameter("error");
		if (error != null)
			Log.e(TAG, String.format("received error: %s", error));

		final String code = parsed.getQueryParameter("code");
		if (code == null) {
			setResult(IAuthProvider.RESULT_ERROR);
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

		mFinishLoginTask = new FinishLoginTask(tokenUrl, params);
		mFinishLoginTask.execute();
	}

	@Override
	public void onStop() {
		super.onStop();

		if (mFinishLoginTask != null) {
			mFinishLoginTask.cancel(true);
		}
	}

	private class FinishLoginTask extends AsyncTask<Void, Void, String> {
		private final String mUrl;
		private final Map<String, String> mParams;

		public FinishLoginTask(final String url, Map<String, String> params) {
			mUrl = url;
			mParams = params;
		}

		@Override
		protected String doInBackground(Void... params) {
			String token = "";
			try {
				JSONObject tokenJson = Utils.fetchJsonByPost(mUrl, mParams);
				Log.d(TAG, String.format("received: %s", tokenJson.toString()));
				token = tokenJson.getString("access_token");
			}  catch (IOException | JSONException e) {
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
				setResult(IAuthProvider.RESULT_AUTH, data);
			} else {
				// Report error to caller
				setResult(IAuthProvider.RESULT_ERROR);
			}
			WebLoginActivity.this.finish();
		}
	}

}

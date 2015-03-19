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
public class BlackBerryLoginActivity extends BaseActivity {

	private static final String TAG = LoginActivity.class.getSimpleName();

	public BlackBerryLoginActivity() {
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
			data.putExtra("google.token", token);
			activity.setResult(RESULT_OK, data);
			activity.finish();

			return true;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final String googleId = "863203863728-i8u7m601c85uq70v7g5jtdcjesr8dnqm.apps.googleusercontent.com";
		final String redirect = "http://localhost";
		final WebView webview = new WebView(this);
		final Context context = getBaseContext();

		setResult(RESULT_CANCELED);

		setContentView(webview);
		webview.setVisibility(View.VISIBLE);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setNetworkAvailable(true);

		StringBuilder url = new StringBuilder();
		url.append("https://accounts.google.com/o/oauth2/auth?client_id=");
		url.append(Utils.uriEncode(googleId));
		url.append("&scope=openid%20email%20profile");
		url.append("&redirect_uri=");
		url.append(Utils.uriEncode(redirect));
		url.append("&state=foobar");
		url.append("&response_type=code");

		Log.d(TAG, "loading URL " + url.toString());
		webview.loadUrl(url.toString());

		webview.setWebViewClient(new WebViewClient() {
			boolean done = false;

			@Override
			public void onPageFinished(WebView view, String url) {
				if(url.startsWith(redirect) && !done) {
					done = true;
					finishBlackBerryGoogleAuth(context, view, url);
				}
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				if(!done) {
					done = true;

					if(errorCode == -2 && failingUrl.startsWith(redirect)) {
						Log.e(TAG, "ignoring errorCode: " + errorCode + " and failingUrl: " + failingUrl);
						finishBlackBerryGoogleAuth(context, view, failingUrl);
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

	private void finishBlackBerryGoogleAuth(final Context context, final WebView view, final String url) throws AppException {
		final String googleId = "863203863728-i8u7m601c85uq70v7g5jtdcjesr8dnqm.apps.googleusercontent.com";
		final String googleSecret = "ZEv4V6XBqCSRDbPtmHLZDLoR";
		final String redirect = "http://localhost";
		final String tokenUrl = "https://accounts.google.com/o/oauth2/token";

		final Uri parsed = Uri.parse(url);
		final String code = parsed.getQueryParameter("code");

		final Map<String, String> params = new HashMap<String, String>(8);
		params.put("code", code);
		params.put("client_id", googleId);
		params.put("client_secret", googleSecret);
		params.put("redirect_uri", redirect);
		params.put("grant_type", "authorization_code");

		Log.d(TAG, "url: " + url);
		Log.d(TAG, "code: " + code);
		Log.d(TAG, "client_id: " + googleId);
		Log.d(TAG, "client_secret: " + googleSecret);
		Log.d(TAG, "redirect_uri: " + redirect);

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

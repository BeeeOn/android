package com.rehivetech.beeeon.gui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.network.authentication.GoogleAuthProvider;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.util.Utils;

import java.util.Locale;

public class WebAuthActivity extends AppCompatActivity {

	public static final String EXTRA_PROVIDER_ID = "provider_id";
	public static final String EXTRA_AUTH_CODE = "auth_code";
	public static final String EXTRA_AUTHORIZATION_STARTED = "authorization_started";

	public static Intent newIntent(Context context) {
		return new Intent(context, WebAuthActivity.class);
	}

	public static Intent createResponseHandlingIntent(Context context, Uri data) {
		Intent intent = newIntent(context);
		intent.setData(data);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return intent;
	}

	private boolean mAuthorizationStarted = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState == null){
			extractState(getIntent().getExtras());
		} else {
			extractState(savedInstanceState);
		}
	}

	protected void onResume() {
		super.onResume();
		if(!mAuthorizationStarted){
			mAuthorizationStarted = true;
			startAuthorizationActivity();
			return;
		}

		if(getIntent().getData() != null){
			//Authorization OK
			extractResponseData(getIntent().getData());
		} else {
			//Authorization Cancelled
			Toast.makeText(this, "Authorization cancelled", Toast.LENGTH_SHORT).show();
		}
		finish();
	}

	private void extractResponseData(Uri data) {
		if(data != null){
			String code = data.getQueryParameter(Constants.OAuthParams.RESPONSE_KEY_AUTHORIZATION_CODE);
			getIntent().putExtra(EXTRA_AUTH_CODE, code);
			setResult(IAuthProvider.RESULT_AUTH, getIntent());
		}
	}
	private void startAuthorizationActivity() {
		Uri uri = buildAuthorizationUri();
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(uri);
		startActivity(intent);
	}

	private Uri buildAuthorizationUri() {
		Uri.Builder builder = Constants.GoogleOauth.AUTHORIZATION_ENDPOINT.buildUpon();
		builder.appendQueryParameter(Constants.OAuthParams.REQUEST_REDIRECT_URI, Constants.GoogleOauth.REDIRECT_URI.toString());
		builder.appendQueryParameter(Constants.OAuthParams.REQUEST_CLIENT_ID, Utils.uriEncode(getString(R.string.api_web)));
		builder.appendQueryParameter(Constants.OAuthParams.REQUEST_RESPONSE_TYPE, Constants.OAuthParams.REQUEST_RESPONSE_TYPE_VALUE_CODE);
		builder.appendQueryParameter(Constants.OAuthParams.REQUEST_SCOPE, "openid email profile");
		return builder.build();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(EXTRA_AUTHORIZATION_STARTED, mAuthorizationStarted);
	}

	private void extractState(Bundle extras) {
		if(extras != null)
			mAuthorizationStarted = extras.getBoolean(EXTRA_AUTHORIZATION_STARTED, false);
	}
}

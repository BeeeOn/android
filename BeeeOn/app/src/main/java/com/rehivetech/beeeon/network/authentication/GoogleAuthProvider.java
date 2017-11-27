package com.rehivetech.beeeon.network.authentication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.activity.LoginActivity;
import com.rehivetech.beeeon.util.Utils;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class GoogleAuthProvider implements IAuthProvider, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
	private static final String TAG = GoogleAuthProvider.class.getSimpleName();

	// This ID must be unique amongst all providers
	public static final int PROVIDER_ID = 201;

	private static final String PROVIDER_NAME = "google-android";
	private static final String PARAMETER_TOKEN = "authCode";
	private static final String AUTH_INTENT_DATA_TOKEN = "token";

	private static Map<String, String> mParameters = new HashMap<>();

	private GoogleApiClient mGoogleApiClient;
	private LoginActivity mActivity;

	@Override
	public boolean isDemo() {
		return false;
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
	public void setTokenParameter(String tokenParameter) {
		mParameters.put(PARAMETER_TOKEN, tokenParameter);
	}

	@Override
	public boolean loadAuthIntent(Context context, Intent data) {
		if (Utils.isGooglePlayServicesAvailable(context)) {
			String token = data.getExtras().getString(AUTH_INTENT_DATA_TOKEN);
			setTokenParameter(token);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void prepareAuth(final LoginActivity activity) {
		if (Utils.isGooglePlayServicesAvailable(activity)) {
			androidAuth(activity);
			mActivity = activity;
		} else {
			activity.hideProgressDialog();
			GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
			int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
			apiAvailability.getErrorDialog(activity, resultCode, PROVIDER_ID).show();
		}
	}

	public GoogleApiClient getGoogleApiClient() {
		return mGoogleApiClient;
	}

	/**
	 * Helper for invalidating Google authentication token.
	 */
	public void invalidateToken() {
		try {
			Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient);
		} catch (NullPointerException ignored) {
		}
	}

	private void androidAuth(final LoginActivity activity) {
		Log.d(TAG, "Start androidAuth");

		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestEmail()
				.requestServerAuthCode(activity.getString(R.string.api_web))
				.requestProfile()
				.build();

		// Build GoogleAPIClient with the Google Sign-In API and the above options.
		mGoogleApiClient = new GoogleApiClient.Builder(activity)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
				.build();

		mGoogleApiClient.connect();
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		Timber.e("Google Auth connection failed");
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		Timber.d("Google Auth connection success");
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
		mActivity.startActivityForResult(signInIntent, GoogleAuthProvider.PROVIDER_ID);
	}

	@Override
	public void onConnectionSuspended(int i) {
		Timber.d("Google Auth connection suspended");
	}
}

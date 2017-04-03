package com.rehivetech.beeeon.network.authentication;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.gui.activity.LoginActivity;
import com.rehivetech.beeeon.gui.activity.WebAuthActivity;
import com.rehivetech.beeeon.util.Utils;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static android.R.attr.description;

public class GoogleAuthProvider implements IAuthProvider, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = GoogleAuthProvider.class.getSimpleName();

    // This ID must be unique amongst all providers
    public static final int PROVIDER_ID = 201;

    private static final String PROVIDER_NAME = "google";
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
    public boolean loadAuthIntent(Intent data) {
        String token = data.getExtras().getString(AUTH_INTENT_DATA_TOKEN);
        setTokenParameter(token);
        return true;
    }

    @Override
    public void prepareAuth(final LoginActivity activity) {
        if (!Utils.isGooglePlayServicesAvailable(activity))
            webloginAuth(activity);
        else
            androidAuth(activity);
        mActivity = activity;
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
        } catch (NullPointerException ignored) {}
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

    public static class GoogleWebViewClient extends WebViewClient {
        private static final String REDIRECT_URL = "http://localhost";
        private final WebAuthActivity mActivity;
        private boolean done = false;

        public GoogleWebViewClient(final WebAuthActivity activity) {
            mActivity = activity;
        }

        public String getLoadUrl() {
            return "https://accounts.google.com/o/oauth2/v2/auth?client_id=" +
                    Utils.uriEncode(mActivity.getString(R.string.api_web)) +
                    "&scope=openid%20email%20profile" +
                    "&redirect_uri=" +
                    Utils.uriEncode(REDIRECT_URL) +
                    "&state=foobar" +
                    "&response_type=code" +
                    "&include_granted_scopes=true";
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
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);

            view.setVisibility(View.INVISIBLE);

            if (!done) {
                done = true;

                Log.e(TAG, String.format("Received SSL error: %s", error.toString()));
                Toast.makeText(mActivity, R.string.login_toast_ssl_error, Toast.LENGTH_LONG).show();

                mActivity.setResult(IAuthProvider.RESULT_ERROR);
                mActivity.finish();
            }
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            handleWebViewError(view, errorCode, failingUrl);
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            handleWebViewError(view, error.getErrorCode(), request.getUrl().toString());
        }

        private void finishWebLoginAuth(final String url) throws AppException {
            final Uri parsed = Uri.parse(url);

            final String error = parsed.getQueryParameter("error");

            if (error != null)
                Log.e(TAG, String.format("received error: %s", error));

            final String code = parsed.getQueryParameter("code");

            if (code != null) {
                final Intent data = new Intent();
                data.putExtra(GoogleAuthProvider.AUTH_INTENT_DATA_TOKEN, code);

                // Report success to caller
                mActivity.setResult(IAuthProvider.RESULT_AUTH, data);
            } else {
                mActivity.setResult(IAuthProvider.RESULT_ERROR);
                mActivity.finish();
                return;
            }

            mActivity.finish();
        }

        private void handleWebViewError(WebView view, int errorCode, String failingUrl) {
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
    }
}

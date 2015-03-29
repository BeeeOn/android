package com.rehivetech.beeeon.activity;

import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.base.BaseActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ErrorCode;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.exception.NotImplementedException;
import com.rehivetech.beeeon.network.authentication.DemoAuthProvider;
import com.rehivetech.beeeon.network.authentication.GoogleAuthProvider;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.util.BetterProgressDialog;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;

/**
 * First sign in class, controls first activity
 * 
 * @author ThinkDeep
 * @author Leopold Podmolik
 * @author Robyer
 */
public class LoginActivity extends BaseActivity {

	public static final String BUNDLE_REDIRECT = "isRedirect";

	public static final int RESULT_AUTH = 100;
	public static final int RESULT_CANCEL = 101;
	public static final int RESULT_ERROR = 102;
	
	private static final String TAG = LoginActivity.class.getSimpleName();
	
	private Controller mController;
	private BetterProgressDialog mProgress;

	private boolean mLoginCancel = false;

	// ////////////////////////////////////////////////////////////////////////////////////
	// ///////////////// Override METHODS
	// ////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		Log.i("BeeeOn app starting...", "___________________________________");

		// Get controller
		mController = Controller.getInstance(getApplicationContext());

		// Prepare progressDialog
		mProgress = new BetterProgressDialog(this);
		mProgress.setCancelable(true);
		mProgress.setCanceledOnTouchOutside(false);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgress.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				mLoginCancel = true;
			}
		});

		// Demo button
		((ImageButton) findViewById(R.id.login_btn_demo)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mProgress.setMessageResource(R.string.progress_loading_demo);
				prepareLogin(new DemoAuthProvider());
			}
		});

		// Get btn for login
		ImageButton btnGoogle = (ImageButton) findViewById(R.id.login_btn_google);
		ImageButton btnMojeID = (ImageButton) findViewById(R.id.login_btn_mojeid);

		// Set onClickListener
		btnGoogle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				prepareLogin(new GoogleAuthProvider());
			}
		});
		btnMojeID.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(v.getContext(), "Not Implemented yet", Toast.LENGTH_LONG).show();
				// prepareLogin(new MojeIdAuthProvider());
			}
		});

		// Check already logged in user
		if (mController.isLoggedIn()) {
			Log.d(TAG, "Already logged in, going to locations screen...");
			onLoggedIn(); // finishes this activity
			return;
		}

		// Do automatic login if we have remembered last logged in user
		IAuthProvider lastAuthProvider = mController.getLastAuthProvider();
		if (lastAuthProvider != null && !(lastAuthProvider instanceof DemoAuthProvider)) {
			// Automatic login with last used provider
			Log.d(TAG, String.format("Automatic login with last provider '%s' and user '%s'...", lastAuthProvider.getProviderName(), lastAuthProvider.getPrimaryParameter()));
			prepareLogin(lastAuthProvider);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log.d(TAG, String.format("onActivityResult: %d, %d", requestCode, resultCode));

		// FIXME: here LoginActivity need to get the result from Auth providers...  They need to specify somehow (at least comments) what values they will return here
		// probably they should have some method to load the intent that is sent here...

		// Probably instead of calling onLoginPrepared from authProvider we will call it from here. So authProvider will communicate with this activity only by this onActivityResult... yes!

		/*if (resultCode == RESULT_CANCELED) {
			mLoginCancel = true;
			mProgress.dismiss();
			return;
		}*/

		// FIXME: how to determine the provider this message is from? Probably load it from data? ... so some factory method for data Intent?
		// What if this intent is not from out provider but from some Google services?

		// IAuthProvider authProvider = null;

		// RequestCode identifies Provider - auth providers must respect that and use it in startActivityForResult(providerId)
		switch (requestCode) {
			case GoogleAuthProvider.PROVIDER_ID: {
				// Using GoogleAuthProvider
				// authProvider = new GoogleAuthProvider();

				switch (resultCode) {
					case RESULT_CANCELED:
					case RESULT_CANCEL: {
						Log.d(TAG, "Received RESULT_CANCEL from GoogleAuthProvider");
						mLoginCancel = true;
						mProgress.dismiss();
						break;
					}
					case RESULT_ERROR: {
						Log.d(TAG, "Received RESULT_ERROR from GoogleAuthProvider");
						mLoginCancel = true;
						mProgress.dismiss();
						// TODO: notify error?
						break;
					}
					case RESULT_OK: {
						// This result can go from Google Play Services intent, lets guess what the result is
						// (before it was used as RESULT_DO_RECOVERABLE_AUTH and RESULT_GET_GOOGLE_ACCOUNT
						String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
						if (email == null) {
							Log.d(TAG, "onActivityResult: no email");
							mProgress.dismiss();
							return;
						}

						GoogleAuthProvider provider = new GoogleAuthProvider();
						provider.setLoginEmail(email);

						prepareLogin(provider);
						break;
					}
					case RESULT_AUTH: {
						String token = data.getStringExtra(GoogleAuthProvider.PARAMETER_TOKEN);
						if (token == null) {
							Log.d(TAG, "no token received");
							mProgress.dismiss();
							return;
						}

						GoogleAuthProvider provider = new GoogleAuthProvider(token);
						onLoginPrepared(provider);
						break;
					}
				}

				break;
			}
			case DemoAuthProvider.PROVIDER_ID: {
				// Using DemoAuthProvider
				IAuthProvider provider = new DemoAuthProvider();

				// We don't expect any errors or cancel here, so just call the result
				// FIXME: really?
				onLoginPrepared(provider);

				break;
			}
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// ///////////////// Custom METHODS
	// ////////////////////////////////////////////////////////////////////////////////////

	protected void setDemoMode(boolean demoMode) {
		// After changing demo mode must be controller reloaded
		Controller.setDemoMode(getApplicationContext(), demoMode);
		mController = Controller.getInstance(getApplicationContext());
	}

	/**
	 * Last logging method that call controller to proceed access to server
	 *
	 * @param authProvider to login with
	 */
	private void prepareLogin(final IAuthProvider authProvider) {
		final boolean demoMode = (authProvider instanceof DemoAuthProvider);
		if (!demoMode && !Utils.isInternetAvailable(this)) {
			mProgress.dismiss();
			Toast.makeText(this, getString(R.string.toast_internet_connection), Toast.LENGTH_LONG).show();
			return;
		}

		mLoginCancel = false;
		mProgress.setMessageResource(R.string.progress_signing);
		mProgress.show();

		authProvider.prepareAuth(LoginActivity.this);
	}

	private void onLoginPrepared(final IAuthProvider authProvider) {
		final boolean demoMode = (authProvider instanceof DemoAuthProvider);

		mProgress.setMessageResource(R.string.progress_signing);
		mProgress.show();

		new Thread(new Runnable() {
			@Override
			public void run() {
				setDemoMode(demoMode);

				String errMessage = "Login failed";
				boolean errFlag = true;

				try {
					mController.beginPersistentConnection();
					Log.i(TAG, "Login started");

					// Here is authProvider already filled with needed parameters so we can send them to the server
					if (mController.login(authProvider)) {
						Log.d(TAG, "Login: true");
						errFlag = false;

						// Load all adapters and data for active one on login
						mProgress.setMessageResource(R.string.progress_loading_adapters);
						mController.reloadAdapters(true);

						Adapter active = mController.getActiveAdapter();
						if (active != null) {
							// Load data for active adapter
							mProgress.setMessageResource(R.string.progress_loading_adapter);
							mController.reloadLocations(active.getId(), true);
							mController.reloadFacilitiesByAdapter(active.getId(), true);
						}

						if (mLoginCancel) {
							// User cancelled login so do logout() to be sure it won't try to login automatically next time
							mController.logout();
						} else {
							// Open MainActivity or just this LoginActivity and let it redirect back
							onLoggedIn(); // finishes this activity
							return;
						}
					}

					Log.i(TAG, "Login finished");
				} catch (AppException e) {
					ErrorCode errorCode = e.getErrorCode();

					if (errorCode instanceof NetworkError) {
						// TODO: move this to GoogleAuthProvider... or to LoginActivity?

						switch ((NetworkError) errorCode) {
							case NOT_VALID_USER: {
								// Server denied our credentials (e.g. Google token, or email+password)
								// FIXME: this will be changed when new error messages are available

								if (authProvider instanceof GoogleAuthProvider) {
									// Probably wrong Google token so invalidate the token and then try it again
									if (Utils.isGooglePlayServicesAvailable(LoginActivity.this)) {
										((GoogleAuthProvider) authProvider).invalidateToken(LoginActivity.this);

										// FIXME: try it again somehow (if we haven't tried it yet)
									}
								}

								break;
							}
						}
					}

					e.printStackTrace();
					errMessage = e.getTranslatedErrorMessage(getApplicationContext());
				} catch (NotImplementedException e) {
					e.printStackTrace();
					errMessage = getString(R.string.toast_not_implemented);
				} catch (Exception e) {
					e.printStackTrace();
					errMessage = getString(R.string.toast_login_failed);
				} finally {
					mController.endPersistentConnection();
				}

				if (errFlag) {
					final String toastMessage = errMessage;

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							final Toast toast = Toast.makeText(LoginActivity.this, toastMessage, Toast.LENGTH_LONG);
							toast.show();
						}
					});
				}

				mProgress.dismiss();
			}
		}).start();
	}

	/**
	 * Finish this activity, dismiss progressDialog and if it's not redirect (set in Intent as BUNDLE_REDIRECT) then also start MainActivity
	 */
	private void onLoggedIn() {
		mProgress.dismiss();

		// Check if this is redirect (e.g., after connection loss) or classic start
		Bundle bundle = getIntent().getExtras();
		if (bundle == null || !bundle.getBoolean(BUNDLE_REDIRECT, false)) {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		}

		finish();
	}

}

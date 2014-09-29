package cz.vutbr.fit.iha.activity;

import android.content.Context;
import android.content.Intent;
import cz.vutbr.fit.iha.controller.Controller;

/**
 * Abstract parent for activities that requires logged in user
 * 
 * When user is not logged in, it will switch to LoginActivity automatically.
 */
public abstract class BaseApplicationActivity extends BaseActivity {

	private boolean triedLoginAlready = false;
	
	@Override
	public void onResume() {
		super.onResume();
	
		if (!Controller.getInstance(this).isLoggedIn()) {
			if (!triedLoginAlready) {
				triedLoginAlready = true;
				redirectToLogin(this);
			} else {
				finish();
			}
		} else {
			triedLoginAlready = false;
		}
	}
	
	public static void redirectToLogin(Context context) {
		Intent intent = new Intent(context, LoginActivity.class);
		intent.putExtra(LoginActivity.BUNDLE_REDIRECT, true);
		intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY);

		context.startActivity(intent);
	}

}

package cz.vutbr.fit.iha.activity;

import android.content.Intent;
import cz.vutbr.fit.iha.controller.Controller;

/**
 * Abstract parent for activities that requires logged in user
 * 
 * When user is not logged in, it will switch to LoginActivity automatically.
 */
public abstract class BaseApplicationActivity extends BaseActivity {

	@Override
	public void onResume() {
		super.onResume();
	
		if (!Controller.getInstance(this).isLoggedIn()) {
			redirectToLogin();
		}
	}
	
	protected void redirectToLogin() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.putExtra(LoginActivity.BUNDLE_REDIRECT, true);
		intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY);

		startActivity(intent);
	}

}

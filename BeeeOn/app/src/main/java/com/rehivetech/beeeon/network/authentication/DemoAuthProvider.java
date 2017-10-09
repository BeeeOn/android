package com.rehivetech.beeeon.network.authentication;

import android.content.Context;
import android.content.Intent;

import com.rehivetech.beeeon.gui.activity.LoginActivity;

import java.util.Map;

public class DemoAuthProvider implements IAuthProvider {

	// This ID must be unique amongst all providers
	public static final int PROVIDER_ID = 200;

	@Override
	public boolean isDemo() {
		return true;
	}

	@Override
	public String getProviderName() {
		return "";
	}

	@Override
	public Map<String, String> getParameters() {
		return null;
	}

	@Override
	public void setTokenParameter(String tokenParameter) {

	}

	@Override
	public boolean loadAuthIntent(Context context, Intent data) {
		return true;
	}

	@Override
	public void prepareAuth(LoginActivity activity) {
		activity.onActivityResult(PROVIDER_ID, IAuthProvider.RESULT_AUTH, null);
	}
}

package com.rehivetech.beeeon.network.authentication;

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
	public String getPrimaryParameter() {
		return "";
	}

	@Override
	public Map<String, String> getParameters() {
		return null;
	}

	@Override
	public void setPrimaryParameter(String parameter) {
		return;
	}

	@Override
	public boolean loadAuthIntent(Intent data) {
		return true;
	}

	@Override
	public void prepareAuth(LoginActivity activity) {
		activity.onActivityResult(PROVIDER_ID, IAuthProvider.RESULT_AUTH, null);
	}
}

package com.rehivetech.beeeon.network.authentication;

import android.app.Activity;

import com.rehivetech.beeeon.activity.LoginActivity;

import java.util.Map;

public class DemoAuthProvider implements IAuthProvider {
	private static final String TAG = DemoAuthProvider.class.getSimpleName();

	// This ID must be unique amongst all providers
	public static final int PROVIDER_ID = 200;

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
		// nothing to do here
	}

	@Override
	public void prepareAuth(LoginActivity activity) {
		activity.onActivityResult(PROVIDER_ID, Activity.RESULT_OK, null);
	}
}

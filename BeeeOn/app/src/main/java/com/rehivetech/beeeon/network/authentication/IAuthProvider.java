package com.rehivetech.beeeon.network.authentication;

import android.content.Context;
import android.content.Intent;

import com.rehivetech.beeeon.gui.activity.LoginActivity;

import java.util.Map;

public interface IAuthProvider {

	// Result codes

	/**
	 * Provider has all data he needs and they are given via Intent object
	 */
	int RESULT_AUTH = 100;

	/**
	 * User cancelled authorization process
	 */
	int RESULT_CANCEL = 101;

	/**
	 * Error happened during authorization process
	 */
	int RESULT_ERROR = 102;


	// Interface methods

	boolean isDemo();

	/**
	 * @return identifier for the provider as server knows it to correctly identify this login service.
	 */
	String getProviderName();

	/**
	 * @return map of parameters required for authentication (e.g. email, password, ...).
	 */
	Map<String, String> getParameters();

	void setTokenParameter(String tokenParameter);

	/**
	 * Loads the data from the RESULT_AUTH activity result.
	 *
	 * @param data
	 * @return true when this provider has authorization parameters correctly loaded; false when given data aren't correct/expected.
	 */
	boolean loadAuthIntent(Context context, Intent data);

	/**
	 * Do steps required for filling the parameters map (e.g. open login dialog to set login/password or load Google auth token).
	 * AuthProvider will provide the result via calling activity.onActivityResult(), read description in each AuthProvider to know what data receive/handle and how.
	 * AuthProvider must NOT throw any exceptions in this method. They won't be catch by caller.
	 * <p/>
	 * This will be called (probably) on UI thread so Provider must start the separate thread by itself.
	 */
	void prepareAuth(LoginActivity activity);
}

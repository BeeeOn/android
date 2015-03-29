package com.rehivetech.beeeon.network.authentication;

import android.content.Intent;

import com.rehivetech.beeeon.activity.LoginActivity;

import java.util.Map;

public interface IAuthProvider {

	// Result codes

	/** Provider has all data he needs and they are given via Intent object */
	public static final int RESULT_AUTH = 100;

	/** User cancelled authorization process */
	public static final int RESULT_CANCEL = 101;

	/** Error happened during authorization process */
	public static final int RESULT_ERROR = 102;


	// Interface methods

	/**
	 * @return identifier for the provider as server knows it to correctly identify this login service.
	 */
	public String getProviderName();

	/**
	 * @return map of parameters required for authentication (e.g. email, password, ...).
	 */
	public Map<String, String> getParameters();

	/**
	 * This is used for remembering last logged in user and then automatic logging in.
	 *
	 * @return value of primary login field (e.g. e-mail or login).
	 */
	public String getPrimaryParameter();

	/**
	 * Set primary parameter (probably from last login attempt) so it could e.g. fill the email/login field automatically.
	 *
	 * @param parameter
	 */
	public void setPrimaryParameter(String parameter);

	/**
	 * Loads the data from the RESULT_AUTH activity result.
	 *
	 * @param data
	 * @return true when this provider has authorization parameters correctly loaded; false when given data aren't correct/expected.
	 */
	public boolean loadAuthIntent(Intent data);

	/**
	 * Do steps required for filling the parameters map (e.g. open login dialog to set login/password or load Google auth token).
	 * AuthProvider will provide the result via calling activity.onActivityResult(), read description in each AuthProvider to know what data receive/handle and how.
	 * AuthProvider must NOT throw any exceptions in this method. They won't be catch by caller.
	 *
	 * This will be called (probably) on UI thread so Provider must start the separate thread by itself.
	 */
	public void prepareAuth(LoginActivity activity);

}

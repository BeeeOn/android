package com.rehivetech.beeeon.socialNetworks;

import android.app.Activity;

/**
 * @author Jan Lamacz
 */
public interface BeeeOnSocialNetwork {
	public void logIn(Activity activity);
	public void logOut();
	public boolean isPaired();
	public String getUserName();
	public String getName();
}

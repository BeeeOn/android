package com.rehivetech.beeeon.socialNetworks;

import android.support.v4.app.FragmentActivity;

/**
 * @author Jan Lamacz
 */
public interface BeeeOnSocialNetwork {
	public void logIn(FragmentActivity activity);
	public void logOut();
	public boolean isPaired();
	public String getUserName();
}

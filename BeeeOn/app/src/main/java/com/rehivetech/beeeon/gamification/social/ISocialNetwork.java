package com.rehivetech.beeeon.gamification.social;

import android.app.Activity;

/**
 * @author Jan Lamacz
 */
public interface ISocialNetwork {
	public void logIn(Activity activity);

	public void logOut();

	public boolean isPaired();

	public String getUserName();

	public String getName();
}

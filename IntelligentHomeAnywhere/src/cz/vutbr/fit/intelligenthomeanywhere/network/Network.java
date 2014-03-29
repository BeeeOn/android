package cz.vutbr.fit.intelligenthomeanywhere.network;

import android.content.Context;
import cz.vutbr.fit.intelligenthomeanywhere.NotImplementedException;
import cz.vutbr.fit.intelligenthomeanywhere.User;

/**
 * Network service that handles communication with server.
 * 
 * @author Robyer
 */
public class Network {

	private final Context mContext;
	
	/**
	 * Constructor.
	 * @param context
	 */
	public Network(Context context) {
		mContext = context;
	}

	/**
	 * Checks if internet connection is available.
	 * @return
	 * @throws NotImplementedException
	 */
	public boolean isAvailable() {
		throw new NotImplementedException();
	}

	public User signIn(String userId) {
		return null;
	}
	
}

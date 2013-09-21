package cz.vutbr.fit.intelligenthomeanywhere;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;


public class LoginFacebookActivity extends FragmentActivity {

	private MainFragment mainFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
	        // Add the fragment on initial activity setup
	        mainFragment = new MainFragment();
	        getSupportFragmentManager()
	        .beginTransaction()
	        .add(android.R.id.content, mainFragment)
	        .commit();
	    } else {
	        // Or set the fragment from restored state info
	        mainFragment = (MainFragment) getSupportFragmentManager()
	        .findFragmentById(android.R.id.content);
	    }

		
	    /*if (Session.getActiveSession().isOpened()){
	    	Log.i("FB","Connected to Fb");
	    }else{
	        Session.openActiveSession(this, true, new Session.StatusCallback() {
	          @Override
	          public void call(Session session, SessionState state, Exception exception){               
	            if (session.isOpened()){
	            	Log.i("FB","Connected to Fb");
	            }
	          }
	          }
	        );
	     }*/
	   
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login_facebook, menu);
		return true;
	}

}

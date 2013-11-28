package cz.vutbr.fit.intelligenthomeanywhere.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import cz.vutbr.fit.intelligenthomeanywhere.R;


public class LoginFacebookActivity extends FragmentActivity {

	private MainFragment mMainFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
	        // Add the fragment on initial activity setup
	        mMainFragment = new MainFragment();
	        getSupportFragmentManager()
	        .beginTransaction()
	        .add(android.R.id.content, mMainFragment)
	        .commit();
	    } else {
	        // Or set the fragment from restored state info
	        mMainFragment = (MainFragment) getSupportFragmentManager()
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

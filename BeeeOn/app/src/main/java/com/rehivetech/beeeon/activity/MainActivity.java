package com.rehivetech.beeeon.activity;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/*
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
*/

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.dialog.AddSensorFragmentDialog;
import com.rehivetech.beeeon.activity.dialog.CustomAlertDialog;
import com.rehivetech.beeeon.activity.fragment.CustomViewFragment;
import com.rehivetech.beeeon.activity.fragment.SensorListFragment;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.location.Location;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.menu.NavDrawerMenu;
import com.rehivetech.beeeon.persistence.Persistence;
import com.rehivetech.beeeon.util.Log;


/**
 * Activity class for choosing location
 * 
 * 
 */
public class MainActivity extends BaseApplicationActivity {
	private static final String TAG = MainActivity.class.getSimpleName();

	private Controller mController;

	public static final String ADD_ADAPTER_TAG = "addAdapterDialog";
	public static final String ADD_SENSOR_TAG = "addSensorDialog";
	public static final String FRG_TAG_LOC = "Loc";
	public static final String FRG_TAG_CUS = "Cus";
	private NavDrawerMenu mNavDrawerMenu;
	private SensorListFragment mListDevices;
	private CustomViewFragment mCustomView;
    private Toolbar mToolbar;

	/**
	 * Instance save state tags
	 */
	private static final String LCTN = "lastlocation";
	private static final String CSTVIEW = "lastcustomView";
	private static final String ADAPTER_ID = "lastAdapterId";
	private static final String IS_DRAWER_OPEN = "draweropen";

	/**
	 * saved instance states
	 */
	private String mActiveLocationId;
	private String mActiveAdapterId;
	private String mActiveCustomViewId;
	private boolean mIsDrawerOpen = false;
	

	private Handler mTimeHandler = new Handler();
	private Runnable mTimeRun;
	
	private boolean mFirstUseApp = true;
	private ShowcaseView mSV;

	/**
	 * Tasks which can be running in this activity and after finishing can try to change GUI -> must be cancelled when activity stop
	 */
	private CustomAlertDialog mDialog;

	private boolean backPressed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_location_screen);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle("Navigation Drawer");
            setSupportActionBar(mToolbar);
        }

		// Get controller
		mController = Controller.getInstance(this);

		setSupportProgressBarIndeterminate(true);
		setSupportProgressBarIndeterminateVisibility(true);
		//getSupportActionBar().setIcon(R.drawable.ic_launcher_white);

		// Create NavDrawerMenu
		mNavDrawerMenu = new NavDrawerMenu(this,mToolbar);
		mNavDrawerMenu.openMenu();
		mNavDrawerMenu.setIsDrawerOpen(mIsDrawerOpen);

		mListDevices = new SensorListFragment();

		mCustomView = new CustomViewFragment();

		if (savedInstanceState != null) {
			mIsDrawerOpen = savedInstanceState.getBoolean(IS_DRAWER_OPEN);
			mNavDrawerMenu.setIsDrawerOpen(mIsDrawerOpen);

			mActiveLocationId = savedInstanceState.getString(LCTN);
			mActiveCustomViewId = savedInstanceState.getString(CSTVIEW);
			mActiveAdapterId = savedInstanceState.getString(ADAPTER_ID);
			mListDevices.setLocationID(mActiveLocationId);
			mListDevices.setAdapterID(mActiveAdapterId);
			mNavDrawerMenu.setLocationID(mActiveLocationId);
			mNavDrawerMenu.setAdapterID(mActiveAdapterId);

		} else {
			setActiveAdapterAndLocation();
			mListDevices.setLocationID(mActiveLocationId);
			mListDevices.setAdapterID(mActiveAdapterId);
			mNavDrawerMenu.setLocationID(mActiveLocationId);
			mNavDrawerMenu.setAdapterID(mActiveAdapterId);
			mNavDrawerMenu.redrawMenu();
		}

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if(mActiveLocationId != null){
			ft.replace(R.id.content_frame, mListDevices, FRG_TAG_LOC);
		}
		else if(mActiveCustomViewId != null) {
			ft.replace(R.id.content_frame, mCustomView, FRG_TAG_CUS);
		}
		ft.commit();
		
		// Init tutorial 
		if(mFirstUseApp) {
			//showTutorial();
		}
	}

	private void showTutorial() {
		// TODO Auto-generated method stub
		RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		
		int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
		lps.setMargins(margin, margin, margin, margin);
		ViewTarget target = new ViewTarget(android.R.id.home, this);
		
		OnShowcaseEventListener	listener = new OnShowcaseEventListener() {
			
			@Override
			public void onShowcaseViewShow(ShowcaseView showcaseView) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onShowcaseViewHide(ShowcaseView showcaseView) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
				// TODO Auto-generated method stub
				
			}
		}; 
		
		mSV = new ShowcaseView.Builder(this, true)
		.setTarget(target)
		.setContentTitle("Open Menu")
		.setContentText("For switch location tap to Icon and Menu will rise.")
		//.setStyle(R.style.CustomShowcaseTheme)
		.setShowcaseEventListener(listener)
		.build();
		mSV.setButtonPosition(lps);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "Request code "+requestCode);
		if(requestCode == Constants.ADD_ADAPTER_REQUEST_CODE ) {
			Log.d(TAG, "Return from add adapter activity");
			if(resultCode == Constants.ADD_ADAPTER_CANCELED) {
				Log.d(TAG, "Activity was canceled");
			}
			else if (resultCode == Constants.ADD_ADAPTER_SUCCESS) {
				// Succes of add adapter -> setActive adapter a redraw ALL
				Log.d(TAG, "Add adapter succes");
				setActiveAdapterAndLocation();
				redrawMenu();
			}
		}
		else if (requestCode == Constants.ADD_SENSOR_REQUEST_CODE) {
			Log.d(TAG, "Return from add sensor activity");
			if(resultCode == Constants.ADD_SENSOR_SUCCESS) {
				// Set active location
				mActiveLocationId = data.getExtras().getString(Constants.SETUP_SENSOR_ACT_LOC);
				Log.d(TAG, "Active locID: "+mActiveLocationId + " adapterID: "+mActiveAdapterId);
				redrawDevices();
				redrawMenu();
			}
		}
	}

	public void onAppResume() {
		Log.d(TAG, "onAppResume()");

		backPressed = false;

		mNavDrawerMenu.redrawMenu();
		mNavDrawerMenu.finishActinMode();
		//Check whitch fragment is visible and redraw
		if(mActiveLocationId != null) { // isnt set active location
			redrawDevices();
		} else if(mActiveCustomViewId != null) { // isnt set active custom view
			redrawCustomView();
		}
		else { // app is empty 
			redrawDevices();
		}

		checkNoAdapters();
	}

	public void onAppPause() {
		mTimeHandler.removeCallbacks(mTimeRun);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");

		this.setSupportProgressBarIndeterminateVisibility(false);

		if (mDialog != null) {
			mDialog.dismiss();
		}
		// Cancel all task if same is running from Menu
		if (mNavDrawerMenu != null)
			mNavDrawerMenu.cancelAllTasks();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (backPressed) {
			backPressed = false;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onBackPressed() {
		Log.d(TAG, "BackPressed - onBackPressed " + String.valueOf(backPressed));
		if (mNavDrawerMenu != null) {
			if (backPressed) {
				// second click
				mNavDrawerMenu.secondTapBack();
			} else {
				// first click
				mNavDrawerMenu.firstTapBack();
				backPressed = true;
			}
			mNavDrawerMenu.finishActinMode();
		}
		return;
	}

	public boolean getBackPressed() {
		return backPressed;
	}

	public void setBackPressed(boolean val) {
		backPressed = val;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(ADAPTER_ID, mActiveAdapterId);
		savedInstanceState.putString(LCTN, mActiveLocationId);
		savedInstanceState.putString(CSTVIEW, mActiveCustomViewId);
		savedInstanceState.putBoolean(IS_DRAWER_OPEN, mNavDrawerMenu.getIsDrawerOpen());
		super.onSaveInstanceState(savedInstanceState);
	}

	public void setActiveAdapterAndLocation() {
		// Set active adapter and location
		Adapter adapter = mController.getActiveAdapter();
		if (adapter != null) {
			mActiveAdapterId = adapter.getId();
			setActiveAdapterID(mActiveAdapterId);
			SharedPreferences prefs = mController.getUserSettings();
			String prefKey = Persistence.getPreferencesLastLocation(adapter.getId());

			// UserSettings can be null when user is not logged in!
			String locationId = (prefs == null) ? "" : prefs.getString(prefKey, "");
			Location location = mController.getLocation(adapter.getId(), locationId);

			if (location == null) {
				// No saved or found location, set first location
				List<Location> locations = mController.getLocations(adapter.getId());

				if (locations.size() > 0) {
					Log.d("default", "DEFAULT POSITION: first position selected");
					location = locations.get(0);
				}
			} else {
				Log.d("default", "DEFAULT POSITION: saved position selected");
			}
			if (location != null) {
				mActiveLocationId = location.getId();
				setActiveLocationID(mActiveLocationId);
			} else {
				setActiveLocationID(null);
			}
		}
		else {
			mActiveAdapterId = null;
			mActiveLocationId = null;
		}
	}

	public boolean redrawDevices() {
		mListDevices = new SensorListFragment();
		mListDevices.setIsPaused(isPaused);
		mListDevices.setLocationID(mActiveLocationId);
		mListDevices.setAdapterID(mActiveAdapterId);
		mNavDrawerMenu.setLocationID(mActiveLocationId);
		mNavDrawerMenu.setAdapterID(mActiveAdapterId);

		// set location layout
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.content_frame, mListDevices, FRG_TAG_LOC);
		
		/**
		 * Changed because of Redmine bug #258
		 * About dialog -> click on email -> don't choose any client -> back -> crash
		 */
		//ft.commit();
		ft.commitAllowingStateLoss();

		return true;
	}

	public void redrawMenu() {
		mNavDrawerMenu.setLocationID(mActiveLocationId);
		mNavDrawerMenu.setAdapterID(mActiveAdapterId);
		mNavDrawerMenu.redrawMenu();
		redrawDevices();
	}

	public void redrawCustomView() {
		// return mCustomView.redrawCustomView();
		mCustomView = new CustomViewFragment();

		// set custom view layout
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.content_frame, mCustomView, FRG_TAG_CUS);
		ft.commit();
	}

	public void checkNoAdapters() {
		if (mController.getActiveAdapter() == null) {
			// UserSettings can be null when user is not logged in!
			Log.d(TAG, "CheckNoAdapter");
			SharedPreferences prefs = mController.getUserSettings();
			if (prefs != null && !prefs.getBoolean(Constants.PERSISTENCE_PREF_IGNORE_NO_ADAPTER, false)) {
				Log.d(TAG, "Call ADD ADAPTER");
				Intent intent = new Intent(MainActivity.this, AddAdapterActivity.class);
				startActivityForResult(intent, Constants.ADD_ADAPTER_REQUEST_CODE);
			}
		}
	}

	public void checkNoDevices() {
		Adapter adapter = mController.getActiveAdapter();
		if (adapter != null && mController.getFacilitiesByAdapter(adapter.getId()).isEmpty()) {
			// Show activity for adding new sensor, when this adapter doesn't have any yet
			Log.i(TAG, String.format("%s is empty", adapter.getName()));
			DialogFragment newFragment = new AddSensorFragmentDialog();
			newFragment.show(getSupportFragmentManager(), ADD_SENSOR_TAG);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mNavDrawerMenu.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.location_screen, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			mNavDrawerMenu.clickOnHome();
			break;

		/*case R.id.action_addadapter: {
			//DialogFragment newFragment = new AddAdapterFragmentDialog();
			//newFragment.show(getSupportFragmentManager(), ADD_ADAPTER_TAG);
			Intent intent = new Intent(MainActivity.this, AddAdapterActivity.class);
			startActivityForResult(intent, Constants.ADD_ADAPTER_REQUEST_CODE);
			break;
		}*/
		case R.id.action_settings: {
			Intent intent = new Intent(MainActivity.this, SettingsMainActivity.class);
			startActivity(intent);
			break;
		}/*
		case R.id.action_intro: {
			Intent intent = new Intent(MainActivity.this, IntroActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.action_intro_dialog: {
			DialogFragment newFragment = new IntroFragmentDialog();
			newFragment.show(getSupportFragmentManager(), "intro_dialog");
			break;
		}*/
		case R.id.action_logout: {
			mController.logout();
			Intent intent = new Intent(MainActivity.this, LoginActivity.class);
			startActivity(intent);
			this.finish();
			break;
		}
		}

		return super.onOptionsItemSelected(item);
	}

	protected void renameLocation(final String location, final TextView view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

		// TODO: use better layout than just single EditText
		final EditText edit = new EditText(MainActivity.this);
		edit.setText(location);
		edit.selectAll();
		// TODO: show keyboard automatically

		builder.setCancelable(false).setView(edit).setTitle("Rename location").setNegativeButton("Cancel", null).setPositiveButton("Rename", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String newName = edit.getText().toString();

				// TODO: show loading while saving new name to
				// server (+ use asynctask)
				Location location = new Location(); // FIXME: get that original location from somewhere
				location.setName(newName);

				boolean saved = mController.saveLocation(location);

				String message = saved ? String.format("Location was renamed to '%s'", newName) : "Location wasn't renamed due to error";

				Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();

				// Redraw item in list
				view.setText(newName);
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void setActiveAdapterID(String adapterId) {
		mActiveAdapterId = adapterId;
		mNavDrawerMenu.setAdapterID(adapterId);
		mListDevices.setAdapterID(adapterId);

	}

	public void setActiveLocationID(String locationId) {
		mActiveLocationId = locationId;
		mNavDrawerMenu.setLocationID(locationId);
		mListDevices.setLocationID(locationId);
		mActiveCustomViewId = null;
	}

	public void setActiveCustomViewID(String customViewId) {
		mActiveCustomViewId = customViewId;
		mActiveLocationId = null;
	}
	
	public NavDrawerMenu getMenu() {
		return mNavDrawerMenu;
	}

}

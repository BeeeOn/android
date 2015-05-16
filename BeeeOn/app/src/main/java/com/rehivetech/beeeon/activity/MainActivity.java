package com.rehivetech.beeeon.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.avast.android.dialogs.fragment.ListDialogFragment;
import com.avast.android.dialogs.iface.IListDialogListener;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.dialog.CustomAlertDialog;
import com.rehivetech.beeeon.activity.fragment.CustomViewFragment;
import com.rehivetech.beeeon.activity.fragment.SensorListFragment;
import com.rehivetech.beeeon.activity.fragment.WatchDogListFragment;
import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.asynctask.ReloadAdapterDataTask;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ErrorCode;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.menu.NavDrawerMenu;
import com.rehivetech.beeeon.util.Log;

public class MainActivity extends BaseApplicationActivity implements IListDialogListener {
	private static final String TAG = MainActivity.class.getSimpleName();

	private Controller mController;

	public static final String ADD_ADAPTER_TAG = "addAdapterDialog";
	public static final String ADD_SENSOR_TAG = "addSensorDialog";
	public static final String FRG_TAG_LOC = "Loc";
    public static final String FRG_TAG_CUS = "Cus";
    public static final String FRG_TAG_WAT = "WAT";
	private static final String FRG_TAG_PRF = "PRF";
	private static final int ADD_ACTION_CODE = 987654;
	private NavDrawerMenu mNavDrawerMenu;
	private SensorListFragment mListDevices;
	private CustomViewFragment mCustomView;
    private WatchDogListFragment mWatchDogApp;
    private Toolbar mToolbar;

	private static final int BACK_TIME_INTERVAL = 2100;
	private Toast mExitToast;
	private long mBackPressed;

	/**
	 * Instance save state tags
	 */
	public static final String ADAPTER_ID = "lastAdapterId";

	private static final String LAST_MENU_ID = "lastMenuId";
	private static final String CSTVIEW = "lastcustomView";
	private static final String IS_DRAWER_OPEN = "draweropen";


	/**
	 * saved instance states
	 */
	private String mActiveMenuId;
	private String mActiveAdapterId;

	private boolean mFirstUseApp = true;
	private ShowcaseView mSV;

	/**
	 * Tasks which can be running in this activity and after finishing can try to change GUI -> must be cancelled when activity stop
	 */
	private CustomAlertDialog mDialog;

	private boolean doRedraw = true;

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d(TAG, "onNewIntent()");

		if(intent == null) return;
		String adapterId = intent.getStringExtra(ADAPTER_ID);
		Log.d(TAG, "chosen adapter = " + adapterId);
		// TODO should perform change of adapter and show location (scroll to it?)
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		setContentView(R.layout.activity_location_screen);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(R.string.app_name);
            setSupportActionBar(mToolbar);
        }

		// Get controller
		mController = Controller.getInstance(this);

		// Create NavDrawerMenu
		mNavDrawerMenu = new NavDrawerMenu(this,mToolbar);

		// creates fragments
		mListDevices = new SensorListFragment();
		mCustomView = new CustomViewFragment();
		mWatchDogApp = new WatchDogListFragment();

		if (savedInstanceState != null) {
			if (!savedInstanceState.getBoolean(IS_DRAWER_OPEN))
				mNavDrawerMenu.closeMenu();

			mActiveMenuId = savedInstanceState.getString(LAST_MENU_ID);
			mActiveAdapterId = savedInstanceState.getString(ADAPTER_ID);
			mNavDrawerMenu.setActiveMenuID(mActiveMenuId);
			mNavDrawerMenu.setAdapterID(mActiveAdapterId);

		} else {
			mNavDrawerMenu.closeMenu();
			setActiveAdapterAndMenu();
			mNavDrawerMenu.setActiveMenuID(mActiveMenuId);
			mNavDrawerMenu.setAdapterID(mActiveAdapterId);
			mNavDrawerMenu.redrawMenu();
		}

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if(mActiveMenuId == null) { // Default screen
            ft.replace(R.id.content_frame, mListDevices, FRG_TAG_LOC);
        }
		else if(mActiveMenuId.equals(Constants.GUI_MENU_CONTROL)){
			ft.replace(R.id.content_frame, mListDevices, FRG_TAG_LOC);
		}
        else if(mActiveMenuId.equals(Constants.GUI_MENU_DASHBOARD)) {
            ft.replace(R.id.content_frame, mCustomView, FRG_TAG_CUS);
        }
        else if(mActiveMenuId.equals(Constants.GUI_MENU_WATCHDOG)) {
            ft.replace(R.id.content_frame, mWatchDogApp, FRG_TAG_WAT);
        }
		else if (mActiveMenuId.equals(Constants.GUI_MENU_PROFILE)){
			Intent intent = new Intent(this, ProfileDetailActivity.class);
			mActiveMenuId = null;
			startActivity(intent);
		}
		ft.commit();

		// Init tutorial 
		if(mFirstUseApp) {
			//showTutorial();
		}
	}

	public void setBeeeOnProgressBarVisibility(boolean b) {
		findViewById(R.id.toolbar_progress).setVisibility((b) ? View.VISIBLE : View.GONE);
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
		.setContentTitle(getString(R.string.tutorial_open_menu))
		.setContentText(getString(R.string.tutorial_open_menu_text))
		//.setStyle(R.style.CustomShowcaseTheme)
		.setShowcaseEventListener(listener)
		.build();
		mSV.setButtonPosition(lps);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log.d(TAG, "Request code " + requestCode);
		if(requestCode == Constants.ADD_ADAPTER_REQUEST_CODE ) {
			Log.d(TAG, "Return from add adapter activity");
			if(resultCode == Constants.ADD_ADAPTER_CANCELED) {
				Log.d(TAG, "Activity was canceled");
			}
			else if (resultCode == Constants.ADD_ADAPTER_SUCCESS) {
				// Succes of add adapter -> setActive adapter a redraw ALL
				Log.d(TAG, "Add adapter succes");
				setActiveAdapterAndMenu();
				doRedraw = false;
			}
		}
		else if (requestCode == Constants.ADD_SENSOR_REQUEST_CODE) {
			Log.d(TAG, "Return from add sensor activity");
			if(resultCode == Constants.ADD_SENSOR_SUCCESS) {
				// Set active location
				String res = data.getExtras().getString(Constants.SETUP_SENSOR_ACT_LOC);
				Log.d(TAG, "Active locID: "+res + " adapterID: "+mActiveAdapterId);
				redraw();
			}
		}
	}

	public void onAppResume() {
		setBeeeOnProgressBarVisibility(true);
		// ASYN TASK - Reload all data, if wasnt download in login activity
		final ReloadAdapterDataTask fullReloadTask = new ReloadAdapterDataTask(this,false,ReloadAdapterDataTask.ReloadWhat.ADAPTERS_AND_ACTIVE_ADAPTER);
		fullReloadTask.setNotifyErrors(false);
		fullReloadTask.setListener(new CallbackTask.CallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (!success) {
					AppException e = fullReloadTask.getException();
					ErrorCode errCode = e.getErrorCode();
					if (errCode != null) {
						if (errCode instanceof NetworkError && errCode == NetworkError.SRV_BAD_BT) {
							finish();
							Intent intent = new Intent(MainActivity.this, LoginActivity.class);
							startActivity(intent);
							return;
						}
						Toast.makeText(MainActivity.this, e.getTranslatedErrorMessage(MainActivity.this), Toast.LENGTH_LONG).show();
					}
				}
				setBeeeOnProgressBarVisibility(false);
				// Redraw Activity - probably list of sensors
				Log.d(TAG, "After reload task - go to redraw mainActivity");
				setActiveAdapterAndMenu();
				if (mController.getActiveAdapter() == null) {
					checkNoAdapters();
				} else {
					redraw();
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(fullReloadTask);

		mNavDrawerMenu.redrawMenu();
		// Redraw Main Fragment
		if(doRedraw) {
			redraw();
		}
		else {
			doRedraw = true;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mDialog != null) {
			mDialog.dismiss();
		}
	}

	@Override
	public void onBackPressed() {
		if (mBackPressed + BACK_TIME_INTERVAL > System.currentTimeMillis()) {
			if (mExitToast != null)
				mExitToast.cancel();

			super.onBackPressed();
			return;
		} else {
			mExitToast = Toast.makeText(getBaseContext(), R.string.toast_tap_again_exit, Toast.LENGTH_SHORT);
			mExitToast.show();

			if (mNavDrawerMenu != null)
				mNavDrawerMenu.openMenu();
		}

		mBackPressed = System.currentTimeMillis();
	}

	@Override
	public void onListItemSelected(CharSequence val, int i, int code) {
		if(code == ADD_ACTION_CODE){
			Log.d(TAG,"Add dialog selected: "+val);
			if(getString(R.string.action_addadapter).equals(val)) {
				// ADD ADAPTER
				mListDevices.showAddAdapterDialog();
			}
			else {
				// ADD SENSOR
				mListDevices.showAddSensorDialog();
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(ADAPTER_ID, mActiveAdapterId);
		savedInstanceState.putString(LAST_MENU_ID, mActiveMenuId);
		savedInstanceState.putBoolean(IS_DRAWER_OPEN, mNavDrawerMenu.isMenuOpened());
		super.onSaveInstanceState(savedInstanceState);
	}

	public void setActiveAdapterAndMenu() {
		// Set active adapter and location
		Adapter adapter = mController.getActiveAdapter();
		if (adapter != null) {
			mActiveAdapterId = adapter.getId();
			setActiveAdapterID(mActiveAdapterId);

			if (mActiveMenuId != null) {
				setActiveMenuID(mActiveMenuId);
			} else {
				setActiveMenuID(Constants.GUI_MENU_CONTROL);
			}
		}
		else {
			mActiveAdapterId = null;
			mActiveMenuId = null;
		}
	}

	public boolean redrawMainFragment() {
        // set location layout
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if(mActiveMenuId == null) {
            mListDevices = new SensorListFragment();
            mListDevices.setIsPaused(isPaused);
            mListDevices.setMenuID(mActiveMenuId);
            mListDevices.setAdapterID(mActiveAdapterId);
            mNavDrawerMenu.setActiveMenuID(mActiveMenuId);
            mNavDrawerMenu.setAdapterID(mActiveAdapterId);

            ft.replace(R.id.content_frame, mListDevices, FRG_TAG_LOC);
        } else if(mActiveMenuId.equals(Constants.GUI_MENU_CONTROL)){
            mListDevices = new SensorListFragment();
            mListDevices.setIsPaused(isPaused);
            mListDevices.setMenuID(mActiveMenuId);
            mListDevices.setAdapterID(mActiveAdapterId);
            mNavDrawerMenu.setActiveMenuID(mActiveMenuId);
            mNavDrawerMenu.setAdapterID(mActiveAdapterId);

            ft.replace(R.id.content_frame, mListDevices, FRG_TAG_LOC);
        }
        else if(mActiveMenuId.equals(Constants.GUI_MENU_DASHBOARD)) {
            mCustomView = new CustomViewFragment();
            ft.replace(R.id.content_frame, mCustomView, FRG_TAG_CUS);
        }
        else if(mActiveMenuId.equals(Constants.GUI_MENU_WATCHDOG)){
            mWatchDogApp = new WatchDogListFragment();
            ft.replace(R.id.content_frame, mWatchDogApp, FRG_TAG_WAT);
        }
		else if (mActiveMenuId.equals(Constants.GUI_MENU_PROFILE)){
			Intent intent = new Intent(this, ProfileDetailActivity.class);
			mActiveMenuId = null;
			startActivity(intent);
		}
		ft.commitAllowingStateLoss();

		return true;
	}

	public void redraw() {
		Log.d(TAG,"REDRAW - activeMenu: "+mActiveMenuId +" activeAdapter: "+mActiveAdapterId);
		mNavDrawerMenu.setActiveMenuID(mActiveMenuId);
		mNavDrawerMenu.setAdapterID(mActiveAdapterId);
		mNavDrawerMenu.redrawMenu();
		redrawMainFragment();
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


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mNavDrawerMenu.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mNavDrawerMenu.isMenuOpened()) {
				mNavDrawerMenu.closeMenu();
			} else {
				mNavDrawerMenu.openMenu();
			}
			break;
		case R.id.action_notification:
			// Notification
			Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void setActiveAdapterID(String adapterId) {
		mActiveAdapterId = adapterId;
		mNavDrawerMenu.setAdapterID(adapterId);
		if(mListDevices != null)
			mListDevices.setAdapterID(adapterId);
	}

	public void setActiveMenuID(String id) {
		mActiveMenuId = id;
		mNavDrawerMenu.setActiveMenuID(id);
		if(mListDevices != null)
			mListDevices.setMenuID(id);
	}

    public void logout() {
        mController.logout();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        this.finish();
    }
	
	public NavDrawerMenu getMenu() {
		return mNavDrawerMenu;
	}

	public void showOldAddDialog(String[] mStringArray) {

		ListDialogFragment
				.createBuilder(this, getSupportFragmentManager())
				.setTitle(getString(R.string.add_action_dialog_title))
				.setItems(mStringArray)
				.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE)
				.setConfirmButtonText(getString(R.string.notification_add))
				.setCancelButtonText(getString(R.string.notification_cancel))
				.setRequestCode(ADD_ACTION_CODE)
				.show();

	}
}

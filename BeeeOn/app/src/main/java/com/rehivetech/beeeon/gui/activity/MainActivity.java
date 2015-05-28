package com.rehivetech.beeeon.gui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.rehivetech.beeeon.gui.dialog.CustomAlertDialog;
import com.rehivetech.beeeon.gui.fragment.CustomViewFragment;
import com.rehivetech.beeeon.gui.fragment.SensorListFragment;
import com.rehivetech.beeeon.gui.fragment.WatchdogListFragment;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.menu.NavDrawerMenu;
import com.rehivetech.beeeon.util.Log;

public class MainActivity extends BaseApplicationActivity implements IListDialogListener {
	private static final String TAG = MainActivity.class.getSimpleName();

	private Controller mController;

	public static final String ADD_GATE_TAG = "addGateDialog";
	public static final String ADD_SENSOR_TAG = "addSensorDialog";
	public static final String FRG_TAG_LOC = "Loc";
	public static final String FRG_TAG_CUS = "Cus";
	public static final String FRG_TAG_WAT = "WAT";
	private static final String FRG_TAG_PRF = "PRF";
	private static final int ADD_ACTION_CODE = 987654;
	private NavDrawerMenu mNavDrawerMenu;
	private SensorListFragment mListModules;
	private CustomViewFragment mCustomView;
	private WatchdogListFragment mWatchdogApp;
	private Toolbar mToolbar;

	private static final int BACK_TIME_INTERVAL = 2100;
	private Toast mExitToast;
	private long mBackPressed;

	/**
	 * Instance save state tags
	 */
	public static final String GATE_ID = "lastGateId";

	private static final String LAST_MENU_ID = "lastMenuId";
	private static final String CSTVIEW = "lastcustomView";
	private static final String IS_DRAWER_OPEN = "draweropen";


	/**
	 * saved instance states
	 */
	private String mActiveMenuId;
	private String mActiveGateId;

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

		if (intent == null) return;
		String gateId = intent.getStringExtra(GATE_ID);
		Log.d(TAG, "chosen gate = " + gateId);
		// TODO should perform change of gate and show location (scroll to it?)
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
		mNavDrawerMenu = new NavDrawerMenu(this, mToolbar);

		// creates fragments
		mListModules = new SensorListFragment();
		mCustomView = new CustomViewFragment();
		mWatchdogApp = new WatchdogListFragment();

		if (savedInstanceState != null) {
			if (!savedInstanceState.getBoolean(IS_DRAWER_OPEN))
				mNavDrawerMenu.closeMenu();

			mActiveMenuId = savedInstanceState.getString(LAST_MENU_ID);
			mActiveGateId = savedInstanceState.getString(GATE_ID);
			mNavDrawerMenu.setActiveMenuID(mActiveMenuId);
			mNavDrawerMenu.setGateId(mActiveGateId);

		} else {
			mNavDrawerMenu.closeMenu();
			setActiveGateAndMenu();
			mNavDrawerMenu.setActiveMenuID(mActiveMenuId);
			mNavDrawerMenu.setGateId(mActiveGateId);
			mNavDrawerMenu.redrawMenu();
		}

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (mActiveMenuId == null) { // Default screen
			ft.replace(R.id.content_frame, mListModules, FRG_TAG_LOC);
		} else if (mActiveMenuId.equals(Constants.GUI_MENU_CONTROL)) {
			ft.replace(R.id.content_frame, mListModules, FRG_TAG_LOC);
		} else if (mActiveMenuId.equals(Constants.GUI_MENU_DASHBOARD)) {
			ft.replace(R.id.content_frame, mCustomView, FRG_TAG_CUS);
		} else if (mActiveMenuId.equals(Constants.GUI_MENU_WATCHDOG)) {
			ft.replace(R.id.content_frame, mWatchdogApp, FRG_TAG_WAT);
		} else if (mActiveMenuId.equals(Constants.GUI_MENU_PROFILE)) {
			Intent intent = new Intent(this, ProfileDetailActivity.class);
			mActiveMenuId = null;
			startActivity(intent);
		}
		ft.commit();

		// Init tutorial 
		if (mFirstUseApp) {
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

		OnShowcaseEventListener listener = new OnShowcaseEventListener() {

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
		if (requestCode == Constants.ADD_GATE_REQUEST_CODE) {
			Log.d(TAG, "Return from add gate activity");
			if (resultCode == Constants.ADD_GATE_CANCELED) {
				Log.d(TAG, "Activity was canceled");
			} else if (resultCode == Constants.ADD_GATE_SUCCESS) {
				// Succes of add gate -> setActive gate a redraw ALL
				Log.d(TAG, "Add gate succes");
				setActiveGateAndMenu();
				doRedraw = false;
			}
		} else if (requestCode == Constants.ADD_SENSOR_REQUEST_CODE) {
			Log.d(TAG, "Return from add sensor activity");
			if (resultCode == Constants.ADD_SENSOR_SUCCESS) {
				// Set active location
				String res = data.getExtras().getString(Constants.SETUP_SENSOR_ACT_LOC);
				Log.d(TAG, "Active locID: " + res + " gateID: " + mActiveGateId);
				redraw();
			}
		}
	}

	public void onAppResume() {
		// ASYN TASK - Reload all data, if wasnt download in login activity
		final ReloadGateDataTask fullReloadTask = new ReloadGateDataTask(this, false, ReloadGateDataTask.ReloadWhat.GATES_AND_ACTIVE_GATE);
		fullReloadTask.setListener(new CallbackTask.CallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					// Redraw Activity - probably list of sensors
					Log.d(TAG, "After reload task - go to redraw mainActivity");
					setActiveGateAndMenu();
					if (mController.getActiveGate() == null) {
						checkNoGates();
					} else {
						redraw();
					}
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(fullReloadTask);

		mNavDrawerMenu.redrawMenu();
		// Redraw Main Fragment
		if (doRedraw) {
			redraw();
		} else {
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
		if (code == ADD_ACTION_CODE) {
			Log.d(TAG, "Add dialog selected: " + val);
			if (getString(R.string.action_addgate).equals(val)) {
				// ADD GATE
				mListModules.showAddGateDialog();
			} else {
				// ADD SENSOR
				mListModules.showAddSensorDialog();
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(GATE_ID, mActiveGateId);
		savedInstanceState.putString(LAST_MENU_ID, mActiveMenuId);
		savedInstanceState.putBoolean(IS_DRAWER_OPEN, mNavDrawerMenu.isMenuOpened());
		super.onSaveInstanceState(savedInstanceState);
	}

	public void setActiveGateAndMenu() {
		// Set active gate and location
		Gate gate = mController.getActiveGate();
		if (gate != null) {
			mActiveGateId = gate.getId();
			setActiveGateId(mActiveGateId);

			if (mActiveMenuId != null) {
				setActiveMenuID(mActiveMenuId);
			} else {
				setActiveMenuID(Constants.GUI_MENU_CONTROL);
			}
		} else {
			mActiveGateId = null;
			mActiveMenuId = null;
		}
	}

	public boolean redrawMainFragment() {
		// set location layout
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (mActiveMenuId == null) {
			mListModules = new SensorListFragment();
			mListModules.setIsPaused(isPaused);
			mListModules.setMenuID(mActiveMenuId);
			mListModules.setGateId(mActiveGateId);
			mNavDrawerMenu.setActiveMenuID(mActiveMenuId);
			mNavDrawerMenu.setGateId(mActiveGateId);

			ft.replace(R.id.content_frame, mListModules, FRG_TAG_LOC);
		} else if (mActiveMenuId.equals(Constants.GUI_MENU_CONTROL)) {
			mListModules = new SensorListFragment();
			mListModules.setIsPaused(isPaused);
			mListModules.setMenuID(mActiveMenuId);
			mListModules.setGateId(mActiveGateId);
			mNavDrawerMenu.setActiveMenuID(mActiveMenuId);
			mNavDrawerMenu.setGateId(mActiveGateId);

			ft.replace(R.id.content_frame, mListModules, FRG_TAG_LOC);
		} else if (mActiveMenuId.equals(Constants.GUI_MENU_DASHBOARD)) {
			mCustomView = new CustomViewFragment();
			ft.replace(R.id.content_frame, mCustomView, FRG_TAG_CUS);
		} else if (mActiveMenuId.equals(Constants.GUI_MENU_WATCHDOG)) {
			mWatchdogApp = new WatchdogListFragment();
			ft.replace(R.id.content_frame, mWatchdogApp, FRG_TAG_WAT);
		} else if (mActiveMenuId.equals(Constants.GUI_MENU_PROFILE)) {
			Intent intent = new Intent(this, ProfileDetailActivity.class);
			mActiveMenuId = null;
			startActivity(intent);
		}
		ft.commitAllowingStateLoss();

		return true;
	}

	public void redraw() {
		Log.d(TAG, "REDRAW - activeMenu: " + mActiveMenuId + " activeGate: " + mActiveGateId);
		mNavDrawerMenu.setActiveMenuID(mActiveMenuId);
		mNavDrawerMenu.setGateId(mActiveGateId);
		mNavDrawerMenu.redrawMenu();
		redrawMainFragment();
	}


	public void checkNoGates() {
		if (mController.getActiveGate() == null) {
			// UserSettings can be null when user is not logged in!
			Log.d(TAG, "CheckNoGate");
			SharedPreferences prefs = mController.getUserSettings();
			if (prefs != null && !prefs.getBoolean(Constants.PERSISTENCE_PREF_IGNORE_NO_GATE, false)) {
				Log.d(TAG, "Call ADD GATE");
				Intent intent = new Intent(MainActivity.this, AddGateActivity.class);
				startActivityForResult(intent, Constants.ADD_GATE_REQUEST_CODE);
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

	public void setActiveGateId(String gateId) {
		mActiveGateId = gateId;
		mNavDrawerMenu.setGateId(gateId);
		if (mListModules != null)
			mListModules.setGateId(gateId);
	}

	public void setActiveMenuID(String id) {
		mActiveMenuId = id;
		mNavDrawerMenu.setActiveMenuID(id);
		if (mListModules != null)
			mListModules.setMenuID(id);
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

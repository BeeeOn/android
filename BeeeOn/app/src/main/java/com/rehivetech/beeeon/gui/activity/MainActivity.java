package com.rehivetech.beeeon.gui.activity;

import android.app.Activity;
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
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.dialog.ConfirmDialog;
import com.rehivetech.beeeon.gui.fragment.CustomViewFragment;
import com.rehivetech.beeeon.gui.fragment.ModuleListFragment;
import com.rehivetech.beeeon.gui.fragment.WatchdogListFragment;
import com.rehivetech.beeeon.gui.menu.NavDrawerMenu;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.watchdog.Watchdog;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.threading.task.UnregisterGateTask;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;

public class MainActivity extends BaseApplicationActivity implements IListDialogListener, ConfirmDialog.ConfirmDialogListener {
	private static final String TAG = MainActivity.class.getSimpleName();

	private static final int TUTORIAL_MARGIN = 12;

	public static final String EXTRA_GATE_ID = "gate_id";

	public static final String ADD_GATE_TAG = "addGateDialog";
	public static final String ADD_DEVICE_TAG = "addDeviceDialog";
	public static final String FRG_TAG_LOC = "Loc";
	public static final String FRG_TAG_CUS = "Cus";
	public static final String FRG_TAG_WAT = "WAT";
	private static final String FRG_TAG_PRF = "PRF";
	private static final int ADD_ACTION_CODE = 987654;
	private NavDrawerMenu mNavDrawerMenu;
	private ModuleListFragment mModuleListFragment;
	private CustomViewFragment mCustomViewFragment;
	private WatchdogListFragment mWatchdogListFragment;

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

	private boolean doRedraw = true;

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (intent == null) return;
		String gateId = intent.getStringExtra(EXTRA_GATE_ID);
		Log.d(TAG, "chosen gate = " + gateId);
		// TODO should perform change of gate and show location (scroll to it?)
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.beeeon_toolbar);
		if (toolbar != null) {
			toolbar.setTitle(R.string.app_name);
			setSupportActionBar(toolbar);
		}

		// Create NavDrawerMenu
		mNavDrawerMenu = new NavDrawerMenu(this, toolbar);

		// creates fragments
		mModuleListFragment = new ModuleListFragment();
		mCustomViewFragment = new CustomViewFragment();
		mWatchdogListFragment = new WatchdogListFragment();

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
			ft.replace(R.id.main_content_frame, mModuleListFragment, FRG_TAG_LOC);
		} else if (mActiveMenuId.equals(Constants.GUI_MENU_CONTROL)) {
			ft.replace(R.id.main_content_frame, mModuleListFragment, FRG_TAG_LOC);
		} else if (mActiveMenuId.equals(Constants.GUI_MENU_DASHBOARD)) {
			ft.replace(R.id.main_content_frame, mCustomViewFragment, FRG_TAG_CUS);
		} else if (mActiveMenuId.equals(Constants.GUI_MENU_WATCHDOG)) {
			ft.replace(R.id.main_content_frame, mWatchdogListFragment, FRG_TAG_WAT);
		} else if (mActiveMenuId.equals(Constants.GUI_MENU_GATEWAY)) {
			mActiveMenuId = null;

			Intent intent = new Intent(this, GateDetailActivity.class);
			intent.putExtra(GateDetailActivity.EXTRA_GATE_ID, mActiveGateId);
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

		int marginPx = Utils.convertDpToPixel(TUTORIAL_MARGIN);
		lps.setMargins(marginPx, marginPx, marginPx, marginPx);
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

		ShowcaseView showcaseView = new ShowcaseView.Builder(this, true)
				.setTarget(target)
				.setContentTitle(getString(R.string.tutorial_open_menu))
				.setContentText(getString(R.string.tutorial_open_menu_text))
						//.setStyle(R.style.CustomShowcaseTheme)
				.setShowcaseEventListener(listener)
				.build();
		showcaseView.setButtonPosition(lps);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != Activity.RESULT_OK)
			return;

		switch (requestCode) {
			case Constants.ADD_GATE_REQUEST_CODE: {
				// Succes of add gate -> setActive gate a redraw ALL
				setActiveGateAndMenu();
				doRedraw = false;
				break;
			}
			case Constants.ADD_DEVICE_REQUEST_CODE: {
				redraw();
				break;
			}
		}
	}

	public void onAppResume() {
		// Reload all data, if wasn't downloaded in login activity
		final ReloadGateDataTask fullReloadTask = new ReloadGateDataTask(this, false, ReloadGateDataTask.RELOAD_GATES_AND_ACTIVE_GATE_DEVICES);
		fullReloadTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					// Redraw Activity - probably list of modules
					Log.d(TAG, "After reload task - go to redraw mainActivity");
					setActiveGateAndMenu();
					if (Controller.getInstance(MainActivity.this).getActiveGate() == null) {
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
				mModuleListFragment.showAddGateDialog();
			} else {
				// ADD DEVICE
				mModuleListFragment.showAddDeviceDialog();
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
		Gate gate = Controller.getInstance(this).getActiveGate();
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
			mModuleListFragment = new ModuleListFragment();
			mModuleListFragment.setIsPaused(isPaused);
			mModuleListFragment.setMenuID(mActiveMenuId);
			mModuleListFragment.setGateId(mActiveGateId);
			mNavDrawerMenu.setActiveMenuID(mActiveMenuId);
			mNavDrawerMenu.setGateId(mActiveGateId);

			ft.replace(R.id.main_content_frame, mModuleListFragment, FRG_TAG_LOC);
		} else if (mActiveMenuId.equals(Constants.GUI_MENU_CONTROL)) {
			mModuleListFragment = new ModuleListFragment();
			mModuleListFragment.setIsPaused(isPaused);
			mModuleListFragment.setMenuID(mActiveMenuId);
			mModuleListFragment.setGateId(mActiveGateId);
			mNavDrawerMenu.setActiveMenuID(mActiveMenuId);
			mNavDrawerMenu.setGateId(mActiveGateId);

			ft.replace(R.id.main_content_frame, mModuleListFragment, FRG_TAG_LOC);
		} else if (mActiveMenuId.equals(Constants.GUI_MENU_DASHBOARD)) {
			mCustomViewFragment = new CustomViewFragment();
			ft.replace(R.id.main_content_frame, mCustomViewFragment, FRG_TAG_CUS);
		} else if (mActiveMenuId.equals(Constants.GUI_MENU_WATCHDOG)) {
			mWatchdogListFragment = new WatchdogListFragment();
			ft.replace(R.id.main_content_frame, mWatchdogListFragment, FRG_TAG_WAT);
		} else if (mActiveMenuId.equals(Constants.GUI_MENU_GATEWAY)) {
			mActiveMenuId = null;
			Intent intent = new Intent(this, GateDetailActivity.class);
			intent.putExtra(GateDetailActivity.EXTRA_GATE_ID, mActiveGateId);
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
		Controller controller = Controller.getInstance(this);
		if (controller.getActiveGate() == null) {
			// UserSettings can be null when user is not logged in!
			Log.d(TAG, "CheckNoGate");
			SharedPreferences prefs = controller.getUserSettings();
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
		inflater.inflate(R.menu.activity_main_menu, menu);
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
			case R.id.main_menu_action_notification:
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
		if (mModuleListFragment != null)
			mModuleListFragment.setGateId(gateId);
	}

	public void setActiveMenuID(String id) {
		mActiveMenuId = id;
		mNavDrawerMenu.setActiveMenuID(id);
		if (mModuleListFragment != null)
			mModuleListFragment.setMenuID(id);
	}

	public void logout() {
		Controller.getInstance(this).logout(false);
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

	private void doUnregisterGateTask(String gateId) {
		UnregisterGateTask unregisterGateTask = new UnregisterGateTask(this);

		unregisterGateTask.setListener(new CallbackTask.ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (success) {
					Toast.makeText(MainActivity.this, R.string.toast_gate_removed, Toast.LENGTH_LONG).show();
					setActiveGateAndMenu();
					redraw();
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(unregisterGateTask, gateId);
	}

	@Override
	public void onConfirm(int confirmType, String dataId) {
		if (confirmType == ConfirmDialog.TYPE_DELETE_GATE) {
			doUnregisterGateTask(dataId);
		} else if (confirmType == ConfirmDialog.TYPE_DELETE_WATCHDOG) {
			Watchdog watchdog = Controller.getInstance(this).getWatchdogsModel().getWatchdog(mActiveGateId, dataId);
			if (watchdog != null) {
				mWatchdogListFragment.doRemoveWatchdogTask(watchdog);
			}
		} else if (confirmType == ConfirmDialog.TYPE_DELETE_DEVICE) {
			Device device = Controller.getInstance(this).getDevicesModel().getDevice(mActiveGateId, dataId);
			if (device != null) {
				mModuleListFragment.doRemoveDeviceTask(device);
			}
		}
	}
}

package com.rehivetech.beeeon.gui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.avast.android.dialogs.fragment.ListDialogFragment;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.dialog.ConfirmDialog;
import com.rehivetech.beeeon.gui.dialog.InfoDialogFragment;
import com.rehivetech.beeeon.gui.fragment.CustomViewFragment;
import com.rehivetech.beeeon.gui.fragment.DevicesListFragment;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.threading.task.UnregisterGateTask;
import com.rehivetech.beeeon.util.Utils;

public class MainActivity extends BaseApplicationActivity implements ConfirmDialog.ConfirmDialogListener, NavigationView.OnNavigationItemSelectedListener {
	private static final String TAG = MainActivity.class.getSimpleName();

	private static final int TUTORIAL_MARGIN = 12;

	public static final String EXTRA_GATE_ID = "gate_id";

	public static final String ADD_GATE_TAG = "addGateDialog";
	public static final String ADD_DEVICE_TAG = "addDeviceDialog";
	public static final String FRG_TAG_LOC = "Loc";
	public static final String FRG_TAG_CUS = "Cus";
	private static final String FRG_TAG_PRF = "PRF";
	private final static String TAG_INFO = "tag_info";
	private static final int ADD_ACTION_CODE = 987654;
//	private NavDrawerMenu mNavDrawerMenu;
	private DevicesListFragment mDevicesListFragment;
	private CustomViewFragment mCustomViewFragment;

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

	private DrawerLayout mDrawerLayout;
	private NavigationView mNavigationView;
	private ActionBarDrawerToggle mDrawerToggle;

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

		Toolbar toolbar = setupToolbar(R.string.manifest_title_main);

		mNavigationView = (NavigationView) findViewById(R.id.navigationview_layout_drawer);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
		mNavigationView.setNavigationItemSelectedListener(this);
		mDrawerToggle = new ActionBarDrawerToggle(
				this, mDrawerLayout, toolbar, R.string.nav_drawer_menu_drawer_open, R.string.nav_drawer_menu_drawer_close);

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerToggle.syncState();

		// Create NavDrawerMenu
//		mNavDrawerMenu = new NavDrawerMenu(this, toolbar);

		// creates fragments
		mDevicesListFragment = DevicesListFragment.newInstance(mActiveGateId);
		mCustomViewFragment = new CustomViewFragment();

		if (savedInstanceState != null) {
			if (!savedInstanceState.getBoolean(IS_DRAWER_OPEN))
//				mNavDrawerMenu.closeMenu();

			mActiveMenuId = savedInstanceState.getString(LAST_MENU_ID);
			mActiveGateId = savedInstanceState.getString(GATE_ID);
//			mNavDrawerMenu.setActiveMenuID(mActiveMenuId);
//			mNavDrawerMenu.setGateId(mActiveGateId);

		} else {
//			mNavDrawerMenu.closeMenu();
			setActiveGateAndMenu();
//			mNavDrawerMenu.setActiveMenuID(mActiveMenuId);
//			mNavDrawerMenu.setGateId(mActiveGateId);
//			mNavDrawerMenu.redrawMenu();
		}

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (mActiveMenuId == null) { // Default screen
			ft.replace(R.id.main_content_frame, mDevicesListFragment, FRG_TAG_LOC);
		} else if (mActiveMenuId.equals(Constants.GUI_MENU_CONTROL)) {
			ft.replace(R.id.main_content_frame, mDevicesListFragment, FRG_TAG_LOC);
		} else if (mActiveMenuId.equals(Constants.GUI_MENU_DASHBOARD)) {
			ft.replace(R.id.main_content_frame, mCustomViewFragment, FRG_TAG_CUS);
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
				.setContentTitle(getString(R.string.main_tut_open_menu))
				.setContentText(getString(R.string.main_tut_open_menu_text))
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

//		mNavDrawerMenu.redrawMenu();
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
			mExitToast = Toast.makeText(getBaseContext(), R.string.main_toast_tap_again_exit, Toast.LENGTH_SHORT);
			mExitToast.show();

//			if (mNavDrawerMenu != null)
//				mNavDrawerMenu.openMenu();
		}

		mBackPressed = System.currentTimeMillis();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(GATE_ID, mActiveGateId);
		savedInstanceState.putString(LAST_MENU_ID, mActiveMenuId);
//		savedInstanceState.putBoolean(IS_DRAWER_OPEN, mNavDrawerMenu.isMenuOpened());
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
		// stop all tasks - so that the reload tasks will not continue
		callbackTaskManager.cancelAndRemoveAll();

		// set location layout
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (mActiveMenuId == null) {
			mDevicesListFragment = DevicesListFragment.newInstance(mActiveGateId);
//			mNavDrawerMenu.setActiveMenuID(mActiveMenuId);
//			mNavDrawerMenu.setGateId(mActiveGateId);
			ft.replace(R.id.main_content_frame, mDevicesListFragment, FRG_TAG_LOC);
		} else if (mActiveMenuId.equals(Constants.GUI_MENU_CONTROL)) {
			mDevicesListFragment = DevicesListFragment.newInstance(mActiveGateId);
//			mNavDrawerMenu.setActiveMenuID(mActiveMenuId);
//			mNavDrawerMenu.setGateId(mActiveGateId);
			ft.replace(R.id.main_content_frame, mDevicesListFragment, FRG_TAG_LOC);
		} else if (mActiveMenuId.equals(Constants.GUI_MENU_DASHBOARD)) {
			mCustomViewFragment = new CustomViewFragment();
			ft.replace(R.id.main_content_frame, mCustomViewFragment, FRG_TAG_CUS);
		} else if (mActiveMenuId.equals(Constants.GUI_MENU_GATEWAY)) {
			mActiveMenuId = null;
			Intent intent = new Intent(this, GateDetailActivity.class);
			intent.putExtra(GateDetailActivity.EXTRA_GATE_ID, mActiveGateId);
			startActivity(intent);
		}
		else if(mActiveMenuId.equals(Constants.GUI_MENU_DEVICES)){
			mActiveMenuId = null;
			Intent intent = new Intent(this, DevicesListActivity.class);
			intent.putExtra(DevicesListActivity.EXTRA_GATE_ID, mActiveGateId);
			startActivity(intent);
		}
		ft.commitAllowingStateLoss();

		return true;
	}

	public void redraw() {
		Log.d(TAG, "REDRAW - activeMenu: " + mActiveMenuId + " activeGate: " + mActiveGateId);
//		mNavDrawerMenu.setActiveMenuID(mActiveMenuId);
//		mNavDrawerMenu.setGateId(mActiveGateId);
//		mNavDrawerMenu.redrawMenu();
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
//		mNavDrawerMenu.onConfigurationChanged(newConfig);
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
//				if (mNavDrawerMenu.isMenuOpened()) {
//					mNavDrawerMenu.closeMenu();
//				} else {
//					mNavDrawerMenu.openMenu();
//				}
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
//		mNavDrawerMenu.setGateId(gateId);
		if (mDevicesListFragment != null)
			mDevicesListFragment.setActiveGateId(gateId);
	}

	public void setActiveMenuID(String id) {
		mActiveMenuId = id;
//		mNavDrawerMenu.setActiveMenuID(id);
	}

	public void logout() {
		Controller.getInstance(this).logout(false);
		Intent intent = new Intent(MainActivity.this, LoginActivity.class);
		startActivity(intent);
		this.finish();
	}

//	public NavDrawerMenu getMenu() {
//		return mNavDrawerMenu;
//	}

	public void showOldAddDialog(String[] mStringArray) {

		ListDialogFragment
				.createBuilder(this, getSupportFragmentManager())
				.setTitle(getString(R.string.main_dialog_title_action_add))
				.setItems(mStringArray)
				.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE)
				.setConfirmButtonText(getString(R.string.main_btn_add))
				.setCancelButtonText(getString(R.string.activity_fragment_btn_cancel))
				.setRequestCode(ADD_ACTION_CODE)
				.show();

	}

	private void doUnregisterGateTask(String gateId) {
		UnregisterGateTask unregisterGateTask = new UnregisterGateTask(this);

		unregisterGateTask.setListener(new CallbackTask.ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (success) {
					Toast.makeText(MainActivity.this, R.string.gate_detail_toast_gate_removed, Toast.LENGTH_LONG).show();
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
		} else if (confirmType == ConfirmDialog.TYPE_DELETE_DEVICE) {
			Device device = Controller.getInstance(this).getDevicesModel().getDevice(mActiveGateId, dataId);
			if (device != null) {
				mDevicesListFragment.doRemoveDeviceTask(device);
			}
		}
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem menuItem) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		menuItem.setChecked(true);
		mDrawerLayout.closeDrawers();

		switch (menuItem.getItemId()) {
			case R.id.nav_drawer_devices:
				mDevicesListFragment = DevicesListFragment.newInstance(mActiveGateId);
				ft.replace(R.id.main_content_frame, mDevicesListFragment, FRG_TAG_LOC).commit();
				return true;
			case R.id.nav_drawer_dashboard:
				mCustomViewFragment = new CustomViewFragment();
				ft.replace(R.id.main_content_frame, mCustomViewFragment, FRG_TAG_CUS).commit();
				return true;
			case R.id.nav_drawer_settings:
				Intent intent = new Intent(this, SettingsMainActivity.class);
				startActivity(intent);
				return true;
			case R.id.nav_drawer_about:
				InfoDialogFragment dialog = new InfoDialogFragment();
				dialog.show(getSupportFragmentManager(), TAG_INFO);
				return true;
			case R.id.nav_drawer_logout:
				logout();
				return true;
		}
		return false;
	}
}

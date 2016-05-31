package com.rehivetech.beeeon.gui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.dialog.ConfirmDialog;
import com.rehivetech.beeeon.gui.dialog.InfoDialogFragment;
import com.rehivetech.beeeon.gui.fragment.DashboardPagerFragment;
import com.rehivetech.beeeon.gui.fragment.DevicesListFragment;
import com.rehivetech.beeeon.gui.fragment.EmptyFragment;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.threading.task.SwitchGateTask;
import com.rehivetech.beeeon.threading.task.UnregisterGateTask;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.State;

public class MainActivity extends BaseApplicationActivity implements ConfirmDialog.ConfirmDialogListener, NavigationView.OnNavigationItemSelectedListener {
	private static final String TAG = MainActivity.class.getSimpleName();

	private static final String FRAGMENT_TAG_DEVICES = "tag_devices";
	private static final String FRAGMENT_TAG_DASHBOARD = "tag_dashboard";
	private static final String FRAGMENT_TAG_INFO = "tag_info";

	public static final String MENU_ITEM_DEVICES = "item_devices";
	public static final String MENU_ITEM_GRAPHS = "item_graphs";
	public static final String MENU_ITEM_GATEWAY = "item_gateway";

	public static final String GATE_ID = "lastGateId";
	private static final String LAST_MENU_ID = "lastMenuId";

	/* Holds active menu and gate ids */
	@State public String mActiveMenuId;
	@State public String mActiveGateId;

	/* Navigation drawer items */
	@Bind(R.id.main_drawer_layout)
	DrawerLayout mDrawerLayout;
	@Bind(R.id.navigationview_layout_drawer)
	NavigationView mNavigationView;

	private View mNavigationHeader;
	private Menu mNavigationMenu;
	private Spinner mGatesSpinner;
	private ArrayAdapter<Gate> mGatesAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		mNavigationView.setNavigationItemSelectedListener(this);

		mNavigationMenu = mNavigationView.getMenu();

		mNavigationHeader = mNavigationView.getHeaderView(0);

		mGatesSpinner = ButterKnife.findById(mNavigationHeader, R.id.menu_profile_gates_spinner);
		mGatesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			private boolean firstSelect = true;

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// Ignore first select after creation (which is automatic - not selected by user)
				if (firstSelect) {
					firstSelect = false;
					return;
				}

				Gate gate = (Gate) parent.getItemAtPosition(position);
				if (gate == null)
					return;

				// TODO: Don't switch gate if this is first "start" (on creating activity)
				doSwitchGateTask(gate.getId());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		mGatesAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item);
		mGatesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mGatesSpinner.setAdapter(mGatesAdapter);

		if (savedInstanceState == null) {
			setActiveGateAndMenu();
		}

		redrawNavigation();
		reloadFragment();

//		migrateDashboard();
	}

	@Override
	public void onResume() {
		super.onResume();
		setActiveGateAndMenu();
		redrawNavigation();
		reloadFragment(); // FIXME: do better (only when needed)
		reloadData();
	}

	/**
	 * Back pressing will close navDrawer if opened, otherwise usual functionality
	 */
	@Override
	public void onBackPressed() {
		if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
			closeNavDrawer();
			return;
		}
		super.onBackPressed();
	}

	private void reloadFragment() {
		if (mActiveMenuId == null || mActiveMenuId.equals(MENU_ITEM_DEVICES)) {
			mNavigationView.setCheckedItem(R.id.nav_drawer_devices);
			changeFragment(DevicesListFragment.newInstance(mActiveGateId), FRAGMENT_TAG_DEVICES);
		} else if (mActiveMenuId.equals(MENU_ITEM_GRAPHS)) {
			mNavigationView.setCheckedItem(R.id.nav_drawer_dashboard);
			changeFragment(DashboardPagerFragment.newInstance(mActiveGateId), FRAGMENT_TAG_DASHBOARD);
		} else if (mActiveMenuId.equals(MENU_ITEM_GATEWAY)) {
			if (mActiveGateId != null) {
				Intent intent = new Intent(this, GateDetailActivity.class);
				intent.putExtra(GateDetailActivity.EXTRA_GATE_ID, mActiveGateId);
				startActivity(intent);
			}
		}
	}

	public void setActiveGateAndMenu() {
		Controller controller = Controller.getInstance(this);

		// Set active gate and location
		Gate gate = controller.getActiveGate();
		if (gate != null) {
			mActiveGateId = gate.getId();

			// FIXME: This should get ACTIVE fragment, not just some random (deviceslist) one
			/*if (mDevicesListFragment != null) {
				mDevicesListFragment.setActiveGateId(mActiveGateId);
			}*/

			if (mActiveMenuId == null) {
				mActiveMenuId = MENU_ITEM_DEVICES;
			}
		} else {
			// User has no gate
			mActiveGateId = null;
			mActiveMenuId = null;

			// UserSettings can be null when user is not logged in!
			// FIXME: Make this work, but after when everything else is finished...
			/*SharedPreferences prefs = controller.getUserSettings();
			if (prefs != null && !prefs.getBoolean(Constants.PERSISTENCE_PREF_IGNORE_NO_GATE, false)) {
				Log.d(TAG, "Call ADD GATE");
				Intent intent = new Intent(MainActivity.this, AddGateActivity.class);
				startActivity(intent);
			}*/
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Shows/hides navDrawer or notifications
	 *
	 * @param item clicked menu item
	 * @return if consumed
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				openNavDrawer();
				break;
			case R.id.main_menu_action_notification:
				// Notification
				Intent intent = new Intent(this, NotificationActivity.class);
				startActivity(intent);
				break;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ButterKnife.unbind(this);
	}

	public void logout() {
		Controller.getInstance(this).logout(false);
		Intent intent = new Intent(MainActivity.this, LoginActivity.class);
		startActivity(intent);
		this.finish();
	}

	private void doUnregisterGateTask(String gateId) {
		UnregisterGateTask unregisterGateTask = new UnregisterGateTask(this);

		unregisterGateTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					Toast.makeText(MainActivity.this, R.string.gate_detail_toast_gate_removed, Toast.LENGTH_LONG).show();
					setActiveGateAndMenu();
					redrawNavigation();
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(unregisterGateTask, gateId);
	}

	private void doSwitchGateTask(final String gateId) {
		SwitchGateTask switchGateTask = new SwitchGateTask(this, true);

		switchGateTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				mActiveGateId = gateId;
				reloadFragment();
				closeNavDrawer();
			}
		});

		callbackTaskManager.executeTask(switchGateTask, gateId);
	}

	@Override
	public void onConfirm(int confirmType, String dataId) {
		if (confirmType == ConfirmDialog.TYPE_DELETE_GATE) {
			doUnregisterGateTask(dataId);
		} else if (confirmType == ConfirmDialog.TYPE_DELETE_DEVICE) {
			Device device = Controller.getInstance(this).getDevicesModel().getDevice(mActiveGateId, dataId);
			if (device != null) {
				DevicesListFragment devicesListFragment = (DevicesListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_DEVICES);
				if (devicesListFragment != null) {
					devicesListFragment.doRemoveDeviceTask(device);
				}
			}
		}
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem menuItem) {
		boolean result = false;

		switch (menuItem.getItemId()) {
			case R.id.nav_drawer_devices: {
				if (mActiveGateId != null) {
					changeFragment(DevicesListFragment.newInstance(mActiveGateId), FRAGMENT_TAG_DEVICES);
					result = true;
				}
				break;
			}
			case R.id.nav_drawer_dashboard: {
				if (mActiveGateId != null) {
					changeFragment(DashboardPagerFragment.newInstance(mActiveGateId), FRAGMENT_TAG_DASHBOARD);
					result = true;
				}
				break;
			}
			case R.id.nav_drawer_gateway: {
				if (mActiveGateId != null) {
					Intent intent = new Intent(this, GateDetailActivity.class);
					intent.putExtra(GateDetailActivity.EXTRA_GATE_ID, mActiveGateId);
					startActivity(intent);
				}
				break;
			}
			case R.id.nav_drawer_add_gateway: {
				Intent intent = new Intent(this, AddGateActivity.class);
				startActivity(intent);
				break;
			}
			case R.id.nav_drawer_settings: {
				Intent intent = new Intent(this, SettingsMainActivity.class);
				startActivity(intent);
				break;
			}
			case R.id.nav_drawer_about: {
				showAboutDialog();
				break;
			}
			case R.id.nav_drawer_logout: {
				logout();
				break;
			}
		}

		// Close navigation drawer only when we're changing main fragment
		if (result) {
			closeNavDrawer();
		}

		return result;
	}

	public void reloadData() {
		// Reload all data, if wasn't downloaded in login activity
		final ReloadGateDataTask fullReloadTask = new ReloadGateDataTask(this, false, ReloadGateDataTask.RELOAD_GATES_AND_ACTIVE_GATE_DEVICES);
		fullReloadTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					// Redraw Activity - probably list of modules
					Log.d(TAG, "After reload task - go to redraw mainActivity");
					setActiveGateAndMenu();
					redrawNavigation();
					reloadFragment(); // FIXME: do better (only when needed)
					migrateDashboard();
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(fullReloadTask);
	}

	private void openNavDrawer() {
		if (mDrawerLayout != null)
			mDrawerLayout.openDrawer(GravityCompat.START);
	}

	private void closeNavDrawer() {
		if (mDrawerLayout != null)
			mDrawerLayout.closeDrawer(GravityCompat.START);
	}

	private void changeFragment(Fragment fragment, String tag) {
		if (tag.equals(FRAGMENT_TAG_DASHBOARD)) {
			mActiveMenuId = MENU_ITEM_GRAPHS;
		} else /*if (tag.equals(FRAGMENT_TAG_DEVICES))*/ {
			mActiveMenuId = MENU_ITEM_DEVICES;
		}

		Gate activeGate = Controller.getInstance(this).getActiveGate();
		if (activeGate == null) {
			fragment = EmptyFragment.newInstance(getString(R.string.nav_drawer_menu_no_gates)); // FIXME: Better string / data
		}

		callbackTaskManager.cancelAndRemoveAll();
		setupRefreshIcon(null);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.main_content_frame, fragment, tag)
				.commit();
	}

	private void redrawNavigation() {
		final Controller controller = Controller.getInstance(this);

		// Fill user info in the header of navigation drawer
		TextView name = ButterKnife.findById(mNavigationHeader, R.id.menu_profile_listview_name);
		TextView email = ButterKnife.findById(mNavigationHeader, R.id.menu_profile_listview_email);
		ImageView picture = ButterKnife.findById(mNavigationHeader, R.id.menu_profile_listview_icon);

		User user = controller.getActualUser();
		name.setText(user.getFullName());
		email.setText(user.getEmail());

		Bitmap bitmap = user.getPicture();
		if (bitmap == null) {
			picture.setImageResource(R.drawable.ic_person_white_48dp);
			picture.setBackgroundResource(R.drawable.oval_primary);
		} else {
			picture.setImageBitmap(bitmap);
		}

		// Fill gates list in the header of navigation drawer
		List<Gate> gates = controller.getGatesModel().getGates();
		boolean hasGates = !gates.isEmpty();

		// Reload gates adapter data
		int activeGatePos = 0;
		mGatesAdapter.clear();
		for (int i = 0; i < gates.size(); i++) {
			Gate gate = gates.get(i);
			mGatesAdapter.add(gate);

			// Remember active gate position
			if (gate.getId().equals(mActiveGateId)) {
				activeGatePos = i;
			}
		}
		mGatesAdapter.notifyDataSetChanged();
		mGatesSpinner.setSelection(activeGatePos);

		// Hide gate selection layout when there are no gates
		mNavigationHeader.findViewById(R.id.menu_profile_gates).setVisibility(hasGates ? View.VISIBLE : View.GONE);

		// Show / hide menu items based on existence of any gates
		mNavigationMenu.setGroupVisible(R.id.item_overview, hasGates);
		mNavigationMenu.findItem(R.id.item_no_gates).setVisible(!hasGates);
	}


	private void migrateDashboard() {
		Controller controller = Controller.getInstance(this);
		SharedPreferences preferences = controller.getUserSettings();
		if (preferences != null) {
			boolean isDashboardMigrated = preferences.getBoolean(Constants.PERSISTENCE_KEY_DASHBOARD_MIGRATE, false);

			if (!isDashboardMigrated) {
				controller.migrateDashboard();
				preferences.edit().putBoolean(Constants.PERSISTENCE_KEY_DASHBOARD_MIGRATE, true).apply();
			}

		}
	}
}

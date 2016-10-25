package com.rehivetech.beeeon.gui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.fragment.DashboardPagerFragment;
import com.rehivetech.beeeon.gui.fragment.DevicesListFragment;
import com.rehivetech.beeeon.gui.fragment.EmptyFragment;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.threading.task.SwitchGateTask;
import com.rehivetech.beeeon.util.Migration;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.State;
import timber.log.Timber;


public class MainActivity extends BaseApplicationActivity implements NavigationView.OnNavigationItemSelectedListener {

	private static final String CONTENT_TAG_DEVICES = "tag_devices";
	private static final String CONTENT_TAG_DASHBOARD = "tag_dashboard";
	private static final String CONTENT_TAG_EMPTY = "tag_empty";

	public static final String GATE_ID = "last_gate_id";

	/* Holds active menu and gate ids */
	@State String mActiveContentTag;
	@State String mActiveGateId;

	/* Navigation drawer items */
	@BindView(R.id.main_drawer_layout)
	DrawerLayout mDrawerLayout;
	@BindView(R.id.navigationview_layout_drawer)
	NavigationView mNavigationView;

	private View mNavigationHeader;
	private Menu mNavigationMenu;
	private Spinner mGatesSpinner;
	private ArrayAdapter<Gate> mGatesAdapter;
	private Handler mHandler = new Handler();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
		Timber.d("onCreate");
		setupDrawer();

		Controller controller = Controller.getInstance(this);

		if(savedInstanceState == null) {
			// loads user's last content fragment
			SharedPreferences userSettings = controller.getUserSettings();
			mActiveContentTag = userSettings == null ? CONTENT_TAG_DEVICES : userSettings.getString(Constants.PERSISTENCE_PREF_LAST_CONTENT_TAG, CONTENT_TAG_DEVICES);
		}

		redrawContent(null, true);
	}


	/**
	 * Setups NavDrawer with its content
	 */
	private void setupDrawer() {
		mNavigationView.setNavigationItemSelectedListener(this);
		mNavigationMenu = mNavigationView.getMenu();
		mNavigationHeader = mNavigationView.getHeaderView(0);

		mGatesAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.overlay_spinner_item);
		mGatesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mGatesSpinner = ButterKnife.findById(mNavigationHeader, R.id.menu_profile_gates_spinner);
		mGatesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			private boolean firstSelect = true;


			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// Ignore first select after creation (which is automatic - not selected by user)
				if(firstSelect) {
					firstSelect = false;
					return;
				}

				Gate gate = (Gate) parent.getItemAtPosition(position);
				if(gate == null) return;

				// TODO: Don't switch gate if this is first "start" (on creating activity)
				doSwitchGateTask(gate.getId());
			}


			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		mGatesSpinner.setAdapter(mGatesAdapter);
	}


	@Override
	protected void onStart() {
		super.onStart();
		Timber.d("onStart");
		doReloadAllData();
	}


	/**
	 * Reloads all data user has (in case we don't load them in LoginActivity)
	 */
	public void doReloadAllData() {
		final ReloadGateDataTask fullReloadTask = new ReloadGateDataTask(this, false, ReloadGateDataTask.RELOAD_GATES_AND_ACTIVE_GATE_DEVICES);
		fullReloadTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if(success) {
					// Redraw Activity - probably list of modules
					Timber.d("After reload task -> redraw mainActivity");
					redrawContent(null, true);
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(fullReloadTask);
	}


	/**
	 * Switching among gates user has. Reloads actually shown page
	 *
	 * @param gateId of gate which should be shown
	 */
	private void doSwitchGateTask(final String gateId) {
		SwitchGateTask switchGateTask = new SwitchGateTask(this, true);

		switchGateTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				drawerCloseAsync();
				redrawContent(null, false);
			}
		});

		callbackTaskManager.executeTask(switchGateTask, gateId);
	}


	/**
	 * Replaces fragment for specified one by its tag. If no active gate found, shows empty fragment
	 * FIXME: do better (only when needed)
	 *
	 * @param newContentTag this specifies which fragment should be shown
	 */
	private void redrawContent(@Nullable String newContentTag, boolean shouldRedrawDrawer) {
		Timber.d("redrawContent");

		String activeGateId = null;

		if(newContentTag == null) newContentTag = mActiveContentTag;
		Controller controller = Controller.getInstance(this);

		Gate activeGate = controller.getActiveGate();
		if (activeGate != null) {
			activeGateId = activeGate.getId();
		} else {
			newContentTag = CONTENT_TAG_EMPTY;
		}

		if(shouldRedrawDrawer) {
			redrawNavDrawer(activeGateId);
		}

		// if the same page and same gate skips instantiating new fragment
		if(activeGateId != null && activeGateId.equals(mActiveGateId) && mActiveContentTag.equals(newContentTag)) {
			// FIXME this is not proper way to prevent multiple restarts of fragment. Existing fragment might be updated by some method in it
//			getSupportFragmentManager().findFragmentByTag(newContentTag);
			Timber.i("Skipping reloading fragment");
			return;
		}

		Fragment fragment;
		switch(newContentTag) {
			case CONTENT_TAG_DASHBOARD:
				Migration.Dashboard.migrate(MainActivity.this);
				mNavigationView.setCheckedItem(R.id.nav_drawer_dashboard);
				fragment = DashboardPagerFragment.newInstance(activeGateId);
				break;

			case CONTENT_TAG_DEVICES:
				mNavigationView.setCheckedItem(R.id.nav_drawer_devices);
				fragment = DevicesListFragment.newInstance(activeGateId);
				break;
			default:
				fragment = EmptyFragment.newInstance(getString(R.string.nav_drawer_menu_no_gates));
				break;

		}

		fragmentReplace(fragment, newContentTag);

		SharedPreferences userSettings = controller.getUserSettings();
		if(userSettings != null) {
			userSettings.edit().putString(Constants.PERSISTENCE_PREF_LAST_CONTENT_TAG, newContentTag).apply();
		}
		mActiveContentTag = newContentTag;
		mActiveGateId = activeGateId;
	}


	/**
	 * Redraws nav drawer with actual data
	 *
	 * @param actualGateId id of active gate
	 */
	private void redrawNavDrawer(@Nullable String actualGateId) {
		Controller controller = Controller.getInstance(this);

		// Fill user info in the header of navigation drawer
		TextView name = ButterKnife.findById(mNavigationHeader, R.id.menu_profile_listview_name);
		TextView email = ButterKnife.findById(mNavigationHeader, R.id.menu_profile_listview_email);
		ImageView pictureView = ButterKnife.findById(mNavigationHeader, R.id.menu_profile_listview_icon);

		User user = controller.getActualUser();
		name.setText(user.getFullName());
		email.setText(user.getEmail());
		// asynchronously loads user picture by Picasso
		user.loadPicture(this, pictureView);

		// Fill gates list in the header of navigation drawer
		List<Gate> gates = controller.getGatesModel().getGates();
		boolean hasGates = !gates.isEmpty();

		// Reload gates adapter data
		int activeGatePos = 0;
		mGatesAdapter.clear();
		for(int i = 0; i < gates.size(); i++) {
			Gate gate = gates.get(i);
			mGatesAdapter.add(gate);

			// Remember active gate position
			if(gate.getId().equals(actualGateId)) {
				activeGatePos = i;
			}
		}
		mGatesAdapter.notifyDataSetChanged();
		mGatesSpinner.setSelection(activeGatePos);

		// Hide gate selection layout when there are no gates
		ButterKnife.findById(mNavigationHeader, R.id.menu_profile_gates).setVisibility(hasGates ? View.VISIBLE : View.GONE);

		// Show / hide menu items based on existence of any gates
		mNavigationMenu.setGroupVisible(R.id.item_overview, hasGates);
		mNavigationMenu.findItem(R.id.item_no_gates).setVisible(!hasGates);
	}


	/**
	 * Back pressing will close navDrawer if opened, otherwise usual functionality
	 */
	@Override
	public void onBackPressed() {
		if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
			mDrawerLayout.closeDrawer(GravityCompat.START);
			return;
		}
		super.onBackPressed();
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
		switch(item.getItemId()) {
			case android.R.id.home:
				mDrawerLayout.openDrawer(GravityCompat.START);
				return true;

			case R.id.main_menu_action_notification:
				// Notification
				Intent intent = new Intent(this, NotificationActivity.class);
				startActivity(intent);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


	/**
	 * On clicking in navigation drawer list
	 *
	 * @param menuItem clicked item
	 * @return if item should stay selected
	 */
	@Override
	public boolean onNavigationItemSelected(MenuItem menuItem) {
		boolean shouldCloseDrawer = false;
		boolean shouldStaySelected = false;

		switch(menuItem.getItemId()) {
			case R.id.nav_drawer_devices:
				redrawContent(CONTENT_TAG_DEVICES, false);
				shouldCloseDrawer = true;
				shouldStaySelected = true;
				break;

			case R.id.nav_drawer_dashboard:
				redrawContent(CONTENT_TAG_DASHBOARD, false);
				shouldCloseDrawer = true;
				shouldStaySelected = true;
				break;

			case R.id.nav_drawer_gateway:
				String gateId = Controller.getInstance(this).getActiveGateId();
				showGateActivity(gateId);
				shouldCloseDrawer = true;
				break;

			case R.id.nav_drawer_add_gateway:
				startActivity(new Intent(this, AddGateActivity.class));
				break;

			case R.id.nav_drawer_settings:
				startActivity(new Intent(this, SettingsMainActivity.class));
				break;

			case R.id.nav_drawer_about:
				showAboutDialog();
				break;

			case R.id.nav_drawer_logout:
				logout();
				break;
		}

		if(shouldCloseDrawer) {
			drawerCloseAsync();
		}

		return shouldStaySelected;
	}


	/**
	 * Handles closing nav drawer asynchronously because hard ui replacing.
	 * Opening is not needed asynchronously
	 * FIXME should be fixed when dashboard fragment loads quickly
	 */
	private void drawerCloseAsync() {
		// closing nav drawer this way because otherwise when closing immediatelly with replacing fragment causes lags
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mDrawerLayout.closeDrawer(GravityCompat.START);
			}
		}, 50);
	}


	/**
	 * Shows gate activity
	 *
	 * @param gateId specifies which gate should be shown
	 */
	private void showGateActivity(String gateId) {
		Intent intent = new Intent(this, GateDetailActivity.class);
		intent.putExtra(GateDetailActivity.EXTRA_GATE_ID, gateId);
		startActivity(intent);
	}
}

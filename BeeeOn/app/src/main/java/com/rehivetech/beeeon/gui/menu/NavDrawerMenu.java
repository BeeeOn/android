package com.rehivetech.beeeon.gui.menu;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.GateDetailActivity;
import com.rehivetech.beeeon.gui.activity.GateEditActivity;
import com.rehivetech.beeeon.gui.activity.GateUsersActivity;
import com.rehivetech.beeeon.gui.activity.MainActivity;
import com.rehivetech.beeeon.gui.activity.SettingsMainActivity;
import com.rehivetech.beeeon.gui.adapter.MenuListAdapter;
import com.rehivetech.beeeon.gui.dialog.ConfirmDialog;
import com.rehivetech.beeeon.gui.dialog.InfoDialogFragment;
import com.rehivetech.beeeon.gui.menuItem.EmptyMenuItem;
import com.rehivetech.beeeon.gui.menuItem.GateMenuItem;
import com.rehivetech.beeeon.gui.menuItem.GroupMenuItem;
import com.rehivetech.beeeon.gui.menuItem.IMenuItem;
import com.rehivetech.beeeon.gui.menuItem.LocationMenuItem;
import com.rehivetech.beeeon.gui.menuItem.ProfileMenuItem;
import com.rehivetech.beeeon.gui.menuItem.SeparatorMenuItem;
import com.rehivetech.beeeon.gui.menuItem.SettingMenuItem;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.threading.CallbackTask.ICallbackTaskListener;
import com.rehivetech.beeeon.threading.task.SwitchGateTask;
import com.rehivetech.beeeon.util.Log;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static android.support.v7.view.ActionMode.Callback;

public class NavDrawerMenu {
	private static final String TAG = "NavDrawerMenu";
	private final static String TAG_INFO = "tag_info";
	private final Toolbar mToolbar;

	private MainActivity mActivity;

	private DrawerLayout mDrawerLayout;
	private StickyListHeadersListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private String mDrawerTitle = "BeeeOn";

	private String mActiveItem;
	private String mActiveGateId;

	private MenuListAdapter mMenuAdapter;

	//
	private ActionMode mMode;
	private IMenuItem mSelectedMenuItem;
	private NavigationView mNavigationView;


	public NavDrawerMenu(MainActivity activity, Toolbar toolbar) {
		// Set activity
		mActivity = activity;
		mToolbar = toolbar;

		// Get GUI element for menu
		getGUIElements();
		// Set all of listener for (listview) menu items
		settingsMenu();
	}

	private void getGUIElements() {
		// Locate DrawerLayout in activity_location_screen.xml
		mDrawerLayout = (DrawerLayout) mActivity.findViewById(R.id.drawer_layout);
		// Locate ListView in activity_location_screen.xml
		mDrawerList = (StickyListHeadersListView) mActivity.findViewById(R.id.listview_drawer);
		// Locate navigationView layout
		mNavigationView = (NavigationView) mActivity.findViewById(R.id.relative_layout_drawer);
	}

	private void settingsMenu() {
		// Capture listview menu item click
		mDrawerList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mSelectedMenuItem = (IMenuItem) mMenuAdapter.getItem(position);
				Gate gate = Controller.getInstance(mActivity).getActiveGate();
				switch (mSelectedMenuItem.getType()) {
					case GATE:
						if (gate == null)
							break;
						// if it is not chosen, switch to selected gate
						if (!gate.getId().equals(mSelectedMenuItem.getId())) {
							doSwitchGateTask(mSelectedMenuItem.getId());

						}
						break;
					case LOCATION:
						// Get the title followed by the position
						if (gate != null) {
							changeMenuItem(mSelectedMenuItem.getId(), true);
							redrawMenu();
						}
						break;

					case SETTING:
						if (mSelectedMenuItem.getId().equals(IMenuItem.ID_ABOUT)) {
							InfoDialogFragment dialog = new InfoDialogFragment();
							dialog.show(mActivity.getSupportFragmentManager(), TAG_INFO);
						} else if (mSelectedMenuItem.getId().equals(IMenuItem.ID_SETTINGS)) {
							Intent intent = new Intent(mActivity, SettingsMainActivity.class);
							mActivity.startActivity(intent);
						} else if (mSelectedMenuItem.getId().equals(IMenuItem.ID_LOGOUT)) {
							mActivity.logout();
						}
						break;

					default:
						Log.d(TAG, "other");
						break;
				}
			}
		});

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				// Set the title on the action when drawer closed
				Gate gate = Controller.getInstance(mActivity).getActiveGate();

				if (gate != null && mActiveItem != null) {

					ActionBar actionBar = mActivity.getSupportActionBar();
					if (actionBar != null) {
						switch (mActiveItem) {
							case Constants.GUI_MENU_CONTROL:
								actionBar.setTitle(mActivity.getString(R.string.menu_devices));
								break;
							case Constants.GUI_MENU_DASHBOARD:
								actionBar.setTitle(mActivity.getString(R.string.menu_charts));
								break;
							case Constants.GUI_MENU_WATCHDOG:
								actionBar.setTitle(mActivity.getString(R.string.menu_watchdog));
								break;
						}
					}
				} else {
					setDefaultTitle();
				}
				super.onDrawerClosed(view);
				if (mMode != null)
					mMode.finish();
			}

			public void onDrawerOpened(View drawerView) {
				// Set the title on the action when drawer open
				if (mActivity.getSupportActionBar() != null)
					mActivity.getSupportActionBar().setTitle(mDrawerTitle);
				super.onDrawerOpened(drawerView);
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerToggle.syncState();

		openMenu();
	}

	public void openMenu() {
		mDrawerLayout.openDrawer(mNavigationView);
	}

	public void closeMenu() {
		mDrawerLayout.closeDrawer(mNavigationView);
	}

	public boolean isMenuOpened() {
		return mDrawerLayout.isDrawerVisible(mNavigationView);
	}

	public void redrawMenu() {
		mMenuAdapter = getMenuAdapter();
		mDrawerList.setAdapter(mMenuAdapter);

		Gate gate = Controller.getInstance(mActivity).getActiveGate();

		if (gate != null && mActiveItem != null) {
			if (mActivity.getSupportActionBar() != null) {
				switch (mActiveItem) {
					case Constants.GUI_MENU_CONTROL:
						mActivity.getSupportActionBar().setTitle(mActivity.getString(R.string.menu_devices));
						break;
					case Constants.GUI_MENU_DASHBOARD:
						mActivity.getSupportActionBar().setTitle(mActivity.getString(R.string.menu_charts));
						break;
					case Constants.GUI_MENU_WATCHDOG:
						mActivity.getSupportActionBar().setTitle(mActivity.getString(R.string.menu_watchdog));
						break;
				}
			}
		} else {
			setDefaultTitle();
		}

		if (mMode != null)
			mMode.finish();
	}

	private void changeMenuItem(String ID, boolean closeDrawer) {
		mActiveItem = ID;
		// TODO
		mActivity.setActiveGateId(mActiveGateId);
		mActivity.setActiveMenuID(mActiveItem);
		mActivity.redrawMainFragment();

		// Close drawer
		if (closeDrawer) {
			closeMenu();
		}
	}

	private void doSwitchGateTask(String gateId) {
		SwitchGateTask switchGateTask = new SwitchGateTask(mActivity, false);

		switchGateTask.setListener(new ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (success) {
					mActivity.setActiveGateAndMenu();
					mActivity.redrawMainFragment();
					redrawMenu();
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(switchGateTask, gateId);
	}

	public MenuListAdapter getMenuAdapter() {
		mMenuAdapter = new MenuListAdapter(mActivity);
		Controller controller = Controller.getInstance(mActivity);
		// Adding profile header
		User actUser = controller.getActualUser();

		mMenuAdapter.addHeader(new ProfileMenuItem(actUser.getFullName(), actUser.getEmail(), actUser.getPicture(), null));

		List<Gate> gates = controller.getGatesModel().getGates();


		// Adding separator as item (we don't want to let it float as header)
		mMenuAdapter.addItem(new SeparatorMenuItem());

		mMenuAdapter.addHeader(new GroupMenuItem(mActivity.getResources().getString(R.string.gate)));

		if (!gates.isEmpty()) {
			Gate activeGate = controller.getActiveGate();
			if (activeGate == null)
				return mMenuAdapter;
			// Adding gates
			for (Gate actGate : gates) {
				mMenuAdapter.addItem(new GateMenuItem(actGate.getName(), actGate.getRole().getStringResource(), activeGate.getId().equals(actGate.getId()), actGate.getId()));
			}

			// Adding separator as item (we don't want to let it float as header)
			mMenuAdapter.addItem(new SeparatorMenuItem());

			// OVERVIEW
			mMenuAdapter.addHeader(new GroupMenuItem(mActivity.getString(R.string.menu_household)));
			mMenuAdapter.addItem(new LocationMenuItem(mActivity.getString(R.string.menu_devices), R.drawable.ic_menu_overview, R.drawable.ic_menu_overview_active, false, Constants.GUI_MENU_CONTROL, (mActiveItem == null) || mActiveItem.equals(Constants.GUI_MENU_CONTROL)));
			mMenuAdapter.addItem(new LocationMenuItem(mActivity.getString(R.string.menu_charts), R.drawable.ic_menu_dashboard, R.drawable.ic_menu_dashboard_active, false, Constants.GUI_MENU_DASHBOARD, (mActiveItem != null) && mActiveItem.equals(Constants.GUI_MENU_DASHBOARD)));
			mMenuAdapter.addItem(new LocationMenuItem(mActivity.getString(R.string.menu_watchdog), R.drawable.ic_menu_watchdog, R.drawable.ic_menu_watchdog_active, false, Constants.GUI_MENU_WATCHDOG, (mActiveItem != null) && mActiveItem.equals(Constants.GUI_MENU_WATCHDOG)));

			mMenuAdapter.addItem(new SeparatorMenuItem());
			// MANAGMENT
			mMenuAdapter.addHeader(new GroupMenuItem(mActivity.getString(R.string.menu_management)));
			mMenuAdapter.addItem(new LocationMenuItem(mActivity.getString(R.string.menu_gate), R.drawable.ic_router_gray_24dp, R.drawable.ic_router_active_24dp, false, Constants.GUI_MENU_GATEWAY, (mActiveItem != null) && mActiveItem.equals(Constants.GUI_MENU_GATEWAY)));

		} else {
			mMenuAdapter.addItem(new EmptyMenuItem(mActivity.getString(R.string.no_gates)));
		}

		// Adding separator as header
		mMenuAdapter.addItem(new SeparatorMenuItem());

		// Adding settings, about etc.
		mMenuAdapter.addItem(new SettingMenuItem(mActivity.getString(R.string.action_settings), IMenuItem.ID_SETTINGS));
		mMenuAdapter.addItem(new SettingMenuItem(mActivity.getString(R.string.action_about), IMenuItem.ID_ABOUT));
		mMenuAdapter.addItem(new SettingMenuItem(mActivity.getString(R.string.action_logout), IMenuItem.ID_LOGOUT));
		return mMenuAdapter;
	}

	public void onConfigurationChanged(Configuration newConfig) {
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	public void setDefaultTitle() {
		mDrawerTitle = "BeeeOn";
	}

	public void setActiveMenuID(String id) {
		mActiveItem = id;
	}

	public void setGateId(String adaID) {
		mActiveGateId = adaID;
	}

	class ActionModeGates implements Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.gate_menu, menu);
			Controller controller = Controller.getInstance(mActivity);

			if (!controller.isUserAllowed(controller.getGatesModel().getGate(mSelectedMenuItem.getId()).getRole())) {
				menu.getItem(0).setVisible(false);// EDIT
				menu.getItem(1).setVisible(false);// USERS
			}
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, android.view.MenuItem item) {
			String title;
			Log.d(TAG, "ActionMode Gate - item id: " + item.getItemId());
			if (item.getItemId() == R.id.ada_menu_del) { // UNREGIST GATE
				Controller controller = Controller.getInstance(mActivity);
				Gate gate = controller.getGatesModel().getGate(mSelectedMenuItem.getId());
				if (gate == null) {
					title = mActivity.getString(R.string.confirm_remove_gate_title_default);
				} else {
					title = mActivity.getString(R.string.confirm_remove_title, gate.getName());
				}

				String message = mActivity.getString(R.string.confirm_remove_gate_message);
				ConfirmDialog.confirm(mActivity, title, message, R.string.button_remove, ConfirmDialog.TYPE_DELETE_GATE, mSelectedMenuItem.getId());

			} else if (item.getItemId() == R.id.ada_menu_users) { // GO TO USERS OF GATE
				Intent intent = new Intent(mActivity, GateUsersActivity.class);
				intent.putExtra(GateUsersActivity.EXTRA_GATE_ID, mSelectedMenuItem.getId());
				mActivity.startActivity(intent);

			} else if (item.getItemId() == R.id.ada_menu_edit) { // RENAME GATE
				Intent intent = new Intent(mActivity, GateEditActivity.class);
				intent.putExtra(GateEditActivity.EXTRA_GATE_ID, mSelectedMenuItem.getId());
				mActivity.startActivity(intent);
			} else if (item.getItemId() == R.id.ada_menu_details) {
				Intent intent = new Intent(mActivity, GateDetailActivity.class);
				intent.putExtra(GateDetailActivity.EXTRA_GATE_ID, mSelectedMenuItem.getId());
				mActivity.startActivity(intent);
			}


			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mMode = null;
			mSelectedMenuItem.setNotSelected();
		}


	}
}

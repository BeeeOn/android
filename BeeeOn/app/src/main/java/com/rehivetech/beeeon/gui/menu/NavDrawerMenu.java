package com.rehivetech.beeeon.gui.menu;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.GateUsersActivity;
import com.rehivetech.beeeon.gui.activity.MainActivity;
import com.rehivetech.beeeon.gui.activity.SettingsMainActivity;
import com.rehivetech.beeeon.gui.adapter.MenuListAdapter;
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
import com.rehivetech.beeeon.threading.task.UnregisterGateTask;
import com.rehivetech.beeeon.util.Log;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static android.support.v7.view.ActionMode.Callback;

public class NavDrawerMenu {
	private static final String TAG = "NavDrawerMenu";
	private final static String TAG_INFO = "tag_info";
	private final Toolbar mToolbar;

	private MainActivity mActivity;
	private Controller mController;

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
	private RelativeLayout mDrawerRelLay;

	public NavDrawerMenu(MainActivity activity, Toolbar toolbar) {
		// Set activity
		mActivity = activity;
		mToolbar = toolbar;

		// Get controller
		mController = Controller.getInstance(mActivity);

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
		// Locate relative layout
		mDrawerRelLay = (RelativeLayout) mActivity.findViewById(R.id.relative_layout_drawer);
	}

	private void settingsMenu() {
		// Capture listview menu item click
		mDrawerList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mSelectedMenuItem = (IMenuItem) mMenuAdapter.getItem(position);
				Gate gate = mController.getActiveGate();
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
		mDrawerList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (mMode != null) {
					// Action Mode is active and I want change
					mSelectedMenuItem.setNotSelected();
					mMode = null;
				}

				Log.d(TAG, "Item Long press");
				mSelectedMenuItem = (IMenuItem) mMenuAdapter.getItem(position);
				switch (mSelectedMenuItem.getType()) {
					case LOCATION:

						break;
					case GATE:
						Log.i(TAG, "Long press - gate");
						mMode = mActivity.startSupportActionMode(new ActionModeGates());
						mSelectedMenuItem.setIsSelected();
						break;
					default:
						// do nothing
						break;
				}
				return true;
			}

		});

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				// Set the title on the action when drawer closed
				Gate gate = mController.getActiveGate();

				if (gate != null && mActiveItem != null) {
					if (mActiveItem.equals(Constants.GUI_MENU_CONTROL)) {
						mActivity.getSupportActionBar().setTitle(mActivity.getString(R.string.menu_control));
					} else if (mActiveItem.equals(Constants.GUI_MENU_DASHBOARD)) {
						mActivity.getSupportActionBar().setTitle(mActivity.getString(R.string.menu_dashboard));
					} else if (mActiveItem.equals(Constants.GUI_MENU_WATCHDOG)) {
						mActivity.getSupportActionBar().setTitle(mActivity.getString(R.string.menu_watchdog));
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
				mActivity.getSupportActionBar().setTitle(mDrawerTitle);
				super.onDrawerOpened(drawerView);
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerToggle.syncState();

		openMenu();
	}

	public void openMenu() {
		mDrawerLayout.openDrawer(mDrawerRelLay);
	}

	public void closeMenu() {
		mDrawerLayout.closeDrawer(mDrawerRelLay);
	}

	public boolean isMenuOpened() {
		return mDrawerLayout.isDrawerVisible(mDrawerRelLay);
	}

	public void redrawMenu() {
		mMenuAdapter = getMenuAdapter();
		mDrawerList.setAdapter(mMenuAdapter);

		Gate gate = mController.getActiveGate();

		if (gate != null && mActiveItem != null) {
			if (mActiveItem.equals(Constants.GUI_MENU_CONTROL)) {
				mActivity.getSupportActionBar().setTitle(mActivity.getString(R.string.menu_control));
			} else if (mActiveItem.equals(Constants.GUI_MENU_DASHBOARD)) {
				mActivity.getSupportActionBar().setTitle(mActivity.getString(R.string.menu_dashboard));
			} else if (mActiveItem.equals(Constants.GUI_MENU_WATCHDOG)) {
				mActivity.getSupportActionBar().setTitle(mActivity.getString(R.string.menu_watchdog));
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

	private void doUnregisterGateTask(String gateId) {
		UnregisterGateTask unregisterGateTask = new UnregisterGateTask(mActivity);

		unregisterGateTask.setListener(new ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (success) {
					Toast.makeText(mActivity, R.string.toast_gate_removed, Toast.LENGTH_LONG).show();
					mActivity.setActiveGateAndMenu();
					mActivity.redraw();
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(unregisterGateTask, gateId);
	}

	public MenuListAdapter getMenuAdapter() {
		mMenuAdapter = new MenuListAdapter(mActivity);

		// Adding profile header
		User actUser = mController.getActualUser();

		Bitmap picture = actUser.getPicture();
		if (picture == null)
			picture = actUser.getDefaultPicture(mActivity);

		mMenuAdapter.addHeader(new ProfileMenuItem(actUser.getFullName(), actUser.getEmail(), picture, new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeMenuItem(Constants.GUI_MENU_PROFILE, true);
			}
		}));

		List<Gate> gates = mController.getGatesModel().getGates();


		// Adding separator as item (we don't want to let it float as header)
		mMenuAdapter.addItem(new SeparatorMenuItem());

		mMenuAdapter.addHeader(new GroupMenuItem(mActivity.getResources().getString(R.string.gate)));

		if (!gates.isEmpty()) {
			Gate activeGate = mController.getActiveGate();
			if (activeGate == null)
				return mMenuAdapter;
			// Adding gates
			for (Gate actGate : gates) {
				mMenuAdapter.addItem(new GateMenuItem(actGate.getName(), actGate.getRole().getStringResource(), activeGate.getId().equals(actGate.getId()), actGate.getId()));
			}

			// Adding separator as item (we don't want to let it float as header)
			mMenuAdapter.addItem(new SeparatorMenuItem());


			// MANAGMENT
			mMenuAdapter.addHeader(new GroupMenuItem(mActivity.getResources().getString(R.string.menu_managment)));
			mMenuAdapter.addItem(new LocationMenuItem(mActivity.getString(R.string.menu_control), R.drawable.ic_overview, false, Constants.GUI_MENU_CONTROL, (mActiveItem != null) ? mActiveItem.equals(Constants.GUI_MENU_CONTROL) : true));
			mMenuAdapter.addItem(new LocationMenuItem(mActivity.getString(R.string.menu_dashboard), R.drawable.ic_dashboard, false, Constants.GUI_MENU_DASHBOARD, (mActiveItem != null) ? mActiveItem.equals(Constants.GUI_MENU_DASHBOARD) : false));

			mMenuAdapter.addItem(new SeparatorMenuItem());
			// APPLICATIONS
			mMenuAdapter.addHeader(new GroupMenuItem(mActivity.getResources().getString(R.string.menu_applications)));
			mMenuAdapter.addItem(new LocationMenuItem(mActivity.getString(R.string.menu_watchdog), R.drawable.ic_app_watchdog, false, Constants.GUI_MENU_WATCHDOG, (mActiveItem != null) ? mActiveItem.equals(Constants.GUI_MENU_WATCHDOG) : false));

		} else {
			mMenuAdapter.addItem(new EmptyMenuItem(mActivity.getResources().getString(R.string.no_gates)));

		}

		// Adding separator as header
		mMenuAdapter.addItem(new SeparatorMenuItem());

		// Adding settings, about etc.
		mMenuAdapter.addItem(new SettingMenuItem(mActivity.getResources().getString(R.string.action_settings), R.drawable.settings, IMenuItem.ID_SETTINGS));
		mMenuAdapter.addItem(new SettingMenuItem(mActivity.getResources().getString(R.string.action_about), R.drawable.info, IMenuItem.ID_ABOUT));
		mMenuAdapter.addItem(new SettingMenuItem(mActivity.getString(R.string.action_logout), R.drawable.logout, IMenuItem.ID_LOGOUT));
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

			if (!mController.isUserAllowed(mController.getGatesModel().getGate(mSelectedMenuItem.getId()).getRole())) {
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
			Log.d(TAG, "ActionMode Gate - item id: " + item.getItemId());
			if (item.getItemId() == R.id.ada_menu_del) { // UNREGIST GATE
				doUnregisterGateTask(mSelectedMenuItem.getId());
			} else if (item.getItemId() == R.id.ada_menu_users) { // GO TO USERS OF GATE
				Intent intent = new Intent(mActivity, GateUsersActivity.class);
				intent.putExtra(Constants.GUI_SELECTED_GATE_ID, mSelectedMenuItem.getId());
				mActivity.startActivity(intent);

			} else if (item.getItemId() == R.id.ada_menu_edit) { // RENAME GATE
				Toast.makeText(mActivity, R.string.toast_not_implemented, Toast.LENGTH_LONG).show();
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

package com.rehivetech.beeeon.menu;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.AdapterUsersActivity;
import com.rehivetech.beeeon.activity.AddAdapterActivity;
import com.rehivetech.beeeon.activity.MainActivity;
import com.rehivetech.beeeon.activity.SettingsMainActivity;
import com.rehivetech.beeeon.activity.dialog.InfoDialogFragment;
import com.rehivetech.beeeon.activity.menuItem.AdapterMenuItem;
import com.rehivetech.beeeon.activity.menuItem.CustomViewMenuItem;
import com.rehivetech.beeeon.activity.menuItem.EmptyMenuItem;
import com.rehivetech.beeeon.activity.menuItem.GroupImageMenuItem;
import com.rehivetech.beeeon.activity.menuItem.LocationMenuItem;
import com.rehivetech.beeeon.activity.menuItem.MenuItem;
import com.rehivetech.beeeon.activity.menuItem.ProfileMenuItem;
import com.rehivetech.beeeon.activity.menuItem.SeparatorMenuItem;
import com.rehivetech.beeeon.activity.menuItem.SettingMenuItem;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.location.Location;
import com.rehivetech.beeeon.arrayadapter.MenuListAdapter;
import com.rehivetech.beeeon.asynctask.CallbackTask.CallbackTaskListener;
import com.rehivetech.beeeon.asynctask.SwitchAdapterTask;
import com.rehivetech.beeeon.asynctask.UnregisterAdapterTask;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.ActualUser;
import com.rehivetech.beeeon.persistence.Persistence;
import com.rehivetech.beeeon.thread.ToastMessageThread;
import com.rehivetech.beeeon.util.Log;

public class NavDrawerMenu {
	private static final String TAG = "NavDrawerMenu";
	private final static String TAG_INFO = "tag_info";

	private MainActivity mActivity;
	private Controller mController;

	private DrawerLayout mDrawerLayout;
	private StickyListHeadersListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private String mDrawerTitle = "BeeeOn";

	private String mActiveLocationId;
	private String mActiveCustomViewId;
	private String mActiveAdapterId;
	private String mAdaterIdUnregist;

	private boolean mIsDrawerOpen;

	private boolean backPressed = false;

	private SwitchAdapterTask mSwitchAdapterTask;
	private UnregisterAdapterTask mUnregisterAdapterTask;

	private MenuListAdapter mMenuAdapter;

	//
	private ActionMode mMode;
	private MenuItem mSelectedMenuItem;

	public NavDrawerMenu(MainActivity activity) {
		// Set activity
		mActivity = activity;

		backPressed = mActivity.getBackPressed();
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
	}

	private void settingsMenu() {
		// Set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		mDrawerLayout.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
					backPressed = ((MainActivity) mActivity).getBackPressed();
					Log.d(TAG, "BackPressed = " + String.valueOf(backPressed));
					if (mDrawerLayout == null) {
						return false;
					}
					if (mDrawerLayout.isDrawerOpen(mDrawerList) && !backPressed) {
						firstTapBack();
						return true;
					} else if (mDrawerLayout.isDrawerOpen(mDrawerList) && backPressed) {
						secondTapBack();
						return true;
					}
				}
				return false;
			}
		});

		// Capture listview menu item click
		mDrawerList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mSelectedMenuItem = (MenuItem) mMenuAdapter.getItem(position);
				switch (mSelectedMenuItem.getType()) {
				case ADAPTER:
					// if it is not chosen, switch to selected adapter
					if (!mController.getActiveAdapter().getId().equals(mSelectedMenuItem.getId())) {
						doSwitchAdapter(mSelectedMenuItem.getId());
					}
					break;

				case CUSTOM_VIEW:
					// TODO: otevrit custom view, jeste nedelame s customView, takze pozdeji

					// TEMP
					mActiveLocationId = null;
					mActiveCustomViewId = mSelectedMenuItem.getId();

					changeCustomView(true);
					redrawMenu();
					break;

				case SETTING:
					if (mSelectedMenuItem.getId().equals(com.rehivetech.beeeon.activity.menuItem.MenuItem.ID_ABOUT)) {
						InfoDialogFragment dialog = new InfoDialogFragment();
						dialog.show(mActivity.getSupportFragmentManager(), TAG_INFO);
					} else if (mSelectedMenuItem.getId().equals(com.rehivetech.beeeon.activity.menuItem.MenuItem.ID_SETTINGS)) {
						Intent intent = new Intent(mActivity, SettingsMainActivity.class);
						mActivity.startActivity(intent);
					}
					break;

				case LOCATION:
					// Get the title followed by the position
					Adapter adapter = mController.getActiveAdapter();
					if (adapter != null) {
						mActiveCustomViewId = null;
						changeLocation(mController.getLocation(adapter.getId(), mSelectedMenuItem.getId()), true);
						redrawMenu();
					}
					break;

				default:
					break;
				}
			}
		});
		mDrawerList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if(mMode != null) {
					// Action Mode is active and I want change
					mSelectedMenuItem.setNotSelected();
					mMode = null;
				}
				
				Log.d(TAG, "Item Long press");
				mSelectedMenuItem = (MenuItem) mMenuAdapter.getItem(position);
				switch (mSelectedMenuItem.getType()) {
				case LOCATION:
					if(!mSelectedMenuItem.getId().equals(Constants.GUI_MENU_ALL_SENSOR_ID))
						mMode = mActivity.startActionMode(new ActionModeLocations());
					break;
				case ADAPTER:
					Log.i(TAG, "Long press - adapter");
					//mAdaterIdUnregist = item.getId();
					mMode = mActivity.startActionMode(new ActionModeAdapters());
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
		mDrawerToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				backPressed = mActivity.getBackPressed();
				if (backPressed)
					mActivity.onBackPressed();
				// Set the title on the action when drawer closed
				Adapter adapter = mController.getActiveAdapter();
				Location location = null;
				if(adapter != null && mActiveLocationId!= null) {
					location = mController.getLocation(adapter.getId(), mActiveLocationId);
				}
				if (adapter != null && location != null) {
					mActivity.getSupportActionBar().setTitle(location.getName());
				} else {
					setDefaultTitle();
				}
				super.onDrawerClosed(view);
				Log.d(TAG, "BackPressed - onDrawerClosed " + String.valueOf(backPressed));
				mIsDrawerOpen = false;
				finishActinMode();
			}

			public void onDrawerOpened(View drawerView) {
				// Set the title on the action when drawer open
				mActivity.getSupportActionBar().setTitle(mDrawerTitle);
				super.onDrawerOpened(drawerView);
				mIsDrawerOpen = true;
				// backPressed = true;
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerToggle.syncState();

		// Enable ActionBar app icon to behave as action to toggle nav drawer
		mActivity.getSupportActionBar().setHomeButtonEnabled(true);
		mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mActivity.setSupportProgressBarIndeterminateVisibility(false);

		openMenu();
	}

	public void openMenu() {
		mIsDrawerOpen = true;
		mDrawerLayout.openDrawer(mDrawerList);
	}

	public void closeMenu() {
		mIsDrawerOpen = false;
		mDrawerLayout.closeDrawer(mDrawerList);
		if (mMode != null) {
			mMode.finish();
		}
	}

	public void redrawMenu() {
		mMenuAdapter = getMenuAdapter();
		mDrawerList.setAdapter(mMenuAdapter);
		
		Adapter adapter = mController.getActiveAdapter();
		Location location = null;
		if(adapter != null && mActiveLocationId!= null) {
			location = mController.getLocation(adapter.getId(), mActiveLocationId);
		}
		if (adapter != null && location != null) {
			mActivity.getSupportActionBar().setTitle(location.getName());
		}
		else if (adapter != null && mActiveLocationId == Constants.GUI_MENU_ALL_SENSOR_ID){ // All sensors
			mActivity.getSupportActionBar().setTitle("All sensors");
		} else {
			setDefaultTitle();
		}

		if (!mIsDrawerOpen) {
			// Close drawer
			closeMenu();
			Log.d(TAG, "LifeCycle: onOrientation");
		}
	}

	protected void changeCustomView(boolean closeDrawer) { // (CustomView customView, boolean closeDrawer){
		// ((MainActivity) mActivity).setActiveCustomView(param);
		mActivity.setActiveCustomViewID("tempValeu");
		mActivity.redrawCustomView();

		// Close drawer
		if (closeDrawer) {
			closeMenu();
		}
	}

	private void changeLocation(Location location, boolean closeDrawer) {
		// save current location
		SharedPreferences prefs = mController.getUserSettings();

		// UserSettings can be null when user is not logged in!
		if (prefs != null && location != null) {
			Editor edit = prefs.edit();

			String pref_key = Persistence.getPreferencesLastLocation(mController.getActiveAdapter().getId());
			edit.putString(pref_key, location.getId());
			edit.commit();
		}

		if(location != null ){
			mActiveLocationId = location.getId();
		}
		else { // All sensors item
			mActiveLocationId = Constants.GUI_MENU_ALL_SENSOR_ID;
		}

		// TODO
		mActivity.setActiveAdapterID(mActiveAdapterId);
		mActivity.setActiveLocationID(mActiveLocationId);
		mActivity.redrawDevices();

		// Close drawer
		if (closeDrawer) {
			closeMenu();
		}
	}

	private void doSwitchAdapter(String adapterId) {
		mSwitchAdapterTask = new SwitchAdapterTask(mActivity, false);

		mSwitchAdapterTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (success) {
					mActivity.setActiveAdapterAndLocation();
					mActivity.redrawDevices();
					redrawMenu();
				}

				mActivity.setSupportProgressBarIndeterminateVisibility(false);
			}
		});

		mActivity.setSupportProgressBarIndeterminateVisibility(true);
		mSwitchAdapterTask.execute(adapterId);
	}

	private void doUnregistAdapter(String adapterId) {
		mUnregisterAdapterTask = new UnregisterAdapterTask(mActivity);

		mUnregisterAdapterTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (success) {
					new ToastMessageThread(mActivity, R.string.toast_adapter_removed).start();
					mActivity.setActiveAdapterAndLocation();
					mActivity.redrawDevices();
					mActivity.redrawMenu();
				}
				mActivity.setSupportProgressBarIndeterminateVisibility(false);
			}
		});
		mActivity.setSupportProgressBarIndeterminateVisibility(true);
		mUnregisterAdapterTask.execute(adapterId);
	}

	public MenuListAdapter getMenuAdapter() {
		mMenuAdapter = new MenuListAdapter(mActivity);

		// Adding profile header
		ActualUser actUser = mController.getActualUser();

		Bitmap picture = actUser.getPicture();
		if (picture == null)
			picture = actUser.getDefaultPicture(mActivity);
		
		mMenuAdapter.addHeader(new ProfileMenuItem(actUser.getName(), actUser.getEmail(), picture));

		List<Adapter> adapters = mController.getAdapters();
		Adapter activeAdapter = mController.getActiveAdapter();

		
		// Adding separator as item (we don't want to let it float as header)
		mMenuAdapter.addItem(new SeparatorMenuItem());

		mMenuAdapter.addHeader(new GroupImageMenuItem(mActivity.getResources().getString(R.string.adapter), R.drawable.add_custom_view, new OnClickListener() {
			@Override
			public void onClick(View v) {
				//DialogFragment newFragment = new AddAdapterFragmentDialog();
				//newFragment.show(mActivity.getSupportFragmentManager(), MainActivity.ADD_ADAPTER_TAG);
				Intent intent = new Intent(mActivity, AddAdapterActivity.class);
				mActivity.startActivityForResult(intent, Constants.ADD_ADAPTER_REQUEST_CODE);
				}
			}));
		
		if (!adapters.isEmpty()) {
			// Adding separator as item (we don't want to let it float as header)
			//mMenuAdapter.addItem(new SeparatorMenuItem());

			// Adding adapters
			for (Adapter actAdapter : adapters) {
				mMenuAdapter.addItem(new AdapterMenuItem(actAdapter.getName(), actAdapter.getRole().name(), activeAdapter.getId().equals(actAdapter.getId()), actAdapter.getId()));
			}

			// Adding separator as item (we don't want to let it float as header)
			mMenuAdapter.addItem(new SeparatorMenuItem());

			// Adding location header
			mMenuAdapter.addHeader(new GroupImageMenuItem(mActivity.getResources().getString(R.string.location), R.drawable.add_custom_view, new OnClickListener() {

				@Override
				public void onClick(View v) {
					Toast.makeText(mActivity, "Not implemented yet", Toast.LENGTH_SHORT).show();
				}
			}));

			List<Location> locations = mController.getLocations(activeAdapter != null ? activeAdapter.getId() : "");
			if (locations.size() > 0) {
				boolean boolActLoc = false;
				if(locations.size() > 1) {
					if(mActiveLocationId != null ) {
						boolActLoc = (mActiveLocationId.equals(Constants.GUI_MENU_ALL_SENSOR_ID)) ? true : false; 
					}
					// ALL Sensor from adapter
					mMenuAdapter.addItem(new LocationMenuItem("All sensors", R.drawable.loc_all, false, Constants.GUI_MENU_ALL_SENSOR_ID,boolActLoc ));
				}
				// Adding location
				for (int i = 0; i < locations.size(); i++) {
					Location actLoc = locations.get(i);
					boolActLoc = false;
					if(mActiveLocationId != null ) {
						boolActLoc = (mActiveLocationId.equals(actLoc.getId())) ? true : false; 
					}
					mMenuAdapter.addItem(new LocationMenuItem(actLoc.getName(), actLoc.getIconResource(), false, actLoc.getId(),boolActLoc ));
				}
			} else {
				mMenuAdapter.addItem(new EmptyMenuItem(mActivity.getResources().getString(R.string.no_location)));
			}

			mMenuAdapter.addItem(new SeparatorMenuItem());
			// Adding custom view header
			mMenuAdapter.addHeader(new GroupImageMenuItem(mActivity.getResources().getString(R.string.custom_view), R.drawable.add_custom_view, new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO doplnit spusteni dialogu pro vytvoreni custom view
					Toast.makeText(mActivity, "Not implemented yet", Toast.LENGTH_SHORT).show();
				}
			}));
			// Adding custom views
			// TODO pridat custom views
			// mMenuAdapter.addItem(new EmptyMenuItem(mActivity.getResources().getString(R.string.no_custom_view)));
			mMenuAdapter.addItem(new CustomViewMenuItem("Graph view", R.drawable.loc_living_room, false, "Custom001", (mActiveCustomViewId != null) ? true : false));
		} else {
			mMenuAdapter.addItem(new EmptyMenuItem(mActivity.getResources().getString(R.string.no_adapters)));
			
		}

		// Adding separator as header
		mMenuAdapter.addItem(new SeparatorMenuItem());

		// Adding settings, about etc.
		mMenuAdapter.addItem(new SettingMenuItem(mActivity.getResources().getString(R.string.action_settings), R.drawable.settings, com.rehivetech.beeeon.activity.menuItem.MenuItem.ID_SETTINGS));
		mMenuAdapter.addItem(new SettingMenuItem(mActivity.getResources().getString(R.string.action_about), R.drawable.info, com.rehivetech.beeeon.activity.menuItem.MenuItem.ID_ABOUT));

		// menuAdapter.log();
		return mMenuAdapter;
	}

	public void clickOnHome() {
		if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
			closeMenu();
		} else {
			openMenu();
		}

	}

	/**
	 * Handling first tap back button
	 */
	public void firstTapBack() {
		Log.d(TAG, "firtstTap");
		Toast.makeText(mActivity, mActivity.getString(R.string.toast_tap_again_exit), Toast.LENGTH_SHORT).show();
		// backPressed = true;
		((MainActivity) mActivity).setBackPressed(true);
		openMenu();
	}

	/**
	 * Handling second tap back button - exiting
	 */
	public void secondTapBack() {
		Log.d(TAG, "secondTap");
		mActivity.finish();
	}

	public void onConfigurationChanged(Configuration newConfig) {
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	public void setDefaultTitle() {
		mDrawerTitle = "BeeeOn";
	}

	public void setIsDrawerOpen(boolean value) {
		mIsDrawerOpen = value;
	}

	public boolean getIsDrawerOpen() {
		return mIsDrawerOpen;
	}

	public void setLocationID(String locID) {
		mActiveLocationId = locID;
		mActiveCustomViewId = null;
	}

	public void setAdapterID(String adaID) {
		mActiveAdapterId = adaID;
	}

	public void cancelAllTasks() {
		if (mSwitchAdapterTask != null) {
			mSwitchAdapterTask.cancel(true);
		}

		if (mUnregisterAdapterTask != null) {
			mUnregisterAdapterTask.cancel(true);
		}
	}

	class ActionModeAdapters implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			if(mController.isUserAllowed(mController.getAdapter(mSelectedMenuItem.getId()).getRole()) ) {
				menu.add("Edit").setIcon(R.drawable.ic_mode_edit_white_24dp).setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
				menu.add("Users").setIcon(R.drawable.ic_group_white_24dp).setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
			}
			menu.add("Unregist").setIcon(R.drawable.ic_delete_white_24dp).setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_IF_ROOM);
			//menu.add("Cancel").setIcon(R.drawable.beeeon_ic_action_cancel).setTitle("Cancel").setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mMode = null;
			mSelectedMenuItem.setNotSelected();
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, com.actionbarsherlock.view.MenuItem item) {
			if (item.getTitle().equals("Unregist")) { // UNREGIST ADAPTER
				doUnregistAdapter(mSelectedMenuItem.getId());
			} else if (item.getTitle().equals("Users")) { // GO TO USERS OF ADAPTER
				Intent intent = new Intent(mActivity, AdapterUsersActivity.class);
				intent.putExtra(Constants.GUI_SELECTED_ADAPTER_ID, mSelectedMenuItem.getId());
				mActivity.startActivity(intent);
				
			} else if (item.getTitle().equals("Edit")) { // RENAME ADAPTER
				new ToastMessageThread(mActivity, R.string.toast_not_implemented).start();
			}

			mode.finish();
			return true;
		}
	}

	class ActionModeLocations implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			menu.add("Edit").setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
			// menu.add("Unregist").setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_IF_ROOM);
			menu.add("Cancel").setIcon(R.drawable.beeeon_ic_action_cancel).setTitle("Cancel").setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mMode = null;

		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, com.actionbarsherlock.view.MenuItem item) {
			if (item.getTitle().equals("Edit")) {
				new ToastMessageThread(mActivity, R.string.toast_not_implemented).start();
			}
			mode.finish();
			return true;
		}
	}

	public void finishActinMode() {
		Log.d(TAG, "Close action mode");
		if (mMode != null)
			mMode.finish();
	}
}

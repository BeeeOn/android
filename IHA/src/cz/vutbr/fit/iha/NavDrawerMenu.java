package cz.vutbr.fit.iha;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;
import cz.vutbr.fit.iha.activity.BaseApplicationActivity;
import cz.vutbr.fit.iha.activity.LocationDetailActivity;
import cz.vutbr.fit.iha.activity.LocationScreenActivity;
import cz.vutbr.fit.iha.activity.SettingsMainActivity;
import cz.vutbr.fit.iha.activity.dialog.InfoDialogFragment;
import cz.vutbr.fit.iha.activity.menuItem.AdapterMenuItem;
import cz.vutbr.fit.iha.activity.menuItem.EmptyMenuItem;
import cz.vutbr.fit.iha.activity.menuItem.GroupImageMenuItem;
import cz.vutbr.fit.iha.activity.menuItem.LocationMenuItem;
import cz.vutbr.fit.iha.activity.menuItem.MenuItem;
import cz.vutbr.fit.iha.activity.menuItem.ProfileMenuItem;
import cz.vutbr.fit.iha.activity.menuItem.SeparatorMenuItem;
import cz.vutbr.fit.iha.activity.menuItem.SettingMenuItem;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.asynctask.CallbackTask.CallbackTaskListener;
import cz.vutbr.fit.iha.asynctask.SwitchAdapterTask;
import cz.vutbr.fit.iha.asynctask.UnregisterAdapterTask;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.household.ActualUser;
import cz.vutbr.fit.iha.persistence.Persistence;
import cz.vutbr.fit.iha.thread.ToastMessageThread;

public class NavDrawerMenu {
	private static final String TAG = "NavDrawerMenu";
	private final static String TAG_INFO = "tag_info";
	
	private BaseApplicationActivity mActivity;
	private Controller mController;
	
	private DrawerLayout mDrawerLayout;
	private StickyListHeadersListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private String mDrawerTitle = "IHA";
	
	private Location mActiveLocation;
	private String mActiveLocationId;
	private String mActiveAdapterId;
	private boolean mIsDrawerOpen;
	
	private boolean backPressed = false;
	
	private SwitchAdapterTask mSwitchAdapterTask;
	private UnregisterAdapterTask mUnregisterAdapterTask;
	
	private MenuListAdapter mMenuAdapter;
	
	public NavDrawerMenu (BaseApplicationActivity activity) {
		// Set activity
		mActivity = activity;
		
		backPressed = ((LocationScreenActivity) mActivity).getBackPressed();
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
					backPressed = ((LocationScreenActivity) mActivity).getBackPressed();
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
						MenuItem item = (MenuItem) mMenuAdapter.getItem(position);
						switch (item.getType()) {
						case ADAPTER:
							// if it is not chosen, switch to selected adapter
							if (!mController.getActiveAdapter().getId().equals(item.getId())) {
								doSwitchAdapter(item.getId());
							}
							break;

						case CUSTOM_VIEW:
							// TODO: otevrit custom view, jeste nedelame s customView, takze pozdeji
							break;

						case SETTING:
							if (item.getId().equals(cz.vutbr.fit.iha.activity.menuItem.MenuItem.ID_ABOUT)) {
								InfoDialogFragment dialog = new InfoDialogFragment();
								dialog.show(mActivity.getSupportFragmentManager(), TAG_INFO);
							} else if (item.getId().equals(cz.vutbr.fit.iha.activity.menuItem.MenuItem.ID_SETTINGS)) {
								Intent intent = new Intent(mActivity, SettingsMainActivity.class);
								mActivity.startActivity(intent);
							}
							break;

						case LOCATION:
							// Get the title followed by the position
							Adapter adapter = mController.getActiveAdapter();
							if (adapter != null){
								changeLocation(mController.getLocation(adapter.getId(), item.getId()), true);
								redrawMenu();
							}
								
		//aaaaaaaaaa
							break;

						default:
							break;
						}
					}
				});
				mDrawerList.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
						Log.d(TAG, "Item Long press");
						MenuItem item = (MenuItem) mMenuAdapter.getItem(position);
						switch (item.getType()) {
						case LOCATION:
							Bundle bundle = new Bundle();
							String myMessage = item.getId();
							bundle.putString("locationID", myMessage);
							Intent intent = new Intent(mActivity, LocationDetailActivity.class);
							intent.putExtras(bundle);
							mActivity.startActivity(intent);
							break;
						case ADAPTER:
							Log.i(TAG, "deleting adapter");
						
							doUnregistAdapter(item.getId());
							break;
						default:
							// do nothing
							break;
						}
						return true;
					}

				});

				// getSupportActionBar().setIcon(R.drawable.ic_launcher_white);
				// ActionBarDrawerToggle ties together the the proper interactions
				// between the sliding drawer and the action bar app icon
				mDrawerToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

					public void onDrawerClosed(View view) {
						backPressed = ((LocationScreenActivity) mActivity).getBackPressed();
						if (backPressed)
							mActivity.onBackPressed();
						// Set the title on the action when drawer closed
						if (mActiveLocation != null)
							mActivity.getSupportActionBar().setTitle(mActiveLocation.getName());
						super.onDrawerClosed(view);
						Log.d(TAG, "BackPressed - onDrawerClosed " + String.valueOf(backPressed));
						mIsDrawerOpen = false;
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
	}
	
	public void redrawMenu() {
		setActiveAdapterAndLocation();
		
		mMenuAdapter = getMenuAdapter();
		mDrawerList.setAdapter(mMenuAdapter);

		if (!mIsDrawerOpen) {
			// Close drawer
			closeMenu();
			Log.d(TAG, "LifeCycle: onOrientation");
		}
	}
	
	public void setActiveAdapterAndLocation() {
		// Set active adapter and location
		Adapter adapter = mController.getActiveAdapter();
		if (adapter != null) {
			mActiveAdapterId = adapter.getId();
			
			String prefKey = Persistence.getPreferencesLastLocation(adapter.getId());
			SharedPreferences prefs = mController.getUserSettings();
			Location location = mController.getLocation(adapter.getId(), prefs.getString(prefKey, ""));
			
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
				changeLocation(location, false);
				return;
			}
		}
		
		// no adapters or sensors
		Log.d("default", "DEFAULT POSITION: Empty adapter or sensor set");
		// TODO
		((LocationScreenActivity) mActivity).setActiveAdapterID(mActiveAdapterId);
		((LocationScreenActivity) mActivity).setActiveLocationID(mActiveLocationId);
		((LocationScreenActivity) mActivity).redrawDevices();
	}
	
	private void changeLocation(Location location, boolean closeDrawer) {
		// save current location
		SharedPreferences prefs = mController.getUserSettings();
		Editor edit = prefs.edit();

		String pref_key = Persistence.getPreferencesLastLocation(mController.getActiveAdapter().getId());
		edit.putString(pref_key, location.getId());
		edit.commit();

		mActiveLocation = location;
		mActiveLocationId = location.getId();

		// TODO
		
		((LocationScreenActivity) mActivity).setActiveAdapterID(mActiveAdapterId);
		((LocationScreenActivity) mActivity).setActiveLocationID(mActiveLocationId);
		((LocationScreenActivity) mActivity).redrawDevices();


		// mDrawerList.setItemChecked(position, true);

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
				if(success) {
					new ToastMessageThread(mActivity, R.string.toast_adapter_removed).start();
					redrawMenu();
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
		mMenuAdapter.addHeader(new ProfileMenuItem(actUser.getName(), actUser.getEmail(), actUser.getPicture(mActivity)));

		List<Adapter> adapters = mController.getAdapters();
		Adapter activeAdapter = mController.getActiveAdapter();
		
		if (!adapters.isEmpty()) {
			// Adding separator as item (we don't want to let it float as header)
			mMenuAdapter.addItem(new SeparatorMenuItem());
	
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
	
				// Adding location
				for (int i = 0; i < locations.size(); i++) {
					Location actLoc = locations.get(i);
					mMenuAdapter.addItem(new LocationMenuItem(actLoc.getName(), actLoc.getIconResource(), false, actLoc.getId(),(mActiveLocationId == actLoc.getId())?true:false));
				}
			} else {
				mMenuAdapter.addItem(new EmptyMenuItem(mActivity.getResources().getString(R.string.no_location)));
			}
	
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
			mMenuAdapter.addItem(new EmptyMenuItem(mActivity.getResources().getString(R.string.no_custom_view)));
		} else {
			// Adding separator as item (we don't want to let it float as header)
			mMenuAdapter.addItem(new SeparatorMenuItem());

			mMenuAdapter.addItem(new EmptyMenuItem(mActivity.getResources().getString(R.string.no_adapters)));
		}

		// Adding separator as header
		mMenuAdapter.addItem(new SeparatorMenuItem());

		// Adding settings, about etc.
		mMenuAdapter.addItem(new SettingMenuItem(mActivity.getResources().getString(R.string.action_settings), R.drawable.settings, cz.vutbr.fit.iha.activity.menuItem.MenuItem.ID_SETTINGS));
		mMenuAdapter.addItem(new SettingMenuItem(mActivity.getResources().getString(R.string.action_about), R.drawable.info, cz.vutbr.fit.iha.activity.menuItem.MenuItem.ID_ABOUT));

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
		//backPressed = true;
		((LocationScreenActivity) mActivity).setBackPressed(true);
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
		mDrawerTitle = "IHA";
	}

	public void setIsDraweOpen(boolean value) {
		mIsDrawerOpen = value;
	}

	public void cancelAllTasks() {
		if (mSwitchAdapterTask != null) {
			mSwitchAdapterTask.cancel(true);
		}
		
		if (mUnregisterAdapterTask != null) {
			mUnregisterAdapterTask.cancel(true);
		}
	}
	
}

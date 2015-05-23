package com.rehivetech.beeeon.menu;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.AdapterUsersActivity;
import com.rehivetech.beeeon.activity.MainActivity;
import com.rehivetech.beeeon.activity.SettingsMainActivity;
import com.rehivetech.beeeon.activity.dialog.InfoDialogFragment;
import com.rehivetech.beeeon.activity.menuItem.AdapterMenuItem;
import com.rehivetech.beeeon.activity.menuItem.EmptyMenuItem;
import com.rehivetech.beeeon.activity.menuItem.GroupMenuItem;
import com.rehivetech.beeeon.activity.menuItem.LocationMenuItem;
import com.rehivetech.beeeon.activity.menuItem.MenuItem;
import com.rehivetech.beeeon.activity.menuItem.ProfileMenuItem;
import com.rehivetech.beeeon.activity.menuItem.SeparatorMenuItem;
import com.rehivetech.beeeon.activity.menuItem.SettingMenuItem;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.arrayadapter.MenuListAdapter;
import com.rehivetech.beeeon.asynctask.CallbackTask.CallbackTaskListener;
import com.rehivetech.beeeon.asynctask.SwitchAdapterTask;
import com.rehivetech.beeeon.asynctask.UnregisterAdapterTask;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.util.Log;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static android.support.v7.view.ActionMode.*;

public class NavDrawerMenu   {
	private static final String TAG = "NavDrawerMenu";
	private final static String TAG_INFO = "tag_info";
  public static final String FRG_TAG_PRO = "PRO";
    private final Toolbar mToolbar;

    private MainActivity mActivity;
	private Controller mController;

	private DrawerLayout mDrawerLayout;
	private StickyListHeadersListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private String mDrawerTitle = "BeeeOn";

	private String mActiveItem;
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
    private RelativeLayout mDrawerRelLay;

    public NavDrawerMenu(MainActivity activity, Toolbar toolbar) {
		// Set activity
		mActivity = activity;
        mToolbar = toolbar;

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
        // Locate relative layout
        mDrawerRelLay = (RelativeLayout) mActivity.findViewById(R.id.relative_layout_drawer);
	}

	private void settingsMenu() {
		mDrawerLayout.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
					backPressed = ((MainActivity) mActivity).getBackPressed();
					Log.d(TAG, "BackPressed = " + String.valueOf(backPressed));
					if (mDrawerLayout == null) {
						return false;
					}
					if (mDrawerLayout.isDrawerOpen(mDrawerRelLay) && !backPressed) {
						firstTapBack();
						return true;
					} else if (mDrawerLayout.isDrawerOpen(mDrawerRelLay) && backPressed) {
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
				Adapter adapter = mController.getActiveAdapter();
				switch (mSelectedMenuItem.getType()) {
				case ADAPTER:
					if(adapter == null)
						break;
					// if it is not chosen, switch to selected adapter
					if (!adapter.getId().equals(mSelectedMenuItem.getId())) {
						doSwitchAdapterTask(mSelectedMenuItem.getId());
					}
					break;
                case LOCATION:
                    // Get the title followed by the position
                    if (adapter != null) {
                        changeMenuItem(mSelectedMenuItem.getId(), true);
                        redrawMenu();
                    }
                    break;

				case SETTING:
					if (mSelectedMenuItem.getId().equals(com.rehivetech.beeeon.activity.menuItem.MenuItem.ID_ABOUT)) {
						InfoDialogFragment dialog = new InfoDialogFragment();
						dialog.show(mActivity.getSupportFragmentManager(), TAG_INFO);
					} else if (mSelectedMenuItem.getId().equals(com.rehivetech.beeeon.activity.menuItem.MenuItem.ID_SETTINGS)) {
						Intent intent = new Intent(mActivity, SettingsMainActivity.class);
						mActivity.startActivity(intent);
					} else if (mSelectedMenuItem.getId().equals(MenuItem.ID_LOGOUT)) {
                        mActivity.logout();
                    }
					break;

				default:
                    Log.d(TAG,"other");
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
                mSelectedMenuItem = (MenuItem) mMenuAdapter.getItem(position);
                switch (mSelectedMenuItem.getType()) {
                    case LOCATION:

                        break;
                    case ADAPTER:
                        Log.i(TAG, "Long press - adapter");
                        mMode = mActivity.startSupportActionMode(new ActionModeAdapters());
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
        mDrawerToggle = new ActionBarDrawerToggle(mActivity,mDrawerLayout,mToolbar,R.string.drawer_open,R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				backPressed = mActivity.getBackPressed();
				if (backPressed)
					mActivity.onBackPressed();
				// Set the title on the action when drawer closed
				Adapter adapter = mController.getActiveAdapter();

				if (adapter != null && mActiveItem != null) {
                    if(mActiveItem.equals(Constants.GUI_MENU_CONTROL)) {
                        mActivity.getSupportActionBar().setTitle(mActivity.getString(R.string.menu_control));
                    }
                    else if (mActiveItem.equals(Constants.GUI_MENU_DASHBOARD)) {
                        mActivity.getSupportActionBar().setTitle(mActivity.getString(R.string.menu_dashboard));
                    }
                    else if(mActiveItem.equals(Constants.GUI_MENU_WATCHDOG)) {
                        mActivity.getSupportActionBar().setTitle(mActivity.getString(R.string.menu_watchdog));
                    }
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

		openMenu();
	}

	public void openMenu() {
		mIsDrawerOpen = true;
		mDrawerLayout.openDrawer(mDrawerRelLay);
	}

	public void closeMenu() {
		mIsDrawerOpen = false;
		mDrawerLayout.closeDrawer(mDrawerRelLay);
		if (mMode != null) {
			mMode.finish();
		}
	}

	public void redrawMenu() {
		mMenuAdapter = getMenuAdapter();
		mDrawerList.setAdapter(mMenuAdapter);
		
		Adapter adapter = mController.getActiveAdapter();

		if (adapter != null && mActiveItem !=null ) {
            if(mActiveItem.equals(Constants.GUI_MENU_CONTROL)) {
                mActivity.getSupportActionBar().setTitle(mActivity.getString(R.string.menu_control));
            }
            else if (mActiveItem.equals(Constants.GUI_MENU_DASHBOARD)) {
                mActivity.getSupportActionBar().setTitle(mActivity.getString(R.string.menu_dashboard));
            }
            else if(mActiveItem.equals(Constants.GUI_MENU_WATCHDOG)) {
                mActivity.getSupportActionBar().setTitle(mActivity.getString(R.string.menu_watchdog));
            }
		} else {
			setDefaultTitle();
		}

		if (!mIsDrawerOpen) {
			// Close drawer
			closeMenu();
			Log.d(TAG, "LifeCycle: onOrientation");
		}
	}

	private void changeMenuItem(String ID, boolean closeDrawer) {

        mActiveItem = ID;
		// TODO
		mActivity.setActiveAdapterID(mActiveAdapterId);
		mActivity.setActiveMenuID(mActiveItem);
		mActivity.redrawMainFragment();

		// Close drawer
		if (closeDrawer) {
			closeMenu();
		}
	}

	private void doSwitchAdapterTask(String adapterId) {
		mSwitchAdapterTask = new SwitchAdapterTask(mActivity, false);

		mSwitchAdapterTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (success) {
					mActivity.setActiveAdapterAndMenu();
					mActivity.redrawMainFragment();
					redrawMenu();
				}
			}
		});

		mSwitchAdapterTask.execute(adapterId);
	}

	private void doUnregisterAdapterTask(String adapterId) {
		mUnregisterAdapterTask = new UnregisterAdapterTask(mActivity);

		mUnregisterAdapterTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (success) {
					Toast.makeText(mActivity, R.string.toast_adapter_removed, Toast.LENGTH_LONG).show();
					mActivity.setActiveAdapterAndMenu();
					mActivity.redraw();
				}
			}
		});
		mUnregisterAdapterTask.execute(adapterId);
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
        	changeMenuItem(Constants.GUI_MENU_PROFILE,true);
            }
        }));

		List<Adapter> adapters = mController.getAdaptersModel().getAdapters();

		
		// Adding separator as item (we don't want to let it float as header)
		mMenuAdapter.addItem(new SeparatorMenuItem());

		mMenuAdapter.addHeader(new GroupMenuItem(mActivity.getResources().getString(R.string.adapter)));
		
		if (!adapters.isEmpty()) {
			Adapter activeAdapter = mController.getActiveAdapter();
			if(activeAdapter == null)
				return mMenuAdapter;
			// Adding adapters
			for (Adapter actAdapter : adapters) {
				mMenuAdapter.addItem(new AdapterMenuItem(actAdapter.getName(), actAdapter.getRole().getStringResource(), activeAdapter.getId().equals(actAdapter.getId()), actAdapter.getId()));
			}

			// Adding separator as item (we don't want to let it float as header)
			mMenuAdapter.addItem(new SeparatorMenuItem());


            // MANAGMENT
            mMenuAdapter.addHeader(new GroupMenuItem(mActivity.getResources().getString(R.string.menu_managment)));
            mMenuAdapter.addItem(new LocationMenuItem(mActivity.getString(R.string.menu_control), R.drawable.ic_overview, false, Constants.GUI_MENU_CONTROL, (mActiveItem != null)? mActiveItem.equals(Constants.GUI_MENU_CONTROL):true));
            mMenuAdapter.addItem(new LocationMenuItem(mActivity.getString(R.string.menu_dashboard),R.drawable.ic_dashboard,false,Constants.GUI_MENU_DASHBOARD,(mActiveItem != null)? mActiveItem.equals(Constants.GUI_MENU_DASHBOARD):false));

            mMenuAdapter.addItem( new SeparatorMenuItem());
            // APPLICATIONS
            mMenuAdapter.addHeader(new GroupMenuItem(mActivity.getResources().getString(R.string.menu_applications)));
            mMenuAdapter.addItem(new LocationMenuItem(mActivity.getString(R.string.menu_watchdog), R.drawable.ic_app_watchdog, false, Constants.GUI_MENU_WATCHDOG, (mActiveItem != null)?mActiveItem.equals(Constants.GUI_MENU_WATCHDOG):false));

		} else {
			mMenuAdapter.addItem(new EmptyMenuItem(mActivity.getResources().getString(R.string.no_adapters)));
			
		}

		// Adding separator as header
		mMenuAdapter.addItem(new SeparatorMenuItem());

		// Adding settings, about etc.
		mMenuAdapter.addItem(new SettingMenuItem(mActivity.getResources().getString(R.string.action_settings), R.drawable.settings, com.rehivetech.beeeon.activity.menuItem.MenuItem.ID_SETTINGS));
		mMenuAdapter.addItem(new SettingMenuItem(mActivity.getResources().getString(R.string.action_about), R.drawable.info, com.rehivetech.beeeon.activity.menuItem.MenuItem.ID_ABOUT));
        mMenuAdapter.addItem(new SettingMenuItem(mActivity.getString(R.string.action_logout), R.drawable.logout,MenuItem.ID_LOGOUT));
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

	public void setActiveMenuID(String id) {
		mActiveItem = id;
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

	class ActionModeAdapters implements Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.adapter_menu, menu);

			if(!mController.isUserAllowed(mController.getAdaptersModel().getAdapter(mSelectedMenuItem.getId()).getRole()) ) {
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
            Log.d(TAG,"ActionMode Adapter - item id: "+ item.getItemId());
           if (item.getItemId() == R.id.ada_menu_del) { // UNREGIST ADAPTER
				doUnregisterAdapterTask(mSelectedMenuItem.getId());
			} else if (item.getItemId() == R.id.ada_menu_users) { // GO TO USERS OF ADAPTER
				Intent intent = new Intent(mActivity, AdapterUsersActivity.class);
				intent.putExtra(Constants.GUI_SELECTED_ADAPTER_ID, mSelectedMenuItem.getId());
				mActivity.startActivity(intent);

			} else if (item.getItemId() == R.id.ada_menu_edit) { // RENAME ADAPTER
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

	public void finishActinMode() {
		Log.d(TAG, "Close action mode");
		if (mMode != null)
			mMode.finish();
	}
}

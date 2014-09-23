package cz.vutbr.fit.iha.activity;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.MenuListAdapter;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.SensorListAdapter;
import cz.vutbr.fit.iha.activity.dialog.AddAdapterActivityDialog;
import cz.vutbr.fit.iha.activity.dialog.AddSensorActivityDialog;
import cz.vutbr.fit.iha.activity.dialog.CustomAlertDialog;
import cz.vutbr.fit.iha.activity.dialog.InfoDialogFragment;
import cz.vutbr.fit.iha.activity.dialog.SetupSensorActivityDialog;
import cz.vutbr.fit.iha.activity.menuItem.AdapterMenuItem;
import cz.vutbr.fit.iha.activity.menuItem.EmptyMenuItem;
import cz.vutbr.fit.iha.activity.menuItem.GroupImageMenuItem;
import cz.vutbr.fit.iha.activity.menuItem.LocationMenuItem;
import cz.vutbr.fit.iha.activity.menuItem.ProfileMenuItem;
import cz.vutbr.fit.iha.activity.menuItem.SeparatorMenuItem;
import cz.vutbr.fit.iha.activity.menuItem.SettingMenuItem;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.household.ActualUser;
import cz.vutbr.fit.iha.persistence.Persistence;
import cz.vutbr.fit.iha.thread.ToastMessageThread;

/**
 * Activity class for choosing location
 * 
 * @author ThinkDeep
 * @author Robyer
 * 
 */
public class LocationScreenActivity extends BaseActivity {
	private static final String TAG = LocationScreenActivity.class.getSimpleName();

	private Controller mController;
	private LocationScreenActivity mActivity;
	private List<Location> mLocations;

	private DrawerLayout mDrawerLayout;
	private StickyListHeadersListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private MenuListAdapter mMenuAdapter;

	private CharSequence mDrawerTitle;

	private SensorListAdapter mSensorAdapter;
	private ListView mSensorList;

	private CharSequence mTitle;

	private int mCntOfAllDev;
	private String mActLocID;

	private static boolean inBackground = false;
	private static boolean isPaused = false;
	private static boolean forceReloadListing = false;
	private static boolean isClosing = false;
	private static boolean isRefreshDone = false;

	/**
	 * Instance save state tags
	 */
	private static final String BKG = "activityinbackground";
	private static final String LCTN = "lastlocation";
	private static final String IS_DRAWER_OPEN = "draweropen";

	private final static int REQUEST_SENSOR_DETAIL = 1;
	private final static int REQUEST_ADD_ADAPTER = 2;

	/**
	 * saved instance states
	 */
	private Location mActiveLocation;
	private String mActiveLocationId;
	private static boolean mOrientation = false;
	private static boolean mIsDrawerOpen;

	private List<BaseDevice> mDevices;
	private List<Facility> mFacilities;
	private Handler mTimeHandler = new Handler();
	private Runnable mTimeRun;

	/**
	 * Tasks which can be running in this activity and after finishing can try to change GUI -> must be cancelled when activity stop
	 */
	private FacilitiesTask mFacilitiesTask;
	private ChangeLocationTask mChangeLocationTask;
	private SwitchAdapter mSwitchAdapter;

	//
	ActionMode mMode;

	protected TextView mDrawerItemText;
	protected EditText mDrawerItemEdit;

	private boolean backPressed = false;

	/**
	 * Constant to tag InfoDIalogFragment
	 */
	private final static String TAG_INFO = "tag_info";

	/**
	 * Represents "pair" of data required for get adapter locations and uninitialized facilities
	 */
	private class AdapterMenuFacilitiesPair {
		public final MenuListAdapter menuListAdapter;
		public final List<Facility> facilities;

		public AdapterMenuFacilitiesPair(final MenuListAdapter menuListAdapter, List<Facility> facilities) {
			this.menuListAdapter = menuListAdapter;
			this.facilities = facilities;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_location_screen);

		// Get controller
		mController = Controller.getInstance(this);

		// Get Activity
		mActivity = this;

		setSupportProgressBarIndeterminate(true);
		setSupportProgressBarIndeterminateVisibility(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher_white);

		if (savedInstanceState != null) {
			inBackground = savedInstanceState.getBoolean(BKG);
			mIsDrawerOpen = savedInstanceState.getBoolean(IS_DRAWER_OPEN);
			mActiveLocationId = savedInstanceState.getString(LCTN);
			if (mActiveLocationId != null)
				mOrientation = true;
		}

		initMenu();

		if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(Constants.NOADAPTER)) {
			Intent intent = new Intent(LocationScreenActivity.this, AddAdapterActivityDialog.class);
			Bundle bundle = new Bundle();
			bundle.putBoolean(Constants.CANCEL, true);
			intent.putExtras(bundle);
			startActivity(intent);
		}

	}

	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume  , inBackground: " + String.valueOf(inBackground));

		mSwitchAdapter = new LocationScreenActivity.SwitchAdapter();
		mSwitchAdapter.execute(new String[0]); // to call setLocationOrEmpty and redrawMenu

		backPressed = false;
		isPaused = false;

	}

	public void onPause() {
		super.onPause();
		isPaused = true;
		mTimeHandler.removeCallbacks(mTimeRun);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");

		this.setSupportProgressBarIndeterminateVisibility(false);

		if (mFacilitiesTask != null) {
			mFacilitiesTask.cancel(true);
			if (mFacilitiesTask.getDialog() != null) {
				mFacilitiesTask.getDialog().dismiss();
			}
		}

		if (mChangeLocationTask != null) {
			mChangeLocationTask.cancel(true);
		}

		if (mSwitchAdapter != null) {
			mSwitchAdapter.cancel(true);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (backPressed) {
			backPressed = false;
		}
		return super.dispatchTouchEvent(ev);
	}

	/**
	 * Handling first tap back button
	 */
	private void firstTapBack() {
		Toast.makeText(this, getString(R.string.toast_tap_again_exit), Toast.LENGTH_SHORT).show();
		backPressed = true;
		if (mDrawerLayout != null)
			mDrawerLayout.openDrawer(mDrawerList);
	}

	/**
	 * Handling second tap back button - exiting
	 */
	private void secondTapBack() {
		// Toast.makeText(this, getString(R.string.toast_leaving_app),
		// Toast.LENGTH_LONG).show();
		// super.onBackPressed();
		this.finish();

		// android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	public void onBackPressed() {
		// second click
		if (backPressed) {
			isClosing = true;
			Log.d(TAG, "kua to co je" + Boolean.toString(isClosing));
			secondTapBack();
		}
		// first click
		else {
			firstTapBack();
		}
		Log.d(TAG, "BackPressed - onBackPressed " + String.valueOf(backPressed));

		return;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstaceState) {
		savedInstaceState.putBoolean(BKG, inBackground);
		savedInstaceState.putString(LCTN, mActiveLocationId);
		savedInstaceState.putBoolean(IS_DRAWER_OPEN, mDrawerLayout.isDrawerOpen(mDrawerList));
		super.onSaveInstanceState(savedInstaceState);
	}

	public static void healActivity() {
		inBackground = false;
	}

	public void onOrientationChanged() {
		if (mOrientation) {
			mActiveLocation = mController.getLocation(mActiveLocationId);

			refreshListing();

			if (!mIsDrawerOpen) {
				// Close drawer
				mDrawerLayout.closeDrawer(mDrawerList);
				Log.d(TAG, "LifeCycle: onOrientation");
			}
		}
		mOrientation = false;
	}

	/**
	 * Use Thread to call it or for refreshing Menu use redrawMenu() instead.
	 */
	private MenuListAdapter getMenuAdapter() {
		MenuListAdapter menuAdapter = new MenuListAdapter(LocationScreenActivity.this);

		// Adding profile header
		ActualUser actUser = mController.getActualUser();
		menuAdapter.addHeader(new ProfileMenuItem(actUser.getName(), actUser.getEmail(), actUser.getPicture(this)));

		List<Adapter> adapters = mController.getAdapters();
		//if (adapters.size() > 1) {
			// Adding separator as item (we don't want to let it float as
			// header)
			menuAdapter.addItem(new SeparatorMenuItem());

			// Adding adapters
			Adapter chosenAdapter = mController.getActiveAdapter();
			for (Adapter actAdapter : adapters) {
				menuAdapter.addItem(new AdapterMenuItem(actAdapter.getName(), actAdapter.getRole().name(), chosenAdapter.getId().equals(actAdapter.getId()), actAdapter.getId()));
			}
		//}

		// Adding separator as item (we don't want to let it float as header)
		menuAdapter.addItem(new SeparatorMenuItem());

		// Adding location header
		menuAdapter.addHeader(new GroupImageMenuItem(getResources().getString(R.string.location), R.drawable.add_custom_view, new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(LocationScreenActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
			}
		}));

		if (mLocations.size() > 0) {

			// Adding location
			for (int i = 0; i < mLocations.size(); i++) {
				Location actLoc = mLocations.get(i);
				menuAdapter.addItem(new LocationMenuItem(actLoc.getName(), actLoc.getIconResource(), false, actLoc.getId()));
			}
		} else {
			menuAdapter.addItem(new EmptyMenuItem(mActivity.getResources().getString(R.string.no_location)));
		}

		// Adding custom view header
		menuAdapter.addHeader(new GroupImageMenuItem(getResources().getString(R.string.custom_view), R.drawable.add_custom_view, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO doplnit spusteni dialogu pro vytvoreni custom
				// view
				Toast.makeText(LocationScreenActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
			}
		}));
		// Adding custom views
		// TODO pridat custom views
		menuAdapter.addItem(new EmptyMenuItem(mActivity.getResources().getString(R.string.no_custom_view)));

		// Adding separator as header
		menuAdapter.addItem(new SeparatorMenuItem());

		// Adding settings, about etc.
		menuAdapter.addItem(new SettingMenuItem(getResources().getString(R.string.action_settings), R.drawable.settings, cz.vutbr.fit.iha.activity.menuItem.MenuItem.ID_SETTINGS));
		menuAdapter.addItem(new SettingMenuItem(getResources().getString(R.string.action_about), R.drawable.info, cz.vutbr.fit.iha.activity.menuItem.MenuItem.ID_ABOUT));

		// menuAdapter.log();
		return menuAdapter;
	}

	public boolean initMenu() {

		Log.d(TAG, "ready to work with Locations");
		mTitle = mDrawerTitle = "IHA";

		// Locate DrawerLayout in activity_location_screen.xml
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		// Locate ListView in activity_location_screen.xml
		mDrawerList = (StickyListHeadersListView) findViewById(R.id.listview_drawer);

		// Set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		mDrawerLayout.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
					Log.d(TAG, "BackPressed = " + String.valueOf(backPressed));
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

		// // Set the MenuListAdapter to the ListView
		// mDrawerList.setAdapter(mMenuAdapter);

		// Capture listview menu item click
		mDrawerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Controller controller = Controller.getInstance(LocationScreenActivity.this);
				cz.vutbr.fit.iha.activity.menuItem.MenuItem item = (cz.vutbr.fit.iha.activity.menuItem.MenuItem) mMenuAdapter.getItem(position);
				switch (item.getType()) {
				case ADAPTER:
					// if it is not chosen, switch to selected adapter
					if (!controller.getActiveAdapter().getId().equals(item.getId())) {

						setSupportProgressBarIndeterminateVisibility(true);
						mSwitchAdapter = new LocationScreenActivity.SwitchAdapter();
						mSwitchAdapter.execute(new String[] { item.getId() });

					}
					break;

				case CUSTOM_VIEW:
					// TODO otevrit custom view, jeste nedelame s customView
					// taze pozdeji
					break;

				case SETTING:
					if (item.getId().equals(cz.vutbr.fit.iha.activity.menuItem.MenuItem.ID_ABOUT)) {
						InfoDialogFragment dialog = new InfoDialogFragment();
						dialog.show(getSupportFragmentManager(), TAG_INFO);
					} else if (item.getId().equals(cz.vutbr.fit.iha.activity.menuItem.MenuItem.ID_SETTINGS)) {
						Intent intent = new Intent(LocationScreenActivity.this, SettingsMainActivity.class);
						startActivity(intent);
					}
					break;

				case LOCATION:
					// Get the title followed by the position
					mActiveLocationId = item.getId();

					changeLocation(mController.getLocation(mActiveLocationId), true);

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

				cz.vutbr.fit.iha.activity.menuItem.MenuItem item = (cz.vutbr.fit.iha.activity.menuItem.MenuItem) mMenuAdapter.getItem(position);
				switch (item.getType()) {
				case LOCATION:
					Bundle bundle = new Bundle();
					String myMessage = item.getId();
					bundle.putString("locationID", myMessage);
					Intent intent = new Intent(mActivity, LocationDetailActivity.class);
					intent.putExtras(bundle);
					startActivityForResult(intent, REQUEST_SENSOR_DETAIL);
					break;
				case ADAPTER:
					// FIXME: debug implementation -> need to set active adapter
					// manualy
					Log.e(TAG, "deleting adapter");
					// mController.setActiveAdapter(item.getId());
					// new Thread(new Runnable() {
					// @Override
					// public void run() {
					// if(mController.unregisterAdapter(mController.getActiveAdapter().getId())){
					// new ToastMessageThread(mActivity,
					// "adapter removed").start();
					// mActivity.runOnUiThread(new Runnable() {
					// @Override
					// public void run() {
					// redrawMenu();
					// }
					// });
					// }
					//
					// }
					// }).run();
					// RemoveAdapter_Debug runnable = new
					// RemoveAdapter_Debug(mActivity, item.getId());
					new Thread(new RemoveAdapter_Debug(mActivity, item.getId())).start();
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
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				if (backPressed)
					onBackPressed();
				// Set the title on the action when drawer closed
				if (mActiveLocation != null)
					getSupportActionBar().setTitle(mActiveLocation.getName());
				super.onDrawerClosed(view);
				Log.d(TAG, "BackPressed - onDrawerClosed " + String.valueOf(backPressed));

			}

			public void onDrawerOpened(View drawerView) {
				// Set the title on the action when drawer open
				getSupportActionBar().setTitle(mDrawerTitle);
				super.onDrawerOpened(drawerView);
				// backPressed = true;
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerToggle.syncState();
		// Enable ActionBar app icon to behave as action to toggle nav drawer
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setSupportProgressBarIndeterminateVisibility(false);

		mDrawerLayout.openDrawer(mDrawerList);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.content_frame, new MainFragment());
		ft.commit();

		return true;
	}

	private void setEmptyDevices() {
		getDevices(new ArrayList<Facility>());
	}

	private void changeLocation(Location location, boolean closeDrawer) {
		// save current location
		SharedPreferences prefs = mController.getUserSettings();
		Editor edit = prefs.edit();

		String pref_key = Persistence.getPreferencesLastLocation(mController.getActiveAdapter().getId());
		edit.putString(pref_key, location.getId());
		edit.commit();

		mActiveLocation = location;

		refreshListing();

		// mDrawerList.setItemChecked(position, true);

		// Close drawer
		if (closeDrawer) {
			mDrawerLayout.closeDrawer(mDrawerList);
		}
	}

	public boolean getDevices(final List<Facility> facilities) {
		Log.d(TAG, "LifeCycle: getsensors start");

		String[] title;
		String[] value;
		String[] unit;
		Time[] time;
		int[] icon;
		int[] relPos;
		int[] facSize;
		mTitle = mDrawerTitle = "IHA";

		// TODO: this works, but its not the best solution
		if (!ListOfDevices.ready) {
			mFacilities = facilities;
			mTimeRun = new Runnable() {
				@Override
				public void run() {
					getDevices(mFacilities);
					Log.d(TAG, "LifeCycle: getsensors in timer");
				}
			};
			if (!isPaused)
				mTimeHandler.postDelayed(mTimeRun, 500);
			Log.d(TAG, "LifeCycle: getsensors timer run");
			return false;
		}
		mTimeHandler.removeCallbacks(mTimeRun);
		Log.d(TAG, "LifeCycle: getsensors timer remove");

		mSensorList = (ListView) findViewById(R.id.listviewofsensors);
		TextView nosensor = (TextView) findViewById(R.id.nosensorlistview);
		ImageView addsensor = (ImageView) findViewById(R.id.nosensorlistview_addsensor_image);
		
		addsensor.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "HERE ADD SENSOR +");
				mController.unignoreUninitialized();

				inBackground = true;
				Intent intent = new Intent(LocationScreenActivity.this, AddSensorActivityDialog.class);
				startActivity(intent);
				return;
			}
		});

		mCntOfAllDev = 0;
		for (Facility facility : facilities) {
			mCntOfAllDev += facility.getDevices().size();
		}

		title = new String[mCntOfAllDev];
		value = new String[mCntOfAllDev];
		unit = new String[mCntOfAllDev];
		icon = new int[mCntOfAllDev];
		time = new Time[mCntOfAllDev];
		relPos = new int[mCntOfAllDev];
		facSize = new int[mCntOfAllDev];

		int iDev = 0;
		int relDev = 0;
		for (Facility facility : facilities) {
			relDev = 0;
			for (BaseDevice device : facility.getDevices()) {
				title[iDev] = device.getName();
				value[iDev] = device.getStringValue();
				unit[iDev] = device.getStringUnit(this);
				icon[iDev] = device.getTypeIconResource();
				time[iDev] = device.getFacility().lastUpdate;
				relPos[iDev] = relDev + 1;
				facSize[iDev] = facility.getDevices().size();
				relDev++;
				iDev++;
			}
			mActLocID = facility.getLocationId();
		}

		if (mSensorList == null) {
			setSupportProgressBarIndeterminateVisibility(false);
			Log.e(TAG, "LifeCycle: bad timing or what?");
			return false; // TODO: this happens when we're in different activity
							// (detail), fix that by changing that activity
							// (fragment?) first?
		}

		// If no sensor - display text only
		if (mCntOfAllDev == 0) {
			if (nosensor != null) {
				nosensor.setVisibility(View.VISIBLE);
				addsensor.setVisibility(View.VISIBLE);
				mSensorList.setVisibility(View.GONE);
			}

			this.setSupportProgressBarIndeterminateVisibility(false);
			return true;
		} else {
			nosensor.setVisibility(View.GONE);
			addsensor.setVisibility(View.GONE);
			mSensorList.setVisibility(View.VISIBLE);
		}

		mSensorAdapter = new SensorListAdapter(LocationScreenActivity.this, title, value, unit, time, icon, relPos, facSize);

		mSensorList.setAdapter(mSensorAdapter);

		// Capture listview menu item click
		mSensorList.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == mCntOfAllDev) {
					Log.d(TAG, "HERE ADD SENSOR +");
					mController.unignoreUninitialized();

					inBackground = true;
					Intent intent = new Intent(LocationScreenActivity.this, AddSensorActivityDialog.class);
					startActivity(intent);
					return;
				}

				// final BaseDevice selectedItem = devices.get(position);

				// setSupportProgressBarIndeterminateVisibility(true);

				Bundle bundle = new Bundle();
				String myMessage = mActLocID;
				bundle.putString("LocationOfSensorID", myMessage);
				bundle.putInt("SensorPosition", position);
				Intent intent = new Intent(mActivity, SensorDetailActivity.class);
				intent.putExtras(bundle);
				startActivityForResult(intent, REQUEST_SENSOR_DETAIL);
				// startActivity(intent);
				// finish();
			}
		});

		if (!isRefreshDone)
			isRefreshDone = true;
		this.setSupportProgressBarIndeterminateVisibility(false);
		Log.d(TAG, "LifeCycle: getsensors end");
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_SENSOR_DETAIL) {
			inBackground = true;
			setSupportProgressBarIndeterminateVisibility(false);

			// mController.reloadAdapters();
			refreshListing();

			Log.d(TAG, "Here");
		} else if (requestCode == REQUEST_ADD_ADAPTER) {
			redrawMenu();
		}
	}

	/**
	 * FIXME: change from protected New thread, it takes changes from server and refresh menu items
	 */
	public void redrawMenu() {
		setSupportProgressBarIndeterminate(true);
		setSupportProgressBarIndeterminateVisibility(true);
		mFacilitiesTask = new FacilitiesTask();
		mFacilitiesTask.execute();
	}

	// @Override
	// protected void onPostCreate(Bundle savedInstanceState) {
	// super.onPostCreate(savedInstanceState);
	// //FIXME: no adapters for user
	// if(mController.getActiveAdapter() == null)
	// return;
	//
	// // Sync the toggle state after onRestoreInstanceState has occurred.
	// mDrawerToggle.syncState();
	// }

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.location_screen, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
				mDrawerLayout.closeDrawer(mDrawerList);
			} else {
				mDrawerLayout.openDrawer(mDrawerList);
			}
			break;
		/*
		 * case R.id.action_refreshlist: { forceReloadListing = true; refreshListing();
		 * 
		 * break; }
		 */
		case R.id.action_addadapter: {
			inBackground = true;
			Intent intent = new Intent(LocationScreenActivity.this, AddAdapterActivityDialog.class);
			Bundle bundle = new Bundle();
			bundle.putBoolean(Constants.CANCEL, true);
			intent.putExtras(bundle);
			startActivityForResult(intent, REQUEST_ADD_ADAPTER);
			break;
		}
		/*
		 * case R.id.action_addsensor: { // Show also ignored devices mController.unignoreUninitialized();
		 * 
		 * inBackground = true; Intent intent = new Intent(LocationScreenActivity.this, AddSensorActivityDialog.class); startActivity(intent);
		 * 
		 * break; }
		 */
		case R.id.action_settings: {
			Intent intent = new Intent(LocationScreenActivity.this, SettingsMainActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.action_logout: {
			mController.logout();
			inBackground = false;
			Intent intent = new Intent(LocationScreenActivity.this, LoginActivity.class);
			startActivity(intent);
			this.finish();
			break;
		}
		}

		return super.onOptionsItemSelected(item);
	}

	protected void renameLocation(final String location, final TextView view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(LocationScreenActivity.this);

		// TODO: use better layout than just single EditText
		final EditText edit = new EditText(LocationScreenActivity.this);
		edit.setText(location);
		edit.selectAll();
		// TODO: show keyboard automatically

		builder.setCancelable(false).setView(edit).setTitle("Rename location").setNegativeButton("Cancel", null).setPositiveButton("Rename", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String newName = edit.getText().toString();

				// TODO: show loading while saving new name to
				// server (+ use asynctask)
				Location location = new Location(); // FIXME: get that original location from somewhere
				location.setName(newName);

				boolean saved = mController.saveLocation(location);

				String message = saved ? String.format("Location was renamed to '%s'", newName) : "Location wasn't renamed due to error";

				Toast.makeText(LocationScreenActivity.this, message, Toast.LENGTH_LONG).show();

				// Redraw item in list
				view.setText(newName);
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	/**
	 * Refresh sensors in actual location
	 */
	public void refreshListing() {
		if (mActiveLocation == null)
			return;
		isRefreshDone = false;
		setSupportProgressBarIndeterminateVisibility(true);
		mChangeLocationTask = new ChangeLocationTask();
		mChangeLocationTask.execute(new Location[] { mActiveLocation });
	}

	public boolean isRefreshListingDone() {
		return isRefreshDone;
	}

	public void setForceRefeshListing(boolean val) {
		forceReloadListing = val;
	}

	private void setNewAdapterRedraw(MenuListAdapter adapter) {
		mMenuAdapter = adapter;
		mDrawerList.setAdapter(mMenuAdapter);
	}

	private void setLocationOnStart(List<Location> locations) {
		String prefKey = Persistence.getPreferencesLastLocation(mController.getActiveAdapter().getId());

		SharedPreferences prefs = mController.getUserSettings();
		String locationID = prefs.getString(prefKey, null);

		if (locationID != null) {
			Location location = mController.getLocation(locationID);
			if (location != null) {
				Log.d("default", "DEFAULT POSITION: saved position selected");
				changeLocation(location, false);
				return;
			}
		}

		if (locations.size() > 0) {
			Log.d("default", "DEFAULT POSITION: first position selected");
			changeLocation(locations.get(0), false);
		} else {
			Log.d("default", "DEFAULT POSITION: Empty sensor set");
			Log.d("default", "EMPTY SENSOR SET");
			setEmptyDevices();
		}
	}

	private void setLocationOrEmpty() {
		final Adapter actAdapter = mController.getActiveAdapter();

		if (actAdapter != null) {
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setLocationOnStart(actAdapter.getLocations());
				}
			});

		}
	}

	/**
	 * Changes adapter and loads locations, checks for uninitialized devices and eventually shows dialog for adding them
	 */
	private class SwitchAdapter extends AsyncTask<String, Void, List<Facility>> {

		@Override
		protected List<Facility> doInBackground(String... params) {
			if (params.length > 0) {
				mController.setActiveAdapter(params[0]);
			}

			setLocationOrEmpty();
			return null;
		}

		@Override
		protected void onPostExecute(List<Facility> result) {
			redrawMenu();
			setSupportProgressBarIndeterminateVisibility(false);
		}

	}

	/**
	 * Loads locations, checks for uninitialized devices and eventually shows dialog for adding them
	 */
	private class FacilitiesTask extends AsyncTask<Void, Void, AdapterMenuFacilitiesPair> {

		private final CustomAlertDialog mDialog = new CustomAlertDialog(LocationScreenActivity.this);

		/**
		 * @return the dialog
		 */
		public CustomAlertDialog getDialog() {
			return mDialog;
		}

		@Override
		protected AdapterMenuFacilitiesPair doInBackground(Void... unused) {

			// FIXME: no adapters for user
			if (mController.getActiveAdapter() == null && !isClosing) {
				mLocations = new ArrayList<Location>();
				// ############################################
				// FIXME: no adapters for user
				if (!mController.getUserSettings().getBoolean(Constants.PERSISTENCE_PREF_IGNORE_NO_ADAPTER, false)) {
					Log.e(TAG, "kulehovnableskyted");
					Log.d(TAG, "data: " + Boolean.toString(isClosing) + " " + Boolean.toString(mController.getActiveAdapter() == null));
					Intent intent = new Intent(LocationScreenActivity.this, AddAdapterActivityDialog.class);
					Bundle bundle = new Bundle();
					bundle.putBoolean(Constants.CANCEL, true);
					intent.putExtras(bundle);
					startActivity(intent);
				}
				return new AdapterMenuFacilitiesPair(getMenuAdapter(), new ArrayList<Facility>());
			}

			// Load locations
			mLocations = mController.getActiveAdapter().getLocations();
			Log.d(TAG, String.format("Found %d locations", mLocations.size()));

			// Load uninitialized facilities
			List<Facility> facilities = mController.getUninitializedFacilities();
			Log.d(TAG, String.format("Found %d uninitialized facilities", facilities.size()));

			return new AdapterMenuFacilitiesPair(getMenuAdapter(), facilities);
		}

		@Override
		protected void onPostExecute(final AdapterMenuFacilitiesPair pair) {
			final List<Facility> uninitializedFacilities = pair.facilities;
			if (uninitializedFacilities == null)
				return;

			// Redraw locations
			setNewAdapterRedraw(pair.menuListAdapter);

			onOrientationChanged();

			setSupportProgressBarIndeterminate(false);
			setSupportProgressBarIndeterminateVisibility(false);

			// Do something with uninitialized facilities
			if (uninitializedFacilities.size() == 0)
				return;

			mDialog.setCancelable(false).setTitle(getString(R.string.notification_title))
					.setMessage(getResources().getQuantityString(R.plurals.notification_new_sensors, uninitializedFacilities.size(), uninitializedFacilities.size()));

			mDialog.setCustomNeutralButton(getString(R.string.notification_ingore), new OnClickListener() {
				@Override
				public void onClick(View v) {
					mController.ignoreUninitialized(uninitializedFacilities);
					// TODO: Get this string from resources
					Toast.makeText(LocationScreenActivity.this, "You can add these devices later through 'Menu / Add sensor'", Toast.LENGTH_LONG).show();
					mDialog.dismiss();
				}
			});

			mDialog.setCustomPositiveButton(getString(R.string.notification_add), new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Open activity for adding new facility
					inBackground = true;
					Intent intent = new Intent(LocationScreenActivity.this, SetupSensorActivityDialog.class);
					intent.putExtra(Constants.ADDSENSOR_COUNT_SENSOR, uninitializedFacilities.size());
					startActivity(intent);
					mDialog.dismiss();
				}
			});

			mDialog.show();
			Log.d(TAG, "LifeCycle: devicetask");
		}
	}

	/**
	 * Changes selected location and redraws list of adapters there
	 */
	private class ChangeLocationTask extends AsyncTask<Location, Void, List<Facility>> {

		@Override
		protected List<Facility> doInBackground(Location... locations) {
			List<Facility> facilities = mController.getFacilitiesByLocation(locations[0].getId(), forceReloadListing);
			// List<BaseDevice> devices = new ArrayList<BaseDevice>();

			// for (Facility facility : facilities) {
			// devices.addAll(facility.getDevices());
			// }

			Log.d(TAG, String.format("Found %d devices in location '%s'", facilities.size(), locations[0].getName()));
			forceReloadListing = false;

			return facilities;
		}

		@Override
		protected void onPostExecute(final List<Facility> facilities) {
			if (!isPaused)
				getDevices(facilities);

		}
	}

	class AnActionModeOfEpicProportions implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			menu.add("Save").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add("Cancel").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			// TODO Auto-generated method stub
			if (item.getTitle().equals("Save")) {
				// sName.setText(sNameEdit.getText());
			}
			// sNameEdit.setVisibility(View.GONE);
			// sName.setVisibility(View.VISIBLE);

			// sNameEdit.clearFocus();
			// getSherlockActivity().getCurrentFocus().clearFocus();
			// InputMethodManager imm = (InputMethodManager) getSystemService(
			// getBaseContext().INPUT_METHOD_SERVICE);
			// imm.hideSoftInputFromWindow(mDrawerItemEdit.getWindowToken(), 0);
			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// TODO Auto-generated method stub
			// sNameEdit.clearFocus();
			// sNameEdit.setVisibility(View.GONE);
			// sName.setVisibility(View.VISIBLE);
			mMode = null;

		}
	}

	private class RemoveAdapter_Debug implements Runnable {

		private String mAdapterId;
		private LocationScreenActivity mActivity;

		public RemoveAdapter_Debug(LocationScreenActivity activity, String adapterId) {
			mActivity = activity;
			mAdapterId = adapterId;
		}

		@Override
		public void run() {
			if (mController.unregisterAdapter(mAdapterId)) {
				new ToastMessageThread(mActivity, "adapter removed").start();
				setLocationOrEmpty();
				mActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mActivity.redrawMenu();
					}
				});
			}
		}
	}
}

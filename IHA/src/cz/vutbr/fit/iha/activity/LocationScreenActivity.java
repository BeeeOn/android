package cz.vutbr.fit.iha.activity;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.household.ActualUser;

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

	private static boolean inBackground = false;
	private static boolean isPaused = false;
	private static boolean forceReloadListing = false;

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

	private List<BaseDevice> mSensors;
	private Handler mTimeHandler = new Handler();
	private Runnable mTimeRun;

	/**
	 * Tasks which can be running in this activity and after finishing can try
	 * to change GUI -> must be cancelled when activity stop
	 */
	private DevicesTask mDevicesTask;
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
	}

	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume  , inBackground: " + String.valueOf(inBackground));
		setLocationOrEmpty();
		redrawMenu();
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

		if (mDevicesTask != null) {
			mDevicesTask.cancel(true);
			if (mDevicesTask.getDialog() != null) {
				mDevicesTask.getDialog().dismiss();
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
		// this.finish();

		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	public void onBackPressed() {
		// second click
		if (backPressed) {
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
		if (adapters.size() > 1) {
			// Adding separator as item (we don't want to let it float as header)
			menuAdapter.addItem(new SeparatorMenuItem());

			// Adding adapters
			Adapter chosenAdapter = mController.getActiveAdapter();
			for (Adapter actAdapter : adapters) {
				menuAdapter.addItem(new AdapterMenuItem(actAdapter.getName(), actAdapter.getRole().name(), chosenAdapter.getId().equals(actAdapter.getId()), actAdapter.getId()));
			}
		}

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
				menuAdapter.addItem(new LocationMenuItem(actLoc.getName(), actLoc.getIconResource(), i != 0, actLoc.getId()));
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
		menuAdapter.addItem(new SettingMenuItem(getResources().getString(R.string.action_settings), R.drawable.loc_unknown, cz.vutbr.fit.iha.activity.menuItem.MenuItem.ID_SETTINGS));
		menuAdapter.addItem(new SettingMenuItem(getResources().getString(R.string.action_about), R.drawable.loc_unknown, cz.vutbr.fit.iha.activity.menuItem.MenuItem.ID_ABOUT));

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
							mSwitchAdapter.execute(item.getId());

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

	private void setEmptySensors() {

		getSensors(new ArrayList<BaseDevice>());
	}

	private void changeLocation(Location location, boolean closeDrawer) {
		mActiveLocation = location;

		refreshListing();

		// mDrawerList.setItemChecked(position, true);

		// Close drawer
		if (closeDrawer) {
			mDrawerLayout.closeDrawer(mDrawerList);
		}
	}

	public boolean getSensors(final List<BaseDevice> sensors) {
		Log.d(TAG, "LifeCycle: getsensors start");

		String[] title;
		String[] value;
		String[] unit;
		Time[] time;
		int[] icon;
		mTitle = mDrawerTitle = "IHA";

		// TODO: this works, but its not the best solution
		if (!ListOfSensors.ready) {
			mSensors = sensors;
			mTimeRun = new Runnable() {
				@Override
				public void run() {
					getSensors(mSensors);
					Log.d(TAG, "LifeCycle: getsensors in timer");
				}
			};
			if(!isPaused)
				mTimeHandler.postDelayed(mTimeRun, 500);
			Log.d(TAG, "LifeCycle: getsensors timer run");
			return false;
		}
		mTimeHandler.removeCallbacks(mTimeRun);
		Log.d(TAG, "LifeCycle: getsensors timer remove");

		mSensorList = (ListView) findViewById(R.id.listviewofsensors);
		TextView nosensor = (TextView) findViewById(R.id.nosensorlistview);
		ImageView addsensorImg = (ImageView) findViewById(R.id.nosensor_addsensor);

		title = new String[sensors.size()];
		value = new String[sensors.size()];
		unit = new String[sensors.size()];
		icon = new int[sensors.size()];
		time = new Time[sensors.size()];
		for (int i = 0; i < sensors.size(); i++) {
			title[i] = sensors.get(i).getName();
			value[i] = sensors.get(i).getStringValue();
			unit[i] = sensors.get(i).getStringUnit(this);
			icon[i] = sensors.get(i).getTypeIconResource();
			time[i] = sensors.get(i).lastUpdate;
		}

		if (mSensorList == null) {
			setSupportProgressBarIndeterminateVisibility(false);
			Log.e(TAG, "LifeCycle: bad timing or what?");
			return false; // TODO: this happens when we're in different activity
							// (detail), fix that by changing that activity
							// (fragment?) first?
		}

		// If no sensor - display text only
		if (sensors.size() == 0) {
			if (nosensor != null) {
				nosensor.setVisibility(View.VISIBLE);
				mSensorList.setVisibility(View.GONE);
				addsensorImg.setVisibility(View.VISIBLE);
				addsensorImg.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						inBackground = true;
						Intent intent = new Intent(LocationScreenActivity.this, AddSensorActivityDialog.class);
						startActivity(intent);
					}
				});
			}

			this.setSupportProgressBarIndeterminateVisibility(false);
			return true;
		} else {
			nosensor.setVisibility(View.GONE);
			mSensorList.setVisibility(View.VISIBLE);
			addsensorImg.setVisibility(View.GONE);
		}

		mSensorAdapter = new SensorListAdapter(LocationScreenActivity.this, title, value, unit, time, icon);

		mSensorList.setAdapter(mSensorAdapter);

		// Capture listview menu item click
		mSensorList.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(position == sensors.size()) {
					Log.d(TAG, "HERE ADD SENSOR +");
					mController.unignoreUninitialized();

					inBackground = true;
					Intent intent = new Intent(LocationScreenActivity.this, AddSensorActivityDialog.class);
					startActivity(intent);
					return;
				}
				
				final BaseDevice selectedItem = sensors.get(position);

				// setSupportProgressBarIndeterminateVisibility(true);

				Bundle bundle = new Bundle();
				String myMessage = selectedItem.getLocationId();
				bundle.putString("LocationOfSensorID", myMessage);
				bundle.putInt("SensorPosition", position);
				Intent intent = new Intent(mActivity, SensorDetailActivity.class);
				intent.putExtras(bundle);
				startActivityForResult(intent, REQUEST_SENSOR_DETAIL);
				// startActivity(intent);
				// finish();
			}
		});

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

			//mController.reloadAdapters();
			refreshListing();

			Log.d(TAG, "Here");
		} else if (requestCode == REQUEST_ADD_ADAPTER) {
			redrawMenu();
		}
	}

	/**
	 * New thread, it takes changes from server and refresh menu items
	 */
	protected void redrawMenu() {
		setSupportProgressBarIndeterminate(true);
		setSupportProgressBarIndeterminateVisibility(true);
		// if (!inBackground) {
		mDevicesTask = new DevicesTask();
		mDevicesTask.execute();
		// }
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

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
			case R.id.action_refreshlist: {
				forceReloadListing = true;
				refreshListing();

				break;
			}
			case R.id.action_addadapter: {
				inBackground = true;
				Intent intent = new Intent(LocationScreenActivity.this, AddAdapterActivityDialog.class);
				Bundle bundle = new Bundle();
				bundle.putBoolean("Cancel", true);
				intent.putExtras(bundle);
				startActivityForResult(intent, REQUEST_ADD_ADAPTER);
				break;
			}/*
			case R.id.action_addsensor: {
				// Show also ignored devices
				mController.unignoreUninitialized();

				inBackground = true;
				Intent intent = new Intent(LocationScreenActivity.this, SetupSensorActivityDialog.class);
				startActivity(intent);

				break;
			}*/
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

				// TODO: show loading while saving new name to server (+ use asynctask)
				Location location = new Location(); // FIXME: get that original location from somewhere
				location.setName(newName);

				boolean saved = mController.saveLocation(location) != null;

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
	private void refreshListing() {
		if (mActiveLocation == null)
			return;

		setSupportProgressBarIndeterminateVisibility(true);
		mChangeLocationTask = new ChangeLocationTask();
		mChangeLocationTask.execute(new Location[] { mActiveLocation });
	}

	private void setNewAdapterRedraw(MenuListAdapter adapter) {
		mMenuAdapter = adapter;
		mDrawerList.setAdapter(mMenuAdapter);
	}

	private void setLocationOnStart(List<Location> locations) {
		if (locations.size() > 0) {
			Log.d("default", "DEFAULT POSITION: first position selected");
			changeLocation(locations.get(0), false);
		} else {
			Log.d("default", "DEFAULT POSITION: Empty sensor set");
			Log.d("default", "EMPTY SENSOR SET");
			setEmptySensors();
		}
	}

	private void setLocationOrEmpty() {
		Adapter actAdapter = mController.getActiveAdapter();

		if (actAdapter != null) {
			setLocationOnStart(actAdapter.getLocations());
		}
	}

	/**
	 * Loads locations, checks for uninitialized devices and eventually shows
	 * dialog for adding them
	 */
	private class SwitchAdapter extends AsyncTask<String, Void, List<BaseDevice>> {

		@Override
		protected List<BaseDevice> doInBackground(String... params) {
			mController.setActiveAdapter(params[0]);

			return null;
		}

		@Override
		protected void onPostExecute(List<BaseDevice> result) {
			setLocationOrEmpty();
			redrawMenu();
			setSupportProgressBarIndeterminateVisibility(false);
		}

	}

	/**
	 * Loads locations, checks for uninitialized devices and eventually shows
	 * dialog for adding them
	 */
	private class DevicesTask extends AsyncTask<Void, Void, List<BaseDevice>> {

		private final CustomAlertDialog mDialog = new CustomAlertDialog(LocationScreenActivity.this);

		/**
		 * @return the dialog
		 */
		public CustomAlertDialog getDialog() {
			return mDialog;
		}

		@Override
		protected List<BaseDevice> doInBackground(Void... unused) {
			// Load locations
			mLocations = mController.getActiveAdapter().getLocations();
			Log.d(TAG, String.format("Found %d locations", mLocations.size()));

			// Load uninitialized devices
			List<BaseDevice> devices = mController.getUninitializedDevices();
			Log.d(TAG, String.format("Found %d uninitialized devices", devices.size()));

			return devices;
		}

		@Override
		protected void onPostExecute(final List<BaseDevice> uninitializedDevices) {
			// Redraw locations
			setNewAdapterRedraw(getMenuAdapter());

			onOrientationChanged();

			setSupportProgressBarIndeterminate(false);
			setSupportProgressBarIndeterminateVisibility(false);

			// Do something with uninitialized devices
			if (uninitializedDevices.size() == 0)
				return;

			mDialog.setCancelable(false).setTitle(getString(R.string.notification_title)).setMessage(getResources().getQuantityString(R.plurals.notification_new_sensors, uninitializedDevices.size(), uninitializedDevices.size()));

			mDialog.setCustomNeutralButton(getString(R.string.notification_ingore), new OnClickListener() {
				@Override
				public void onClick(View v) {
					mController.ignoreUninitialized(uninitializedDevices);
					// TODO: Get this string from resources
					Toast.makeText(LocationScreenActivity.this, "You can add these devices later through 'Menu / Add sensor'", Toast.LENGTH_LONG).show();
					mDialog.dismiss();
				}
			});

			mDialog.setCustomPositiveButton(getString(R.string.notification_add), new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Open activity for adding new device
					inBackground = true;
					Intent intent = new Intent(LocationScreenActivity.this, SetupSensorActivityDialog.class);
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
	private class ChangeLocationTask extends AsyncTask<Location, Void, List<BaseDevice>> {

		@Override
		protected List<BaseDevice> doInBackground(Location... locations) {
			List<BaseDevice> devices = mController.getDevicesByLocation(locations[0].getId(), forceReloadListing);
			Log.d(TAG, String.format("Found %d devices in location '%s'", devices.size(), locations[0].getName()));
			forceReloadListing = false;

			return devices;
		}

		@Override
		protected void onPostExecute(final List<BaseDevice> devices) {
			if(!isPaused)
				getSensors(devices);

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

}

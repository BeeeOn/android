package cz.vutbr.fit.iha.activity;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
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
import cz.vutbr.fit.iha.activity.menuItem.AdapterMenuItem;
import cz.vutbr.fit.iha.activity.menuItem.GroupImageMenuItem;
import cz.vutbr.fit.iha.activity.menuItem.GroupMenuItem;
import cz.vutbr.fit.iha.activity.menuItem.LocationMenuItem;
import cz.vutbr.fit.iha.activity.menuItem.ProfileMenuItem;
import cz.vutbr.fit.iha.activity.menuItem.SeparatorMenuItem;
import cz.vutbr.fit.iha.activity.menuItem.SettingMenuItem;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.network.ActualUser;

/**
 * Activity class for choosing location
 * 
 * @author ThinkDeep
 * @author Robyer
 * 
 */
public class LocationScreenActivity extends BaseActivity {

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

	private static final String TAG = "Location";

	private static boolean inBackground = false;
	private static final String BKG = "activityinbackground";

	private static int SENSOR_DETAIL = 1;

	private Location mActiveLocation;
	private String mActiveLocationId;
	private static final String LCTN = "lastlocation";
	private static boolean mOrientation = false;
	private List<BaseDevice> mSensors;
	private Handler mTimeHandler = new Handler();
	private Runnable mTimeRun;

	private DevicesTask mTask;

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
			mActiveLocationId = savedInstanceState.getString(LCTN);
			if(mActiveLocationId != null)
				mOrientation = true;
		}
	}

	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume  , inBackground: "+String.valueOf(inBackground));
		if (!inBackground) {
			mTask = new DevicesTask();
			mTask.execute();
		}
		backPressed = false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		if (mTask != null) {
			if (mTask.getDialog() != null) {
				mTask.getDialog().dismiss();
			}
		}
	}

	@Override
	public void onBackPressed() {
		if (backPressed) {
			// Toast.makeText(this, getString(R.string.toast_leaving_app),
			// Toast.LENGTH_LONG).show();
			super.onBackPressed();
			// this.finish();

			android.os.Process.killProcess(android.os.Process.myPid());
		} else {
			backPressed = true;
			if (mDrawerLayout != null)
				mDrawerLayout.openDrawer(mDrawerList);
		}
		Log.d(TAG, "BackPressed - onBackPressed " + String.valueOf(backPressed));

		return;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstaceState) {
		savedInstaceState.putBoolean(BKG, inBackground);
		savedInstaceState.putString(LCTN, mActiveLocationId);
		super.onSaveInstanceState(savedInstaceState);
	}

	public static void healActivity() {
		inBackground = false;
	}

	public void onOrientationChanged(){
		if(mOrientation){
			mActiveLocation = mController.getLocation(mActiveLocationId);
	
			refreshListing();
	
			// Close drawer
			mDrawerLayout.closeDrawer(mDrawerList);
			Log.d("LifeCycle", "onOrientation");
		}
		mOrientation = false;
	}
	
	public boolean getLocations(List<Location> locs) {

		Log.d(TAG, "ready to work with Locations");
		mTitle = mDrawerTitle = "IHA";

		// Locate DrawerLayout in activity_location_screen.xml
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		// Locate ListView in activity_location_screen.xml
		mDrawerList = (StickyListHeadersListView) findViewById(R.id.listview_drawer);

		// Set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		// Pass string arrays to MenuListAdapter
		mMenuAdapter = new MenuListAdapter(LocationScreenActivity.this);

		// FIXME zmenit obrazek na obrazek uzivatele
		Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.loc_unknown);
		Controller controller = Controller.getInstance(this);

		ActualUser actUser = ActualUser.getActualUser();

		// mMenuAdapter.addHeader(new GroupMenuItem("GROUP1"));
		// mMenuAdapter.addItem(new GroupMenuItem("1:Item1"));
		// mMenuAdapter.addItem(new GroupMenuItem("1:Item2"));
		// mMenuAdapter.addItem(new GroupMenuItem("1:Item3"));
		//
		// mMenuAdapter.addHeader(new GroupMenuItem("GROUP2"));
		// mMenuAdapter.addItem(new GroupMenuItem("2:Item1"));
		// mMenuAdapter.addItem(new GroupMenuItem("2:Item2"));
		// mMenuAdapter.addItem(new GroupMenuItem("2:Item3"));

		// Adding profile header
		mMenuAdapter.addHeader(new ProfileMenuItem(actUser.getName(), actUser.getEmail(), largeIcon));

		

		List<Adapter> adapters = controller.getAdapters();
		if (adapters.size() > 1) {
			// Adding separator as item (we don't want to let it float as header)
			mMenuAdapter.addItem(new SeparatorMenuItem());
			
			// Adding adapters
			Adapter chosenAdapter = controller.getActiveAdapter();
			for (Adapter actAdapter : adapters) {
				mMenuAdapter
						.addItem(new AdapterMenuItem(actAdapter.getName(),
								actAdapter.getRole().name(), chosenAdapter
										.getId().equals(actAdapter.getId()),
								actAdapter.getId()));
			}
		}

		// Adding separator as item (we don't want to let it float as header)
		mMenuAdapter.addItem(new SeparatorMenuItem());

		// Adding location header
		mMenuAdapter.addHeader(new GroupMenuItem(getResources().getString(R.string.location)));

		// Adding location
		for (int i = 0; i < locs.size(); i++) {
			Location actLoc = locs.get(i);
			mMenuAdapter.addItem(new LocationMenuItem(actLoc.getName(), actLoc.getIconResource(), i != 0, actLoc.getId()));
		}

		// Adding custom view header
		mMenuAdapter.addHeader(new GroupImageMenuItem(getResources().getString(R.string.custom_view), R.drawable.add_custom_view, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO doplnit spusteni dialogu pro vytvoreni custom
				// view
				Toast.makeText(LocationScreenActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
			}
		}));
		// Adding custom views
		// TODO pridat custom views

		// Adding separator as header
		mMenuAdapter.addItem(new SeparatorMenuItem());

		// Adding settings, about etc.
		mMenuAdapter.addItem(new SettingMenuItem(getResources().getString(R.string.action_settings), R.drawable.loc_unknown, cz.vutbr.fit.iha.activity.menuItem.MenuItem.ID_SETTINGS));
		mMenuAdapter.addItem(new SettingMenuItem(getResources().getString(R.string.action_about), R.drawable.loc_unknown, cz.vutbr.fit.iha.activity.menuItem.MenuItem.ID_ABOUT));

		// mMenuAdapter.log();

		// Set the MenuListAdapter to the ListView
		mDrawerList.setAdapter(mMenuAdapter);

		// Capture listview menu item click
		mDrawerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Controller controller = Controller.getInstance(LocationScreenActivity.this);
				cz.vutbr.fit.iha.activity.menuItem.MenuItem item = (cz.vutbr.fit.iha.activity.menuItem.MenuItem) mMenuAdapter.getItem(position);
				switch (item.getType()) {
				case ADAPTER:
					// if it is not chosen, switch to selected adapter
					if (controller.getActiveAdapter().getId()
							.equals(item.getId())) {
						// TODO prepnuti addapteru, prekreslit menuListDrawer
					}
					break;

				case CUSTOM_VIEW:
					// TODO otevrit custom view, jeste nedelame s customView
					// taze pozdeji
					break;

				case SETTING:
					if (item.getId()
							.equals(cz.vutbr.fit.iha.activity.menuItem.MenuItem.ID_ABOUT)) {
						InfoDialogFragment dialog = new InfoDialogFragment();
						dialog.show(getSupportFragmentManager(), TAG_INFO);
					} else if (item
							.getId()
							.equals(cz.vutbr.fit.iha.activity.menuItem.MenuItem.ID_SETTINGS)) {
						Intent intent = new Intent(LocationScreenActivity.this,
								SettingsActivity.class);
						startActivity(intent);
					}
					break;

				case LOCATION:
					// Get the title followed by the position
					mActiveLocationId = item.getId();
					mActiveLocation = controller.getLocation(mActiveLocationId);

					refreshListing();

					mDrawerList.setItemChecked(position, true);

					// Close drawer
					mDrawerLayout.closeDrawer(mDrawerList);
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

				cz.vutbr.fit.iha.activity.menuItem.MenuItem item = (cz.vutbr.fit.iha.activity.menuItem.MenuItem) mMenuAdapter
						.getItem(position);
				switch (item.getType()) {
					case LOCATION:
						Bundle bundle = new Bundle();
						String myMessage = item.getId();
						bundle.putString("locationID", myMessage);
						Intent intent = new Intent(mActivity, LocationDetailActivity.class);
						intent.putExtras(bundle);
						startActivityForResult(intent, SENSOR_DETAIL);
						break;
					default:
						// do nothing
						break;
				}
				return true;
			}

		});

		// Enable ActionBar app icon to behave as action to toggle nav drawer
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
		this.setSupportProgressBarIndeterminateVisibility(false);

		mDrawerLayout.openDrawer(mDrawerList);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.content_frame, new MainFragment());
		ft.commit();

		return true;
	}

	public boolean getSensors(final List<BaseDevice> sensors) {
		Log.d("LifeCycle", "getsensors start");
		
		//TODO: this works, but its not the best solution
	/*if(!ListOfSensors.ready){
			mSensors = sensors;
			mTimeRun = new Runnable() {
				@Override
				public void run() {
					getSensors(mSensors);
					Log.d("LifeCycle", "getsensors in timer");
				}
			};
			mTimeHandler.postDelayed(mTimeRun, 500);
			Log.d("LifeCycle", "getsensors timer run");
			return false;
		}
		mTimeHandler.removeCallbacks(mTimeRun);
		Log.d("LifeCycle", "getsensors timer remove");*/

		String[] title;
		String[] value;
		String[] unit;
		Time[] time;
		int[] icon;
		mTitle = mDrawerTitle = "IHA";
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

		mSensorList = (ListView) findViewById(R.id.listviewofsensors);
		if (mSensorList == null) {
			setSupportProgressBarIndeterminateVisibility(false);
			Log.e("LifeCycle", "bad timing or what?");
			return false; // TODO: this happens when we're in different activity
							// (detail), fix that by changing that activity
							// (fragment?) first?
		}

		mSensorAdapter = new SensorListAdapter(LocationScreenActivity.this, title, value, unit, time, icon);

		mSensorList.setAdapter(mSensorAdapter);

		// Capture listview menu item click
		mSensorList.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final BaseDevice selectedItem = sensors.get(position);

				//setSupportProgressBarIndeterminateVisibility(true);

				Bundle bundle = new Bundle();
				String myMessage = selectedItem.getLocationId();
				bundle.putString("LocationOfSensorID", myMessage);
				bundle.putInt("SensorPosition", position);
				Intent intent = new Intent(mActivity, SensorDetailActivity.class);
				intent.putExtras(bundle);
				startActivityForResult(intent, SENSOR_DETAIL);
				// startActivity(intent);
				// finish();
			}
		});

		this.setSupportProgressBarIndeterminateVisibility(false);
		Log.d("LifeCycle", "getsensors end");
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SENSOR_DETAIL) {
			inBackground = true;
			setSupportProgressBarIndeterminateVisibility(false);

			mController.reloadAdapters();
			refreshListing();

			Log.d(TAG, "Here");
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.

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
				mController.reloadAdapters();
				refreshListing();
				break;
			}
			case R.id.action_addadapter: {
				// Toast.makeText(this, "go to old", Toast.LENGTH_LONG).show();

				// Intent intent = new Intent(LocationScreenActivity.this,
				// AddAdapterActivity.class);
				inBackground = true;
				Intent intent = new Intent(LocationScreenActivity.this, AddAdapterActivityDialog.class);
				startActivity(intent);
				break;
			}
			case R.id.action_addsensor: {
				// Toast.makeText(this, "go to old", Toast.LENGTH_LONG).show();

				// Show also ignored devices
				mController.unignoreUninitialized();

				inBackground = true;
				Intent intent = new Intent(LocationScreenActivity.this, AddSensorActivityDialog.class);
				startActivity(intent);

			break;
		}
		case R.id.action_settings: {
			Intent intent = new Intent(LocationScreenActivity.this,
					SettingsActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.action_logout: {
			mController.logout();
			inBackground = false;
			Intent intent = new Intent(LocationScreenActivity.this,
					LoginActivity.class);
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
				// server (+ use
				// asynctask)
				Location location = new Location(); // FIXME:
													// get that
													// original
													// location
													// from
													// somewhere
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

	private void refreshListing() {
		if (mActiveLocation == null)
			return;

		setSupportProgressBarIndeterminateVisibility(true);
		ChangeLocationTask task = new ChangeLocationTask();
		task.execute(new Location[] { mActiveLocation });
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
			mLocations = mController.getLocations();
			Log.d(TAG, String.format("Found %d locations", mLocations.size()));

			// Load uninitialized devices
			List<BaseDevice> devices = mController.getUninitializedDevices();
			Log.d(TAG, String.format("Found %d uninitialized devices", devices.size()));

			return devices;
		}

		@Override
		protected void onPostExecute(final List<BaseDevice> uninitializedDevices) {
			// Redraw locations
			getLocations(mLocations);
			onOrientationChanged();

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
					Intent intent = new Intent(LocationScreenActivity.this, AddSensorActivityDialog.class);
					startActivity(intent);
					mDialog.dismiss();
				}
			});

			mDialog.show();
			Log.d("LifeCycle", "devicetask");
		}
	}

	/**
	 * Changes selected location and redraws list of adapters there
	 */
	private class ChangeLocationTask extends AsyncTask<Location, Void, List<BaseDevice>> {

		@Override
		protected List<BaseDevice> doInBackground(Location... locations) {
			List<BaseDevice> devices = mController.getDevicesByLocation(locations[0].getId());
			Log.d(TAG, String.format("Found %d devices in location '%s'", devices.size(), locations[0].getName()));
			
			return devices;
		}

		@Override
		protected void onPostExecute(final List<BaseDevice> devices) {
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

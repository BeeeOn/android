package cz.vutbr.fit.iha.activity;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import cz.vutbr.fit.iha.Compatibility;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.MenuListAdapter;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.SensorListAdapter;
import cz.vutbr.fit.iha.activity.dialog.AddAdapterActivityDialog;
import cz.vutbr.fit.iha.activity.dialog.AddSensorActivityDialog;
import cz.vutbr.fit.iha.activity.dialog.CustomAlertDialog;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.listing.Location;
import cz.vutbr.fit.iha.listing.LocationListing;

/**
 * Activity class for choosing location
 * 
 * @author ThinkDeep
 * @author Robyer
 * 
 */
public class LocationScreenActivity extends SherlockFragmentActivity {

	private Controller mController;
	private LocationScreenActivity mActivity;
	private List<Location> mLocations;

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
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

	private DevicesTask mTask;
	
	private boolean backPressed = false;

	/**
	 * Call XML parser to file on SDcard
	 */
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
		// int marginTop = 5;
		// int ID = Constants.BUTTON_ID;
		/*
		 * for(LocationListing location : locations) { if
		 * (addLocationButton(location.getName(), ID, marginTop)) ID++; }
		 * if(locations.size() == 1){ Button onlyOne =
		 * (Button)findViewById(--ID); onlyOne.performClick(); }
		 */

		if (savedInstanceState != null)
			inBackground = savedInstanceState.getBoolean(BKG);
	}

	public void onResume() {
		super.onResume();
		//healActivity();
		Log.d(TAG, "onResume");
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
		if(backPressed){
			//Toast.makeText(this, getString(R.string.toast_leaving_app), Toast.LENGTH_LONG).show();
			super.onBackPressed();
			//this.finish();
			
			android.os.Process.killProcess(android.os.Process.myPid());
		}
		else{
			backPressed = true;
			if(mDrawerLayout != null)
				mDrawerLayout.openDrawer(mDrawerList);
		}
		Log.d(TAG, "BackPressed - onBackPressed " + String.valueOf(backPressed));
		
	return;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstaceState) {
		savedInstaceState.putBoolean(BKG, inBackground);
		super.onSaveInstanceState(savedInstaceState);
	}

	public static void healActivity() {
		inBackground = false;
	}

	public boolean getLocations(List<Location> locs) {

		Log.d(TAG, "ready to work with Locations");
		mTitle = mDrawerTitle = "IHA";
		String[] title = new String[locs.size()];
		String[] subtitle = new String[locs.size()];
		int[] icon = new int[locs.size()];
		for (int i = 0; i < locs.size(); i++) {
			title[i] = locs.get(i).getName();
			subtitle[i] = locs.get(i).getName();
			icon[i] = locs.get(i).getIconResource();
		}

		// Locate DrawerLayout in activity_location_screen.xml
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		// Locate ListView in activity_location_screen.xml
		mDrawerList = (ListView) findViewById(R.id.listview_drawer);

		// Set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		// Pass string arrays to MenuListAdapter
		mMenuAdapter = new MenuListAdapter(LocationScreenActivity.this, title,
				subtitle, icon);

		// Set the MenuListAdapter to the ListView
		mDrawerList.setAdapter(mMenuAdapter);

		// Capture listview menu item click
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// Enable ActionBar app icon to behave as action to toggle nav drawer
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		//getSupportActionBar().setIcon(R.drawable.ic_launcher_white);
		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				if(backPressed) 
					onBackPressed();
				// Set the title on the action when drawer closed
				getSupportActionBar().setTitle(mActiveLocation.getName());
				super.onDrawerClosed(view);
				Log.d(TAG, "BackPressed - onDrawerClosed " + String.valueOf(backPressed));
				
				
			}

			public void onDrawerOpened(View drawerView) {
				// Set the title on the action when drawer open
				getSupportActionBar().setTitle(mDrawerTitle);
				super.onDrawerOpened(drawerView);
				//backPressed = true;
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
			return false; // TODO: this happens when we're in different activity
							// (detail), fix that by changing that activity
							// (fragment?) first?
		}

		mSensorAdapter = new SensorListAdapter(LocationScreenActivity.this,	title, value, unit,time, icon);

		mSensorList.setAdapter(mSensorAdapter);

		// Capture listview menu item click
		mSensorList.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();

				final BaseDevice selectedItem = sensors.get(position);

				setSupportProgressBarIndeterminateVisibility(true);

				Bundle bundle = new Bundle();
				String myMessage = selectedItem.getId();
				bundle.putString("sensorID", myMessage);
				// SensorDetailFragment fragment = new SensorDetailFragment();
				// fragment.setArguments(bundle);

				// ft.replace(R.id.content_frame, fragment);
				// ft.addToBackStack(null);
				// ft.commit();

				Intent intent = new Intent(mActivity,
						SensorDetailActivity.class);
				intent.putExtras(bundle);
				startActivityForResult(intent, SENSOR_DETAIL);
				// startActivity(intent);
				// finish();
			}
		});

		this.setSupportProgressBarIndeterminateVisibility(false);
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

	// ListView click listener in the navigation drawer
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// Get the title followed by the position
			mActiveLocation = mLocations.get(position);

			refreshListing();

			mDrawerList.setItemChecked(position, true);

			// Close drawer
			mDrawerLayout.closeDrawer(mDrawerList);
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
			Intent intent = new Intent(LocationScreenActivity.this,
					AddAdapterActivityDialog.class);
			startActivity(intent);
			break;
		}
		case R.id.action_addsensor: {
			// Toast.makeText(this, "go to old", Toast.LENGTH_LONG).show();

			// Show also ignored devices
			mController.unignoreUninitialized();

			inBackground = true;
			Intent intent = new Intent(LocationScreenActivity.this,
					AddSensorActivityDialog.class);
			startActivity(intent);

			break;
		}
		case R.id.action_settings: {
			Intent intent = new Intent(LocationScreenActivity.this,
					PreferenceActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.action_logout: {
			mController.logout();
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
		AlertDialog.Builder builder = new AlertDialog.Builder(
				LocationScreenActivity.this);

		// TODO: use better layout than just single EditText
		final EditText edit = new EditText(LocationScreenActivity.this);
		edit.setText(location);
		edit.selectAll();
		// TODO: show keyboard automatically

		builder.setCancelable(false)
				.setView(edit)
				.setTitle("Rename location")
				.setNegativeButton("Cancel", null)
				.setPositiveButton("Rename",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String newName = edit.getText().toString();

								// TODO: show loading while saving new name to server (+ use asynctask)
								Location location = new Location(); // FIXME: get that original location from somewhere
								location.setName(newName);
								
								boolean saved = mController.saveLocation(location);

								String message = saved
										? String.format("Location was renamed to '%s'", newName)
										: "Location wasn't renamed due to error";

								Toast.makeText(LocationScreenActivity.this, message, Toast.LENGTH_LONG).show();

								// Redraw item in list
								view.setText(newName);
							}
						});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	/**
	 * Draw a button to GUI
	 * 
	 * @param s
	 *            specific location name
	 * @param ID
	 *            id of button
	 * @param marginTop
	 *            margin of the button
	 * @return true on success and false in other cases
	 *//*
	private boolean addLocationButton(String s, int ID, int marginTop) {
		final Button button = new Button(this);
		button.setText(s);
		button.setTextSize(getResources().getDimension(R.dimen.textsize));
		button.setId(ID);
		if (s == null || s.length() < 1)
			return false;

		// LinearLayout mylayout =
		// (LinearLayout)findViewById(R.id.location_scroll);
		// mylayout.setOrientation(LinearLayout.VERTICAL);
		// drawerListView = (ListView) findViewById(R.id.left_drawer);

		LinearLayout.LayoutParams params_btn = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params_btn.setMargins(0, marginTop, 0, 0);
		button.setLayoutParams(params_btn);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Button clicked = (Button) v;
				Log.i("kliknuto-na", clicked.getText().toString());

				Intent intent = new Intent(getBaseContext(),
						DataOfLocationScreenActivity.class);
				intent.putExtra(Constants.LOCATION_CLICKED, clicked.getText()
						.toString());
				startActivity(intent);
			}
		});
		button.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				renameLocation(button.getText().toString(), button);
				return true;
			}
		});

		Compatibility.setBackground(button,
				getResources().getDrawable(R.drawable.shape));

		// mylayout.addView(button);
		return true;
	}
*/
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

			// Do something with uninitialized devices
			if (uninitializedDevices.size() == 0)
				return;

			mDialog.setCancelable(false)
					.setTitle(getString(R.string.notification_title))
					.setMessage(getResources().getQuantityString(
									R.plurals.notification_new_sensors,
									uninitializedDevices.size(),
									uninitializedDevices.size()));

			mDialog.setCustomNeutralButton(getString(R.string.notification_ingore),
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							mController.ignoreUninitialized(uninitializedDevices);
							// TODO: Get this string from resources
							Toast.makeText(
									LocationScreenActivity.this,
									"You can add these devices later through 'Menu / Add sensor'",
									Toast.LENGTH_LONG).show();
							mDialog.dismiss();
						}
					});

			mDialog.setCustomPositiveButton(getString(R.string.notification_add),
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							// Open activity for adding new device
							inBackground = true;
							Intent intent = new Intent(
									LocationScreenActivity.this,
									AddSensorActivityDialog.class);
							startActivity(intent);
							mDialog.dismiss();
						}
					});

			mDialog.show();
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

}

package cz.vutbr.fit.intelligenthomeanywhere.activity;

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
import android.util.Log;
import android.view.View;
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

import cz.vutbr.fit.intelligenthomeanywhere.Compatibility;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.MenuListAdapter;
import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.SensorListAdapter;
import cz.vutbr.fit.intelligenthomeanywhere.activity.dialog.AddAdapterActivityDialog;
import cz.vutbr.fit.intelligenthomeanywhere.activity.dialog.AddSensorActivityDialog;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.controller.Controller;
import cz.vutbr.fit.intelligenthomeanywhere.extension.watches.smartwatch2.SW2PreferenceActivity;
import cz.vutbr.fit.intelligenthomeanywhere.listing.LocationListing;

/**
 * Activity class for choosing location
 * 
 * @author ThinkDeep
 * @author Robyer
 * 
 */
public class LocationScreenActivity extends SherlockFragmentActivity {

	private Controller mController;
	private List<LocationListing> mLocations;

	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private MenuListAdapter mMenuAdapter;
    
    private CharSequence mDrawerTitle;
    
    private SensorListAdapter mSensorAdapter;
    private ListView mSensorList;
    
    private CharSequence mTitle;

    private static final String TAG = "Location";
    
	
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

		setSupportProgressBarIndeterminate(true);
		setSupportProgressBarIndeterminateVisibility(true);
		
//		int marginTop = 5;
//		int ID = Constants.BUTTON_ID;
		/*
		 * for(LocationListing location : locations) { if
		 * (addLocationButton(location.getName(), ID, marginTop)) ID++; }
		 * if(locations.size() == 1){ Button onlyOne =
		 * (Button)findViewById(--ID); onlyOne.performClick(); }
		 */
	}

	public void onResume() {
		super.onResume();
		
		DevicesTask task = new DevicesTask();
	    task.execute();
	}

	public boolean getLocations(List<LocationListing> locs) {

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

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				// TODO Auto-generated method stub
				super.onDrawerClosed(view);
			}

			public void onDrawerOpened(View drawerView) {
				// TODO Auto-generated method stub
				// Set the title on the action when drawer open
				getSupportActionBar().setTitle(mDrawerTitle);
				super.onDrawerOpened(drawerView);
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

	public boolean getSensors(List<BaseDevice> sensors) {

		String[] title;
		String[] value;
		int[] icon;
		mTitle = mDrawerTitle = "IHA";
		title = new String[sensors.size()];
		value = new String[sensors.size()];
		icon = new int[sensors.size()];
		for (int i = 0; i < sensors.size(); i++) {
			title[i] = sensors.get(i).getName();
			value[i] = sensors.get(i).getStringValue();
			icon[i] = sensors.get(i).getTypeIconResource();
		}

		this.mSensorAdapter = new SensorListAdapter(
				LocationScreenActivity.this, title, value, icon);
		this.mSensorList = (ListView) findViewById(R.id.listviewofsensors);
		this.mSensorList.setAdapter(mSensorAdapter);

		// Capture listview menu item click
		// mSensorList.setOnItemClickListener(new DrawerItemClickListener());
		this.setSupportProgressBarIndeterminateVisibility(false);
		return true;
	}

	

	// ListView click listener in the navigation drawer
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// Get the title followed by the position
			LocationListing selectedItem = mLocations.get(position);
			String name = (selectedItem != null ? selectedItem.getName() : ""); 

			ChangeLocationTask task = new ChangeLocationTask();
		    task.execute(new String[] { name });
			
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
		case R.id.action_addadapter:
		{
//			Toast.makeText(this, "go to old", Toast.LENGTH_LONG).show();

//			Intent intent = new Intent(LocationScreenActivity.this, AddAdapterActivity.class);
			Intent intent = new Intent(LocationScreenActivity.this, AddAdapterActivityDialog.class);
			startActivity(intent);
			break;
		}
		case R.id.action_addsensor:
		{
//			Toast.makeText(this, "go to old", Toast.LENGTH_LONG).show();

			Intent intent = new Intent(LocationScreenActivity.this, AddSensorActivityDialog.class);
			startActivity(intent);
			break;
		}
		case R.id.action_settings:
		{
			Intent intent = new Intent(LocationScreenActivity.this, SW2PreferenceActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.action_logout:
		{	
			mController.logout();
			Intent intent = new Intent(LocationScreenActivity.this, LoginActivity.class);
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
							public void onClick(DialogInterface dialog, int which) {
								String newName = edit.getText().toString();

								// TODO: show loading while saving new name to
								// server
								boolean saved = mController.renameLocation(
										location, newName);

								String message = saved ? String
										.format("Location was renamed to '%s'",
												newName)
										: "Location wasn't renamed due to error";

								Toast.makeText(LocationScreenActivity.this,
										message, Toast.LENGTH_LONG).show();

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
	 */
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

	/**
	 * Loads locations, checks for uninitialized devices and eventually shows dialog for adding them
	 */
	private class DevicesTask extends AsyncTask<Void, Void, List<BaseDevice>> {
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
    	protected void onPostExecute(List<BaseDevice> uninitializedDevices) {
    		// Redraw locations
    		getLocations(mLocations);
    		
    		// Do something with uninitialized devices
    		if (uninitializedDevices.size() == 0)
    			return;
				
			AlertDialog.Builder builder = new AlertDialog.Builder(LocationScreenActivity.this);
				
			builder.setCancelable(false)
				.setTitle(R.string.notification_title)
				.setMessage(getResources().getQuantityString(R.plurals.notification_new_sensors, uninitializedDevices.size(), uninitializedDevices.size()))
				.setNeutralButton(R.string.notification_ingore, null)
				.setPositiveButton(R.string.notification_add, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Open activity for adding new device
						Intent intent = new Intent(LocationScreenActivity.this, AddSensorActivity.class);
						startActivity(intent);
					}
				});
			
			AlertDialog dialog = builder.create();
			dialog.show();
    	}
	}
	
	/**
	 * Loads locations, checks for uninitialized devices and eventually shows dialog for adding them
	 */
	private class ChangeLocationTask extends AsyncTask<String, Void, List<BaseDevice>> {
    	@Override
    	protected List<BaseDevice> doInBackground(String... locations) {
    		List<BaseDevice> devices = mController.getDevicesByLocation(locations[0]);
    		Log.d(TAG, String.format("Found %d devices in location '%s'", devices.size(), locations[0]));
    		
			return devices;
    	}

    	@Override
    	protected void onPostExecute(List<BaseDevice> devices) {
    		getSensors(devices);
    	}
	}
	
}

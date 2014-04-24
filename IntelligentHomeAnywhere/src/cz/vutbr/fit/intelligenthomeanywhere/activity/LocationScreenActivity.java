package cz.vutbr.fit.intelligenthomeanywhere.activity;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
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
import com.actionbarsherlock.view.MenuItem;

import cz.vutbr.fit.intelligenthomeanywhere.Compatibility;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.MenuListAdapter;
import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.controller.Controller;
import cz.vutbr.fit.intelligenthomeanywhere.listing.LocationListing;

/**
 * Activity class for choosing location
 * @author ThinkDeep
 * @author Robyer
 *
 */
public class LocationScreenActivity extends SherlockFragmentActivity {
	
	private Controller mController;
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private MenuListAdapter mMenuAdapter;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    String[] title;
    String[] subtitle;
    int[] icon;
	
	/**
	 * Call XML parser to file on SDcard
	 */
	@SuppressWarnings("null")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locaction_screen);
		
		mController = Controller.getInstance(this);
		
		
		
		Thread thUniniDev = new Thread(new Runnable() {
			@Override
			public void run() {
				checkUninitializedDevices();
			}
		});
		thUniniDev.start();
		
		List<LocationListing> locations = null;
		Thread thLoc = new Thread(new Runnable() {
			@Override
			public void run() {
				List<LocationListing> locations2 = mController.getLocations();
				Log.d("lokace",locations2.toString());
				getLocations(locations2);
			}
		});
		thLoc.start();

		
		mTitle = mDrawerTitle = "IHA";
		
		// TEST VALUES
		// Generate title
        title = new String[] { "Title Fragment 1", "Title Fragment 2",
                "Title Fragment 3" };
 
        // Generate subtitle
        subtitle = new String[] { "Subtitle Fragment 1", "Subtitle Fragment 2",
                "Subtitle Fragment 3" };
 
        // Generate icon
        icon = new int[] { R.drawable.action_about, R.drawable.action_settings,
                R.drawable.collections_cloud };
		
		
		// Locate DrawerLayout in activity_locaction_screen.xml
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
 
        // Locate ListView in activity_locaction_screen.xml
        mDrawerList = (ListView) findViewById(R.id.listview_drawer);
		
        // Set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        
        // Pass string arrays to MenuListAdapter
        mMenuAdapter = new MenuListAdapter(LocationScreenActivity.this, title, subtitle,  icon);
 
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
 
        if (savedInstanceState == null) {
            selectItem(0);
        }
		
		int marginTop = 5;
		int ID = Constants.BUTTON_ID;
		/*for(LocationListing location : locations) {
			if (addLocationButton(location.getName(), ID, marginTop))
				ID++;
		}
		if(locations.size() == 1){
			Button onlyOne = (Button)findViewById(--ID);
			onlyOne.performClick();
		}*/
	}
	
	public boolean getLocations(List<LocationListing> locations) {
		
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
 
        if (item.getItemId() == android.R.id.home) {
 
            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        }
 
        return super.onOptionsItemSelected(item);
    }
	
	// ListView click listener in the navigation drawer
    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            selectItem(position);
        }
    }
 
    private void selectItem(int position) {
 
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // Locate Position
        switch (position) {
        case 0:
            //ft.replace(R.id.content_frame, fragment1);
            break;
        case 1:
            //ft.replace(R.id.content_frame, fragment2);
            break;
        case 2:
            //ft.replace(R.id.content_frame, fragment3);
            break;
        }
        ft.commit();
        mDrawerList.setItemChecked(position, true);
 
        // Get the title followed by the position
        setTitle(title[position]);
        // Close drawer
        mDrawerLayout.closeDrawer(mDrawerList);
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
	
	
    /*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.locaction_screen, menu);
		return true;
	}*/

	/**
	 * Checks if there are any uninitialized devices and if so, shows dialog to ask user for adding them.
	 */
	private void checkUninitializedDevices() {
		List<BaseDevice> devices = mController.getUninitializedDevices();
		if (devices.size() > 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			builder.setCancelable(false)
				.setTitle(R.string.notification_title)
				.setMessage(getResources().getQuantityString(R.plurals.notification_new_sensors, devices.size(), devices.size()))
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
	
	protected void renameLocation(final String location, final TextView view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(LocationScreenActivity.this);
		
		// TODO: use better layout than just single EditText
		final EditText edit = new EditText(LocationScreenActivity.this);
		edit.setText(location);
		edit.selectAll();
		// TODO: show keyboard automatically
		
		builder.setCancelable(false)
			.setView(edit)
			.setTitle("Rename location")
			.setNegativeButton("Cancel", null)
			.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String newName = edit.getText().toString();
					
					// TODO: show loading while saving new name to server
					boolean saved = mController.renameLocation(location, newName);
					
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
	 * @param s specific location name
	 * @param ID id of button
	 * @param marginTop margin of the button
	 * @return true on success and false in other cases
	 */
	private boolean addLocationButton(String s, int ID, int marginTop){
		final Button button = new Button(this);
		button.setText(s);
		button.setTextSize(getResources().getDimension(R.dimen.textsize));
		button.setId(ID);
		if(s == null || s.length() < 1)
			return false;
		
		//LinearLayout mylayout = (LinearLayout)findViewById(R.id.location_scroll);
		//mylayout.setOrientation(LinearLayout.VERTICAL);
		//drawerListView = (ListView) findViewById(R.id.left_drawer);
		
		LinearLayout.LayoutParams params_btn = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		params_btn.setMargins(0, marginTop, 0, 0);
		button.setLayoutParams(params_btn);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Button clicked = (Button)v;
				Log.i("kliknuto-na",clicked.getText().toString());
				
				Intent intent = new Intent(getBaseContext(), DataOfLocationScreenActivity.class);
				intent.putExtra(Constants.LOCATION_CLICKED, clicked.getText().toString());
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

		Compatibility.setBackground(button, getResources().getDrawable(R.drawable.shape));
		
		//mylayout.addView(button);
		return true;
	}

}

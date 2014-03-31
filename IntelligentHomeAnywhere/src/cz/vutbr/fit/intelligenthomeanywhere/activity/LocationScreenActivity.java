package cz.vutbr.fit.intelligenthomeanywhere.activity;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cz.vutbr.fit.intelligenthomeanywhere.Compatibility;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
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
public class LocationScreenActivity extends Activity {
	
	private Controller mController;
	
	/**
	 * Call XML parser to file on SDcard
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locaction_screen);
		
		mController = Controller.getInstance(this);
		
		checkUninitializedDevices();
		
		List<LocationListing> locations = mController.getLocations();
		Log.d("lokace",locations.toString());
		
		int marginTop = 5;
		int ID = Constants.BUTTON_ID;
		for(LocationListing location : locations) {
			if (addLocationButton(location.getName(), ID, marginTop))
				ID++;
		}
		if(locations.size() == 1){
			Button onlyOne = (Button)findViewById(--ID);
			onlyOne.performClick();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.locaction_screen, menu);
		return true;
	}

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
		
		LinearLayout mylayout = (LinearLayout)findViewById(R.id.location_scroll);
		mylayout.setOrientation(LinearLayout.VERTICAL);
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
		
		mylayout.addView(button);
		return true;
	}

}

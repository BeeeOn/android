package cz.vutbr.fit.intelligenthomeanywhere.activity;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import cz.vutbr.fit.intelligenthomeanywhere.Compatibility;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.controller.Controller;
import cz.vutbr.fit.intelligenthomeanywhere.listing.LocationListing;

/**
 * Activity class for choosing location
 * @author ThinkDeep
 *
 */
public class LocationScreenActivity extends Activity {

	private static final int REQUEST_RENAME = 1;
	private static final int REQUEST_CHANGE_LOCATION = 2;
	
	private Controller mController;
	
	/**
	 * Call XML parser to file on SDcard
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locaction_screen);
		
		mController = Controller.getInstance(this);
		
		List<BaseDevice> devices = mController.getUninitializedDevices();
		if (devices.size() > 0) {
			Intent intent = new Intent(this, Notification.class);
			startActivity(intent); // TODO: shoudln't this be startActivityForResult()?
		}
		
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		//mController = Controller.getInstance(this);

		if (requestCode == REQUEST_CHANGE_LOCATION) {
			// Finish location change
			if (resultCode == RESULT_OK) {
				String location = data.getStringExtra(ChangeLocationNameActivity.LOCATION_NAME);
				String newName = data.getStringExtra(ChangeLocationNameActivity.NEW_NAME);
				
				// FIXME: redraw location's name in list
				//((TextView)((RelativeLayout)mPressed).getChildAt(0)).setText(newName);
			}
		}

		if (requestCode == REQUEST_RENAME) {
			// Finish device rename
			if (resultCode == RESULT_OK) {
				String deviceId = data.getStringExtra(ChangeDeviceNameActivity.DEVICE_ID);
				String newName = data.getStringExtra(ChangeDeviceNameActivity.NEW_NAME);
				
				// FIXME: redraw device's name in list
				//((TextView)((RelativeLayout)mPressed).getChildAt(0)).setText(newName);
			}
		}
	}
	
	/**
	 * Repaint GUI
	 */
	/*@Override
	public void onResume(){
		super.onResume();
		final int ID = Constants.BUTTON_ID;
		
		mController = Controller.getInstance(this);
		
		//DEBUG: maybe saving on SDCard even after calling sever as a cache
		XmlCreator xmlcreator = new XmlCreator(mController.getAdapter());
		xmlcreator.saveXml(getExternalFilesDir(null).getPath(), Constants.DEMO_FILENAME);
		
		Log.i("onResume",this.getLocalClassName());
		
		if(mController.getAdapter().isNewInit()){
			List<String> Old = GetLocationsFromButtons(ID);
			List<String> New = mController.getAdapter().getLocations();
			Log.d("Old",Old.toString());
			Log.d("New", New.toString());
			if(Old.size() != New.size()){
				New.removeAll(Old);
				Log.i("Wanted",New.toString());
				addLocationButton(New.get(0), ID + Old.size() + 1, 5);
			}
		}
		if(mController.getAdapter().isNewLocationName()){
			List<String> Old = GetLocationsFromButtons(ID);
			List<String> New = mController.getAdapter().getLocations();
			Log.d("Old",Old.toString());
			Log.d("New", New.toString());
			List<String> diff = GetDiffOfLocatins(Old, New);
			Log.i("before",diff.get(0));
			Log.i("after",diff.get(1));
			try{
				Button changedOne = GetButtonByName(diff.get(0), ID);
					if(changedOne != null)
						changedOne.setText(diff.get(1));
			}catch(Exception e){
				//e.printStackTrace();
			}
			
		}
	}*/
	
	/**
	 * Draw a button to GUI
	 * @param s specific location name
	 * @param ID id of button
	 * @param marginTop margin of the button
	 * @return true on success and false in other cases
	 */
	private boolean addLocationButton(String s, int ID, int marginTop){
		Button button = new Button(this);
		button.setText(s);
		button.setTextSize(getResources().getDimension(R.dimen.textsize));
		button.setId(ID);
		if(s == null || s.length() < 1)
			return false;
		
		LinearLayout mylayout = (LinearLayout)findViewById(R.id.location_scroll);
		mylayout.setOrientation(LinearLayout.VERTICAL);
		
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
				Button longPress = (Button)v;
				Log.i("longClick",longPress.getText().toString());
				
				Intent intent = new Intent(getBaseContext(), ChangeLocationNameActivity.class);
				intent.putExtra(ChangeLocationNameActivity.LOCATION_NAME, longPress.getText().toString());
				startActivity(intent);
				return false;
			}
		});

		Compatibility.setBackground(button, getResources().getDrawable(R.drawable.shape));
		
		mylayout.addView(button);
		return true;
	}
	
	/**
	 * Return list of location names of buttons
	 * @param ID number of start id (end with first null)
	 * @return ArrayList with location names
	 */
	/*private List<String> GetLocationsFromButtons(int ID){
		List<String> result = new ArrayList<String>();
		try{
			for(int i = ID; i > 0; i++)
				result.add(((Button)findViewById(i)).getText().toString());
		}catch(Exception e){
			//e.printStackTrace();
		}
		return result;
	}*/
	
	/**
	 * Method find two different items in two lists
	 * @param Old first list
	 * @param New second list
	 * @return pair of different items in list
	 */
	/*private List<String> GetDiffOfLocatins(List<String> Old, List<String> New){
		List<String> result = new ArrayList<String>();
		final int oSize = Old.size();
		for(int x = oSize-1; x >= 0; x--){
			final int nSize = New.size();
			for(int y = nSize-1; y >= 0; y--){
				if(Old.get(x).equals(New.get(y))){
					Old.remove(x);
					New.remove(y);
					break;
				}
			}
		}
		result.add(Old.get(0));
		result.add(New.get(0));
		return result;
	}*/
	
	/**
	 * Method return button with specific name (label) and ID
	 * @param name label of the searching button for
	 * @param ID start of ID, ID has to be less or equal to searching for
	 * @return button with specific properties
	 */
	/*private Button GetButtonByName(String name, int ID){
		Button result = null;
		try{
			for(int i = ID; i > 0; i++){
				result = (Button)findViewById(ID++);
				if(result.getText().toString().equals(name))
					break;
			}
		}catch(Exception e){
			e.printStackTrace();
			result = null;
		}
		return result;
	}*/
}

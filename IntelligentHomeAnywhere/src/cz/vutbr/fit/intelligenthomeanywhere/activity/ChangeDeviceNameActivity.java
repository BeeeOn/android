package cz.vutbr.fit.intelligenthomeanywhere.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice.SaveDevice;
import cz.vutbr.fit.intelligenthomeanywhere.controller.Controller;

public class ChangeDeviceNameActivity extends Activity
{
	public static final String DEVICE_ID = "device_id";
	public static final String NEW_NAME = "new_name";
	
	private BaseDevice mDevice = null;
	
	private Controller mController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		mController = Controller.getInstance(this);
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			String id = bundle.getString(DEVICE_ID);
			mDevice = mController.getDevice(id);
		} else {
			Toast.makeText(this, "Error: Given no device id.", Toast.LENGTH_LONG).show();
			finish();
		}
		
		setContentView(R.layout.activity_change_device_name);
		initButtons();
	}
	
	/**
	 * Initialize listeners
	 */
	private void initButtons() {
		// Set change name button callback
		((Button)findViewById(R.id.change_device_name_button)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String newName = ((EditText)findViewById(R.id.change_device_name_edittext)).getText().toString();
				mDevice.setName(newName);
				
				// TODO: show loading while saving new name to server
				boolean saved = mController.saveDevice(mDevice, SaveDevice.SAVE_NAME);
				
				if (saved) {
					Intent result = new Intent();
					result.putExtra(DEVICE_ID, mDevice.getId());
					result.putExtra(NEW_NAME, mDevice.getName());
				
					setResult(RESULT_OK, result);
				} else {
					setResult(RESULT_CANCELED); // TODO: or don't close this window and let user try another name or cancel it by himself?
				}

				finish();
			}
		});

		// Add actual device name to edit
		((EditText)findViewById(R.id.change_device_name_edittext)).setText(mDevice.getName());
	}
	
}

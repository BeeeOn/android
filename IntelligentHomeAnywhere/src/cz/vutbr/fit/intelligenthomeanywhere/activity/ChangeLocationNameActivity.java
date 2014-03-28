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
import cz.vutbr.fit.intelligenthomeanywhere.controller.Controller;

/**
 * Class that handle screen to changing name of location
 * @author ThinkDeep
 */
public class ChangeLocationNameActivity extends Activity
{
	public static final String LOCATION_NAME = "location_name";
	public static final String NEW_NAME = "new_name";
	
	private String mOldLocation = null;
	
	private Controller mController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		mController = Controller.getInstance(this);
		
		setContentView(R.layout.activity_change_location_name);
		initButtons();
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			mOldLocation = bundle.getString(LOCATION_NAME);
		} else {
			Toast.makeText(this, "Error: Given no location.", Toast.LENGTH_LONG).show();
			finish();
		}
	}
	
	/**
	 * Initialize listeners
	 */
	private void initButtons() {
		// Set change location name button callback
		((Button)findViewById(R.id.change_location_name_button)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String newName = ((EditText)findViewById(R.id.change_location_name_edittext)).getText().toString();
				
				boolean saved = mController.renameLocation(mOldLocation, newName);
				
				if (saved) {
					Intent result = new Intent();
					result.putExtra(LOCATION_NAME, mOldLocation);
					result.putExtra(NEW_NAME, newName);
				
					setResult(RESULT_OK, result);
				} else {
					setResult(RESULT_CANCELED); // TODO: or don't close this window and let user try another name or cancel it by himself?
				}

				finish();
			}
		});
	}
	
}

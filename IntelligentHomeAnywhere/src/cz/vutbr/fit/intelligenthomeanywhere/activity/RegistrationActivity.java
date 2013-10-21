package cz.vutbr.fit.intelligenthomeanywhere.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;

public class RegistrationActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registration);
		initButtons();
		
		Bundle bundle = this.getIntent().getExtras();
		String serialNumber = bundle.getString(Constants.ADAPTER_SERIAL_NUMBER);
		TextView txtvwSerialNumber = (TextView)findViewById(R.id.registration_serial_number);
		txtvwSerialNumber.setText(txtvwSerialNumber.getText().toString() + " " + serialNumber);
	}

	/**
	 * Initialize listeners
	 */
	private void initButtons() {
		// Registration button
		((Button)findViewById(R.id.registration_button)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO: call to server
				RegistrationActivity.this.finish();
			}
		});
	}
	
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.registration, menu);
		return true;
	}*/

}

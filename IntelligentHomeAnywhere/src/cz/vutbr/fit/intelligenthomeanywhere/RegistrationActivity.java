package cz.vutbr.fit.intelligenthomeanywhere;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class RegistrationActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registration);
		
		Bundle bundle = this.getIntent().getExtras();
		String serialNumber = bundle.getString(Constants.ADAPTER_SERIAL_NUMBER);
		TextView txtvwSerialNumber = (TextView)findViewById(R.id.registration_serial_number);
		txtvwSerialNumber.setText(txtvwSerialNumber.getText().toString() + " " + serialNumber);
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.registration, menu);
		return true;
	}*/
	
	public void registrationMethod(View v){
		//TODO: call to server
		this.finish();
	}

}

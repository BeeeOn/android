package cz.vutbr.fit.intelligenthomeanywhere.activity.dialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.activity.AddAdapterActivity;
import cz.vutbr.fit.intelligenthomeanywhere.activity.LocationScreenActivity;
import cz.vutbr.fit.intelligenthomeanywhere.thread.AdapterRegisterThread;

public class AddAdapterActivityDialog extends Activity {

	public AddAdapterActivityDialog mActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_add_adapter_activity_dialog);
		
		initButtons();
		
		mActivity = this;
	}
	
	/**
	 * Initialize listeners
	 */
	private void initButtons() {
		// QR code button - register new adapter by QR code
		((ImageButton)findViewById(R.id.addadapter_qrcode_button)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
				    Intent intent = new Intent("com.google.zxing.client.android.SCAN");
				    intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // PRODUCT_MODE for bar codes

				    startActivityForResult(intent, 0);
				} catch (Exception e) {
				    Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
				    Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
				    startActivity(marketIntent);
				}
			}
		});

		// Serial number button - register new adapter by serial number
		((Button)findViewById(R.id.addadapter_add_button)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText serialNuber = (EditText)findViewById(R.id.addadapter_ser_num);
				Log.i("seriove cislo",serialNuber.getText().toString());
				
				new Thread(new AdapterRegisterThread(serialNuber.getText().toString(), mActivity)).start();
			}
		});
	}
	
	@Override
	public void onBackPressed(){
		LocationScreenActivity.healActivity();
		this.finish();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {           
	    super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == 0) {

	        if (resultCode == RESULT_OK) {
	            String contents = data.getStringExtra("SCAN_RESULT");
	            Log.i("seriove cislo",contents);
	            new Thread(new AdapterRegisterThread(contents, mActivity)).start();
	        }
	        if(resultCode == RESULT_CANCELED){
	        	LocationScreenActivity.healActivity();
	        	//TODO: handle cancel ?
	        }
	        this.finish();
	    }
	}
}

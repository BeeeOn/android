package cz.vutbr.fit.iha.activity.dialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.LocationScreenActivity;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.thread.AdapterRegisterThread;

public class AddAdapterActivityDialog extends BaseActivityDialog {

	private static final String TAG = AddAdapterActivityDialog.class.getSimpleName();

	public AddAdapterActivityDialog mActivity;
	private Button mAddButton;
	private Button mCancelButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_add_adapter_activity_dialog);

		mActivity = this;

		initButtons();
		initViews();
	}

	/**
	 * Initialize listeners
	 */
	private void initButtons() {
		// QR code button - register new adapter by QR code
		((ImageButton) findViewById(R.id.addadapter_qrcode_button)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Intent intent = new Intent("com.google.zxing.client.android.SCAN");
					intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // PRODUCT_MODE for bar codes

					startActivityForResult(intent, 0);
				} catch (Exception e) {
					Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
					Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
					startActivity(marketIntent);
				}
			}
		});

		mAddButton = (Button) findViewById(R.id.addadapter_add_button);
		mCancelButton = (Button) findViewById(R.id.addadapter_cancel_button);

		// Serial number button - register new adapter by serial number
		if (!mActivity.getIntent().getExtras().getBoolean(Constants.CANCEL)) {
			mAddButton.setLayoutParams(new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 260, getResources().getDisplayMetrics()), (int) TypedValue
					.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics())));
		}

		mAddButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText serialNuber = (EditText) findViewById(R.id.addadapter_ser_num);
				Log.i(TAG, "seriove cislo: " + serialNuber.getText().toString());

				new Thread(new AdapterRegisterThread(serialNuber.getText().toString(), mActivity)).start();
			}
		});

		// If this dialog as first use (from login page)- invisible button
		if (!mActivity.getIntent().getExtras().getBoolean(Constants.CANCEL))
			mCancelButton.setVisibility(View.INVISIBLE);

		mCancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences settings = Controller.getInstance(mActivity).getUserSettings();
				settings.edit().putBoolean(Constants.PERSISTENCE_PREF_IGNORE_NO_ADAPTER, true).commit();
				mActivity.finish();
			}
		});
	}

	/**
	 * Initialize TextWatchers
	 */
	private void initViews() {
		EditText serialInput = (EditText) findViewById(R.id.addadapter_ser_num);

		TextWatcher tw = new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* nothing to do now */
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) { /* nothing to do now */
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() > 0)
					mAddButton.setEnabled(true);
				else
					mAddButton.setEnabled(false);
			}
		};

		serialInput.addTextChangedListener(tw);
	}

	@Override
	public void onBackPressed() {
		LocationScreenActivity.healActivity();
		this.finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0) {

			if (resultCode == RESULT_OK) {
				String contents = data.getStringExtra("SCAN_RESULT");
				Log.i(TAG, "seriove cislo: " + contents);
				new Thread(new AdapterRegisterThread(contents, mActivity)).start();
			}
			if (resultCode == RESULT_CANCELED) {
				LocationScreenActivity.healActivity();
				// TODO: handle cancel ?
			}
			this.finish();
		}
	}

}

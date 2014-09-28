package cz.vutbr.fit.iha.activity.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.LocationScreenActivity;
import cz.vutbr.fit.iha.activity.TrackDialogFragment;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.thread.AdapterRegisterThread;

public class AddAdapterFragmentDialog extends TrackDialogFragment {

	private static final String TAG = AddAdapterFragmentDialog.class.getSimpleName();

	public Activity mActivity;
	private View mView;
	private String mAdName;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get activity
		mActivity = getActivity();

		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

		LayoutInflater inflater = mActivity.getLayoutInflater();

		// Get View
		mView = inflater.inflate(R.layout.activity_add_adapter_activity_dialog, null);

		// Set on ImageView onClick
		((ImageButton) mView.findViewById(R.id.addadapter_qrcode_button)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Intent intent = new Intent("com.google.zxing.client.android.SCAN");
					intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // PRODUCT_MODE for bar codes
					mAdName = ((EditText) mView.findViewById(R.id.addadapter_text_name)).getText().toString();
					startActivityForResult(intent, 0);
				} catch (Exception e) {
					Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
					Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
					startActivity(marketIntent);
				}
			}
		});

		builder.setView(mView).setPositiveButton(R.string.notification_add, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// ADD
				EditText serialNuber = (EditText) mView.findViewById(R.id.addadapter_ser_num);
				EditText adapterName = (EditText) mView.findViewById(R.id.addadapter_text_name);
				if (serialNuber.getTextSize() > 0) {
					Log.i(TAG, "seriove cislo: " + serialNuber.getText().toString());
					new Thread(new AdapterRegisterThread(adapterName.getText().toString(), serialNuber.getText().toString(), mActivity)).start();
				}
			}
		}).setNegativeButton(R.string.notification_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog, remember that
				SharedPreferences settings = Controller.getInstance(mActivity).getUserSettings();
				settings.edit().putBoolean(Constants.PERSISTENCE_PREF_IGNORE_NO_ADAPTER, true).commit();
			}
		});

		// Create the AlertDialog object and return it
		return builder.create();

		// requestWindowFeature(Window.FEATURE_NO_TITLE);

		// setContentView(R.layout.activity_add_adapter_activity_dialog);

		// mActivity = this;

		// initButtons();
		// initViews();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0) {

			if (resultCode == mActivity.RESULT_OK) {
				String contents = data.getStringExtra("SCAN_RESULT");
				Log.i(TAG, "seriove cislo: " + contents);
				new Thread(new AdapterRegisterThread(mAdName, contents, mActivity)).start();
			}
			if (resultCode == mActivity.RESULT_CANCELED) {
				LocationScreenActivity.healActivity();
				// TODO: handle cancel ?
			}
			this.dismiss();
		}
	}

}

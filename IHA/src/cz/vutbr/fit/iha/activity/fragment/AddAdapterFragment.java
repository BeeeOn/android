package cz.vutbr.fit.iha.activity.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.MainActivity;
import cz.vutbr.fit.iha.asynctask.CallbackTask.CallbackTaskListener;
import cz.vutbr.fit.iha.asynctask.RegisterAdapterTask;
import cz.vutbr.fit.iha.base.BaseActivity;
import cz.vutbr.fit.iha.base.TrackDialogFragment;
import cz.vutbr.fit.iha.base.TrackFragment;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.pair.RegisterAdapterPair;
import cz.vutbr.fit.iha.thread.ToastMessageThread;
import cz.vutbr.fit.iha.util.Log;

public class AddAdapterFragment extends TrackFragment {

	private static final String TAG = AddAdapterFragment.class.getSimpleName();

	public BaseActivity mActivity;
	private LinearLayout mLayout;
	private View mView;
	private Controller mController;

	private RegisterAdapterTask mRegisterAdapterTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get activity and controller
		mActivity =  (BaseActivity) getActivity();
		mController = Controller.getInstance(mActivity.getApplicationContext());

		return;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.activity_add_adapter_activity_dialog, container, false);

		mLayout = (LinearLayout) mView.findViewById(R.id.container);

		initLayout();
		
		return mView;
	}
	
	private void initLayout() {
		((ImageButton) mView.findViewById(R.id.addadapter_qrcode_button)).setOnClickListener(new OnClickListener() {
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
		
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 0 && resultCode == MainActivity.RESULT_OK) {
			// Fill scanned code into edit text
			EditText serialNumberEdit = (EditText) mView.findViewById(R.id.addadapter_ser_num);
			serialNumberEdit.setText(data.getStringExtra("SCAN_RESULT"));

			// And click positive button
			//AlertDialog dialog = (AlertDialog) getDialog();
			//dialog.getButton(Dialog.BUTTON_POSITIVE).performClick();
		}
	}

	// To prevent automatically closing of dialog - see http://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked
	@Override
	public void onStart() {
		super.onStart();

		//final AlertDialog dialog = (AlertDialog) getDialog();
		InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		//dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		/*if (dialog != null) {
			dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// Try to register adapter
					EditText serialNumberEdit = (EditText) mView.findViewById(R.id.addadapter_ser_num);
					EditText adapterNameEdit = (EditText) mView.findViewById(R.id.addadapter_text_name);

					if (serialNumberEdit.getTextSize() > 0) {
						String serialNumber = serialNumberEdit.getText().toString();
						String adapterName = adapterNameEdit.getText().toString();
						Log.i(TAG, "seriove cislo: " + serialNumber);

						doRegisterAdapterTask(new RegisterAdapterPair(serialNumber, adapterName));
					}
				}
			});

			dialog.getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// User cancelled the dialog, remember that

					// UserSettings can be null when user is not logged in!
					SharedPreferences prefs = mController.getUserSettings();
					if (prefs != null) {
						prefs.edit().putBoolean(Constants.PERSISTENCE_PREF_IGNORE_NO_ADAPTER, true).commit();
					}

					dialog.dismiss();
				}
			});
		}*/
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mRegisterAdapterTask != null) {
			mRegisterAdapterTask.cancel(true);
		}
	}

	public void doRegisterAdapterTask(RegisterAdapterPair pair) {
		mRegisterAdapterTask = new RegisterAdapterTask(getActivity().getApplicationContext());

		mRegisterAdapterTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				int messageId = success ? R.string.toast_adapter_activated : R.string.toast_adapter_activate_failed;
				Log.d(TAG, mActivity.getString(messageId));
				new ToastMessageThread(mActivity, messageId).start();

				if (success) {
					//AddAdapterFragment.this.dismiss();
					/*
					mActivity.setActiveAdapterAndLocation();
					mActivity.redrawMenu();
					mActivity.checkNoDevices();*/
				}
			}
		});

		mRegisterAdapterTask.execute(pair);
	}

}

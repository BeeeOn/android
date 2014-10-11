package cz.vutbr.fit.iha.activity.dialog;

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
import cz.vutbr.fit.iha.asynctask.CallbackTask.CallbackTaskListener;
import cz.vutbr.fit.iha.asynctask.RegisterAdapterTask;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.pair.RegisterAdapterPair;
import cz.vutbr.fit.iha.thread.ToastMessageThread;

public class AddAdapterFragmentDialog extends TrackDialogFragment {

	private static final String TAG = AddAdapterFragmentDialog.class.getSimpleName();

	public LocationScreenActivity mActivity;
	private View mView;
	private Controller mController;
	
	private RegisterAdapterTask mRegisterAdapterTask;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get activity and controller
		mActivity = (LocationScreenActivity)getActivity();
		mController = Controller.getInstance(mActivity.getApplicationContext());

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
					startActivityForResult(intent, 0);
				} catch (Exception e) {
					Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
					Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
					startActivity(marketIntent);
				}
			}
		});
		
		DialogInterface.OnClickListener dummyListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Do nothing here because we override this button later to change the close behaviour. 
                // However, we still need this because on older versions of Android unless we 
                // pass a handler the button doesn't get instantiated
			}
		};

		builder.setView(mView)
			.setPositiveButton(R.string.notification_add, dummyListener)
			.setNegativeButton(R.string.notification_cancel, dummyListener);

		// Create the AlertDialog object and return it
		return builder.create();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 0 && resultCode == LocationScreenActivity.RESULT_OK) {
			// Fill scanned code into edit text
			EditText serialNumberEdit = (EditText) mView.findViewById(R.id.addadapter_ser_num);
			serialNumberEdit.setText(data.getStringExtra("SCAN_RESULT"));

			// And click positive button
			AlertDialog dialog = (AlertDialog)getDialog();
			dialog.getButton(Dialog.BUTTON_POSITIVE).performClick();
		}
	}
	
	// To prevent automatically closing of dialog - see http://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked
	@Override
	public void onStart() {
	    super.onStart();
	    
	    final AlertDialog dialog = (AlertDialog)getDialog();

	    if (dialog != null) {
	        dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {

	        	@Override
                public void onClick(View v)
                {
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
                public void onClick(View v)
                {
	        		// User cancelled the dialog, remember that
	        		SharedPreferences settings = mController.getUserSettings();
	        		settings.edit().putBoolean(Constants.PERSISTENCE_PREF_IGNORE_NO_ADAPTER, true).commit();
	        		
	        		dialog.dismiss();
                }
            });
	    }
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
					AddAdapterFragmentDialog.this.dismiss();
					mActivity.redrawMenu();
					mActivity.checkNoDevices();
				}
			}
		});
		
		mRegisterAdapterTask.execute(pair);
	}


}
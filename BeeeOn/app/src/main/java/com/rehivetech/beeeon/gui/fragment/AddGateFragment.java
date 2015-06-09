package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.activity.AddGateActivity;
import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;
import com.rehivetech.beeeon.gui.activity.MainActivity;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.RegisterGateTask;
import com.rehivetech.beeeon.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddGateFragment extends TrackFragment {

	private static final String TAG = AddGateFragment.class.getSimpleName();
	private static final int SCAN_REQUEST = 0;

	private ProgressDialog mProgress;

	public OnAddGateListener mCallback;
	private View mView;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Prepare progress dialog
		mProgress = new ProgressDialog(getActivity());
		mProgress.setMessage(getString(R.string.progress_saving_data));
		mProgress.setCancelable(false);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			// Get activity and controller
			mCallback = (AddGateActivity) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(String.format("%s must implement OnAddGateListener",activity.toString()));
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_add_gate_dialog_new, container, false);

		mView.findViewById(R.id.add_gate_qr_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Intent intent = new Intent("com.google.zxing.client.android.SCAN");
					intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // PRODUCT_MODE for bar codes
					startActivityForResult(intent, SCAN_REQUEST);
				} catch (ActivityNotFoundException e) {
					try {
						Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
						Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
						startActivity(marketIntent);
					} catch (ActivityNotFoundException e1) {
						Toast.makeText(getActivity(), R.string.toast_error_no_qr_reader, Toast.LENGTH_LONG).show();
					}
				}
			}
		});

		mView.findViewById(R.id.add_gate_write_it_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// overlay dialog must popup here
				mCallback.onWriteManuallyClicked();
			}
		});


		return mView;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == SCAN_REQUEST && resultCode == MainActivity.RESULT_OK) {
			onScanQRCode(data.getStringExtra("SCAN_RESULT"));
		}
	}

	private void onScanQRCode(String data) {
		Pattern pattern = Pattern.compile("id=(\\d+)");
		Matcher matcher = pattern.matcher(data);

		if (matcher.find()) {
			doRegisterGateTask(matcher.group(1),true);
		} else {
			Toast.makeText(getActivity(), R.string.toast_error_invalid_qr_code, Toast.LENGTH_LONG).show();
		}
	}

	public String getGateName() {
		EditText gateName = (EditText) mView.findViewById(R.id.addgate_text_name);
		return gateName.getText().toString();
	}

	public String getGateCode() {
		EditText gateCode = (EditText) mView.findViewById(R.id.addgate_ser_num);
		return gateCode.getText().toString();
	}

	public interface OnAddGateListener {
		/**
		 * This is called after user scans the QR code
		 */
		void onCodeScanned();

		void onWriteManuallyClicked();
	}

	public void doRegisterGateTask(Gate gate) {
		RegisterGateTask registerGateTask = new RegisterGateTask(getActivity());

		registerGateTask.setListener(new CallbackTask.ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				mProgress.cancel();

				if (success) {
					Toast.makeText(getActivity(), R.string.toast_adapter_activated, Toast.LENGTH_SHORT).show();

					getActivity().setResult(Activity.RESULT_OK);
					//InputMethodManager imm = (InputMethodManager) AddGateActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
					//imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
					getActivity().finish();
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		((BaseApplicationActivity) getActivity()).callbackTaskManager.executeTask(registerGateTask, gate);
	}

	public void doAction() {

		String gateName = getGateName();
		String gateCode = getGateCode();
		Log.d(TAG, String.format("Name: %s Code: %s", gateName, gateCode));

		if (gateCode.isEmpty()) {
			Toast.makeText(getActivity(), R.string.addadapter_fill_code, Toast.LENGTH_LONG).show();
		} else {
			// Show progress bar for saving
			mProgress.show();
			doRegisterGateTask(gateCode,true);
		}
	}

	public void doRegisterGateTask(String id, final boolean fromQR) {
		// TODO: finish
		Gate gate = new Gate();
		gate.setId(id);
		gate.setName("");

		RegisterGateTask registerGateTask = new RegisterGateTask(getActivity());
		registerGateTask.setListener(new CallbackTask.ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				mProgress.cancel();

				if (success) {
					Toast.makeText(getActivity(), R.string.toast_adapter_activated, Toast.LENGTH_LONG).show();

					getActivity().setResult(Activity.RESULT_OK);
					//InputMethodManager imm = (InputMethodManager) AddGateActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
					//imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
					getActivity().finish();
				} else {
					Toast.makeText(getActivity(), R.string.toast_adapter_activate_failed, Toast.LENGTH_SHORT).show();
					if(fromQR) {
						// QR scanning again, make it a function?
						try {
							Intent intent = new Intent("com.google.zxing.client.android.SCAN");
							intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // PRODUCT_MODE for bar codes
							startActivityForResult(intent, SCAN_REQUEST);
						} catch (ActivityNotFoundException e) {
							try {
								Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
								Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
								startActivity(marketIntent);
							} catch (ActivityNotFoundException e1) {
								Toast.makeText(getActivity(), R.string.toast_error_no_qr_reader, Toast.LENGTH_LONG).show();
							}
						}
					} else {
						mCallback.onWriteManuallyClicked();
					}
				}
			}
		});
		// Execute and remember task so it can be stopped automatically
		((BaseApplicationActivity) getActivity()).callbackTaskManager.executeTask(registerGateTask, gate);
	}
}

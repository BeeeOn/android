package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avast.android.dialogs.core.BaseDialogFragment;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.activity.ScanQrActivity;
import com.rehivetech.beeeon.gui.dialog.EditTextDialog;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.RegisterGateTask;
import com.rehivetech.beeeon.util.Utils;

import org.joda.time.DateTimeZone;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddGateFragment extends BaseApplicationFragment implements EditTextDialog.IPositiveButtonDialogListener {
	private static final int REQUEST_SCAN = 0;
	private static final int REQUEST_DIALOG_GATE_CODE = 1;
	private static final String TAG = AddGateFragment.class.getSimpleName();

	@Override
	public void onResume() {
		super.onResume();
		setScanQrButtonEnabled(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_gate_add, container, false);

		view.findViewById(R.id.gate_add_qr_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setScanQrButtonEnabled(false);
				showQrScanner();
			}
		});

		view.findViewById(R.id.gate_add_write_it_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// overlay dialog must popup here
				EditTextDialog
						.createBuilder(mActivity, mActivity.getSupportFragmentManager())
						.setTitle(mActivity.getString(R.string.gate_add_dialog_title_enter_text))
						.showKeyboard()
						.setTargetFragment(AddGateFragment.this, REQUEST_DIALOG_GATE_CODE)
						.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL)
						.setPositiveButtonText(mActivity.getString(R.string.fragment_configuration_widget_dialog_btn_ok))
						.setNegativeButtonText(mActivity.getString(R.string.activity_fragment_btn_cancel))
						.show();
			}
		});

		return view;
	}

	public void setScanQrButtonEnabled(boolean enabled) {
		View view = getView();
		if (view == null)
			return;

		Button button = (Button) view.findViewById(R.id.gate_add_qr_button);
		if (button == null)
			return;

		if (enabled) {
			button.setEnabled(true);
			button.setText(R.string.gate_add_btn_qr);
			button.setTextColor(ContextCompat.getColor(mActivity, R.color.white));
		} else {
			button.setEnabled(false);
			button.setText(R.string.gate_add_btn_qr_loading);
			button.setTextColor(ContextCompat.getColor(mActivity, R.color.gray_light));
		}
	}

	@Override
	public void onPositiveButtonClicked(int requestCode, View view, BaseDialogFragment fragment) {
		TextInputLayout textInputLayout = (TextInputLayout) view.findViewById(R.id.dialog_edit_text_input_layout);
		if (!Utils.validateInput(mActivity, textInputLayout)) {
			return;
		}

		EditText editText = textInputLayout.getEditText();
		if (editText != null) {
			doRegisterGateTask(editText.getText().toString(), false);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_SCAN && resultCode == Activity.RESULT_OK) {
			// Enable the Scan QR button again
			setScanQrButtonEnabled(true);
			Log.d(TAG, data.getStringExtra("SCAN_FORMAT"));
			onScanQRCode(data.getStringExtra("SCAN_RESULT"));
		}
	}

	private void onScanQRCode(String data) {
		Pattern pattern = Pattern.compile("id=(\\d+)");
		Matcher matcher = pattern.matcher(data);

		if (matcher.find()) {
			doRegisterGateTask(matcher.group(1), true);
		} else {
			Toast.makeText(mActivity, R.string.gate_add_toast_error_invalid_qr_code, Toast.LENGTH_LONG).show();
		}
	}

	public void doRegisterGateTask(String id, final boolean scanned) {
		Gate gate = new Gate(id, null);

		// Set default timezone as the gate timezone
		gate.setUtcOffset(DateTimeZone.getDefault().getOffset(null) / (1000 * 60));

		RegisterGateTask registerGateTask = new RegisterGateTask(mActivity);
		registerGateTask.setListener(new CallbackTask.ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (success) {
					Toast.makeText(mActivity, R.string.gate_add_toast_gate_activated, Toast.LENGTH_LONG).show();
					mActivity.finish();
				} else {
					Toast.makeText(mActivity, R.string.gate_add_toast_gate_activate_failed, Toast.LENGTH_SHORT).show();
					if (scanned) {
						// QR scanning again
//						showQrScanner();
					}
					// the code dialog is still opened, nothing to do here
				}
			}
		});
		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(registerGateTask, gate, CallbackTaskManager.ProgressIndicator.PROGRESS_DIALOG);
	}

	private void showQrScanner() {
		Intent intent = new Intent(mActivity, ScanQrActivity.class);
		startActivityForResult(intent, REQUEST_SCAN);
	}
}

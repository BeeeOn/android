package com.rehivetech.beeeon.gui.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.avast.android.dialogs.core.BaseDialogFragment;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.IntroFragmentPagerAdapter;
import com.rehivetech.beeeon.gui.dialog.EditTextDialog;
import com.rehivetech.beeeon.gui.fragment.AddGateFragment;
import com.rehivetech.beeeon.gui.fragment.IntroImageFragment;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.RegisterGateTask;
import com.rehivetech.beeeon.util.TimezoneWrapper;
import com.rehivetech.beeeon.util.Utils;

import org.joda.time.DateTimeZone;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddGateActivity extends BaseGuideActivity implements AddGateFragment.OnAddGateListener, EditTextDialog.IPositiveButtonDialogListener {
	private static final String TAG = AddGateActivity.class.getSimpleName();
	private static final int SCAN_REQUEST = 0;
	public static final String TAG_FRAGMENT_CODE_DIALOG = "Code_dialog";

	@Override
	public void onResume() {
		super.onResume();

		// Enable scan QR button on each resume because we might not get any onActivityResult call
		setScanQrButtonEnabled(true);
	}

	@Override
	protected void onLastFragmentActionNext() {
		//there is nothing to do, but it is required to implement it
	}

	@Override
	protected IntroFragmentPagerAdapter initPagerAdapter() {
		// creating list of objects that will be used as params for the constructor of AddingUniversalFragment
		List<IntroImageFragment.ImageTextPair> pairs = Arrays.asList(
				new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_aa_first_step, R.string.gate_add_tut_add_gate_text_1, R.string.gate_add_tut_add_gate_title_1),
				new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_aa_second_step, R.string.gate_add_tut_add_gate_text_2, R.string.gate_add_tut_add_gate_title_2)
		);
		return new IntroFragmentPagerAdapter(getSupportFragmentManager(), pairs, new AddGateFragment());
	}

	@Override
	protected void closeActivity() {
		SharedPreferences prefs = Controller.getInstance(this).getUserSettings();
		if (prefs != null) {
			prefs.edit().putBoolean(Constants.PERSISTENCE_PREF_IGNORE_NO_GATE, true).apply();
		}
		super.closeActivity();
	}

	@Override
	protected int getLastPageNextTextResource() {
		mNext.setVisibility(View.INVISIBLE);
		return R.string.gate_add_btn_add;
	}

	public void doRegisterGateTask(String id, final boolean scanned) {
		Gate gate = new Gate(id, null);

		// Set default timezone as the gate timezone
		gate.setUtcOffset(DateTimeZone.getDefault().getOffset(null) / (1000 * 60));

		RegisterGateTask registerGateTask = new RegisterGateTask(this);
		registerGateTask.setListener(new CallbackTask.ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (success) {
					Toast.makeText(AddGateActivity.this, R.string.gate_add_toast_gate_activated, Toast.LENGTH_LONG).show();
					finish();
				} else {
					Toast.makeText(AddGateActivity.this, R.string.gate_add_toast_gate_activate_failed, Toast.LENGTH_SHORT).show();
					if (scanned) {
						// QR scanning again
						showQrScanner();
					}
					// the code dialog is still opened, nothing to do here
				}
			}
		});
		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(registerGateTask, gate, CallbackTaskManager.ProgressIndicator.PROGRESS_DIALOG);
	}

	@Override
	public void showQrScanner() {
		try {
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // PRODUCT_MODE for bar codes
			startActivityForResult(intent, SCAN_REQUEST);
		} catch (ActivityNotFoundException e) {
			try {
				Toast.makeText(this, R.string.gate_add_toast_error_no_qr_reader, Toast.LENGTH_LONG).show();
				// Let user to download e.g. Barcode Scanner
				Uri marketUri = Utils.isBlackBerry()
					? Uri.parse("https://appworld.blackberry.com/webstore/content/20395272")
					: Uri.parse("market://details?id=com.google.zxing.client.android");
				Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
				startActivity(marketIntent);
			} catch (ActivityNotFoundException e1) {
				Toast.makeText(this, R.string.gate_add_toast_error_no_google_play, Toast.LENGTH_LONG).show();
				setScanQrButtonEnabled(true);
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == SCAN_REQUEST && resultCode == RESULT_OK) {
			// Enable the Scan QR button again
			setScanQrButtonEnabled(true);
			onScanQRCode(data.getStringExtra("SCAN_RESULT"));
		}
	}

	private void onScanQRCode(String data) {
		Pattern pattern = Pattern.compile("id=(\\d+)");
		Matcher matcher = pattern.matcher(data);

		if (matcher.find()) {
			doRegisterGateTask(matcher.group(1), true);
		} else {
			Toast.makeText(this, R.string.gate_add_toast_error_invalid_qr_code, Toast.LENGTH_LONG).show();
		}
	}

	private void setScanQrButtonEnabled(boolean enabled) {
		AddGateFragment fragment = (AddGateFragment) mPagerAdapter.getFinalFragment();
		if (fragment != null) {
			fragment.setScanQrButtonEnabled(enabled);
		}
	}

	@Override
	public void onPositiveButtonClicked(int requestCode, View view, BaseDialogFragment fragment) {
		TextInputLayout textInputLayout = (TextInputLayout) view.findViewById(R.id.dialog_edit_text_input_layout);
		if(!Utils.validateInput(this, textInputLayout)){
			return;
		}

		EditText editText = textInputLayout.getEditText();
		if (editText != null) {
			doRegisterGateTask(editText.getText().toString(), false);
		}
	}
}

package com.rehivetech.beeeon.gui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.IntroFragmentPagerAdapter;
import com.rehivetech.beeeon.gui.fragment.EnterCodeDialogFragment;
import com.rehivetech.beeeon.gui.fragment.AddGateFragment;
import com.rehivetech.beeeon.gui.fragment.IntroImageFragment;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.RegisterGateTask;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddGateActivity extends BaseGuideActivity implements AddGateFragment.OnAddGateListener, EnterCodeDialogFragment.EnterCodeDialogListener {
	private static final String TAG = AddGateActivity.class.getSimpleName();
	private static final int SCAN_REQUEST = 0;
	public static final String TAG_FRAGMENT_CODE_DIALOG = "Code_dialog";

	@Override
	protected void onLastFragmentActionNext() {
		//there is nothing to do, but it is required to implement it
	}

	@Override
	protected IntroFragmentPagerAdapter initPagerAdapter() {
		// creating list of objects that will be used as params for the constructor of AddingUniversalFragment
		List<IntroImageFragment.ImageTextPair> pairs = Arrays.asList(
				new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_aa_first_step, R.string.tut_add_gate_text_1, R.string.tut_add_gate_title_1),
				new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_aa_second_step, R.string.tut_add_gate_text_2, R.string.tut_add_gate_title_2),
				new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_aa_third_step, R.string.tut_add_gate_text_3, R.string.tut_add_gate_title_3)
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
		return R.string.tutorial_add;
	}

	@Override
	public void onPositiveButtonClick(EnterCodeDialogFragment enterCodeDialogFragment, String id) {
		doRegisterGateTask(id, false);
	}

	@Override
	public void showEnterCodeDialog() {
		EnterCodeDialogFragment enterCodeDialogFragment = new EnterCodeDialogFragment();
		enterCodeDialogFragment.show(getSupportFragmentManager(), TAG_FRAGMENT_CODE_DIALOG);
	}

	public void doRegisterGateTask(String id, final boolean scanned) {
		Gate gate = new Gate();
		gate.setId(id);

		RegisterGateTask registerGateTask = new RegisterGateTask(this);
		registerGateTask.setListener(new CallbackTask.ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (success) {
					Toast.makeText(AddGateActivity.this, R.string.toast_adapter_activated, Toast.LENGTH_LONG).show();
					EnterCodeDialogFragment enterCodeDialogFragment = (EnterCodeDialogFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_CODE_DIALOG);
					if (enterCodeDialogFragment != null) {
						enterCodeDialogFragment.dismiss();
					}
					setResult(Activity.RESULT_OK);
					finish();
				} else {
					Toast.makeText(AddGateActivity.this, R.string.toast_adapter_activate_failed, Toast.LENGTH_SHORT).show();
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
				Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
				Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
				startActivity(marketIntent);
			} catch (ActivityNotFoundException e1) {
				Toast.makeText(this, R.string.toast_error_no_qr_reader, Toast.LENGTH_LONG).show();
			}
		}
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
			doRegisterGateTask(matcher.group(1), true);
		} else {
			Toast.makeText(this, R.string.toast_error_invalid_qr_code, Toast.LENGTH_LONG).show();
		}
	}
}

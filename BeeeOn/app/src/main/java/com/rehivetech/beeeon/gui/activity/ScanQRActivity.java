package com.rehivetech.beeeon.gui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;

import com.google.zxing.Result;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanQRActivity extends BaseApplicationActivity implements ZXingScannerView.ResultHandler {
	public static final String EXTRA_SCAN_RESULT = "SCAN_RESULT";
	public static final String EXTRA_CAMERA_PERMISSION_DENIED = "CAMERA_PERMISSION_DENIED";
	public static final String EXTRA_SCAN_FORMAT = "SCAN_FORMAT";

	private static final String STATE_IS_PERMISSION_DIALOG_SHOWN = "STATE_IS_PERMISSION_DIALOG_SHOWN";

	private boolean mIsRequestDialogShown = false;
	private ZXingScannerView mScannerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_qr);
		setupToolbar(R.string.gate_add_scan_qr_title, true);
		mScannerView = (ZXingScannerView) findViewById(R.id.scanner_view);
	}

	/**
	 * Is called only when any instance was saved (likewise in onCreate())
	 * @param savedInstanceState
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mIsRequestDialogShown = savedInstanceState.getBoolean(STATE_IS_PERMISSION_DIALOG_SHOWN);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_IS_PERMISSION_DIALOG_SHOWN, mIsRequestDialogShown);
	}

	@Override
	public void onResume() {
		super.onResume();
		mScannerView.setResultHandler(this);
		if (checkCameraPermission()) {
			mScannerView.startCamera();
		}
	}

	/**
	 * Checks if user has allowed camera permission, otherwise shows dialog
	 *
	 * @return success
	 */
	private boolean checkCameraPermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
			if (!mIsRequestDialogShown) {
				mIsRequestDialogShown = true;
				// explanation shown
				ActivityCompat.requestPermissions(this, new String[]{
						Manifest.permission.CAMERA,
				}, Constants.PERMISSION_CODE_CAMERA);
			}
			return false;
		}
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		mIsRequestDialogShown = false;
		switch (requestCode) {
			case Constants.PERMISSION_CODE_CAMERA:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission granted
					mScannerView.startCamera();
				} else {
					// permission denied
					Intent resultIntent = new Intent();
					resultIntent.putExtra(EXTRA_CAMERA_PERMISSION_DENIED, true);
					setResult(Activity.RESULT_CANCELED, resultIntent);
					finish();
				}
				break;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mScannerView.stopCamera();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
		}
		return false;
	}

	@Override
	public void handleResult(Result result) {
		Intent resultIntent = new Intent();
		resultIntent.putExtra(EXTRA_SCAN_RESULT, result.getText());
		resultIntent.putExtra(EXTRA_SCAN_FORMAT, result.getBarcodeFormat().toString());
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}
}

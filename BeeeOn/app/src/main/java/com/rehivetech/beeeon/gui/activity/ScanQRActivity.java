package com.rehivetech.beeeon.gui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.zxing.Result;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;

import icepick.State;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanQRActivity extends BaseApplicationActivity implements ZXingScannerView.ResultHandler {
	public static final String EXTRA_SCAN_RESULT = "SCAN_RESULT";
	public static final String EXTRA_CAMERA_PERMISSION_DENIED = "CAMERA_PERMISSION_DENIED";
	public static final String EXTRA_SCAN_FORMAT = "SCAN_FORMAT";

	@State public boolean mIsRequestDialogShown = false;
	private ZXingScannerView mScannerView;
	private String mScannerFormat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_qr);
		setupToolbar(R.string.gate_add_scan_qr_title, INDICATOR_BACK);

		mScannerView = (ZXingScannerView) findViewById(R.id.scanner_view);

		mScannerFormat = getIntent().getStringExtra(EXTRA_SCAN_FORMAT);
		if (mScannerFormat == null) {
			mScannerFormat = "QR_CODE";
		}
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
				// permission(s) granted
				if (grantResults.length > 0) {
					boolean isCameraGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
					if (isCameraGranted) {
						mScannerView.startCamera();
					}
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
	public void handleResult(Result result) {
		String scannedText = result.getText();
		String scannedFormat = result.getBarcodeFormat().toString();
		if (!mScannerFormat.equals(scannedFormat)) {
			Toast.makeText(this, R.string.gate_add_toast_error_invalid_qr_code, Toast.LENGTH_LONG).show();
			// resume scanning
			mScannerView.resumeCameraPreview(this);
			return;
		}

		// send good result
		Intent resultIntent = new Intent();
		resultIntent.putExtra(EXTRA_SCAN_RESULT, scannedText);
		resultIntent.putExtra(EXTRA_SCAN_FORMAT, scannedFormat);
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}
}

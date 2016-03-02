package com.rehivetech.beeeon.gui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanQrActivity extends BaseApplicationActivity implements ZXingScannerView.ResultHandler {
	private ZXingScannerView mScannerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mScannerView = new ZXingScannerView(this);
		setContentView(mScannerView);
	}

	@Override
	public void onResume() {
		super.onResume();
		mScannerView.setResultHandler(this);
		mScannerView.startCamera();
	}

	@Override
	public void onPause() {
		super.onPause();
		mScannerView.stopCamera();
	}

	@Override
	public void handleResult(Result result) {
		Intent resultIntent = new Intent();
		resultIntent.putExtra("SCAN_RESULT", result.getText());
		resultIntent.putExtra("SCAN_FORMAT", result.getBarcodeFormat().toString());
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}
}

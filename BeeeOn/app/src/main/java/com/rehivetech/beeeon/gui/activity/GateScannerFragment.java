package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.Result;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.BaseApplicationFragment;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * @author mlyko
 * @since 02.03.2016
 */
public class GateScannerFragment extends BaseApplicationFragment implements ZXingScannerView.ResultHandler {
	private ZXingScannerView mScannerView;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_gate_scan, container, false);
		mScannerView = (ZXingScannerView) view.findViewById(R.id.scanner_view);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		mScannerView.startCamera();

		mScannerView.setResultHandler(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mScannerView.stopCamera();
	}

	@Override
	public void handleResult(Result result) {
		Log.d("AAAAA", result.getText());
	}

	public void startCamera() {

		new Runnable() {
			@Override
			public void run() {
				mScannerView.setVisibility(View.VISIBLE);
			}
		}.run();
	}
}

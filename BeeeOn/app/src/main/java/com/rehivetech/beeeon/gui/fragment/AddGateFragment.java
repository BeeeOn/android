package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.activity.AddGateActivity;
import com.rehivetech.beeeon.gui.activity.MainActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddGateFragment extends TrackFragment {

	private static final String TAG = AddGateFragment.class.getSimpleName();
	private static final int SCAN_REQUEST = 0;

	public OnAddGateListener mCallback;
	private View mView;

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
		mView = inflater.inflate(R.layout.fragment_add_gate_dialog, container, false);

		initLayout();

		return mView;
	}

	private void initLayout() {
		((ImageButton) mView.findViewById(R.id.addgate_qrcode_button)).setOnClickListener(new OnClickListener() {
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
			String id = matcher.group(1);

			// Fill scanned data into edit text
			EditText serialNumberEdit = (EditText) mView.findViewById(R.id.addgate_ser_num);
			serialNumberEdit.setText(id);

			mCallback.onCodeScanned();
		} else {
			Toast.makeText(getActivity(), R.string.toast_error_invalid_qr_code, Toast.LENGTH_LONG).show();
		}

		//TODO: And click positive button
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
	}

}

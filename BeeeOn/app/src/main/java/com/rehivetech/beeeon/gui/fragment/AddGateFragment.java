package com.rehivetech.beeeon.gui.fragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.AddGateActivity;
import com.rehivetech.beeeon.gui.activity.MainActivity;
import com.rehivetech.beeeon.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddGateFragment extends TrackFragment {

	private static final String TAG = AddGateFragment.class.getSimpleName();
	private static final int SCAN_REQUEST = 0;

	public AddGateActivity mActivity;
	private LinearLayout mLayout;
	private View mView;
	private Controller mController;

	private EditText mGateCode;
	private EditText mGateName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get activity and controller
		mActivity = (AddGateActivity) getActivity();
		mController = Controller.getInstance(mActivity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.activity_add_gate_activity_dialog, container, false);

		mLayout = (LinearLayout) mView.findViewById(R.id.container);

		initLayout();

		return mView;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			Log.d(TAG, "ADD GATE fragment is visible");
			mActivity.setBtnLastPage();
			mActivity.setFragment(this);
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		}

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
						Toast.makeText(getActivity(),R.string.error_no_qr_reader, Toast.LENGTH_LONG).show();
					}
				}
			}
		});

		mGateCode = (EditText) mView.findViewById(R.id.addgate_ser_num);
		mGateName = (EditText) mView.findViewById(R.id.addgate_text_name);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == SCAN_REQUEST && resultCode == MainActivity.RESULT_OK) {
			onScanQRCode(data.getStringExtra("SCAN_RESULT"));
		}
	}

	private void onScanQRCode(String data) {
		// Fill scanned data into edit text
		EditText serialNumberEdit = (EditText) mView.findViewById(R.id.addgate_ser_num);
		serialNumberEdit.setText(data);

		Pattern pattern = Pattern.compile("id=(\\d+)");

		Matcher matcher = pattern.matcher(data);

		if (matcher.find()) {
			Log.d(TAG,String.format("Found code: %s", matcher.group(1)));
		}

		//TODO: And click positive button
	}

	public String getGateName() {
		return mGateName.getText().toString();
	}

	public String getGateCode() {
		return mGateCode.getText().toString();
	}

}

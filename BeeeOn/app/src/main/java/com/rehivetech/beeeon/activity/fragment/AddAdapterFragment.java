package com.rehivetech.beeeon.activity.fragment;

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
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.AddAdapterActivity;
import com.rehivetech.beeeon.activity.MainActivity;
import com.rehivetech.beeeon.base.TrackFragment;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;

public class AddAdapterFragment extends TrackFragment {

	private static final String TAG = AddAdapterFragment.class.getSimpleName();

	public AddAdapterActivity mActivity;
	private LinearLayout mLayout;
	private View mView;
	private Controller mController;
	
	private EditText mAdapterCode;
	private EditText mAdapterName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get activity and controller
		mActivity =  (AddAdapterActivity) getActivity();
		mController = Controller.getInstance(mActivity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.activity_add_adapter_activity_dialog, container, false);

		mLayout = (LinearLayout) mView.findViewById(R.id.container);

		initLayout();
		
		return mView;
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
	    super.setUserVisibleHint(isVisibleToUser);
	    if (isVisibleToUser) {
	    	Log.d(TAG, "ADD ADAPTER fragment is visible");
	    	mActivity.setBtnLastPage();
	    	mActivity.setFragment(this);
	    	InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	    }

	}
	
	private void initLayout() {
		((ImageButton) mView.findViewById(R.id.addadapter_qrcode_button)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Intent intent = new Intent("com.google.zxing.client.android.SCAN");
					intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // PRODUCT_MODE for bar codes
					startActivityForResult(intent, 0);
				} catch (Exception e) {
					Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
					Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
					startActivity(marketIntent);
				}
			}
		});
		
		mAdapterCode = (EditText) mView.findViewById(R.id.addadapter_ser_num);
		mAdapterName = (EditText) mView.findViewById(R.id.addadapter_text_name);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 0 && resultCode == MainActivity.RESULT_OK) {
			// Fill scanned code into edit text
			EditText serialNumberEdit = (EditText) mView.findViewById(R.id.addadapter_ser_num);
			serialNumberEdit.setText(data.getStringExtra("SCAN_RESULT"));

			//TODO: And click positive button
		}
	}
	
	public String getAdapterName() {
		return mAdapterName.getText().toString();
	}

	public String getAdapterCode() {
		return mAdapterCode.getText().toString();
	}

}

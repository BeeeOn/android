package com.rehivetech.beeeon.activity.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;

import com.viewpagerindicator.CirclePageIndicator;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.base.TrackDialogFragment;
import com.rehivetech.beeeon.test.TestFragmentAdapter;

public class IntroFragmentDialog extends TrackDialogFragment {
	
	private View mView;
	
	private TestFragmentAdapter mAdapter;
	private ViewPager mPager;
	private CirclePageIndicator mIndicator;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		// Get View
		mView = inflater.inflate(R.layout.activity_intro, null);
		
		
		
		builder.setView(mView);
		
		return builder.create();
		
	}
	

	
	@Override
	public void onResume() {
		super.onResume();

		final AlertDialog dialog = (AlertDialog) getDialog();
		dialog.getButton(Dialog.BUTTON_POSITIVE).setVisibility(View.GONE);
		dialog.getButton(Dialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
		
		mAdapter = new TestFragmentAdapter(getChildFragmentManager());
		
		mPager = (ViewPager)dialog.findViewById(R.id.intro_pager);
		mPager.setAdapter(mAdapter);
		
		mIndicator = (CirclePageIndicator)dialog.findViewById(R.id.intro_indicator);
		mIndicator.setViewPager(mPager);
		
		mIndicator.setPageColor(0x88000000);
		mIndicator.setFillColor(0xFF000000);
		mIndicator.setStrokeColor(0x88000000);
		
	}

}

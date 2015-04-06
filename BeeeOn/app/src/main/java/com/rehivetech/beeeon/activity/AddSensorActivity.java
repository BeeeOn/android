package com.rehivetech.beeeon.activity;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.viewpagerindicator.CirclePageIndicator;

import com.rehivetech.beeeon.AddSensorFragmentAdapter;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.fragment.AddSensorFragment;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.asynctask.CallbackTask.CallbackTaskListener;
import com.rehivetech.beeeon.asynctask.PairRequestTask;
import com.rehivetech.beeeon.asynctask.ReloadUninitializedFacilitiesTask;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;

public class AddSensorActivity extends BaseApplicationActivity {
	private static final String TAG = AddSensorActivity.class.getSimpleName();
	
	private Controller mController;
	private Adapter mPairAdapter;
	
	private AddSensorFragmentAdapter mAdapter;
	private ViewPager mPager;
	private CirclePageIndicator mIndicator;
	
	private AddSensorFragment mFragment;
	

	private ReloadUninitializedFacilitiesTask mReloadUninitializedFacilitiesTask;

	private PairRequestTask mPairRequestTask;
	
	private Button mSkip;
	private Button mCancel;
	private Button mNext;
	

	private boolean mFirstUse = true;
	
	private Activity mActivity;
    private Toolbar mToolbar;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(R.string.title_activity_add_sensor);
            setSupportActionBar(mToolbar);
        }
		
		// Get controller
		mController = Controller.getInstance(this);
		mPairAdapter = mController.getActiveAdapter();
		
		mActivity = this;
		
		mAdapter = new AddSensorFragmentAdapter(getSupportFragmentManager(),mActivity);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mPager = (ViewPager)findViewById(R.id.intro_pager);
		mPager.setAdapter(mAdapter);
		mPager.setOffscreenPageLimit(mAdapter.getCount());
		
		mIndicator = (CirclePageIndicator)findViewById(R.id.intro_indicator);
		mIndicator.setViewPager(mPager);
		
		mIndicator.setPageColor(0x88FFFFFF);
		mIndicator.setFillColor(0xFFFFFFFF);
		mIndicator.setStrokeColor(0x88FFFFFF);
		
		initButtons();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == Constants.SETUP_SENSOR_REQUEST_CODE ) {
			Log.d(TAG, "Return from setup sensor activity");
			if(resultCode == Constants.SETUP_SENSOR_CANCELED) {
				Log.d(TAG, "Activity was canceled");
			}
			else if (resultCode == Constants.SETUP_SENSOR_SUCCESS) {
				// Succes of add adapter -> setActive adapter a redraw ALL
				Log.d(TAG, "Setup sensor success");
				setResult(Constants.ADD_SENSOR_SUCCESS, data);
				finish();
			}
		}
	}
	
	
	private void initButtons() {
		mSkip = (Button) findViewById(R.id.add_adapter_skip);
		mCancel = (Button) findViewById(R.id.add_adapter_cancel);
		mNext = (Button) findViewById(R.id.add_adapter_next);
		
		mSkip.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mPager.setCurrentItem(mAdapter.getCount()-1);
			}
		});
		
		mCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(Constants.ADD_SENSOR_CANCELED);
				InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
				finish();
			}
		});
		
		mNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mNext.getText().equals(mActivity.getString(R.string.tutorial_next))){
					mPager.setCurrentItem(mPager.getCurrentItem()+1);
				}
				else if (mNext.getText().equals(mActivity.getString(R.string.addsensor_send_pair))) {
					doPairRequestTask(mPairAdapter.getId());
					mNext.setEnabled(false);
				}
			}
		});
		
		
	}
	
	public void setBtnLastPage() {
		mSkip.setVisibility(View.INVISIBLE);
		mNext.setText(mActivity.getString(R.string.addsensor_send_pair));
	}


	@Override
	protected void onAppResume() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onAppPause() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			setResult(Constants.ADD_SENSOR_CANCELED);
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	public void resetBtn() {
		mSkip.setVisibility(View.VISIBLE);
		mNext.setText(mActivity.getString(R.string.tutorial_next));
	}


	public void setFragment(AddSensorFragment fragment) {
		mFragment = fragment;
	}
	
	public void doReloadUninitializedFacilitiesTask(String adapterId, boolean forceReload) {
		mReloadUninitializedFacilitiesTask = new ReloadUninitializedFacilitiesTask(mActivity.getApplicationContext(), forceReload);

		mReloadUninitializedFacilitiesTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (!success) {
					return;
				}

				List<Facility> facilities = mController.getUninitializedFacilitiesModel().getUninitializedFacilitiesByAdapter(mPairAdapter.getId());

				if (facilities.size() > 0) {
					mFragment.stopTimer();
					Log.d(TAG, "Nasel jsem neinicializovane zarizeni !!!!");
					mFragment.stopTimer();
					// go to setup uninit sensor
					 Intent intent = new Intent(mActivity, SetupSensorActivity.class);
					 startActivityForResult(intent, Constants.SETUP_SENSOR_REQUEST_CODE);
				} else{
					if(mFirstUse) {
						mFirstUse = false;
						doPairRequestTask(mPairAdapter.getId());
						mNext.setEnabled(false);
					}
				}
			}

		});

		mReloadUninitializedFacilitiesTask.execute(adapterId);
	}
	
	private void doPairRequestTask(String adapterId) {
		// Send First automatic pair request
		mPairRequestTask = new PairRequestTask(mActivity.getApplicationContext());
		mPairRequestTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (success) {
					// Request was successfully sent
					mFragment.startTimer();
				} else {
					// Request wasn't send
					resetBtnPair();
				}
			}

		});
		mPairRequestTask.execute(adapterId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mReloadUninitializedFacilitiesTask != null) {
			mReloadUninitializedFacilitiesTask.cancel(true);
		}
	}


	public void checkUnInitSensor() {
		Log.d(TAG, "Send if some uninit facility");
		doReloadUninitializedFacilitiesTask(mPairAdapter.getId(), true);
	}


	public void resetBtnPair() {
		mNext.setText(mActivity.getString(R.string.addsensor_send_pair));
		mNext.setEnabled(true);
	}

}
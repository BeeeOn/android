package cz.vutbr.fit.iha.activity;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.CirclePageIndicator;

import cz.vutbr.fit.iha.AddSensorFragmentAdapter;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.fragment.AddSensorFragment;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.asynctask.CallbackTask.CallbackTaskListener;
import cz.vutbr.fit.iha.asynctask.PairRequestTask;
import cz.vutbr.fit.iha.asynctask.ReloadUninitializedTask;
import cz.vutbr.fit.iha.base.BaseApplicationActivity;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.util.Log;

public class AddSensorActivity extends BaseApplicationActivity {
	private static final String TAG = AddSensorActivity.class.getSimpleName();
	
	private Controller mController;
	private Adapter mPairAdapter;
	
	private AddSensorFragmentAdapter mAdapter;
	private ViewPager mPager;
	private CirclePageIndicator mIndicator;
	
	private AddSensorFragment mFragment;
	

	private ReloadUninitializedTask mReloadUninitializedTask;

	private PairRequestTask mPairRequestTask;
	
	private Button mSkip;
	private Button mCancel;
	private Button mNext;
	

	private boolean mFirstUse = true;
	
	private Activity mActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);
		
		// Get controller
		mController = Controller.getInstance(this);
		mPairAdapter = mController.getActiveAdapter();
		
		mActivity = this;
		
		mAdapter = new AddSensorFragmentAdapter(getSupportFragmentManager());

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher_null);
		
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
				//setActiveAdapterAndLocation();
				//redrawMenu();
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
				//SharedPreferences prefs = mController.getUserSettings();
				//if (prefs != null) {
				//	prefs.edit().putBoolean(Constants.PERSISTENCE_PREF_IGNORE_NO_ADAPTER, true).commit();
				//}
				setResult(Constants.ADD_SENSOR_CANCELED);
				finish();
			}
		});
		
		mNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mNext.getText().equals("NEXT")){
					mPager.setCurrentItem(mPager.getCurrentItem()+1);
				}
				else if (mNext.getText().equals("SEND PAIR")) {
					doPairRequestTask(mPairAdapter.getId());
					mNext.setEnabled(false);
				}
			}
		});
		
		
	}
	
	public void setBtnLastPage() {
		mSkip.setVisibility(View.INVISIBLE);
		mNext.setText("SEND PAIR");
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
		mNext.setText("NEXT");
	}


	public void setFragment(AddSensorFragment fragment) {
		mFragment = fragment;
	}
	
	public void doReloadUninitializedFacilitiesTask(String adapterId) {
		mReloadUninitializedTask = new ReloadUninitializedTask(mActivity.getApplicationContext());

		mReloadUninitializedTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (!success) {
					return;
				}

				List<Facility> facilities = mController.getUninitializedFacilities(mPairAdapter.getId());

				if (facilities.size() > 0) {
					mFragment.stopTimer();
					Log.d(TAG, "Nasel jsem neinicializovane zarizeni !!!!");
					mFragment.stopTimer();
					// go to setup uninit sensor

					 Intent intent = new Intent(mActivity, SetupSensorActivity.class);
					// intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					// intent.putExtras(bundle);
					 startActivityForResult(intent, Constants.SETUP_SENSOR_REQUEST_CODE);
					// finish();
				} else{
					if(mFirstUse) {
						mFirstUse = false;
						doPairRequestTask(mPairAdapter.getId());
						mNext.setEnabled(false);
					}
				}
			}

		});

		mReloadUninitializedTask.execute(adapterId);
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

		if (mReloadUninitializedTask != null) {
			mReloadUninitializedTask.cancel(true);
		}
	}


	public void checkUnInitSensor() {
		Log.d(TAG, "Send if some uninit facility");
		doReloadUninitializedFacilitiesTask(mPairAdapter.getId());
	}


	public void resetBtnPair() {
		mNext.setText("SEND PAIR");
		mNext.setEnabled(true);
	}

}

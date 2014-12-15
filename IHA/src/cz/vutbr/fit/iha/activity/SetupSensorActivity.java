package cz.vutbr.fit.iha.activity;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.viewpagerindicator.CirclePageIndicator;

import cz.vutbr.fit.iha.AddSensorFragmentAdapter;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.SetupSensorFragmentAdapter;
import cz.vutbr.fit.iha.activity.fragment.AddSensorFragment;
import cz.vutbr.fit.iha.activity.fragment.SetupSensorFragment;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.asynctask.CallbackTask.CallbackTaskListener;
import cz.vutbr.fit.iha.asynctask.InitializeFacilityTask;
import cz.vutbr.fit.iha.asynctask.PairRequestTask;
import cz.vutbr.fit.iha.asynctask.ReloadUninitializedTask;
import cz.vutbr.fit.iha.base.BaseApplicationActivity;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.pair.InitializeFacilityPair;
import cz.vutbr.fit.iha.util.Log;

public class SetupSensorActivity extends BaseApplicationActivity {
	private static final String TAG = SetupSensorActivity.class.getSimpleName();
	
	private Controller mController;
	private Adapter mPairAdapter;
	
	private SetupSensorFragmentAdapter mAdapter;
	private ViewPager mPager;
	private CirclePageIndicator mIndicator;
	
	private SetupSensorFragment mFragment;
	

	private ProgressDialog mProgress;
	
	private InitializeFacilityTask mTask;

	private Spinner mSpinner;
	private ListView mListOfName;
	private TextView mNewLocation;
	private Spinner mNewIconSpinner;
	
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
		
		mAdapter = new SetupSensorFragmentAdapter(getSupportFragmentManager());

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher_null);
		
		mPager = (ViewPager)findViewById(R.id.intro_pager);
		mPager.setAdapter(mAdapter);
		mPager.setOffscreenPageLimit(mAdapter.getCount());
		
		mIndicator = (CirclePageIndicator)findViewById(R.id.intro_indicator);
		mIndicator.setViewPager(mPager);
		mIndicator.setVisibility(View.GONE);
		
		// Prepare progress dialog
		mProgress = new ProgressDialog(mActivity);
		mProgress.setMessage(getString(R.string.progress_saving_data));
		mProgress.setCancelable(false);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		
		initButtons();
	}
	
	
	private void initButtons() {
		mSkip = (Button) findViewById(R.id.add_adapter_skip);
		mCancel = (Button) findViewById(R.id.add_adapter_cancel);
		mNext = (Button) findViewById(R.id.add_adapter_next);
		
		mSkip.setVisibility(View.INVISIBLE);
		
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
		mNext.setText("SAVE");
		mNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mSpinner = mFragment.getSpinner();
				mListOfName = mFragment.getListOfName();
				mNewLocation = mFragment.getNewLocation();
				mNewIconSpinner = mFragment.getNewIconSpinner();
				Facility newFacility = mController.getUninitializedFacilities(mPairAdapter.getId()).get(0);

				// Controll if Names arent empty
				for (int i = 0; i < newFacility.getDevices().size(); i++) {
					// Get new names from EditText
					String name = ((EditText) mListOfName.getChildAt(i).findViewById(R.id.setup_sensor_item_name)).getText().toString();
					Log.d(TAG, "Name of " + i + " is" + name);
					if (name.isEmpty()) {
						Toast.makeText(mActivity, getString(R.string.toast_empty_sensor_name), Toast.LENGTH_LONG).show();
						return;
					}
					// Set this new name to sensor
					newFacility.getDevices().get(i).setName(name);

				}

				Location location = null;
				// last location - means new one
				if (mSpinner.getSelectedItemPosition() == mSpinner.getCount() - 1) {

					// check new location name
					if (mNewLocation != null && mNewLocation.length() < 1) {
						Toast.makeText(mActivity, getString(R.string.toast_need_sensor_location_name), Toast.LENGTH_LONG).show();
						return;
					}

					location = new Location(Location.NEW_LOCATION_ID, mNewLocation.getText().toString(), mNewIconSpinner.getSelectedItemPosition());

				} else {
					location = (Location) mSpinner.getSelectedItem();
				}

				// Set location to facility
				newFacility.setLocationId(location.getId());

				// Set that facility was initialized
				newFacility.setInitialized(true);
				// Show progress bar for saving
				mProgress.show();

				// Save that facility
				Log.d(TAG, String.format("InitializeFacility - facility: %s, loc: %s", newFacility.getId(), location.getId()));
				doSaveDeviceTask(new InitializeFacilityPair(newFacility, location));
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


	public void resetBtn() {
		mSkip.setVisibility(View.VISIBLE);
		mNext.setText("NEXT");
	}


	public void setFragment(SetupSensorFragment fragment) {
		mFragment = fragment;
	}
	
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mTask != null) {
			mTask.cancel(true);
		}
	}
	
	private void doSaveDeviceTask(final InitializeFacilityPair pair) {
		mTask = new InitializeFacilityTask(mActivity.getApplicationContext());
		mTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {

				//AlertDialog dialog = (AlertDialog) getDialog();
				//if (dialog != null) {
					Toast.makeText(mActivity, getString(success ? R.string.toast_new_sensor_added : R.string.toast_new_sensor_not_added), Toast.LENGTH_LONG).show();
					mProgress.cancel();
					if(success){
						Intent intent = new Intent();
						intent.putExtra(Constants.SETUP_SENSOR_ACT_LOC, pair.location.getId());
						setResult(Constants.SETUP_SENSOR_SUCCESS,intent);
						finish();
					}
					
					//dialog.dismiss();
					//mActivity.setActiveAdapterID(mAdapter.getId());
					//mActivity.setActiveLocationID(pair.location.getId());
					//mActivity.redrawMenu();
					//mActivity.redrawDevices();
				//}
			}

		});

		mTask.execute(pair);
	}


}

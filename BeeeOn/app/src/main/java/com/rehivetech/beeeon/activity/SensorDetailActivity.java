package com.rehivetech.beeeon.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;


import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.fragment.SensorDetailFragment;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.asynctask.CallbackTask.CallbackTaskListener;
import com.rehivetech.beeeon.asynctask.ReloadFacilitiesTask;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.view.CustomViewPager;

/**
 * Class that handle screen with detail of some sensor
 * 
 * @author ThinkDeep
 * 
 */
public class SensorDetailActivity extends BaseApplicationActivity {

	private static final String TAG = SensorDetailActivity.class.getSimpleName();

	public static final String EXTRA_DEVICE_ID = "device_id";
	public static final String EXTRA_ADAPTER_ID = "adapter_id";
	public static final String EXTRA_ACTIVE_POS = "act_device_pos";

	private Controller mController;
	private List<Device> mDevices;

	private PagerAdapter mPagerAdapter;
	private ViewPager mPager;

	private String mActiveAdapterId;
	private String mActiveDeviceId;
	private int mActiveDevicePosition;

	private ProgressDialog mProgress;

	private ReloadFacilitiesTask mReloadFacilitiesTask;

    private Toolbar mToolbar;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_sensor_detail_wraper);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(R.string.title_activity_sensor_detail);
            setSupportActionBar(mToolbar);
        }
        
		setSupportProgressBarIndeterminate(true);
		setProgressBarIndeterminateVisibility(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Get controller
		mController = Controller.getInstance(this);



		Log.d(TAG, "onCreate()");

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mActiveAdapterId = bundle.getString(EXTRA_ADAPTER_ID);
			mActiveDeviceId = bundle.getString(EXTRA_DEVICE_ID);
		}
		if(savedInstanceState != null ){
			bundle = savedInstanceState;
			if (bundle != null) {
				mActiveAdapterId = bundle.getString(EXTRA_ADAPTER_ID);
				mActiveDeviceId = bundle.getString(EXTRA_DEVICE_ID);
				mActiveDevicePosition = bundle.getInt(EXTRA_ACTIVE_POS);
			}
		}

		if (mActiveAdapterId == null || mActiveDeviceId == null) {
			Toast.makeText(this, R.string.toast_wrong_or_no_device, Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == Constants.EDIT_SENSOR_REQUEST_CODE ) {
			Log.d(TAG, "Return from add adapter activity");
			if(resultCode == Constants.EDIT_SENSOR_CANCELED) {
				Log.d(TAG, "Activity was canceled");
			}
			else if (resultCode == Constants.EDIT_SENSOR_SUCCESS) {
				Log.d(TAG, "Edit sensor succes");
				doReloadFacilitiesTask(mActiveAdapterId, false);
			}
		}
	}

	@Override
	protected void onAppResume() {
		Log.d(TAG, "onAppResume()");

		if (mReloadFacilitiesTask == null) {
			doReloadFacilitiesTask(mActiveAdapterId, false);
		}
	}

	@Override
	protected void onAppPause() {
		if (mReloadFacilitiesTask != null) {
			mReloadFacilitiesTask.cancel(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return false;
	}

	@Override
	public void onStop() {
		if (mReloadFacilitiesTask != null) {
			mReloadFacilitiesTask.cancel(true);
		}
		super.onStop();
	}

	private void doReloadFacilitiesTask(final String adapterId, final boolean forceReload) {
		mReloadFacilitiesTask = new ReloadFacilitiesTask(this, forceReload);

		mReloadFacilitiesTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				Log.d(TAG, "Start reload task");
				Device device = mController.getDevice(adapterId, mActiveDeviceId);
				if (device == null) {
					Log.d(TAG, "Stop reload task");
					return;
				}

				List<Facility> facilities = mController.getFacilitiesByLocation(adapterId, device.getFacility().getLocationId());

				List<Device> devices = new ArrayList<Device>();
				for (Facility facility : facilities) {
					devices.addAll(facility.getDevices());
				}
				mDevices = devices;

				// Determine position of wanted device in this list
				for (int i = 0; i < devices.size(); i++) {
					if (devices.get(i).getId().equals(device.getId())) {
						mActiveDevicePosition = i;
						break;
					}
				}

				Log.d(TAG, String.format("String: %s, Size: %d", mDevices.toString(), mDevices.size()));
				initLayouts();
			}

		});

		mReloadFacilitiesTask.execute(adapterId);
	}

	/**
	 * A simple pager adapter that represents 5 {@link SensorDetailFragment} objects, in sequence.
	 */
	public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

		public ScreenSlidePagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public android.support.v4.app.Fragment getItem(int position) {
			Log.d(TAG, "Here 2 " + position);
			SensorDetailFragment fragment = new SensorDetailFragment();
			fragment.setSensorID(mDevices.get(position).getId());
			fragment.setLocationID(mDevices.get(position).getFacility().getLocationId());
			fragment.setPosition(position);
			fragment.setSelectedPosition(mActiveDevicePosition);
			fragment.setAdapterID(mActiveAdapterId);
			fragment.setFragmentAdapter(this);

			return fragment;
		}

		@Override
		public int getCount() {
			return mDevices.size();
		}


	}

	public void initLayouts() {
		// Instantiate a ViewPager and a PagerAdapter.
		mPager = (ViewPager) findViewById(R.id.sensor_detail_wraper);
		mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				mActiveDevicePosition = position;
				mActiveDeviceId = mDevices.get(position).getId();
				mActiveAdapterId = mDevices.get(position).getFacility().getAdapterId();

				// When changing pages, reset the action bar actions since they are dependent
				// on which page is currently active. An alternative approach is to have each
				// fragment expose actions itself (rather than the activity exposing actions),
				// but for simplicity, the activity provides the actions in this sample.
				// invalidateOptionsMenu();

			}
		});
		((CustomViewPager) mPager).setPagingEnabled(true);
		mPager.setOffscreenPageLimit(mDevices.size());
		mPager.setCurrentItem(mActiveDevicePosition);
	}

	public void setEnableSwipe(boolean state) {
		((CustomViewPager) mPager).setPagingEnabled(state);
	}

	public void setCurrentViewPager() {
		mPager.setCurrentItem(mActiveDevicePosition);
	}



	public ViewPager getPager() {
		return mPager;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(EXTRA_ADAPTER_ID, mActiveAdapterId);
		savedInstanceState.putString(EXTRA_DEVICE_ID, mActiveDeviceId);
		savedInstanceState.putInt(EXTRA_ACTIVE_POS,mActiveDevicePosition);

		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}

}

package cz.vutbr.fit.iha.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.fragment.SensorDetailFragment;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.asynctask.CallbackTask.CallbackTaskListener;
import cz.vutbr.fit.iha.asynctask.ReloadFacilitiesTask;
import cz.vutbr.fit.iha.base.BaseApplicationActivity;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.util.Log;
import cz.vutbr.fit.iha.view.CustomViewPager;

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
	
	private Controller mController;
	private List<BaseDevice> mDevices;
	
	private PagerAdapter mPagerAdapter;
	private ViewPager mPager;
	
	private String mActiveAdapterId;
	private String mActiveDeviceId;
	private int mActiveDevicePosition;
	

	private ProgressDialog mProgress;

	private ReloadFacilitiesTask mReloadFacilitiesTask;

	private boolean mForceReload = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_sensor_detail_wraper);

		setSupportProgressBarIndeterminate(true);
		setProgressBarIndeterminateVisibility(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher_white);

		// Get controller
		mController = Controller.getInstance(getApplicationContext());

		// Prepare progress dialog
		mProgress = new ProgressDialog(this);
		mProgress.setMessage(getString(R.string.progress_saving_data));
		mProgress.setCancelable(false);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		Log.d(TAG, "onCreate()");
		
	    Bundle bundle = getIntent().getExtras();
	    if (bundle != null) {
	    	mActiveAdapterId = bundle.getString(EXTRA_ADAPTER_ID);
			mActiveDeviceId = bundle.getString(EXTRA_DEVICE_ID);
	    } else {
	    	bundle = savedInstanceState;
	    	if (bundle != null) {
	    		mActiveAdapterId = bundle.getString(EXTRA_ADAPTER_ID);
	    		mActiveDeviceId = bundle.getString(EXTRA_DEVICE_ID);
	    	}
	    }
		
		if (mActiveAdapterId == null || mActiveDeviceId == null) {
			Toast.makeText(this, R.string.toast_wrong_or_no_device, Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}
	
	@Override
	protected void onAppResume() {
		Log.d(TAG, "onAppResume()");
		
		if (mReloadFacilitiesTask == null) {
			doReloadFacilitiesTask(mActiveAdapterId);
		}
	}

	@Override
	protected void onAppPause() {}

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

	private void doReloadFacilitiesTask(final String adapterId) {
		mReloadFacilitiesTask = new ReloadFacilitiesTask(this, mForceReload);
		
		mReloadFacilitiesTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				Log.d(TAG, "Start reload task");
				BaseDevice device = mController.getDevice(adapterId, mActiveDeviceId);
				if (device == null) {
					Log.d(TAG, "Stop reload task");
					return;
				}

				List<Facility> facilities = mController.getFacilitiesByLocation(adapterId, device.getFacility().getLocationId());

				List<BaseDevice> devices = new ArrayList<BaseDevice>();
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
	 * A simple pager adapter that represents 5 {@link ScreenSlidePageFragment} objects, in sequence.
	 */
	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		public ScreenSlidePagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public SensorDetailFragment getItem(int position) {
			Log.d(TAG, "Here 2 " + position);
			return SensorDetailFragment.create(mDevices.get(position).getId(), mDevices.get(position).getFacility().getLocationId(), position, mActiveDevicePosition, mActiveAdapterId);
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
	
	public ProgressDialog getProgressDialog() {
		return mProgress;
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    savedInstanceState.putString(EXTRA_ADAPTER_ID, mActiveAdapterId);
	    savedInstanceState.putString(EXTRA_DEVICE_ID, mActiveDeviceId);
	    
	    // Always call the superclass so it can save the view hierarchy state
	    super.onSaveInstanceState(savedInstanceState);
	}	
	
	public void redraw(){
		Log.d(TAG, "Start redraw ActiveDevice:"+mActiveDeviceId+" ActiveAdapter:"+mActiveAdapterId);
		mForceReload  = true;
		doReloadFacilitiesTask(mActiveAdapterId);
		
	}
	
}

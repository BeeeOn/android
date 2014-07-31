package cz.vutbr.fit.iha.activity;

import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.view.CustomViewPager;

/**
 * Class that handle screen with detail of some sensor
 * @author ThinkDeep
 *
 */
public class SensorDetailActivity extends BaseActivity
{
	
	private Controller mController;
	private List<BaseDevice> mDevices;
	
	private PagerAdapter mPagerAdapter;
	private ViewPager mPager;
	protected int countSensor;
	
	private String mLocationOfSensorID;
	private int mSensorPosition;
	
	private static final String TAG = SensorDetailActivity.class.getSimpleName();
	
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
		mController = Controller.getInstance(this);
		
		//SensorDetailFragment fragment = new SensorDetailFragment();
		//fragment.setArguments(getIntent().getExtras());
		//FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		
		//ft.replace(R.id.sensor_detail_wraper, fragment);
		//ft.commit();
		
		Bundle bundle = getIntent().getExtras();
		mLocationOfSensorID = bundle.getString("LocationOfSensorID");
		mSensorPosition = bundle.getInt("SensorPosition");
		
		GetDevicesTask task = new GetDevicesTask();
		task.execute(new String[] { mLocationOfSensorID });
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
	
	
	/**
	 * Changes selected location and redraws list of adapters there
	 */
	private class GetDevicesTask extends AsyncTask<String, Void, List<BaseDevice> > {
		@Override
		protected List<BaseDevice> doInBackground(String... locationID) {

			List<BaseDevice> devices = mController.getDevicesByLocation(locationID[0]);
			Log.d(TAG, "String:" + devices.toString() + " Size:" + devices.size());
			
			return devices;
		}

		@Override
		protected void onPostExecute(List<BaseDevice>  devices) {
			initLayouts(devices);
			
		}
	}

	
	/**
     * A simple pager adapter that represents 5 {@link ScreenSlidePageFragment} objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public SensorDetailFragment getItem(int position) {
        	Log.d(TAG, "Here 2 "+ position);
            return SensorDetailFragment.create(mDevices.get(position).getId(),mDevices.get(position).getLocationId(),position,mSensorPosition);
        }

        @Override
        public int getCount() {
            return countSensor;
        }
    }


	public void initLayouts(List<BaseDevice> devices) {
		// Set number of fragments
		countSensor = devices.size();
		
		// Set devices
		mDevices = devices;
		
		
		// Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.sensor_detail_wraper);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When changing pages, reset the action bar actions since they are dependent
                // on which page is currently active. An alternative approach is to have each
                // fragment expose actions itself (rather than the activity exposing actions),
                // but for simplicity, the activity provides the actions in this sample.
                //invalidateOptionsMenu();
            }
        });
        ((CustomViewPager) mPager).setPagingEnabled(true);
        mPager.setOffscreenPageLimit(countSensor);
        mPager.setCurrentItem(mSensorPosition);
	}
	
	public void setEnableSwipe (boolean state) {
		((CustomViewPager) mPager).setPagingEnabled(state);
	}
	
	public void setCurrentViewPager() {
		mPager.setCurrentItem(mSensorPosition);
	}
}

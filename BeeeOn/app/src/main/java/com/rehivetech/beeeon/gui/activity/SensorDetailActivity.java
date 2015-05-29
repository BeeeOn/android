package com.rehivetech.beeeon.gui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.SensorDetailFragment;
import com.rehivetech.beeeon.threading.CallbackTask.ICallbackTaskListener;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.gui.view.CustomViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that handle screen with detail of some sensor
 *
 * @author ThinkDeep
 */
public class SensorDetailActivity extends BaseApplicationActivity {

	private static final String TAG = SensorDetailActivity.class.getSimpleName();

	public static final String EXTRA_MODULE_ID = "module_id";
	public static final String EXTRA_GATE_ID = "gate_id";
	public static final String EXTRA_ACTIVE_POS = "act_module_pos";

	private Controller mController;
	private List<Module> mModules;

	private PagerAdapter mPagerAdapter;
	private ViewPager mPager;

	private String mActiveGateId;
	private String mActiveModuleId;
	private int mActiveModulePosition;

	private ProgressDialog mProgress;

	private Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor_detail_wraper);

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if (mToolbar != null) {
			mToolbar.setTitle(R.string.title_activity_sensor_detail);
			setSupportActionBar(mToolbar);
		}

		//setBeeeOnProgressBarVisibility(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Get controller
		mController = Controller.getInstance(this);


		Log.d(TAG, "onCreate()");

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mActiveGateId = bundle.getString(EXTRA_GATE_ID);
			mActiveModuleId = bundle.getString(EXTRA_MODULE_ID);
		}
		if (savedInstanceState != null) {
			bundle = savedInstanceState;
			if (bundle != null) {
				mActiveGateId = bundle.getString(EXTRA_GATE_ID);
				mActiveModuleId = bundle.getString(EXTRA_MODULE_ID);
				mActiveModulePosition = bundle.getInt(EXTRA_ACTIVE_POS);
			}
		}

		if (mActiveGateId == null || mActiveModuleId == null) {
			Toast.makeText(this, R.string.toast_wrong_or_no_device, Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.EDIT_SENSOR_REQUEST_CODE) {
			Log.d(TAG, "Return from add gate activity");
			if (resultCode == Constants.EDIT_SENSOR_CANCELED) {
				Log.d(TAG, "Activity was canceled");
			} else if (resultCode == Constants.EDIT_SENSOR_SUCCESS) {
				Log.d(TAG, "Edit sensor succes");
				doReloadDevicesTask(mActiveGateId, false);
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		doReloadDevicesTask(mActiveGateId, false);
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

	private void doReloadDevicesTask(final String gateId, final boolean forceReload) {
		ReloadGateDataTask reloadDevicesTask = new ReloadGateDataTask(this, forceReload, ReloadGateDataTask.ReloadWhat.DEVICES);

		reloadDevicesTask.setListener(new ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				Log.d(TAG, "Start reload task");
				Module module = mController.getDevicesModel().getModule(gateId, mActiveModuleId);
				if (module == null) {
					Log.d(TAG, "Stop reload task");
					return;
				}

				List<Device> devices = mController.getDevicesModel().getDevicesByLocation(gateId, module.getDevice().getLocationId());

				List<Module> modules = new ArrayList<Module>();
				for (Device device : devices) {
					modules.addAll(device.getModules());
				}
				mModules = modules;

				// Determine position of wanted module in this list
				for (int i = 0; i < modules.size(); i++) {
					if (modules.get(i).getId().equals(module.getId())) {
						mActiveModulePosition = i;
						break;
					}
				}

				Log.d(TAG, String.format("String: %s, Size: %d", mModules.toString(), mModules.size()));
				initLayouts();
			}

		});

		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(reloadDevicesTask, gateId);
	}

	/**
	 * A simple pager gate that represents 5 {@link SensorDetailFragment} objects, in sequence.
	 */
	public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

		public ScreenSlidePagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public android.support.v4.app.Fragment getItem(int position) {
			Log.d(TAG, "Here 2 " + position);
			SensorDetailFragment fragment = new SensorDetailFragment();
			fragment.setSensorID(mModules.get(position).getId());
			fragment.setLocationID(mModules.get(position).getDevice().getLocationId());
			fragment.setPosition(position);
			fragment.setSelectedPosition(mActiveModulePosition);
			fragment.setGateId(mActiveGateId);
			fragment.setFragmentAdapter(this);

			return fragment;
		}

		@Override
		public int getCount() {
			return mModules.size();
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
				mActiveModulePosition = position;
				mActiveModuleId = mModules.get(position).getId();
				mActiveGateId = mModules.get(position).getDevice().getGateId();

				// When changing pages, reset the action bar actions since they are dependent
				// on which page is currently active. An alternative approach is to have each
				// fragment expose actions itself (rather than the activity exposing actions),
				// but for simplicity, the activity provides the actions in this sample.
				// invalidateOptionsMenu();

			}
		});
		((CustomViewPager) mPager).setPagingEnabled(true);
		mPager.setOffscreenPageLimit(mModules.size());
		mPager.setCurrentItem(mActiveModulePosition);
		//setBeeeOnProgressBarVisibility(false);
		visibleAllElements();
	}

	private void visibleAllElements() {
		//HIDE progress
		findViewById(R.id.sensor_progress_wraper).setVisibility(View.INVISIBLE);
		// SHOW details
		mPager.setVisibility(View.VISIBLE);
	}

	public void setEnableSwipe(boolean state) {
		((CustomViewPager) mPager).setPagingEnabled(state);
	}

	public void setCurrentViewPager() {
		mPager.setCurrentItem(mActiveModulePosition);
	}

	public ViewPager getPager() {
		return mPager;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(EXTRA_GATE_ID, mActiveGateId);
		savedInstanceState.putString(EXTRA_MODULE_ID, mActiveModuleId);
		savedInstanceState.putInt(EXTRA_ACTIVE_POS, mActiveModulePosition);

		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}
}

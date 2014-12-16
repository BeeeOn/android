package cz.vutbr.fit.iha.activity.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.AddAdapterActivity;
import cz.vutbr.fit.iha.activity.AddSensorActivity;
import cz.vutbr.fit.iha.activity.MainActivity;
import cz.vutbr.fit.iha.activity.SensorDetailActivity;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.Device;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.arrayadapter.SensorListAdapter;
import cz.vutbr.fit.iha.asynctask.CallbackTask.CallbackTaskListener;
import cz.vutbr.fit.iha.asynctask.ReloadFacilitiesTask;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.thread.ToastMessageThread;
import cz.vutbr.fit.iha.util.Log;

public class SensorListFragment extends SherlockFragment {

	private static final String TAG = SensorListFragment.class.getSimpleName();

	private static final String LCTN = "lastlocation";
	private static final String ADAPTER_ID = "lastAdapterId";

	public static boolean ready = false;
	private SwipeRefreshLayout mSwipeLayout;
	private MainActivity mActivity;
	private Controller mController;
	private ReloadFacilitiesTask mReloadFacilitiesTask;

	private SensorListAdapter mSensorAdapter;
	private ListView mSensorList;

	private View mView;

	private String mActiveLocationId;
	private String mActiveAdapterId;
	private boolean isPaused;

	//
	private ActionMode mMode;
	
	// For tutorial
	private boolean mFirstUseAddAdapter = true;
	private boolean mFirstUseAddSensor = true;
	private ShowcaseView mSV;
	private RelativeLayout.LayoutParams lps;

	public SensorListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		ready = false;

		if (!(getSherlockActivity() instanceof MainActivity)) {
			throw new IllegalStateException("Activity holding SensorListFragment must be MainActivity");
		}

		mActivity = (MainActivity) getSherlockActivity();
		mController = Controller.getInstance(mActivity);

		if (savedInstanceState != null) {
			mActiveLocationId = savedInstanceState.getString(LCTN);
			mActiveAdapterId = savedInstanceState.getString(ADAPTER_ID);
		}
		// Check if tutoril was showed
		SharedPreferences prefs = mController.getUserSettings();
		if (prefs != null) {
			mFirstUseAddAdapter = prefs.getBoolean(Constants.TUTORIAL_ADD_ADAPTER_SHOWED, true);
			mFirstUseAddSensor = prefs.getBoolean(Constants.TUTORIAL_ADD_SENSOR_SHOWED, true);
		}
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.listofsensors, container, false);
		redrawDevices();
		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated()");
		ready = true;

		// mActivity = (MainActivity) getActivity();

		// Init swipe-refreshig layout
		mSwipeLayout = (SwipeRefreshLayout) mActivity.findViewById(R.id.swipe_container);
		if (mSwipeLayout == null) {
			return;
		}
		mSwipeLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {

				Adapter adapter = mController.getActiveAdapter();
				if (adapter == null) {
					mSwipeLayout.setRefreshing(false);
					return;
				}

				doReloadFacilitiesTask(adapter.getId());
			}
		});
		mSwipeLayout.setColorScheme(R.color.iha_separator, R.color.iha_item_bg, R.color.iha_secundary_pink, R.color.iha_text_color);
	}

	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause()");
		ready = false;
	}

	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume()");
		ready = true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy()");

		ready = false;

		if (mReloadFacilitiesTask != null) {
			mReloadFacilitiesTask.cancel(true);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(ADAPTER_ID, mActiveAdapterId);
		savedInstanceState.putString(LCTN, mActiveLocationId);
		super.onSaveInstanceState(savedInstanceState);
	}

	private void doReloadFacilitiesTask(String adapterId) {
		mReloadFacilitiesTask = new ReloadFacilitiesTask(getActivity().getApplicationContext(), true);

		mReloadFacilitiesTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				mActivity.redrawDevices();
				mActivity.redrawMenu();
				mSwipeLayout.setRefreshing(false);
			}
		});

		mReloadFacilitiesTask.execute(adapterId);
	}
	
	

	public boolean redrawDevices() {
		if (isPaused) {
			mActivity.setSupportProgressBarIndeterminateVisibility(false);
			return false;
		}
		List<Facility> facilities;
		
		if(mActiveLocationId == null){
			facilities = mController.getFacilitiesByLocation(mActiveAdapterId, mActiveLocationId);
		}
		else if(mActiveLocationId.equals(Constants.GUI_MENU_ALL_SENSOR_ID)){
			// All sensor from adapter
			facilities = mController.getFacilitiesByAdapter(mActiveAdapterId);
		}
		else {
			// Facilities from one location 
			facilities = mController.getFacilitiesByLocation(mActiveAdapterId, mActiveLocationId);
		}

		Log.d(TAG, "LifeCycle: redraw devices list start");

		mSensorList = (ListView) mView.findViewById(R.id.listviewofsensors);
		TextView noItem = (TextView) mView.findViewById(R.id.nosensorlistview);
		ImageView addBtn = (ImageView) mView.findViewById(R.id.nosensorlistview_addsensor_image);

		

		List<Device> devices = new ArrayList<Device>();
		for (Facility facility : facilities) {
			devices.addAll(facility.getDevices());
		}

		if (mSensorList == null) {
			mActivity.setSupportProgressBarIndeterminateVisibility(false);
			Log.e(TAG, "LifeCycle: bad timing or what?");
			return false; // TODO: this happens when we're in different activity
							// (detail), fix that by changing that activity
							// (fragment?) first?
		}

		boolean haveDevices = devices.size() > 0;
		boolean haveAdapters = mController.getAdapters().size() > 0;
		
		
		
		if(!haveAdapters) { // NO Adapter
			noItem.setVisibility(View.VISIBLE);
			noItem.setText(R.string.no_adapter_cap);
			addBtn.setVisibility(View.VISIBLE);
			mSensorList.setVisibility(View.GONE);
			addBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showAddAdapterDialog();
				}
			});
			
			SharedPreferences prefs = mController.getUserSettings();
			if (!(prefs != null && !prefs.getBoolean(Constants.PERSISTENCE_PREF_IGNORE_NO_ADAPTER, false))) {
				// TUTORIAL
				if(mFirstUseAddAdapter) {
					showTutorialAddAdapter();
					if (prefs != null) {
						prefs.edit().putBoolean(Constants.TUTORIAL_ADD_ADAPTER_SHOWED, false).commit();
					}
				}
			}
			
		}
		else if (!haveDevices) { // Have Adapter but any Devices
			noItem.setVisibility(View.VISIBLE);
			addBtn.setVisibility(View.VISIBLE);
			mSensorList.setVisibility(View.GONE);
			addBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showAddSensorDialog();
				}
			});
			if(mFirstUseAddSensor){
				showTutorialAddSensor();
				SharedPreferences prefs = mController.getUserSettings();
				if (prefs != null) {
					prefs.edit().putBoolean(Constants.TUTORIAL_ADD_SENSOR_SHOWED, false).commit();
				}
			}
		}
		else { // Have adapter and devices
			noItem.setVisibility(View.GONE);
			addBtn.setVisibility(View.GONE);
			mSensorList.setVisibility(View.VISIBLE);
		}

		OnClickListener addSensorListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				showAddSensorDialog();
			}
		};

		// Update list adapter
		mSensorAdapter = new SensorListAdapter(mActivity, devices, addSensorListener);
		mSensorList.setAdapter(mSensorAdapter);

		if (haveDevices) {
			// Capture listview menu item click
			mSensorList.setOnItemClickListener(new ListView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if (position == mSensorAdapter.getCount() - 1) {
						showAddSensorDialog();
						return;
					}

					Device device = mSensorAdapter.getDevice(position);

					Bundle bundle = new Bundle();
					bundle.putString(SensorDetailActivity.EXTRA_ADAPTER_ID, device.getFacility().getAdapterId());
					bundle.putString(SensorDetailActivity.EXTRA_DEVICE_ID, device.getId());
					Intent intent = new Intent(mActivity, SensorDetailActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});
			mSensorList.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					mMode = getSherlockActivity().startActionMode(new ActionModeEditSensors());
					return true;
				}
			});
		}

		mActivity.setSupportProgressBarIndeterminateVisibility(false);
		Log.d(TAG, "LifeCycle: getsensors end");
		return true;
	}

	private void showTutorialAddSensor() {
		lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		int marginPixel = 15;
		lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

		int margin = ((Number) (getResources().getDisplayMetrics().density * marginPixel)).intValue();
		lps.setMargins(margin, margin, margin, margin);
		ViewTarget target = new ViewTarget(mView.findViewById(R.id.nosensorlistview_addsensor_image));
		
		OnShowcaseEventListener	listener = new OnShowcaseEventListener() {
			
			@Override
			public void onShowcaseViewShow(ShowcaseView showcaseView) {
				Log.d(TAG, "OnShowCase show");
				
			}
			
			@Override
			public void onShowcaseViewHide(ShowcaseView showcaseView) {
				Log.d(TAG, "OnShowCase hide");
				// TODO: Save that ADD ADAPTER was clicked
				
			}
			
			@Override
			public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
				Log.d(TAG, "OnShowCase did hide");
				
			}
		}; 
		
		mSV = new ShowcaseView.Builder(mActivity, true)
		.setTarget(target)
		.setContentTitle("ADD SENSOR")
		.setContentText("For add your new sensor, please click on plus.")
		.setStyle(R.style.CustomShowcaseTheme)
		.setShowcaseEventListener(listener)
		.build();
		mSV.setButtonPosition(lps);
		mSV.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Showcase click");
			}
		});
		
	}

	private void showTutorialAddAdapter() {
		lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		int marginPixel = 15;
		lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

		int margin = ((Number) (getResources().getDisplayMetrics().density * marginPixel)).intValue();
		lps.setMargins(margin, margin, margin, margin);
		ViewTarget target = new ViewTarget(mView.findViewById(R.id.nosensorlistview_addsensor_image));
		
		OnShowcaseEventListener	listener = new OnShowcaseEventListener() {
			
			@Override
			public void onShowcaseViewShow(ShowcaseView showcaseView) {
				Log.d(TAG, "OnShowCase show");
				
			}
			
			@Override
			public void onShowcaseViewHide(ShowcaseView showcaseView) {
				Log.d(TAG, "OnShowCase hide");
				// TODO: Save that ADD ADAPTER was clicked
				
			}
			
			@Override
			public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
				Log.d(TAG, "OnShowCase did hide");
				
			}
		}; 
		
		mSV = new ShowcaseView.Builder(mActivity, true)
		.setTarget(target)
		.setContentTitle("ADD ADAPTER")
		.setContentText("For add your new adapter, please click on plus.")
		.setStyle(R.style.CustomShowcaseTheme)
		.setShowcaseEventListener(listener)
		.build();
		mSV.setButtonPosition(lps);
		mSV.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Showcase click");
			}
		});
	}

	protected void showAddAdapterDialog() {
		Log.d(TAG, "HERE ADD ADAPTER +");
		Intent intent = new Intent(mActivity, AddAdapterActivity.class);
		mActivity.startActivityForResult(intent, Constants.ADD_ADAPTER_REQUEST_CODE);
	}
	
	protected void showAddSensorDialog() {
		Log.d(TAG, "HERE ADD SENSOR +");
		Intent intent = new Intent(mActivity, AddSensorActivity.class);
		mActivity.startActivityForResult(intent, Constants.ADD_SENSOR_REQUEST_CODE);
	}
	public void setLocationID(String locID) {
		mActiveLocationId = locID;
	}

	public void setAdapterID(String adaID) {
		mActiveAdapterId = adaID;
	}

	public void setIsPaused(boolean value) {
		isPaused = value;
	}

	class ActionModeEditSensors implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.sensorlist_actionmode, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			if (item.getTitle().equals(getResources().getString(R.string.action_hide_sensor))) {
				// doHideSensorTask(mDeviceHide);
			} else if (item.getTitle().equals(getResources().getString(R.string.action_hide_facility))) {
				new ToastMessageThread(mActivity, R.string.toast_not_implemented).start();
			} else if (item.getTitle().equals(getResources().getString(R.string.action_unregist_facility))) {
				new ToastMessageThread(mActivity, R.string.toast_not_implemented).start();
			}

			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mMode = null;

		}
	}

}
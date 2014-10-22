package cz.vutbr.fit.iha.activity.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.MainActivity;
import cz.vutbr.fit.iha.activity.SensorDetailActivity;
import cz.vutbr.fit.iha.activity.dialog.AddSensorFragmentDialog;
import cz.vutbr.fit.iha.activity.fragment.SensorDetailFragment.AnActionModeOfEpicProportions;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.arrayadapter.SensorListAdapter;
import cz.vutbr.fit.iha.asynctask.CallbackTask.CallbackTaskListener;
import cz.vutbr.fit.iha.asynctask.ReloadFacilitiesTask;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.menu.NavDrawerMenu;

public class SensorListFragment extends SherlockFragment {

	private static final String TAG = SensorListFragment.class.getSimpleName();
	private static final String ADD_SENSOR_TAG = "addSensorDialog";
	public static boolean ready = false;
	private SwipeRefreshLayout mSwipeLayout;
	private MainActivity mActivity;
	private Controller mController;
	private ReloadFacilitiesTask mReloadFacilitiesTask;
	
	private NavDrawerMenu mNavDrawerMenu;
	
	private SensorListAdapter mSensorAdapter;
	private ListView mSensorList;
	
	private Handler mTimeHandler = new Handler();
	private Runnable mTimeRun;
	
	private String mActiveLocationId;
	private String mActiveAdapterId;
	private boolean isPaused;
	

	//
	private ActionMode mMode;
	
	public SensorListFragment(MainActivity context) {
		mActivity = context;
	}
	
	public SensorListFragment () {
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		ready = false;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.listofsensors, container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated()");
		ready = true;

		mActivity = (MainActivity) getActivity();
		mController = Controller.getInstance(mActivity.getApplicationContext());
		
		// Init swipe-refreshig layout
		mSwipeLayout = (SwipeRefreshLayout) mActivity.findViewById(R.id.swipe_container);
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
	
	private void doReloadFacilitiesTask(String adapterId) {
		mReloadFacilitiesTask = new ReloadFacilitiesTask(getActivity().getApplicationContext(), true);
		
		mReloadFacilitiesTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				mActivity.redrawDevices();
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
		
		// TODO: this works, but its not the best solution
		if (!SensorListFragment.ready) {
			mTimeRun = new Runnable() {
				@Override
				public void run() {
					redrawDevices();
					Log.d(TAG, "LifeCycle: getsensors in timer");
				}
			};
			if (!isPaused)
				mTimeHandler.postDelayed(mTimeRun, 500);

			Log.d(TAG, "LifeCycle: getsensors timer run");
			return false;
		}
		mTimeHandler.removeCallbacks(mTimeRun);
		Log.d(TAG, "LifeCycle: getsensors timer remove");

		
		List<Facility> facilities = mController.getFacilitiesByLocation(mActiveAdapterId, mActiveLocationId);
		
		Log.d(TAG, "LifeCycle: redraw devices list start");

		mNavDrawerMenu.setDefaultTitle();

		mSensorList = (ListView) getView().findViewById(R.id.listviewofsensors);
		TextView nosensor = (TextView) getView().findViewById(R.id.nosensorlistview);
		ImageView addsensor = (ImageView) getView().findViewById(R.id.nosensorlistview_addsensor_image);
		
		addsensor.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "HERE ADD SENSOR +");
				mController.unignoreUninitialized(mActiveAdapterId);

				DialogFragment newFragment = new AddSensorFragmentDialog();
			    newFragment.show(mActivity.getSupportFragmentManager(), ADD_SENSOR_TAG);
				return;
			}
		});

		List<BaseDevice> devices = new ArrayList<BaseDevice>();
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
		
		// If no sensors - display text
		nosensor.setVisibility(haveDevices ? View.GONE : View.VISIBLE);
		
		// If we have no sensors but we have adapters - display add button
		addsensor.setVisibility(haveDevices || !haveAdapters ? View.GONE : View.VISIBLE);
		
		// If we have adapters (but we're right now in empty room) show list so we can pull it to refresh
		mSensorList.setVisibility(haveDevices || haveAdapters ? View.VISIBLE : View.GONE);

		OnClickListener addSensorListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "HERE ADD SENSOR +");
				mController.unignoreUninitialized(mActiveAdapterId);

				//Intent intent = new Intent(LocationScreenActivity.this, AddSensorFragmentDialog.class);
				//startActivity(intent);
				
				DialogFragment newFragment = new AddSensorFragmentDialog();
			    newFragment.show(mActivity.getSupportFragmentManager(), ADD_SENSOR_TAG);
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
						Log.d(TAG, "HERE ADD SENSOR +");
						mController.unignoreUninitialized(mActiveAdapterId);
	
						//Intent intent = new Intent(LocationScreenActivity.this, AddSensorFragmentDialog.class);
						//startActivity(intent);
						
						DialogFragment newFragment = new AddSensorFragmentDialog();
					    newFragment.show(mActivity.getSupportFragmentManager(), ADD_SENSOR_TAG);
						return;
					}
	
					// final BaseDevice selectedItem = devices.get(position);
	
					// setSupportProgressBarIndeterminateVisibility(true);
	
					BaseDevice device = mSensorAdapter.getDevice(position);
					
					Bundle bundle = new Bundle();
					bundle.putString(SensorDetailActivity.EXTRA_ADAPTER_ID, device.getFacility().getAdapterId());
					bundle.putString(SensorDetailActivity.EXTRA_DEVICE_ID, device.getId());
					Intent intent = new Intent(mActivity, SensorDetailActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
					// finish();
				}
			});
			mSensorList.setOnItemLongClickListener( new  OnItemLongClickListener() {
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

	public void setMenu(NavDrawerMenu menu) {
		mNavDrawerMenu = menu;
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
			menu.add("Hide sensor").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add("Hide facility").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			menu.add("Cancel").setIcon(R.drawable.iha_ic_action_cancel).setTitle("Cancel").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			// TODO Auto-generated method stub
			if (item.getTitle().equals("Save")) {
				// sName.setText(sNameEdit.getText());
			}
			// sNameEdit.setVisibility(View.GONE);
			// sName.setVisibility(View.VISIBLE);

			// sNameEdit.clearFocus();
			// getSherlockActivity().getCurrentFocus().clearFocus();
			// InputMethodManager imm = (InputMethodManager) getSystemService(
			// getBaseContext().INPUT_METHOD_SERVICE);
			// imm.hideSoftInputFromWindow(mDrawerItemEdit.getWindowToken(), 0);
			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// TODO Auto-generated method stub
			// sNameEdit.clearFocus();
			// sNameEdit.setVisibility(View.GONE);
			// sName.setVisibility(View.VISIBLE);
			mMode = null;

		}
	}

}
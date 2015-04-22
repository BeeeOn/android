package com.rehivetech.beeeon.activity.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.AddAdapterActivity;
import com.rehivetech.beeeon.activity.AddSensorActivity;
import com.rehivetech.beeeon.activity.MainActivity;
import com.rehivetech.beeeon.activity.SensorDetailActivity;
import com.rehivetech.beeeon.activity.listItem.LocationListItem;
import com.rehivetech.beeeon.activity.listItem.SensorListItem;
import com.rehivetech.beeeon.arrayadapter.SenListAdapter;
import com.rehivetech.beeeon.asynctask.CallbackTask.CallbackTaskListener;
import com.rehivetech.beeeon.asynctask.ReloadAdapterDataTask;
import com.rehivetech.beeeon.asynctask.RemoveFacilityTask;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.pair.DelFacilityPair;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TutorialHelper;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class SensorListFragment extends Fragment {

	private static final String TAG = SensorListFragment.class.getSimpleName();

	private static final String LCTN = "lastlocation";
	private static final String ADAPTER_ID = "lastAdapterId";

	public static boolean ready = false;
	private SwipeRefreshLayout mSwipeLayout;
	private MainActivity mActivity;
	private Controller mController;
	private ReloadAdapterDataTask mReloadFacilitiesTask;

	private SenListAdapter mSensorAdapter;
	private StickyListHeadersListView mSensorList;
    private FloatingActionButton mFAM;
	private ArrayList<Integer> mFABMenuIcon = new ArrayList<>();
	private ArrayList<String> mFABMenuLabels = new ArrayList<>();


	private View mView;

	private String mActiveLocationId;
	private String mActiveAdapterId;
	private boolean isPaused;

	//
	private ActionMode mMode;
	
	// For tutorial
	private boolean mFirstUseAddAdapter = true;
	private boolean mFirstUseAddSensor = true;

    private Device mSelectedItem;
    private int mSelectedItemPos;
    private RemoveFacilityTask mRemoveFacilityTask;
	private ReloadAdapterDataTask mFullReloadTask;

	public SensorListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		ready = false;

		if (!(getActivity() instanceof MainActivity)) {
			throw new IllegalStateException("Activity holding SensorListFragment must be MainActivity");
		}

		mActivity = (MainActivity) getActivity();
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
				Log.d(TAG,"Refreshing list of sensors");
				Adapter adapter = mController.getActiveAdapter();
				if (adapter == null) {
					mSwipeLayout.setRefreshing(false);
					return;
				}
				mActivity.redraw();
				doReloadFacilitiesTask(adapter.getId(), true);
			}
		});
		mSwipeLayout.setColorSchemeColors(  R.color.beeeon_primary_cyan, R.color.beeeon_text_color,R.color.beeeon_secundary_pink);
	}

	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause()");
		ready = false;
		if(mMode != null)
			mMode.finish();
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

	public boolean redrawDevices() {
		if (isPaused) {
			mActivity.setSupportProgressBarIndeterminateVisibility(false);
			return false;
		}
		List<Facility> facilities;
        List<Location> locations;

		Log.d(TAG, "LifeCycle: redraw devices list start");


		// get UI elements
		mSwipeLayout = (SwipeRefreshLayout) mView.findViewById(R.id.swipe_container);
		mSensorList = (StickyListHeadersListView) mView.findViewById(R.id.listviewofsensors);
		TextView noItem = (TextView) mView.findViewById(R.id.nosensorlistview);
		Button refreshBtn = (Button) mView.findViewById(R.id.sensor_list_refresh_btn);

		// REFRESH listener
		OnClickListener refreshNoAdapter = new OnClickListener() {
			@Override
			public void onClick(View v) {
				doFullReloadTask(true);
			}
		};
		OnClickListener refreshNoSensor = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Adapter adapter = mController.getActiveAdapter();
				if(adapter != null) {
					doReloadFacilitiesTask(adapter.getId(), true);
				} else {
					doFullReloadTask(true);
				}
			}
		};

		mSensorAdapter = new SenListAdapter(mActivity);

        mFAM = (FloatingActionButton) mView.findViewById(R.id.fab);

        // All locations on adapter
        locations = mController.getLocationsModel().getLocationsByAdapter(mActiveAdapterId);

        List<Device> devices = new ArrayList<Device>();
		for (Location loc : locations) {
			mSensorAdapter.addHeader(new LocationListItem(loc.getName(),loc.getIconResource(),loc.getId()));
            // all facilities from actual location
            facilities = mController.getFacilitiesModel().getFacilitiesByLocation(mActiveAdapterId,loc.getId());
            for(Facility fac : facilities) {
				for(int x = 0; x < fac.getDevices().size(); x++) {
					Device dev = fac.getDevices().get(x);
					mSensorAdapter.addItem(new SensorListItem(dev,dev.getId(),mActivity,(x==(fac.getDevices().size()-1))?true:false));
				}
				devices.addAll(fac.getDevices());
			}
		}

		if (mSensorList == null) {
			mActivity.setSupportProgressBarIndeterminateVisibility(false);
			Log.e(TAG, "LifeCycle: bad timing or what?");
			return false; // TODO: this happens when we're in different activity
							// (detail), fix that by changing that activity
							// (fragment?) first?
		}

		boolean haveDevices = devices.size() > 0;
		boolean haveAdapters = mController.getAdaptersModel().getAdapters().size() > 0;

		// Buttons in floating menu

		if(!haveAdapters) { // NO Adapter
			// Set right visibility
			noItem.setVisibility(View.VISIBLE);
			noItem.setText(R.string.no_adapter_cap);
			refreshBtn.setVisibility(View.VISIBLE);
			mSensorList.setVisibility(View.GONE);
			if (mSwipeLayout != null)
				mSwipeLayout.setVisibility(View.GONE);
			// FAB
			mFABMenuIcon.add(R.drawable.ic_add_white_24dp);
			mFABMenuLabels.add(mActivity.getString(R.string.action_addadapter));
			refreshBtn.setOnClickListener(refreshNoAdapter);

			SharedPreferences prefs = mController.getUserSettings();
			if (!(prefs != null && !prefs.getBoolean(Constants.PERSISTENCE_PREF_IGNORE_NO_ADAPTER, false))) {
				// TUTORIAL
				if(mFirstUseAddAdapter && !Controller.isDemoMode()) {
					mFirstUseAddAdapter = false;
					mActivity.getMenu().closeMenu();
					TutorialHelper.showAddAdapterTutorial(mActivity, mView);
					if (prefs != null) {
						prefs.edit().putBoolean(Constants.TUTORIAL_ADD_ADAPTER_SHOWED, false).apply();
					}
				}
			}
			
		}
		else if (!haveDevices) { // Have Adapter but any Devices
			// Set right visibility
			noItem.setVisibility(View.VISIBLE);
			refreshBtn.setVisibility(View.VISIBLE);
			mSensorList.setVisibility(View.GONE);
			if (mSwipeLayout != null)
				mSwipeLayout.setVisibility(View.GONE);

			refreshBtn.setOnClickListener(refreshNoSensor);
			// FAB
			mFABMenuIcon.add(R.drawable.ic_add_white_24dp);
			mFABMenuIcon.add(R.drawable.ic_add_white_24dp);
			mFABMenuLabels.add(mActivity.getString(R.string.action_addadapter));
			mFABMenuLabels.add(mActivity.getString(R.string.action_addsensor));
			if(mFirstUseAddSensor && !Controller.isDemoMode()){
				mFirstUseAddSensor = false;
				mActivity.getMenu().closeMenu();
				TutorialHelper.showAddSensorTutorial(mActivity, mView);
				SharedPreferences prefs = mController.getUserSettings();
				if (prefs != null) {
					prefs.edit().putBoolean(Constants.TUTORIAL_ADD_SENSOR_SHOWED, false).apply();
				}
			}
		}
		else { // Have adapter and devices
			noItem.setVisibility(View.GONE);
			refreshBtn.setVisibility(View.GONE);
			mSensorList.setVisibility(View.VISIBLE);
			if (mSwipeLayout != null)
				mSwipeLayout.setVisibility(View.VISIBLE);
			// FAB
			mFABMenuIcon.add(R.drawable.ic_add_white_24dp);
			mFABMenuIcon.add(R.drawable.ic_add_white_24dp);
			mFABMenuLabels.add(mActivity.getString(R.string.action_addadapter));
			mFABMenuLabels.add(mActivity.getString(R.string.action_addsensor));
		}

        // Listener for add dialogs
		OnClickListener fabMenuListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mFABMenuLabels.get(v.getId()).equals(mActivity.getString(R.string.action_addsensor))) {
					showAddSensorDialog();
				}
				else if (mFABMenuLabels.get(v.getId()).equals(mActivity.getString(R.string.action_addadapter))) {
					showAddAdapterDialog();
				}
				Log.d(TAG, "FAB MENU HERE " + v.getId());
			}
		};

		mFAM.setMenuItems(convertToInt(mFABMenuIcon), mFABMenuLabels.toArray(new String[mFABMenuLabels.size()]),
				R.style.fab_item_menu,fabMenuListener, getResources().getDrawable(R.drawable.ic_action_cancel));
		mFAM.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG,"FAB BTN HER");
				mFAM.triggerMenu(90);
			}
		});

		mSensorList.setAdapter(mSensorAdapter);
		mFAM.attachToListView(mSensorList.getWrappedList());

		if (haveDevices) {
			// Capture listview menu item click
			mSensorList.setOnItemClickListener(new ListView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Device device = mSensorAdapter.getDevice(position);

					Bundle bundle = new Bundle();
					bundle.putString(SensorDetailActivity.EXTRA_ADAPTER_ID, device.getFacility().getAdapterId());
					bundle.putString(SensorDetailActivity.EXTRA_DEVICE_ID, device.getId());
					Intent intent = new Intent(mActivity, SensorDetailActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});
			Adapter tmpAda = mController.getAdaptersModel().getAdapter(mActiveAdapterId);
			if(tmpAda != null) {
				if(mController.isUserAllowed(tmpAda.getRole())) {
					mSensorList.setOnItemLongClickListener(new OnItemLongClickListener() {
						@Override
						public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
							mMode =  ((ActionBarActivity) getActivity()).startSupportActionMode(new ActionModeEditSensors());
                            mSelectedItem = mSensorAdapter.getDevice(position);
                            mSelectedItemPos = position;
                            mSensorAdapter.getItem(mSelectedItemPos).setIsSelected();
							return true;
						}
					});
				}
			}
		}

		mActivity.setSupportProgressBarIndeterminateVisibility(false);
		Log.d(TAG, "LifeCycle: getsensors end");
		return true;
	}

	private int[] convertToInt(ArrayList<Integer> IntegerList) {
		int s = IntegerList.size();
		int[] intArray = new int[s];
		for (int i = 0; i < s; i++) {
			intArray[i] = IntegerList.get(i).intValue();
		}
		return  intArray;
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

	public void setMenuID(String locID) {
		mActiveLocationId = locID;
	}

	public void setAdapterID(String adaID) {
		mActiveAdapterId = adaID;
	}

	public void setIsPaused(boolean value) {
		isPaused = value;
	}

    private void doReloadFacilitiesTask(String adapterId, boolean forceRefresh) {
        mReloadFacilitiesTask = new ReloadAdapterDataTask(getActivity().getApplicationContext(), forceRefresh, ReloadAdapterDataTask.ReloadWhat.FACILITIES);

        mReloadFacilitiesTask.setListener(new CallbackTaskListener() {

            @Override
            public void onExecute(boolean success) {
				if (!success)
					return;

                mActivity.redraw();
                mSwipeLayout.setRefreshing(false);
            }
        });

        mReloadFacilitiesTask.execute(adapterId);
    }

	private void doFullReloadTask(boolean forceRefresh) {
		mFullReloadTask = new ReloadAdapterDataTask(getActivity().getApplicationContext(), forceRefresh, ReloadAdapterDataTask.ReloadWhat.ADAPTERS_AND_ACTIVE_ADAPTER);

		mFullReloadTask.setListener(new CallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (!success)
					return;

				mActivity.setActiveAdapterAndMenu();
				mActivity.redraw();
			}
		});

		mFullReloadTask.execute();
	}

    private void doRemoveFacilityTask(Facility facility) {
        mRemoveFacilityTask = new RemoveFacilityTask(getActivity().getApplicationContext());
        DelFacilityPair pair = new DelFacilityPair(facility.getId(), facility.getAdapterId());

        mRemoveFacilityTask.setListener(new CallbackTaskListener() {
            @Override
            public void onExecute(boolean success) {
                mActivity.redraw();
                if (success) {
                    // Hlaska o uspechu
                }
                else {
                    // Hlaska o neuspechu
                }
            }
        });
        mRemoveFacilityTask.execute(pair);
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
			if (item.getItemId() == R.id.sensor_menu_del) {
				doRemoveFacilityTask(mSelectedItem.getFacility());
			}

			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mSensorAdapter.getItem(mSelectedItemPos).setNotSelected();
            mSelectedItem = null;
            mSelectedItemPos = 0;
			mMode = null;

		}
	}
}
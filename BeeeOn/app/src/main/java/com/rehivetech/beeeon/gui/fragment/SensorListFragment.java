package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ScrollDirectionListener;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.AddGateActivity;
import com.rehivetech.beeeon.gui.activity.AddDeviceActivity;
import com.rehivetech.beeeon.gui.activity.MainActivity;
import com.rehivetech.beeeon.gui.activity.SensorDetailActivity;
import com.rehivetech.beeeon.gui.adapter.SenListAdapter;
import com.rehivetech.beeeon.gui.listItem.LocationListItem;
import com.rehivetech.beeeon.gui.listItem.SensorListItem;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.threading.CallbackTask.ICallbackTaskListener;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.threading.task.RemoveDeviceTask;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TutorialHelper;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class SensorListFragment extends BaseApplicationFragment {

	private static final String TAG = SensorListFragment.class.getSimpleName();

	private static final String LCTN = "lastlocation";
	private static final String GATE_ID = "lastGateId";


	private SwipeRefreshLayout mSwipeLayout;
	private MainActivity mActivity;

	private SenListAdapter mSensorAdapter;
	private ArrayList<Integer> mFABMenuIcon = new ArrayList<>();
	private ArrayList<String> mFABMenuLabels = new ArrayList<>();


	private View mView;

	private String mActiveLocationId;
	private String mActiveGateId;
	private boolean isPaused;

	//
	private ActionMode mMode;

	// For tutorial
	private boolean mFirstUseAddGate = true;
	private boolean mFirstUseAddSensor = true;

	private Module mSelectedItem;
	private int mSelectedItemPos;

	public SensorListFragment() {
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mActivity = (MainActivity) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must be subclass of MainActivity");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mActiveLocationId = savedInstanceState.getString(LCTN);
			mActiveGateId = savedInstanceState.getString(GATE_ID);
		}
		// Check if tutoril was showed
		SharedPreferences prefs = Controller.getInstance(mActivity).getUserSettings();
		if (prefs != null) {
			mFirstUseAddGate = prefs.getBoolean(Constants.TUTORIAL_ADD_GATE_SHOWED, true);
			mFirstUseAddSensor = prefs.getBoolean(Constants.TUTORIAL_ADD_SENSOR_SHOWED, true);
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "OnCreateView");
		mView = inflater.inflate(R.layout.listofsensors, container, false);

		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated()");

		redrawModules();

		// Init swipe-refreshig layout
		mSwipeLayout = (SwipeRefreshLayout) mActivity.findViewById(R.id.swipe_container);
		if (mSwipeLayout == null) {
			return;
		}
		mSwipeLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				Log.d(TAG, "Refreshing list of sensors");
				Gate gate = Controller.getInstance(mActivity).getActiveGate();
				if (gate == null) {
					mSwipeLayout.setRefreshing(false);
					return;
				}
				mActivity.redraw();
				doReloadDevicesTask(gate.getId(), true);
			}
		});
		mSwipeLayout.setColorSchemeColors(R.color.beeeon_primary_cyan, R.color.beeeon_text_color, R.color.beeeon_secundary_pink);
	}

	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause()");
		if (mMode != null)
			mMode.finish();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(GATE_ID, mActiveGateId);
		savedInstanceState.putString(LCTN, mActiveLocationId);
		super.onSaveInstanceState(savedInstanceState);
	}

	public boolean redrawModules() {
		if (isPaused) {
			return false;
		}
		List<Device> devices;
		List<Location> locations;
		final Controller controller = Controller.getInstance(mActivity);

		Log.d(TAG, "LifeCycle: redraw modules list start");

		mView = getView();
		// get UI elements
		mSwipeLayout = (SwipeRefreshLayout) mView.findViewById(R.id.swipe_container);
		final StickyListHeadersListView sensorList = (StickyListHeadersListView) mView.findViewById(R.id.listviewofsensors);
		TextView noItem = (TextView) mView.findViewById(R.id.nosensorlistview);
		Button refreshBtn = (Button) mView.findViewById(R.id.sensor_list_refresh_btn);

		// REFRESH listener
		OnClickListener refreshNoGate = new OnClickListener() {
			@Override
			public void onClick(View v) {
				doFullReloadTask(true);
			}
		};
		OnClickListener refreshNoSensor = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Gate gate = controller.getActiveGate();
				if (gate != null) {
					doReloadDevicesTask(gate.getId(), true);
				} else {
					doFullReloadTask(true);
				}
			}
		};

		mSensorAdapter = new SenListAdapter(mActivity);

		final FloatingActionButton floatingActionButton = (FloatingActionButton) mView.findViewById(R.id.fab);

		// All locations on gate
		locations = controller.getLocationsModel().getLocationsByGate(mActiveGateId);

		List<Module> modules = new ArrayList<Module>();
		for (Location loc : locations) {
			mSensorAdapter.addHeader(new LocationListItem(loc.getName(), loc.getIconResource(), loc.getId()));
			// all devices from actual location
			devices = controller.getDevicesModel().getDevicesByLocation(mActiveGateId, loc.getId());
			for (Device fac : devices) {
				for (int x = 0; x < fac.getModules().size(); x++) {
					Module dev = fac.getModules().get(x);
					mSensorAdapter.addItem(new SensorListItem(dev, dev.getId(), mActivity, (x == (fac.getModules().size() - 1)) ? true : false));
				}
				modules.addAll(fac.getModules());
			}
		}

		if (sensorList == null) {
			Log.e(TAG, "LifeCycle: bad timing or what?");
			return false; // TODO: this happens when we're in different activity
			// (detail), fix that by changing that activity
			// (fragment?) first?
		}

		boolean haveModules = modules.size() > 0;
		boolean haveGates = controller.getGatesModel().getGates().size() > 0;

		// Buttons in floating menu

		if (!haveGates) { // NO Gate
			// Set right visibility
			noItem.setVisibility(View.VISIBLE);
			noItem.setText(R.string.no_gate_cap);
			refreshBtn.setVisibility(View.VISIBLE);
			sensorList.setVisibility(View.GONE);
			if (mSwipeLayout != null)
				mSwipeLayout.setVisibility(View.GONE);
			// FAB
			mFABMenuIcon.add(R.drawable.ic_add_white_24dp);
			mFABMenuLabels.add(mActivity.getString(R.string.action_addgate));
			refreshBtn.setOnClickListener(refreshNoGate);

			SharedPreferences prefs = controller.getUserSettings();
			if (!(prefs != null && !prefs.getBoolean(Constants.PERSISTENCE_PREF_IGNORE_NO_GATE, false))) {
				// TUTORIAL
				if (mFirstUseAddGate && !controller.isDemoMode()) {
					mFirstUseAddGate = false;
					mActivity.getMenu().closeMenu();
					TutorialHelper.showAddGateTutorial((MainActivity) mActivity, mView);
					if (prefs != null) {
						prefs.edit().putBoolean(Constants.TUTORIAL_ADD_GATE_SHOWED, false).apply();
					}
				}
			}

		} else if (!haveModules) { // Have Gate but any Modules
			// Set right visibility
			noItem.setVisibility(View.VISIBLE);
			noItem.setText(R.string.no_sensor_cap);
			refreshBtn.setVisibility(View.VISIBLE);
			sensorList.setVisibility(View.GONE);
			if (mSwipeLayout != null)
				mSwipeLayout.setVisibility(View.GONE);

			refreshBtn.setOnClickListener(refreshNoSensor);
			// FAB
			mFABMenuIcon.add(R.drawable.ic_add_white_24dp);
			mFABMenuIcon.add(R.drawable.ic_add_white_24dp);
			mFABMenuLabels.add(mActivity.getString(R.string.action_addgate));
			mFABMenuLabels.add(mActivity.getString(R.string.action_addsensor));
			if (mFirstUseAddSensor && !controller.isDemoMode()) {
				mFirstUseAddSensor = false;
				mActivity.getMenu().closeMenu();
				TutorialHelper.showAddSensorTutorial((MainActivity) mActivity, mView);
				SharedPreferences prefs = controller.getUserSettings();
				if (prefs != null) {
					prefs.edit().putBoolean(Constants.TUTORIAL_ADD_SENSOR_SHOWED, false).apply();
				}
			}
		} else { // Have gate and modules
			noItem.setVisibility(View.GONE);
			refreshBtn.setVisibility(View.GONE);
			sensorList.setVisibility(View.VISIBLE);
			if (mSwipeLayout != null)
				mSwipeLayout.setVisibility(View.VISIBLE);
			// FAB
			mFABMenuIcon.add(R.drawable.ic_add_white_24dp);
			mFABMenuIcon.add(R.drawable.ic_add_white_24dp);
			mFABMenuLabels.add(mActivity.getString(R.string.action_addgate));
			mFABMenuLabels.add(mActivity.getString(R.string.action_addsensor));
		}

		// Listener for add dialogs
		OnClickListener fabMenuListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mFABMenuLabels.get(v.getId()).equals(mActivity.getString(R.string.action_addsensor))) {
					showAddSensorDialog();
				} else if (mFABMenuLabels.get(v.getId()).equals(mActivity.getString(R.string.action_addgate))) {
					showAddGateDialog();
				}
				Log.d(TAG, "FAB MENU HERE " + v.getId());
			}
		};

		sensorList.setAdapter(mSensorAdapter);

		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) { // API 14 +
			floatingActionButton.setMenuItems(Utils.convertIntegers(mFABMenuIcon), mFABMenuLabels.toArray(new String[mFABMenuLabels.size()]),
					R.style.fab_item_menu, fabMenuListener, getResources().getDrawable(R.drawable.ic_action_cancel));
			floatingActionButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d(TAG, "FAB BTN HER");
					floatingActionButton.triggerMenu(90);
				}
			});
		} else {
			// API 10 to 13
			// Show dialof to select Add Gate or Add sensor

			floatingActionButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String[] mStringArray = new String[mFABMenuLabels.size()];
					mStringArray = mFABMenuLabels.toArray(mStringArray);
					mActivity.showOldAddDialog(mStringArray);
				}
			});

		}

		AbsListView.OnScrollListener ListListener = new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				int topRowVerticalPosition = (sensorList == null || sensorList.getListChildCount() == 0) ?
						0 : sensorList.getListChildAt(0).getTop();
				mSwipeLayout.setEnabled((topRowVerticalPosition >= 0));
			}
		};

		floatingActionButton.attachToListView(sensorList.getWrappedList(), new ScrollDirectionListener() {
			@Override
			public void onScrollDown() {

			}

			@Override
			public void onScrollUp() {

			}
		}, ListListener);


		if (haveModules) {
			// Capture listview menu item click
			sensorList.setOnItemClickListener(new ListView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Module module = mSensorAdapter.getModule(position);
					Bundle bundle = new Bundle();
					bundle.putString(SensorDetailActivity.EXTRA_GATE_ID, module.getDevice().getGateId());
					bundle.putString(SensorDetailActivity.EXTRA_MODULE_ID, module.getId());
					Intent intent = new Intent(mActivity, SensorDetailActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});
			Gate tmpAda = controller.getGatesModel().getGate(mActiveGateId);
			if (tmpAda != null) {
				if (controller.isUserAllowed(tmpAda.getRole())) {
					sensorList.setOnItemLongClickListener(new OnItemLongClickListener() {
						@Override
						public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
							mMode = mActivity.startSupportActionMode(new ActionModeEditSensors());
							mSelectedItem = mSensorAdapter.getModule(position);
							mSelectedItemPos = position;
							mSensorAdapter.getItem(mSelectedItemPos).setIsSelected();
							return true;
						}
					});
				}
			}
		}

		Log.d(TAG, "LifeCycle: getsensors end");
		return true;
	}

	public void showAddGateDialog() {
		Log.d(TAG, "HERE ADD GATE +");
		Intent intent = new Intent(mActivity, AddGateActivity.class);
		mActivity.startActivityForResult(intent, Constants.ADD_GATE_REQUEST_CODE);
	}

	public void showAddSensorDialog() {
		Log.d(TAG, "HERE ADD SENSOR +");
		Intent intent = new Intent(mActivity, AddDeviceActivity.class);
		mActivity.startActivityForResult(intent, Constants.ADD_SENSOR_REQUEST_CODE);
	}

	public void setMenuID(String locId) {
		mActiveLocationId = locId;
	}

	public void setGateId(String gateId) {
		mActiveGateId = gateId;
	}

	public void setIsPaused(boolean value) {
		isPaused = value;
	}

	private void doReloadDevicesTask(String gateId, boolean forceRefresh) {
		ReloadGateDataTask reloadDevicesTask = new ReloadGateDataTask(mActivity, forceRefresh, ReloadGateDataTask.ReloadWhat.DEVICES);

		reloadDevicesTask.setListener(new ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (!success)
					return;
				Log.d(TAG, "Success -> refresh GUI");
				mActivity.redraw();
				mSwipeLayout.setRefreshing(false);
			}
		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(reloadDevicesTask, gateId);
	}

	private void doFullReloadTask(boolean forceRefresh) {
		ReloadGateDataTask fullReloadTask = new ReloadGateDataTask(mActivity, forceRefresh, ReloadGateDataTask.ReloadWhat.GATES_AND_ACTIVE_GATE);

		fullReloadTask.setListener(new ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (!success)
					return;
				mActivity.setActiveGateAndMenu();
				mActivity.redraw();
			}
		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(fullReloadTask);
	}

	private void doRemoveDeviceTask(Device device) {
		RemoveDeviceTask removeDeviceTask = new RemoveDeviceTask(mActivity);
		removeDeviceTask.setListener(new ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				mActivity.redraw();
				if (success) {
					Toast.makeText(mActivity, R.string.toast_delete_success, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(mActivity, R.string.toast_delete_fail, Toast.LENGTH_SHORT).show();
				}
				doFullReloadTask(true);
			}
		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(removeDeviceTask, device);

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
				final Module mItemModule = mSelectedItem;
				ConfirmDialogFragment.confirm(mActivity, R.string.confirm_unregister_device_title, R.string.confirm_unregister_device_message, R.string.button_unregister, new ConfirmDialogFragment.DeleteConfirmDialogEvent() {
					@Override
					public void onDeleteDialogButtonClick() {
						doRemoveDeviceTask(mItemModule.getDevice());
					}
				});

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
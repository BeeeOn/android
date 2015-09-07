package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.AddDeviceActivity;
import com.rehivetech.beeeon.gui.activity.AddGateActivity;
import com.rehivetech.beeeon.gui.activity.DeviceDetailActivity;
import com.rehivetech.beeeon.gui.activity.MainActivity;
import com.rehivetech.beeeon.gui.adapter.ModuleListAdapter;
import com.rehivetech.beeeon.gui.dialog.ConfirmDialog;
import com.rehivetech.beeeon.gui.listItem.LocationListItem;
import com.rehivetech.beeeon.gui.listItem.ModuleListItem;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.model.DevicesModel;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTask.ICallbackTaskListener;
import com.rehivetech.beeeon.threading.ICallbackTaskFactory;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.threading.task.RemoveDeviceTask;
import com.rehivetech.beeeon.util.ActualizationTime;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TutorialHelper;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class ModuleListFragment extends BaseApplicationFragment {

	private static final String TAG = ModuleListFragment.class.getSimpleName();

	private static final String LCTN = "lastlocation";
	private static final String GATE_ID = "lastGateId";

	private static final String MODULE_LIST_FRAGMENT_AUTO_RELOAD_ID = "moduleListFragmentAutoReload";

	private ICallbackTaskFactory mICallbackTaskFactory;


	private MainActivity mActivity;

	private ModuleListAdapter mModuleAdapter;
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
	private boolean mFirstUseAddDevice = true;

	private Module mSelectedItem;
	private int mSelectedItemPos;

	public ModuleListFragment() {
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
			mFirstUseAddDevice = prefs.getBoolean(Constants.TUTORIAL_ADD_DEVICE_SHOWED, true);
		}

		setAutoReloadDataTimer();

	}

	private void setAutoReloadDataTimer() {
		SharedPreferences prefs = Controller.getInstance(getActivity()).getUserSettings();
		String reloadTime = prefs.getString(ActualizationTime.PERSISTENCE_ACTUALIZATON_KEY, null);
		int period = Integer.parseInt(reloadTime);
		Toast.makeText(getActivity(),"Time = " + period,Toast.LENGTH_SHORT).show();


		mICallbackTaskFactory = new ICallbackTaskFactory() {
			@Override
			public CallbackTask createTask() {
				ReloadGateDataTask fullReloadTask = new ReloadGateDataTask(mActivity, false, ReloadGateDataTask.RELOAD_GATES_AND_ACTIVE_GATE_DEVICES);

				fullReloadTask.setListener(new ICallbackTaskListener() {
					@Override
					public void onExecute(boolean success) {
						if (!success)
							return;
						mActivity.setActiveGateAndMenu();
						mActivity.redraw();
					}
				});
				return fullReloadTask;
			}

			@Override
			public Object createParam() {
				return null;
			}
		};
		if (period > 0)    // zero means do not update
			mActivity.callbackTaskManager.executeTaskEvery(mICallbackTaskFactory, MODULE_LIST_FRAGMENT_AUTO_RELOAD_ID, period);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "OnCreateView");
		mView = inflater.inflate(R.layout.fragment_module_list, container, false);

		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated()");

		redrawModules();

		mActivity.setupRefreshIcon(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Refreshing list of modules");
				Gate gate = Controller.getInstance(mActivity).getActiveGate();
				if (gate != null) {
					doReloadDevicesTask(gate.getId(), true);
				} else {
					doFullReloadTask(true);
				}
				mActivity.redraw();
			}
		});
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
		final StickyListHeadersListView moduleList = (StickyListHeadersListView) mView.findViewById(R.id.module_list_stickylistheader);
		TextView noItem = (TextView) mView.findViewById(R.id.module_list_nomodules_text);
		Button refreshBtn = (Button) mView.findViewById(R.id.module_list_refresh_button);

		// REFRESH listener
		OnClickListener refreshNoGate = new OnClickListener() {
			@Override
			public void onClick(View v) {
				doFullReloadTask(true);
			}
		};
		OnClickListener refreshNoModule = new OnClickListener() {
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

		mModuleAdapter = new ModuleListAdapter(mActivity);

		final FloatingActionButton floatingActionButton = (FloatingActionButton) mView.findViewById(R.id.module_list_fab);

		// All locations on gate
		locations = controller.getLocationsModel().getLocationsByGate(mActiveGateId);

		DevicesModel devicesModel = controller.getDevicesModel();

		List<Module> modules = new ArrayList<>();
		for (Location loc : locations) {
			mModuleAdapter.addHeader(new LocationListItem(loc.getName(), loc.getIconResource(), loc.getId()));
			// all devices from actual location
			for (Device device : devicesModel.getDevicesByLocation(mActiveGateId, loc.getId())) {
				Iterator<Module> it = device.getVisibleModules().iterator();
				while (it.hasNext()) {
					Module module = it.next();
					mModuleAdapter.addItem(new ModuleListItem(module, module.getAbsoluteId(), mActivity, !it.hasNext()));
					modules.add(module);
				}
			}
		}

		if (moduleList == null) {
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
			noItem.setText(R.string.module_list_no_gate_cap);
			refreshBtn.setVisibility(View.VISIBLE);
			moduleList.setVisibility(View.GONE);
			// FAB
			mFABMenuIcon.add(R.drawable.ic_add_white_24dp);
			mFABMenuLabels.add(mActivity.getString(R.string.main_action_gate_add));
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
			noItem.setText(R.string.module_list_no_device_cap);
			refreshBtn.setVisibility(View.VISIBLE);
			moduleList.setVisibility(View.GONE);

			refreshBtn.setOnClickListener(refreshNoModule);
			// FAB
			mFABMenuIcon.add(R.drawable.ic_add_white_24dp);
			mFABMenuIcon.add(R.drawable.ic_add_white_24dp);
			mFABMenuLabels.add(mActivity.getString(R.string.main_action_gate_add));
			mFABMenuLabels.add(mActivity.getString(R.string.module_list_action_module_add));
			if (mFirstUseAddDevice && !controller.isDemoMode()) {
				mFirstUseAddDevice = false;
				mActivity.getMenu().closeMenu();
				TutorialHelper.showAddDeviceTutorial(mActivity, mView);
				SharedPreferences prefs = controller.getUserSettings();
				if (prefs != null) {
					prefs.edit().putBoolean(Constants.TUTORIAL_ADD_DEVICE_SHOWED, false).apply();
				}
			}
		} else { // Have gate and modules
			noItem.setVisibility(View.GONE);
			refreshBtn.setVisibility(View.GONE);
			moduleList.setVisibility(View.VISIBLE);

			// FAB
			mFABMenuIcon.add(R.drawable.ic_add_white_24dp);
			mFABMenuIcon.add(R.drawable.ic_add_white_24dp);
			mFABMenuLabels.add(mActivity.getString(R.string.main_action_gate_add));
			mFABMenuLabels.add(mActivity.getString(R.string.module_list_action_module_add));
		}

		// Listener for add dialogs
		OnClickListener fabMenuListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mFABMenuLabels.get(v.getId()).equals(mActivity.getString(R.string.module_list_action_module_add))) {
					showAddDeviceDialog();
				} else if (mFABMenuLabels.get(v.getId()).equals(mActivity.getString(R.string.main_action_gate_add))) {
					showAddGateDialog();
				}
				Log.d(TAG, "FAB MENU HERE " + v.getId());
			}
		};

		moduleList.setAdapter(mModuleAdapter);

		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) { // API 14 +
			floatingActionButton.setMenuItems(Utils.convertIntegers(mFABMenuIcon), mFABMenuLabels.toArray(new String[mFABMenuLabels.size()]),
					R.style.BeeeOn_Fab_Mini, fabMenuListener, getResources().getDrawable(R.drawable.ic_action_cancel));
			floatingActionButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d(TAG, "FAB BTN HER");
					floatingActionButton.triggerMenu(90);
				}
			});
		} else {
			// API 10 to 13
			// Show dialof to select Add Gate or Add device

			floatingActionButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String[] mStringArray = new String[mFABMenuLabels.size()];
					mStringArray = mFABMenuLabels.toArray(mStringArray);
					mActivity.showOldAddDialog(mStringArray);
				}
			});

		}

		// Auto-hiding of FAB
		floatingActionButton.attachToListView(moduleList.getWrappedList());

		if (haveModules) {
			// Capture listview menu item click
			moduleList.setOnItemClickListener(new ListView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Module module = mModuleAdapter.getModule(position);
					Bundle bundle = new Bundle();
					bundle.putString(DeviceDetailActivity.EXTRA_GATE_ID, module.getDevice().getGateId());
					bundle.putString(DeviceDetailActivity.EXTRA_DEVICE_ID, module.getDevice().getId());
					Intent intent = new Intent(mActivity, DeviceDetailActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});
			Gate tmpAda = controller.getGatesModel().getGate(mActiveGateId);
			if (tmpAda != null) {
				if (controller.isUserAllowed(tmpAda.getRole())) {
					moduleList.setOnItemLongClickListener(new OnItemLongClickListener() {
						@Override
						public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
							mMode = mActivity.startSupportActionMode(new ActionModeEditModules());
							mSelectedItem = mModuleAdapter.getModule(position);
							mSelectedItemPos = position;
							mModuleAdapter.getItem(mSelectedItemPos).setIsSelected();
							return true;
						}
					});
				}
			}
		}

		Log.d(TAG, "LifeCycle: getModules end");
		return true;
	}

	public void showAddGateDialog() {
		Log.d(TAG, "HERE ADD GATE +");
		Intent intent = new Intent(mActivity, AddGateActivity.class);
		mActivity.startActivityForResult(intent, Constants.ADD_GATE_REQUEST_CODE);
	}

	public void showAddDeviceDialog() {
		Log.d(TAG, "HERE ADD DEVICE +");
		Intent intent = new Intent(mActivity, AddDeviceActivity.class);
		mActivity.startActivityForResult(intent, Constants.ADD_DEVICE_REQUEST_CODE);
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
			}
		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(reloadDevicesTask, gateId);
	}

	private void doFullReloadTask(boolean forceRefresh) {
		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(mICallbackTaskFactory.createTask());
	}

	public void doRemoveDeviceTask(Device device) {
		RemoveDeviceTask removeDeviceTask = new RemoveDeviceTask(mActivity);
		removeDeviceTask.setListener(new ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				mActivity.redraw();
				if (success) {
					Toast.makeText(mActivity, R.string.activity_fragment_toast_delete_success, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(mActivity, R.string.activity_fragment_toast_delete_fail, Toast.LENGTH_SHORT).show();
				}
				doFullReloadTask(true);
			}
		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(removeDeviceTask, device);

	}


	class ActionModeEditModules implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.fragment_module_list_actionmode, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			if (item.getItemId() == R.id.module_list_actionmode_del) {
				String title = getString(R.string.module_list_dialog_title_unregister_device, mSelectedItem.getName(mActivity));
				String message = getString(R.string.module_list_dialog_message_unregister_device);
				ConfirmDialog.confirm(mActivity, title, message, R.string.module_list_btn_unregister, ConfirmDialog.TYPE_DELETE_DEVICE, mSelectedItem.getDevice().getId());
			}
			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mModuleAdapter.getItem(mSelectedItemPos).setNotSelected();
			mSelectedItem = null;
			mSelectedItemPos = 0;
			mMode = null;

		}
	}
}
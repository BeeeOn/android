package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.MainActivity;
import com.rehivetech.beeeon.gui.activity.WatchdogEditRuleActivity;
import com.rehivetech.beeeon.gui.adapter.WatchdogListAdapter;
import com.rehivetech.beeeon.gui.dialog.ConfirmDialog;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.watchdog.Watchdog;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.ICallbackTaskFactory;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.threading.task.RemoveWatchdogTask;
import com.rehivetech.beeeon.threading.task.SaveWatchdogTask;
import com.rehivetech.beeeon.util.ActualizationTime;
import com.rehivetech.beeeon.util.Log;

import java.util.List;

/**
 * Fragment for list of rules for algorithm Watchdog
 *
 * @author mlyko
 */
public class WatchdogListFragment extends BaseApplicationFragment {
	private static final String TAG = WatchdogListFragment.class.getSimpleName();

	private static final String GATE_ID = "lastGateId";
	private static final String WATCHDOG_AUTO_RELOAD_ID = "watchdogAutoReload";

	private MainActivity mActivity;

	private ListView mWatchdogListView;
	private WatchdogListAdapter mWatchdogAdapter;
	private Button mRefreshBtn;

	List<Watchdog> mWatchdogs;

	private String mActiveGateId;

	private View mView;
	private ActionMode mMode;

	private Watchdog mSelectedItem;
	private int mSelectedItemPos;

	private ICallbackTaskFactory mICallbackTaskFactory;

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

	/**
	 * Initialize variables
	 *
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");

		if (savedInstanceState != null) {
			mActiveGateId = savedInstanceState.getString(GATE_ID);
		}
		setAutoReloadDataTimer();
	}

	private void setAutoReloadDataTimer() {
		SharedPreferences prefs = Controller.getInstance(getActivity()).getUserSettings();
		String reloadTime = prefs.getString(ActualizationTime.PERSISTENCE_ACTUALIZATON_KEY, null);
		int period = Integer.parseInt(reloadTime);

		mICallbackTaskFactory = new ICallbackTaskFactory() {
			@Override
			public CallbackTask createTask() {
				ReloadGateDataTask reloadWatchdogTask = new ReloadGateDataTask(mActivity, false, ReloadGateDataTask.ReloadWhat.WATCHDOGS);

				reloadWatchdogTask.setListener(new CallbackTask.ICallbackTaskListener() {
					@Override
					public void onExecute(boolean success) {
						redrawRules();
					}
				});
				return reloadWatchdogTask;
			}

			@Override
			public Object createParam() {
				return null;
			}
		};
		if (period > 0)    // zero means do not update
			mActivity.callbackTaskManager.executeTaskEvery(mICallbackTaskFactory, WATCHDOG_AUTO_RELOAD_ID, period);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_watchdog_list, container, false);
		return mView;
	}

	/**
	 * Init layout
	 *
	 * @param savedInstanceState
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated()");

		initLayout();

		mActivity.setupRefreshIcon(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshListListener();
			}
		});
	}

	private void refreshListListener() {
		Gate gate = Controller.getInstance(mActivity).getActiveGate();
		if (gate == null) {
			return;
		}
		doReloadWatchdogsTask(gate.getId(), true);
	}

	@Override
	public void onResume() {
		super.onResume();
		Controller controller = Controller.getInstance(mActivity);

		if (mActiveGateId == null) {
			Gate gate = controller.getActiveGate();
			if (gate == null)
				return;
			mActiveGateId = gate.getId();
		}

		// if we don't have any data first time shows button to refresh
		redrawRules();
		// try to reload data
		doReloadWatchdogsTask(mActiveGateId, false);
	}

	/**
	 * Finish actionMode
	 */
	public void onPause() {
		super.onPause();

		if (mMode != null) {
			mMode.finish();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(GATE_ID, mActiveGateId);
		super.onSaveInstanceState(savedInstanceState);
	}

	/**
	 * Sets empty elements for design without filling with data
	 */
	private void initLayout() {
		mWatchdogListView = (ListView) mView.findViewById(R.id.watchdog_listview);
		mWatchdogAdapter = new WatchdogListAdapter(mActivity, mActivity.getLayoutInflater());
		mWatchdogListView.setAdapter(mWatchdogAdapter);

		// onclicklistener for Switch button in one row
		mWatchdogAdapter.setSwitchOnclickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int objPosition = (int) v.getTag();
				Watchdog watchdog = (Watchdog) mWatchdogAdapter.getItem(objPosition);
				if (watchdog == null) return;

				// so that progress bar can be seen
				if (mMode != null) mMode.finish();

				SwitchCompat sw = (SwitchCompat) v;
				doSaveWatchdogTask(watchdog, sw);
			}
		});

		// when listview is empty
		TextView emptyView = (TextView) mView.findViewById(R.id.watchdog_list_empty_text);
		mWatchdogListView.setEmptyView(emptyView);

		// refresh button
		mRefreshBtn = (Button) mView.findViewById(R.id.watchdog_list_refresh_btn);
		mRefreshBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshListListener();
			}
		});

		// add new watchdog rule
		FloatingActionButton fab = (FloatingActionButton) mView.findViewById(R.id.watchdog_list_fab);
		fab.attachToListView(mWatchdogListView);

		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mActivity, WatchdogEditRuleActivity.class);
				startActivity(intent);
			}
		});

		// switch activity to detail
		mWatchdogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Watchdog rule = mWatchdogAdapter.getRule(position);

				Bundle bundle = new Bundle();
				bundle.putString(WatchdogEditRuleActivity.EXTRA_GATE_ID, rule.getGateId());
				bundle.putString(WatchdogEditRuleActivity.EXTRA_RULE_ID, rule.getId());

				Intent intent = new Intent(mActivity, WatchdogEditRuleActivity.class);
				intent.putExtras(bundle);

				startActivity(intent);
			}
		});

		// shows actionMode with delete option
		mWatchdogListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				mMode = mActivity.startSupportActionMode(new ActionModeEditRules());
				mSelectedItem = mWatchdogAdapter.getRule(position);
				mSelectedItemPos = position;
				setRuleSelected();
				return true;
			}
		});
	}

	/**
	 * Redraw GUI rules, called asynchronously (callback) when new data available
	 */
	private void redrawRules() {
		mWatchdogs = Controller.getInstance(mActivity).getWatchdogsModel().getWatchdogsByGate(mActiveGateId);

		boolean haveWatchdogs = mWatchdogs.size() > 0;

		if (!haveWatchdogs) {
			mRefreshBtn.setVisibility(View.VISIBLE);
			mWatchdogListView.setVisibility(View.GONE);
		} else {
			mRefreshBtn.setVisibility(View.GONE);
			mWatchdogListView.setVisibility(View.VISIBLE);
		}

		mWatchdogAdapter.updateData(mWatchdogs);
	}

	// ----- ASYNC TASKS ----- //
	private void doSaveWatchdogTask(Watchdog watchdog, final SwitchCompat sw) {
		// disable so that nobody can change it now
		sw.setEnabled(false);

		watchdog.setEnabled(sw.isChecked());

		SaveWatchdogTask saveWatchdogTask = new SaveWatchdogTask(mActivity);
		saveWatchdogTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				//Toast.makeText(mActivity, getResources().getString(success ? R.string.toast_success_save_data : R.string.toast_fail_save_data), Toast.LENGTH_LONG).show();
				sw.setEnabled(true);
			}
		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(saveWatchdogTask, watchdog);
	}

	/**
	 * Async task for reloading fresh watchdog data
	 *
	 * @param gateId
	 */
	public void doReloadWatchdogsTask(String gateId, boolean forceReload) {
		Log.d(TAG, "reloadWatchdogsTask()");

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(mICallbackTaskFactory.createTask(), gateId);
	}

	/**
	 * Async task for deleting watchdog
	 *
	 * @param watchdog
	 */
	public void doRemoveWatchdogTask(Watchdog watchdog) {
		RemoveWatchdogTask removeWatchdogTask = new RemoveWatchdogTask(mActivity);

		removeWatchdogTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				Toast.makeText(mActivity, getResources().getString(success ? R.string.activity_fragment_toast_delete_success : R.string.activity_fragment_toast_delete_fail), Toast.LENGTH_SHORT).show();
				if (success) {
					redrawRules();
				}
			}
		});


		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(removeWatchdogTask, watchdog);
	}

	// ----- HELPERS + ACTIONMODE ----- //

	/**
	 * Changes color of selected item row
	 */
	private void setRuleSelected() {
		getViewByPosition(mSelectedItemPos, mWatchdogListView).findViewById(R.id.list_watchdog_item_layout).setBackgroundColor(mActivity.getResources().getColor(R.color.gray_light));
	}

	private void setRuleUnselected() {
		getViewByPosition(mSelectedItemPos, mWatchdogListView).findViewById(R.id.list_watchdog_item_layout).setBackgroundColor(mActivity.getResources().getColor(R.color.white));
	}

	/**
	 * Helper for getting item from listView
	 *
	 * @param pos
	 * @param listView
	 * @return
	 */
	public View getViewByPosition(int pos, ListView listView) {
		final int firstListItemPosition = listView.getFirstVisiblePosition();
		final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

		if (pos < firstListItemPosition || pos > lastListItemPosition) {
			return listView.getAdapter().getView(pos, null, listView);
		} else {
			final int childIndex = pos - firstListItemPosition;
			return listView.getChildAt(childIndex);
		}
	}

	/**
	 * Class for managing when longclicked on item (ActionMode)
	 */
	class ActionModeEditRules implements ActionMode.Callback {
		@Override
		public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
			MenuInflater inflater = actionMode.getMenuInflater();
			inflater.inflate(R.menu.fragment_watchdog_list_actionmode, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
			if (menuItem.getItemId() == R.id.watchdog_list_action_delete) {
				String title = getString(R.string.activity_fragment_menu_dialog_title_remove, mSelectedItem.getName());
				String message = getString(R.string.watchdog_rule_edit_message_remove_watchdog);
				ConfirmDialog.confirm(mActivity, title, message, R.string.activity_fragment_menu_btn_remove, ConfirmDialog.TYPE_DELETE_WATCHDOG, mSelectedItem.getId());
			}

			actionMode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode actionMode) {
			setRuleUnselected();
			mSelectedItem = null;
			mSelectedItemPos = 0;
			mMode = null;
		}
	}
}

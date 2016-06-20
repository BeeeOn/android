package com.rehivetech.beeeon.gui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.dialog.ConfirmDialog;
import com.rehivetech.beeeon.gui.fragment.GateDetailFragment;
import com.rehivetech.beeeon.household.gate.GateInfo;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.ICallbackTaskFactory;
import com.rehivetech.beeeon.threading.task.ReloadGateInfoTask;
import com.rehivetech.beeeon.threading.task.UnregisterGateTask;
import com.rehivetech.beeeon.util.ActualizationTime;

/**
 * @author david
 * @since 30.05.2016
 */
public class GateDetailActivity extends BaseApplicationActivity implements GateDetailFragment.OnGateDetailsButtonsClickedListener {
	private static final String TAG = GateDetailActivity.class.getSimpleName();

	private static final String FRAGMENT_DETAILS = "fragment_details";

	public static final String EXTRA_GATE_ID = "gate_id";

	private static final String GATE_DETAIL_ACIVITY_AUTO_RELOAD_ID = "gateDetailActivityAutoReload";

	private final ICallbackTaskFactory mICallbackTaskFactory = new ICallbackTaskFactory() {
		@Override
		public CallbackTask createTask() {
			return createReloadGateInfoTask(true);
		}

		@Override
		public Object createParam() {
			return mGateId;
		}
	};

	private CallbackTask createReloadGateInfoTask(boolean forceReload) {
		ReloadGateInfoTask reloadGateInfoTask = new ReloadGateInfoTask(GateDetailActivity.this, forceReload);

		reloadGateInfoTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				GateInfo gateInfo = Controller.getInstance(GateDetailActivity.this).getGatesModel().getGateInfo(mGateId);
				if (gateInfo == null) {
					Log.e(TAG, String.format("Gate #%s does not exists", mGateId));
					finish();
				} else {
					if (mFragment != null) {
						mFragment.fillData();
					}
				}
			}
		});
		return reloadGateInfoTask;
	}

	private String mGateId;
	@Nullable
	private GateDetailFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gate_detail);
		setupToolbar(R.string.empty, INDICATOR_BACK);

		mGateId = getIntent().getStringExtra(EXTRA_GATE_ID);
		if (mGateId == null) {
			Toast.makeText(this, R.string.gate_detail_toast_not_specified_gate, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (savedInstanceState == null) {
			mFragment = GateDetailFragment.newInstance(mGateId);
			getSupportFragmentManager().beginTransaction().replace(R.id.gate_detail_container, mFragment, FRAGMENT_DETAILS).commit();
		} else {
			mFragment = (GateDetailFragment) getSupportFragmentManager().findFragmentById(R.id.gate_detail_container);
		}
		setAutoReloadDataTimer();
	}

	private void setAutoReloadDataTimer() {
		SharedPreferences prefs = Controller.getInstance(this).getUserSettings();
		ActualizationTime.Item item = (ActualizationTime.Item) new ActualizationTime().fromSettings(prefs);
		int period = item.getSeconds();
		if (period > 0)
			callbackTaskManager.executeTaskEvery(mICallbackTaskFactory, GATE_DETAIL_ACIVITY_AUTO_RELOAD_ID, period);
	}

	@Override
	public void onResume() {
		super.onResume();
		doReloadGateInfo(mGateId, false);
	}

	private void doReloadGateInfo(final String gateId, boolean forceReload) {
		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(createReloadGateInfoTask(forceReload), gateId, CallbackTaskManager.PROGRESS_ICON);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.gate_detail_menu_edit:
				Intent intent = new Intent(this, GateEditActivity.class);
				intent.putExtra(GateEditActivity.EXTRA_GATE_ID, mGateId);
				startActivity(intent);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_gate_detail_menu, menu);
		return true;
	}

	@Override
	public void onGateUsersClicked() {
		Intent intent = new Intent(this, GateUsersActivity.class);
		intent.putExtra(GateUsersActivity.EXTRA_GATE_ID, mGateId);
		startActivity(intent);
	}

	@Override
	public void onGateDevicesClicked() {
		Intent intent = new Intent(this, DevicesListActivity.class);
		intent.putExtra(DevicesListActivity.EXTRA_GATE_ID, mGateId);
		startActivity(intent);
	}

	@Override
	public void onForceReloadData() {
		doReloadGateInfo(mGateId, true);
	}
}

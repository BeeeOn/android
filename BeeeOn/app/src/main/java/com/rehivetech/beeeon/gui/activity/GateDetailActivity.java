package com.rehivetech.beeeon.gui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.dialog.ConfirmDialog;
import com.rehivetech.beeeon.gui.fragment.GateDetailFragment;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.threading.task.UnregisterGateTask;
import com.rehivetech.beeeon.util.Log;

import java.util.EnumSet;

/**
 * Created by david on 23.6.15.
 */
public class GateDetailActivity extends BaseApplicationActivity implements GateDetailFragment.OnGateDetailsButtonsClickedListener, ConfirmDialog.ConfirmDialogListener {
	private static final String TAG = GateDetailActivity.class.getSimpleName();

	private static final String FRAGMENT_DETAILS = "fragment_details";

	public static final String EXTRA_GATE_ID = "gate_id";

	private String mGateId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gate_detail);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			toolbar.setTitle("");
			setSupportActionBar(toolbar);
		}
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		mGateId = getIntent().getStringExtra(EXTRA_GATE_ID);
		if (mGateId == null) {
			Toast.makeText(this, R.string.toast_not_specified_gate, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (savedInstanceState == null) {
			GateDetailFragment gateDetailFragment = GateDetailFragment.newInstance(mGateId);
			getSupportFragmentManager().beginTransaction().replace(R.id.container, gateDetailFragment, FRAGMENT_DETAILS).commit();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		doReloadGatesAndActiveGateTask(mGateId, false);
	}

	private void doReloadGatesAndActiveGateTask(final String gateId, boolean forceReload) {
		ReloadGateDataTask reloadGateDataTask = new ReloadGateDataTask(this, forceReload, EnumSet.of(
				ReloadGateDataTask.ReloadWhat.GATES,
				ReloadGateDataTask.ReloadWhat.DEVICES,
				ReloadGateDataTask.ReloadWhat.USERS
		));

		reloadGateDataTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				Gate gate = Controller.getInstance(GateDetailActivity.this).getGatesModel().getGate(mGateId);
				if (gate == null) {
					Log.e(TAG, String.format("Gate #%s does not exists", mGateId));
					finish();
				} else {
					GateDetailFragment fragment = (GateDetailFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_DETAILS);
					fragment.fillData();
				}
			}
		});
		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(reloadGateDataTask, gateId, CallbackTaskManager.ProgressIndicator.PROGRESS_ICON);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home: {
				finish();
				break;
			}
			case R.id.ada_menu_delete: {
				String title = getString(R.string.confirm_remove_gate_title_default);
				String message = getString(R.string.confirm_remove_gate_message);

				Gate gate = Controller.getInstance(this).getGatesModel().getGate(mGateId);
				if (gate != null) {
					title = getString(R.string.confirm_remove_gate_title, gate.getName());
				}

				ConfirmDialog.confirm(this, title, message, R.string.button_remove, ConfirmDialog.TYPE_DELETE_GATE, mGateId);
				break;
			}
			case R.id.ada_menu_edit: {
				Intent intent = new Intent(this, GateEditActivity.class);
				intent.putExtra(GateEditActivity.EXTRA_GATE_ID, mGateId);
				startActivity(intent);
				break;
			}
			case R.id.ada_menu_users: {
				Intent intent = new Intent(this, GateUsersActivity.class);
				intent.putExtra(GateUsersActivity.EXTRA_GATE_ID, mGateId);
				startActivity(intent);
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.gate_detail_menu, menu);
		return true;
	}

	private void doUnregisterGateTask(String gateId) {
		UnregisterGateTask unregisterGateTask = new UnregisterGateTask(this);

		unregisterGateTask.setListener(new CallbackTask.ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (success) {
					Toast.makeText(GateDetailActivity.this, R.string.toast_gate_removed, Toast.LENGTH_LONG).show();
					finish();
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		this.callbackTaskManager.executeTask(unregisterGateTask, gateId);
	}

	@Override
	public void onGateUsersClicked() {
		Intent intent = new Intent(this, GateUsersActivity.class);
		intent.putExtra(GateUsersActivity.EXTRA_GATE_ID, mGateId);
		startActivity(intent);
	}

	@Override
	public void onGateDevicesClicked() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(MainActivity.EXTRA_GATE_ID, mGateId);
		startActivity(intent);
	}

	@Override
	public void onForceReloadData() {
		doReloadGatesAndActiveGateTask(mGateId, true);
	}


	@Override
	public void onConfirm(int confirmType, String dataId) {
		if (confirmType == ConfirmDialog.TYPE_DELETE_GATE) {
			doUnregisterGateTask(dataId);
			finish();
		}
	}
}

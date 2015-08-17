package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.dialog.ConfirmDialog;
import com.rehivetech.beeeon.gui.fragment.GateEditFragment;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.gate.GateInfo;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.EditGateTask;
import com.rehivetech.beeeon.threading.task.ReloadGateInfoTask;
import com.rehivetech.beeeon.threading.task.UnregisterGateTask;
import com.rehivetech.beeeon.util.Log;

/**
 * Created by david on 17.6.15.
 */
public class GateEditActivity extends BaseApplicationActivity implements ConfirmDialog.ConfirmDialogListener {
	private static final String TAG = GateEditActivity.class.getSimpleName();

	private static final String FRAGMENT_EDIT = "fragment_edit";

	public static final String EXTRA_GATE_ID = "gate_id";

	private String mGateId;
	@Nullable private GateEditFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gate_edit);

		setupToolbar(R.string.empty);
		if (mActionBar != null) {
			mActionBar.setHomeButtonEnabled(true);
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setHomeAsUpIndicator(R.drawable.ic_action_cancel);
		}

		mGateId = getIntent().getStringExtra(EXTRA_GATE_ID);
		if (mGateId == null) {
			Toast.makeText(this, "Not specified Gate.", Toast.LENGTH_LONG).show(); // FIXME: string from resources
			finish();
			return;
		}

		if (savedInstanceState == null) {
			GateEditFragment gateEditFragment = GateEditFragment.newInstance(mGateId);
			getSupportFragmentManager().beginTransaction().replace(R.id.gate_edit_container, gateEditFragment, FRAGMENT_EDIT).commit();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		doReloadGateInfo(mGateId, false);
	}

	@Override
	public void onFragmentAttached(Fragment fragment) {
		super.onFragmentAttached(fragment);
		try {
			mFragment = (GateEditFragment) fragment;
		} catch (ClassCastException e) {
			throw new ClassCastException(String.format("%s must be GateEditFragment", fragment.toString()));
		}
	}

	private void doReloadGateInfo(final String gateId, boolean forceReload) {
		ReloadGateInfoTask reloadGateInfoTask = new ReloadGateInfoTask(this, forceReload);
		reloadGateInfoTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				GateInfo gate = Controller.getInstance(GateEditActivity.this).getGatesModel().getGateInfo(mGateId);
				if (gate == null) {
					Log.e(TAG, String.format("Gate #%s does not exists", mGateId));
					finish();
				} else {
					if (success && mFragment != null) {
						mFragment.fillData(null);
					}
				}
			}
		});
		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(reloadGateInfoTask, gateId, CallbackTaskManager.ProgressIndicator.PROGRESS_ICON);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_gate_edit_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home: {
				finish();
				break;
			}
			case R.id.gate_edit_action_delete: {
				String title = getString(R.string.activity_menu_dialog_title_remove_gate_default);
				String message = getString(R.string.activity_menu_dialog_message_remove_gate);

				GateInfo gate = Controller.getInstance(this).getGatesModel().getGateInfo(mGateId);
				if (gate != null) {
					title = getString(R.string.activity_fragment_menu_dialog_title_remove, gate.getName());
				}

				ConfirmDialog.confirm(this, title, message, R.string.activity_fragment_menu_btn_remove, ConfirmDialog.TYPE_DELETE_GATE, mGateId);
				break;
			}
			case R.id.gate_edit_action_save: {
				if (mFragment != null) {
					Gate gate = mFragment.getNewGate();
					doEditGateTask(gate);
				}
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private void doEditGateTask(Gate gate) {
		EditGateTask editGateTask = new EditGateTask(this);
		editGateTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					Toast.makeText(GateEditActivity.this, R.string.gate_edit_toast_success, Toast.LENGTH_SHORT).show();
					finish();
				}
			}
		});
		callbackTaskManager.executeTask(editGateTask, gate, CallbackTaskManager.ProgressIndicator.PROGRESS_DIALOG);
	}

	private void doUnregisterGateTask(String gateId) {
		UnregisterGateTask unregisterGateTask = new UnregisterGateTask(this);

		unregisterGateTask.setListener(new CallbackTask.ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (success) {
					Toast.makeText(GateEditActivity.this, R.string.gate_detail_toast_gate_removed, Toast.LENGTH_LONG).show();
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		this.callbackTaskManager.executeTask(unregisterGateTask, gateId);
	}

	@Override
	public void onConfirm(int confirmType, String dataId) {
		if (confirmType == ConfirmDialog.TYPE_DELETE_GATE) {
			doUnregisterGateTask(dataId);
			finish();
		}
	}
}

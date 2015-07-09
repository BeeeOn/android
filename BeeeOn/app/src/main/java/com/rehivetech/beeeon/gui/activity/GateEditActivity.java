package com.rehivetech.beeeon.gui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.fragment.GateEditFragment;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.EditGateTask;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.threading.task.UnregisterGateTask;
import com.rehivetech.beeeon.util.Log;

/**
 * Created by david on 17.6.15.
 */
public class GateEditActivity extends BaseApplicationActivity {
	private static final String TAG = GateEditActivity.class.getSimpleName();

	private static final String FRAGMENT_EDIT = "fragment_edit";

	public static final String EXTRA_GATE_ID = "gate_id";

	private String mGateId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gate_edit);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			toolbar.setTitle("");
			setSupportActionBar(toolbar);
		}
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeAsUpIndicator(R.drawable.ic_action_cancel);
		}

		mGateId = getIntent().getStringExtra(EXTRA_GATE_ID);
		if (mGateId == null) {
			Toast.makeText(this, "Not specified Gate.", Toast.LENGTH_LONG).show(); // FIXME: string from resources
			finish();
			return;
		}

		if (savedInstanceState == null) {
			GateEditFragment gateEditFragment = GateEditFragment.newInstance(mGateId);
			getSupportFragmentManager().beginTransaction().replace(R.id.container, gateEditFragment, FRAGMENT_EDIT).commit();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		doReloadGates(mGateId, false);
	}

	private void doReloadGates(final String gateId, boolean forceReload) {
		ReloadGateDataTask reloadGateDataTask = new ReloadGateDataTask(this, forceReload, ReloadGateDataTask.ReloadWhat.GATES);
		reloadGateDataTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				Gate gate = Controller.getInstance(GateEditActivity.this).getGatesModel().getGate(mGateId);
				if (gate == null) {
					Log.e(TAG, String.format("Gate #%s does not exists", mGateId));
					finish();
				} else {
					GateEditFragment fragment = (GateEditFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_EDIT);
					if (success) {
						fragment.fillData(null);
					}
				}
			}
		});
		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(reloadGateDataTask, gateId, CallbackTaskManager.ProgressIndicator.PROGRESS_ICON);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_gate_edit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		GateEditFragment gateEditFragment = (GateEditFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_EDIT);
		//noinspection SimplifiableIfStatement
		if (id == R.id.action_save) {
			Gate gate = gateEditFragment.getNewGate();
			doEditGateTask(gate);
		} else if (id == R.id.action_delete) {
			doUnregisterGateTask(mGateId);
			setResult(Activity.RESULT_OK);
			finish();
		} else if (id == android.R.id.home) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}

		return super.onOptionsItemSelected(item);
	}

	private void doEditGateTask(Gate gate) {
		EditGateTask editGateTask = new EditGateTask(this);
		editGateTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					Toast.makeText(GateEditActivity.this, R.string.edit_gate_success, Toast.LENGTH_SHORT).show();
					setResult(Activity.RESULT_OK);
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
					Toast.makeText(GateEditActivity.this, R.string.toast_gate_removed, Toast.LENGTH_LONG).show();
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		this.callbackTaskManager.executeTask(unregisterGateTask, gateId);
	}
}

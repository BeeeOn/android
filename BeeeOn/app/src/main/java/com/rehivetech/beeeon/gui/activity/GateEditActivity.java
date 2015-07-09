package com.rehivetech.beeeon.gui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.fragment.GateEditFragment;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.EditGateTask;
import com.rehivetech.beeeon.threading.task.UnregisterGateTask;

/**
 * Created by david on 17.6.15.
 */
public class GateEditActivity extends BaseApplicationActivity {
	private static final String TAG = SensorEditActivity.class.getSimpleName();
	private static final String TAG_FRAGMENT_EDIT_GATE_DIALOG = "TAG_FRAGMENT_EDIT_GATE_DIALOG";
	private String mGateId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gate_edit);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			toolbar.setTitle(R.string.title_activity_gate_edit);
			setSupportActionBar(toolbar);
		}
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeAsUpIndicator(R.drawable.ic_action_cancel);
		}
		mGateId = getIntent().getStringExtra(Constants.GUI_EDIT_GATE_ID);
		FragmentManager fragmentManager = getSupportFragmentManager();
		GateEditFragment gateEditFragment = (GateEditFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENT_EDIT_GATE_DIALOG);
		if (gateEditFragment == null)
			gateEditFragment = GateEditFragment.newInstance(mGateId);
		fragmentManager.beginTransaction().replace(R.id.edit_gate_fragment_container, gateEditFragment, TAG_FRAGMENT_EDIT_GATE_DIALOG).commit();
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
		Controller controller = Controller.getInstance(this);
		GateEditFragment gateEditFragment = (GateEditFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_EDIT_GATE_DIALOG);
		//noinspection SimplifiableIfStatement
		if (id == R.id.action_save) {
			Gate gate = controller.getGatesModel().getGate(mGateId);
			String newGateName = gateEditFragment.getNewGateName();
			GateEditFragment.MyTimeZone newGateZone = gateEditFragment.getNewGateZone();
			gate.setName(newGateName);
			gate.setUtcOffset(newGateZone.offsetInMinutes);
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
					// TODO: finish acitivity correctly
					Toast.makeText(GateEditActivity.this, R.string.edit_gate_success, Toast.LENGTH_SHORT).show();
					setResult(Activity.RESULT_OK);
					finish();
				} else {
					Toast.makeText(GateEditActivity.this, R.string.edit_gate_failure, Toast.LENGTH_SHORT).show();
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
				} else {
					Toast.makeText(GateEditActivity.this, R.string.gate, Toast.LENGTH_LONG).show();
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		this.callbackTaskManager.executeTask(unregisterGateTask, gateId);
	}
}

package com.rehivetech.beeeon.gui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.GateDetailFragment;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.UnregisterGateTask;

/**
 * Created by david on 23.6.15.
 */
public class GateDetailActivity extends BaseApplicationActivity implements GateDetailFragment.OnGateDetailsButtonsClickedListener {
	private String mGateId;
	public static final String FRAGMENT_GATE_DETAIL = "FRAGMENT_GATE_DETAIL";
	private boolean mFirstTime = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gate_detail_wrapper);

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
		mGateId = getIntent().getStringExtra("GATE_ID");

		if (mGateId == null) {
			Toast.makeText(this, "Gate ID is null :/", Toast.LENGTH_LONG).show();
			finish();
		}

		FragmentManager fragmentManager = getSupportFragmentManager();
		GateDetailFragment gateDetailFragment = (GateDetailFragment) fragmentManager.findFragmentByTag(FRAGMENT_GATE_DETAIL);
		if (gateDetailFragment == null)
			gateDetailFragment = GateDetailFragment.newInstance(mGateId);
		fragmentManager.beginTransaction().replace(R.id.container, gateDetailFragment, FRAGMENT_GATE_DETAIL).commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				setResult(Activity.RESULT_OK);
				finish();
				break;
			case R.id.ada_menu_delete:
				doUnregisterGateTask(mGateId);
				setResult(Activity.RESULT_OK);
				finish();
				break;
			case R.id.ada_menu_edit:
				Intent intent = new Intent(this, GateEditActivity.class);
				intent.putExtra(Constants.GUI_EDIT_GATE_ID, mGateId);
				startActivity(intent);
				break;
			case R.id.ada_menu_users:
				startActivity(new Intent(this,GateUsersActivity.class).putExtra(Constants.GUI_SELECTED_GATE_ID, mGateId));
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		this.callbackTaskManager.executeTask(unregisterGateTask, gateId);
	}

	@Override
	public void onDetailsButtonClicked(Class newClass) {
		Intent intent = new Intent(this, newClass);
		intent.putExtra(Constants.GUI_SELECTED_GATE_ID, mGateId);
		startActivity(intent);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!mFirstTime) {
			GateDetailFragment gateDetailFragment = (GateDetailFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_GATE_DETAIL);
			if (gateDetailFragment != null) {
				gateDetailFragment.reloadData();
			}
		} else
			mFirstTime = false;
	}
}

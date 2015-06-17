package com.rehivetech.beeeon.gui.activity;

import android.app.Activity;
import android.os.Bundle;
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
import com.rehivetech.beeeon.household.location.Location;

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
			toolbar.setTitle("");
			setSupportActionBar(toolbar);
		}
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeAsUpIndicator(R.drawable.ic_action_cancel);
		}
		mGateId = getIntent().getStringExtra(Constants.GUI_EDIT_GATE_ID);
		GateEditFragment gateEditFragment = new GateEditFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.container,gateEditFragment, TAG_FRAGMENT_EDIT_GATE_DIALOG).commit();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(Constants.GUI_EDIT_GATE_ID, mGateId);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mGateId = savedInstanceState.getString(Constants.GUI_EDIT_GATE_ID);

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
			String gateName = null;
			Location gateLocation = null;
			// Overit, co je vse potreba zmenit, pote zavolat task
			doEditGateTask(gateName,gateLocation);
		} else if (id == android.R.id.home) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}

		return super.onOptionsItemSelected(item);

	}
	private void doEditGateTask(String gateName,Location gateLocation){
		Toast.makeText(this,"Zde se bude volat server",Toast.LENGTH_SHORT).show();
	}
}

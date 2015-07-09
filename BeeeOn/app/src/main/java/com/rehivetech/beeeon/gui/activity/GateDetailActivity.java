package com.rehivetech.beeeon.gui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.GateDetailFragment;

/**
 * Created by david on 23.6.15.
 */
public class GateDetailActivity extends BaseApplicationActivity {
	private String mGateId;
	public static final String FRAGMENT_GATE_DETAIL = "FRAGMENT_GATE_DETAIL";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gate_detail_wrapper);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			toolbar.setTitle(R.string.title_activity_gate_detail);
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
		if(gateDetailFragment == null)
			gateDetailFragment = GateDetailFragment.newInstance(mGateId);
		fragmentManager.beginTransaction().replace(R.id.container,gateDetailFragment,FRAGMENT_GATE_DETAIL).commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				setResult(Activity.RESULT_OK);
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}

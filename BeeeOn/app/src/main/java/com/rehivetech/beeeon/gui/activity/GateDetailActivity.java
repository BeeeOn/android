package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.GateDetailFragment;

/**
 * Created by david on 23.6.15.
 */
public class GateDetailActivity extends BaseApplicationActivity {
	private String mGateId;
	private static final String FRAGMENT_GATE_DETAIL = "FRAGMENT_GATE_DETAIL";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(R.layout.activity_gate_detail_wrapper);

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
			gateDetailFragment = new GateDetailFragment();
		fragmentManager.beginTransaction().replace(R.id.container,gateDetailFragment,FRAGMENT_GATE_DETAIL).commit();
	}
}

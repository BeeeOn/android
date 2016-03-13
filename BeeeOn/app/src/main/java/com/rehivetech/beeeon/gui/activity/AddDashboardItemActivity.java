package com.rehivetech.beeeon.gui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.AddDashboardItemFragment;
import com.rehivetech.beeeon.gui.fragment.BaseApplicationFragment;

/**
 * Created by martin on 13.1.16.
 */
public class AddDashboardItemActivity extends BaseApplicationActivity {

	public static final String ARG_GATE_ID = "gate_id";

	public static Intent getADdDashBoardActivityIntent(Context context, String gateId) {
		Intent intent = new Intent(context, AddDashboardItemActivity.class);
		intent.putExtra(ARG_GATE_ID, gateId);

		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_dashboard_item);


		setupToolbar(R.string.app_name);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		Bundle args = getIntent().getExtras();

		BaseApplicationFragment fragment;

		String gateId = args.getString(ARG_GATE_ID);

		if (savedInstanceState == null) {
			fragment = AddDashboardItemFragment.newInstance(gateId);
			getSupportFragmentManager().beginTransaction().replace(R.id.activity_add_dashboard_container, fragment).commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
		}
		return false;
	}
}

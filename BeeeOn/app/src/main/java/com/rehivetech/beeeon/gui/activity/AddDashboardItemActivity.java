package com.rehivetech.beeeon.gui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.AddDashboardActualValueFragment;
import com.rehivetech.beeeon.gui.fragment.AddDashboardGraphItemFragment;
import com.rehivetech.beeeon.gui.fragment.BaseApplicationFragment;

/**
 * Created by martin on 13.1.16.
 */
public class AddDashboardItemActivity extends BaseApplicationActivity {

	public static final String ARG_GATE_ID = "gate_id";
	public static final String ARG_ITEM_TYPE = "item_type";
	public static final int KEY_VALUE_TYPE_GRAPH_ITEM = 0;
	public static final int KEY_VALUE_TYPE_MODULE_ITEM = 1;


	public static Intent getADdDashBoardActivityIntent(Context context, String gateId, int itemType) {
		Intent intent = new Intent(context, AddDashboardItemActivity.class);
		intent.putExtra(ARG_GATE_ID, gateId);
		intent.putExtra(ARG_ITEM_TYPE, itemType);

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

		BaseApplicationFragment fragment = null;

		String gateId = args.getString(ARG_GATE_ID);
		int itemType = args.getInt(ARG_ITEM_TYPE);

		if (savedInstanceState == null) {

			switch (itemType) {
				case KEY_VALUE_TYPE_GRAPH_ITEM:
					fragment = AddDashboardGraphItemFragment.newInstance(gateId);
					break;
				case KEY_VALUE_TYPE_MODULE_ITEM:
					fragment = AddDashboardActualValueFragment.newInstance(gateId);
					break;
			}

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

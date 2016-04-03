package com.rehivetech.beeeon.gui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.BaseItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.GraphItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.OverviewGraphItem;
import com.rehivetech.beeeon.gui.fragment.DashboardGraphDetailFragment;
import com.rehivetech.beeeon.gui.fragment.DashboardOverviewGraphDetailFragment;

/**
 * Created by martin on 19.3.16.
 */
public class DashboardDetailActivity extends BaseApplicationActivity {

	private static final String ARG_BASE_DASHBOARD_ITEM = "base_dashboard_item";

	public static Intent getActivityIntent(Context context, BaseItem item) {
		Intent intent = new Intent(context, DashboardDetailActivity.class);
		intent.putExtra(ARG_BASE_DASHBOARD_ITEM, item);

		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_module_detail);

		setupToolbar("", true);

		Bundle args = getIntent().getExtras();
		BaseItem item = args.getParcelable(ARG_BASE_DASHBOARD_ITEM);

		Fragment fragment = null;

		if (savedInstanceState == null) {

			if (item instanceof OverviewGraphItem) {
				fragment = DashboardOverviewGraphDetailFragment.newInstance((OverviewGraphItem) item);
			} else if (item instanceof GraphItem) {
				fragment = DashboardGraphDetailFragment.newInstance((GraphItem) item);
			}

			getSupportFragmentManager().beginTransaction().replace(R.id.module_detail_container, fragment).commit();
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

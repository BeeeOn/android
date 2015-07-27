package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.ModuleDetailFragment;

/**
 * Class that handle screen with detail of some sensor
 */
public class ModuleDetailActivity extends BaseApplicationActivity {

	private static final String TAG = ModuleDetailActivity.class.getSimpleName();

	public static final String EXTRA_MODULE_ID = "module_id";
	public static final String EXTRA_GATE_ID = "gate_id";
	public static final String EXTRA_ACTIVE_POS = "act_module_pos";

	private String mActiveGateId;
	private String mActiveModuleId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_module_detail);

		Toolbar toolbar = (Toolbar) findViewById(R.id.beeeon_toolbar);
		if (toolbar != null) {
			toolbar.setTitle("");
			setSupportActionBar(toolbar);
		}

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mActiveGateId = bundle.getString(EXTRA_GATE_ID);
			mActiveModuleId = bundle.getString(EXTRA_MODULE_ID);
		}

		if (mActiveGateId == null || mActiveModuleId == null) {
			Toast.makeText(this, R.string.toast_not_specified_gate_or_module, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		ModuleDetailFragment moduleDetailFragment = ModuleDetailFragment.newInstance(mActiveGateId, mActiveModuleId);
		getSupportFragmentManager().beginTransaction().replace(R.id.module_detail_container, moduleDetailFragment).commit();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return false;
	}
}

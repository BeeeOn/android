package com.rehivetech.beeeon.gui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.Window;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.AddDashboardItemFragment;
import com.rehivetech.beeeon.gui.fragment.BaseApplicationFragment;

import java.util.List;

/**
 * Created by martin on 13.1.16.
 */
public class AddDashboardItemActivity extends BaseApplicationActivity {

	private static final String ARG_GATE_ID = "gate_id";
	private static final String ARG_INDEX = "index";

	public static Intent getAddDashBoardActivityIntent(Context context, int index, String gateId) {
		Intent intent = new Intent(context, AddDashboardItemActivity.class);
		intent.putExtra(ARG_INDEX, index);
		intent.putExtra(ARG_GATE_ID, gateId);

		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_add_dashboard_item);

		if(!getResources().getBoolean(R.bool.is_tablet)){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		setupToolbar("", INDICATOR_BACK);

		Bundle args = getIntent().getExtras();

		BaseApplicationFragment fragment;

		String gateId = args.getString(ARG_GATE_ID);
		int index = args.getInt(ARG_INDEX);

		if (savedInstanceState == null) {
			fragment = AddDashboardItemFragment.newInstance(index, gateId);
			getSupportFragmentManager().beginTransaction().replace(R.id.activity_add_dashboard_container, fragment).commit();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		List<Fragment> fragments = getSupportFragmentManager().getFragments();
		if (fragments != null) {
			for (Fragment fragment : fragments) {
				fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
			}
		}
	}
}

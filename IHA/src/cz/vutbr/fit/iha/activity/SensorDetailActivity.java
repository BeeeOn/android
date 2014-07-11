package cz.vutbr.fit.iha.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.view.MenuItem;

import cz.vutbr.fit.iha.R;

/**
 * Class that handle screen with detail of some sensor
 * @author ThinkDeep
 *
 */
public class SensorDetailActivity extends BaseActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor_detail_wraper);
		
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher_white);
		SensorDetailFragment fragment = new SensorDetailFragment();
		fragment.setArguments(getIntent().getExtras());
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		
		ft.replace(R.id.sensor_wraper, fragment);
		ft.commit();
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

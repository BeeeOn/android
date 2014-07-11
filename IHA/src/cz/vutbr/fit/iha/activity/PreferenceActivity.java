package cz.vutbr.fit.iha.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.view.MenuItem;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.extension.watches.smartwatch2.DialogFragmentDefaultAdapter;
import cz.vutbr.fit.iha.extension.watches.smartwatch2.DialogFragmentDefaultLocation;

/**
 * The control preference activity handles the preferences for the control
 * extension.
 */
public class PreferenceActivity extends BaseActivity {

	private static final String TAG_DEF_LOCATION = "def_location";
	private static final String TAG_DEF_ADAPTER = "def_adapter";

	private static final int UNDEFINED = -1;

	private ListView listView;

	private int posAdap;
	private int posLoc;

	List<String> preferenceList;

	Controller mController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		posAdap = UNDEFINED;
		posLoc = UNDEFINED;

		setContentView(R.layout.sw2_activity_preference);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mController = Controller.getInstance(this);

		preferenceList = new ArrayList<String>();

		// Get ListView object from xml
		listView = (ListView) findViewById(R.id.list_preference);

		List<Adapter> adapters = mController.getAdapters();
		if (adapters.size() > 1) {
			posAdap = preferenceList.size();
			preferenceList
					.add(getString(R.string.preference_set_default_adapter));
		}

		posLoc = preferenceList.size();
		preferenceList.add(getString(R.string.preference_set_default_location));

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1,
				preferenceList.toArray(new String[preferenceList.size()]));

		// Assign adapter to ListView
		listView.setAdapter(adapter);

		// ListView Item Click Listener
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (!mController.isLoggedIn()) {
					Toast.makeText(getApplicationContext(),
							R.string.please_log_in, Toast.LENGTH_LONG).show();
					return;
				}

				/* LOCATION SETTINGS */
				if (posLoc == position) {
					SharedPreferences prefs = PreferenceManager
							.getDefaultSharedPreferences(getApplicationContext());
					String adapterId = prefs.getString(
							Constants.SW2_PREF_DEF_ADAPTER, null);
					if (adapterId == null) {
						// if it doesn't exist and there is only one adapter
						// left, set it as default
						List<Adapter> adapters = mController.getAdapters();
						if (adapters.size() == 1) {
							adapterId = adapters.get(0).getId();
							prefs.edit()
									.putString(Constants.SW2_PREF_DEF_ADAPTER,
											adapterId).commit();
						} else {
							Toast.makeText(getApplicationContext(),
									R.string.adapter_isnt_set,
									Toast.LENGTH_LONG).show();
							return;
						}
					}

					// check if saved adapter still exists
					Adapter adapter = mController.getAdapter(adapterId, false);
					if (adapter == null) {
						// if it doesnt exist and there is only one adapter
						// left, set it as default
						List<Adapter> adapters = mController.getAdapters();
						if (adapters.size() == 1) {
							adapter = adapters.get(0);
							prefs.edit()
									.putString(Constants.SW2_PREF_DEF_ADAPTER,
											adapter.getId()).commit();
						} else {
							prefs.edit()
									.putString(Constants.SW2_PREF_DEF_ADAPTER,
											null).commit();
							Toast.makeText(getApplicationContext(),
									R.string.adapter_doest_exist,
									Toast.LENGTH_LONG).show();
							return;
						}
					}

					// check if there is anything to choose
					List<BaseDevice> listDevice = adapter.getDevices();
					if (listDevice.size() < 1) {
						Toast.makeText(getApplicationContext(),
								R.string.no_location_available_def_adapter,
								Toast.LENGTH_LONG).show();
						return;
					}

					SherlockDialogFragment locationChooser = new DialogFragmentDefaultLocation();
					locationChooser.show(getSupportFragmentManager(),
							TAG_DEF_LOCATION);
				}

				/* ADAPTER SETTINGS */
				else if (posAdap == position) {
					SherlockDialogFragment locationChooser = new DialogFragmentDefaultAdapter();
					locationChooser.show(getSupportFragmentManager(),
							TAG_DEF_ADAPTER);
				}
			}

		});
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

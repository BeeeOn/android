package com.rehivetech.beeeon.gui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.geofence.SimpleGeofence;
import com.rehivetech.beeeon.gui.adapter.ModuleArrayAdapter;
import com.rehivetech.beeeon.gui.adapter.SpinnerMultiAdapter;
import com.rehivetech.beeeon.gui.dialog.ConfirmDialog;
import com.rehivetech.beeeon.gui.spinnerItem.GeofenceSpinnerItem;
import com.rehivetech.beeeon.gui.spinnerItem.ISpinnerItem;
import com.rehivetech.beeeon.gui.spinnerItem.ModuleSpinnerItem;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.watchdog.Watchdog;
import com.rehivetech.beeeon.household.watchdog.WatchdogBaseType;
import com.rehivetech.beeeon.household.watchdog.WatchdogGeofenceType;
import com.rehivetech.beeeon.household.watchdog.WatchdogSensorType;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.RemoveWatchdogTask;
import com.rehivetech.beeeon.threading.task.SaveWatchdogTask;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.List;

// TODO probably create some interface for watchdogActionTypes so that there does not need to be switch everywhere for that
// TODO handle on-off sensor values

/**
 * Activity for creation and editation of watchdog rule
 *
 * @author mlyko
 */
public class WatchdogEditRuleActivity extends BaseApplicationActivity implements ConfirmDialog.ConfirmDialogListener {
	private static final String TAG = WatchdogEditRuleActivity.class.getSimpleName();

	public static final String EXTRA_GATE_ID = "gate_id";
	public static final String EXTRA_RULE_ID = "rule_id";
	public static final String EXTRA_IS_NEW = "rule_is_new";
	public static final String EXTRA_GEOFENCE_ID_PICKED = "geofence_id_picked";

	// helper variables for getting modules from list
	private static final int MODULES_ALL = 0;
	private static final int MODULES_ACTORS = 1;
	private static final int MODULES_SENSORS = 2;

	// extras
	private String mActiveGateId;
	private String mActiveRuleId;
	private String mActiveGeoId;
	private boolean mIsNew = false;

	private Menu mOptionsMenu;
	private UnitsHelper mUnitsHelper;
	private SharedPreferences mPrefs;

	private List<Location> mLocations;
	private List<Device> mDevices;
	private List<SimpleGeofence> mGeofences;

	// TODO??
	private List<Module> _sensors;
	private List<Module> _actors;

	private Watchdog mWatchdog;
	private String mWatchdogAction;
	private WatchdogBaseType mWatchdogOperator;

	SpinnerMultiAdapter mSpinnerMultiAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_watchdog_edit_rule);
		Log.d(TAG, "onCreate()");

		// prepare toolbar
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			toolbar.setTitle(R.string.watchdog_rule);
			setSupportActionBar(toolbar);
		}

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
			// x instead of <- indicating that no changes will be saved upon click
			actionBar.setHomeAsUpIndicator(R.drawable.ic_action_cancel);
		}

		Bundle bundle = getIntent().getExtras();
		// if not regular bundle, try to saved instance bundle
		if (bundle == null) {
			bundle = savedInstanceState;
		}

		if (bundle != null) {
			mActiveGateId = bundle.getString(EXTRA_GATE_ID);
			mActiveRuleId = bundle.getString(EXTRA_RULE_ID);
			mActiveGeoId = bundle.getString(EXTRA_GEOFENCE_ID_PICKED);
			mIsNew = bundle.getBoolean(EXTRA_IS_NEW);
		} else {
			mActiveGateId = "";
			mActiveRuleId = "";
			mActiveGeoId = "";
			mIsNew = true;
		}

		// it's existing rule
		if (!mIsNew && (mActiveGateId == null || mActiveGateId.isEmpty() || mActiveRuleId == null || mActiveRuleId.isEmpty())) {
			Toast.makeText(this, R.string.toast_wrong_or_no_watchdog_rule, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// get controller
		Controller controller = Controller.getInstance(this);
		// get gate
		Gate gate = (mActiveGateId == null || mActiveGateId.isEmpty()) ? controller.getActiveGate() : controller.getGatesModel().getGate(mActiveGateId);
		if (gate == null) {
			Toast.makeText(this, R.string.toast_something_wrong, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// UserSettings can be null when user is not logged in!
		mPrefs = controller.getUserSettings();
		mUnitsHelper = (mPrefs == null) ? null : new UnitsHelper(mPrefs, this);

		// get all locations for spinners
		mLocations = controller.getLocationsModel().getLocationsByGate(gate.getId());
		// get all geofence areas
		String userId = controller.getActualUser().getId();
		mGeofences = controller.getGeofenceModel().getAllGeofences(userId);

		// devices get by cycling through all locations
		mDevices = new ArrayList<Device>();
		for (Location loc : mLocations) {
			List<Device> tempFac = controller.getDevicesModel().getDevicesByLocation(gate.getId(), loc.getId());
			mDevices.addAll(tempFac);
		}

		// get watchdog rule
		if (!mIsNew) {
			mWatchdog = controller.getWatchdogsModel().getWatchdog(gate.getId(), mActiveRuleId);
			if (mWatchdog == null) {
				Toast.makeText(this, R.string.toast_something_wrong, Toast.LENGTH_LONG).show();
				finish();
				return;
			}
		} else {
			mWatchdog = new Watchdog(Watchdog.TYPE_SENSOR);
			mWatchdog.setGateId(gate.getId());
		}

		mWatchdogOperator = mWatchdog.getOperatorType();

		// needs to set units helper if want to use it
		mWatchdogOperator.setUnitsHelper(mUnitsHelper);

		// hide keyboard
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		initLayout();
		setValues();
	}

	/**
	 * Initializes GUI elements to work
	 */
	private void initLayout() {
		// init gui elements
		final Spinner ifItemSpinner = (Spinner) findViewById(R.id.watchdog_edit_if_item_spinner);
		final FloatingActionButton operatorButton = (FloatingActionButton) findViewById(R.id.watchdog_edit_operator);
		final EditText ruleTreshold = (EditText) findViewById(R.id.watchdog_edit_treshold);
		final TextView ruleTresholdUnit = (TextView) findViewById(R.id.watchdog_edit_treshold_unit);
		final RadioGroup actionType = (RadioGroup) findViewById(R.id.watchdog_edit_action_radiogroup);
		final Spinner actorSpinner = (Spinner) findViewById(R.id.watchdog_edit_actor_spinner);

		// ----- prepare list of available module & geofences
		mSpinnerMultiAdapter = new SpinnerMultiAdapter(this);
		boolean isAnyIfInput = false;
		// modules
		List<Module> moduleSensors = getModulesArray(MODULES_SENSORS);
		if (!moduleSensors.isEmpty()) {
			mSpinnerMultiAdapter.addHeader(getString(R.string.modules));
			for (Module dev : moduleSensors) {
				Location loc = Utils.getFromList(dev.getDevice().getLocationId(), mLocations);
				mSpinnerMultiAdapter.addItem(new ModuleSpinnerItem(dev, loc, dev.getId(), this));
			}
			isAnyIfInput = true;
		}

		// geofence areas
		if (Utils.isGooglePlayServicesAvailable(this) && !mGeofences.isEmpty()) {
			mSpinnerMultiAdapter.addHeader(getString(R.string.title_activity_map_geofence));
			for (SimpleGeofence geo : mGeofences) {
				mSpinnerMultiAdapter.addItem(new GeofenceSpinnerItem(geo, geo.getId(), this));
			}
			isAnyIfInput = true;
		}

		// if nothing is to select, we show header with message
		if (!isAnyIfInput) {
			mSpinnerMultiAdapter.addHeader(getString(R.string.multiadapter_no_item));
			ifItemSpinner.setEnabled(false);
		}

		ifItemSpinner.setAdapter(mSpinnerMultiAdapter);
		ifItemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				ISpinnerItem selected = mSpinnerMultiAdapter.getItem(position);

				switch (selected.getType()) {
					case MODULE:
						mWatchdogOperator = new WatchdogSensorType(mWatchdogOperator.getIndex());
						mWatchdog.setType(Watchdog.TYPE_SENSOR);
						mWatchdog.setGeoRegionId("");
						break;

					case GEOFENCE:
						mWatchdogOperator = new WatchdogGeofenceType(mWatchdogOperator.getIndex());
						mWatchdog.setType(Watchdog.TYPE_GEOFENCE);
						break;

					// if not any of mentioned types, do nothing
					default:
						mWatchdogOperator.clearGUI(operatorButton, ruleTreshold, ruleTresholdUnit);
						return;
				}
				// we need to refresh UnitHelper cause setOperator destroys it
				mWatchdogOperator.setUnitsHelper(mUnitsHelper);
				// setup gui based on type
				mWatchdogOperator.setupGUI(selected, operatorButton, ruleTreshold, ruleTresholdUnit);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		// setup gui based on type in default position
		int defaultPos = mSpinnerMultiAdapter.getRealPosition(0);
		ifItemSpinner.setSelection(defaultPos);

		// changing specified layout when checked
		actionType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RelativeLayout NotifLayout = (RelativeLayout) findViewById(R.id.watchdog_detail_notification);
				RelativeLayout ActionLayout = (RelativeLayout) findViewById(R.id.watchdog_edit_actor_layout);

				switch (checkedId) {
					case R.id.watchdog_edit_notification:
						NotifLayout.setVisibility(View.VISIBLE);
						ActionLayout.setVisibility(View.GONE);
						mWatchdogAction = Watchdog.ACTION_NOTIFICATION;
						break;

					case R.id.watchdog_edit_actor:
						NotifLayout.setVisibility(View.GONE);
						ActionLayout.setVisibility(View.VISIBLE);
						mWatchdogAction = Watchdog.ACTION_ACTOR;
						break;
				}
			}
		});

		// ------- choose action type if not loaded
		if (mIsNew) {
			// first checked
			RadioButton radioNotification = (RadioButton) findViewById(R.id.watchdog_edit_notification);
			radioNotification.setChecked(true);
		}

		// ------- choose actor
		final ModuleArrayAdapter actorAdapter = new ModuleArrayAdapter(this, R.layout.custom_spinner2_item, getModulesArray(MODULES_ACTORS), mLocations);
		actorAdapter.setLayoutInflater(getLayoutInflater());
		actorAdapter.setDropDownViewResource(R.layout.custom_spinner2_dropdown_item);
		actorSpinner.setAdapter(actorAdapter);
	}

	/**
	 * Fills gui elements with values
	 */
	private void setValues() {
		final Spinner actorSpinner = (Spinner) findViewById(R.id.watchdog_edit_actor_spinner);
		final EditText notificationText = (EditText) findViewById(R.id.watchdog_edit_notification_text);
		final EditText ruleTreshold = (EditText) findViewById(R.id.watchdog_edit_treshold);
		final FloatingActionButton operatorButton = (FloatingActionButton) findViewById(R.id.watchdog_edit_operator);
		final Spinner ifItemSpinner = (Spinner) findViewById(R.id.watchdog_edit_if_item_spinner);
		final SwitchCompat ruleEnabled = (SwitchCompat) findViewById(R.id.watchdog_edit_switch);
		final EditText ruleName = (EditText) findViewById(R.id.watchdog_edit_name);
		final RadioGroup actionType = (RadioGroup) findViewById(R.id.watchdog_edit_action_radiogroup);
		// check first if sent geofence id as extra, then if watchdog already has it
		String geoId = "";
		if (mActiveGeoId != null && !mActiveGeoId.isEmpty())
			geoId = mActiveGeoId;
		else if (mWatchdog.getGeoRegionId() != null && !mWatchdog.getGeoRegionId().isEmpty())
			geoId = mWatchdog.getGeoRegionId();

		// set spinner in the "IF" section
		if (!geoId.isEmpty()) {
			int index = Utils.getObjectIndexFromList(geoId, mGeofences);
			if (index > -1) ifItemSpinner.setSelection(mSpinnerMultiAdapter.getRealPosition(index, ISpinnerItem.SpinnerItemType.GEOFENCE));
		} else if (mWatchdog.getModules() != null && mWatchdog.getModules().size() > 0) {
			int index = Utils.getObjectIndexFromList(mWatchdog.getModules().get(0), getModulesArray(MODULES_SENSORS));
			if (index > -1) ifItemSpinner.setSelection(mSpinnerMultiAdapter.getRealPosition(index, ISpinnerItem.SpinnerItemType.MODULE));
		}

		// if this is new watchdog, we don't set anything
		if (mWatchdog.getId() == null) return;

		ruleName.setText(mWatchdog.getName());
		ruleEnabled.setChecked(mWatchdog.isEnabled());
		ruleTreshold.setText(mWatchdog.getParams().get(Watchdog.PAR_TRESHOLD));

		// get parameter action value
		String par_action_value = mWatchdog.getParam(Watchdog.PAR_ACTION_VALUE);
		// based on action type set different part of GUI
		switch (mWatchdog.getAction()) {
			case Watchdog.ACTION_NOTIFICATION:
				actionType.check(R.id.watchdog_edit_notification);
				if (par_action_value != null) notificationText.setText(par_action_value);
				break;

			case Watchdog.ACTION_ACTOR:
				actionType.check(R.id.watchdog_edit_actor);
				int actorIndex = par_action_value == null ? -1 : Utils.getObjectIndexFromList(par_action_value, getModulesArray(MODULES_ACTORS));
				if (actorIndex > -1) actorSpinner.setSelection(actorIndex);
				break;
		}

		// setup operators icon
		String par_operator = mWatchdog.getParam(Watchdog.PAR_OPERATOR);
		if (par_operator != null) {
			mWatchdogOperator.setByType(par_operator);
			operatorButton.setImageResource(mWatchdogOperator.getIconResource());
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(EXTRA_GATE_ID, mActiveGateId);
		savedInstanceState.putString(EXTRA_RULE_ID, mActiveRuleId);
		savedInstanceState.putBoolean(EXTRA_IS_NEW, mIsNew);
		savedInstanceState.putString(EXTRA_GEOFENCE_ID_PICKED, mActiveGeoId);

		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}

	/**
	 * Hides delete button when we have new watchdog
	 *
	 * @param menu
	 * @return
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.watchdog_edit_menu, menu);
		mOptionsMenu = menu;

		onUpdateOptionsMenu();
		return true;
	}

	/**
	 * Called when want to show delete icon
	 */
	private void onUpdateOptionsMenu() {
		if (mOptionsMenu == null) return;

		MenuItem deleteActionButton = mOptionsMenu.findItem(R.id.wat_menu_delete);
		deleteActionButton.setVisible(mIsNew ? false : true);
	}

	/**
	 * Clicked on actionbar button (delete / save)
	 *
	 * @param item
	 * @return
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
			case R.id.wat_menu_save:
				doSaveWatchdogTask();
				break;
			case R.id.wat_menu_delete:
				String title = getString(R.string.confirm_remove_title, mWatchdog.getName());
				String message = getString(R.string.confirm_remove_watchdog_message);
				ConfirmDialog.confirm(this, title, message, R.string.button_remove, ConfirmDialog.TYPE_DELETE_WATCHDOG, mWatchdog.getId());
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	// ---------- ASYNC TASKS ---------- //

	/**
	 * Async task for saving watchdog
	 */
	private void doSaveWatchdogTask() {
		final Spinner actorSpinner = (Spinner) findViewById(R.id.watchdog_edit_actor_spinner);
		final EditText notificationText = (EditText) findViewById(R.id.watchdog_edit_notification_text);
		final EditText ruleTreshold = (EditText) findViewById(R.id.watchdog_edit_treshold);
		final Spinner ifItemSpinner = (Spinner) findViewById(R.id.watchdog_edit_if_item_spinner);
		final SwitchCompat ruleEnabled = (SwitchCompat) findViewById(R.id.watchdog_edit_switch);
		final EditText ruleName = (EditText) findViewById(R.id.watchdog_edit_name);
		if (!Utils.validateInput(this, ruleName)) {
			return;
		}

		ArrayList<String> devsIds = new ArrayList<>();
		ArrayList<String> newParams = new ArrayList<>();

		// FIXME: must be because network sends short tag if empty
		String tresholdValue = "_";

		ISpinnerItem selected = mSpinnerMultiAdapter.getItem(ifItemSpinner.getSelectedItemPosition());
		switch (selected.getType()) {
			case MODULE:
				if (!Utils.validateInput(this, ruleTreshold, Utils.ValidationType.INTEGER)) return;

				Module selectedModule = (Module) selected.getObject();
				devsIds.add(selectedModule.getId());
				newParams.add(selectedModule.getId());

				tresholdValue = ruleTreshold.getText().toString();
				break;

			case GEOFENCE:
				SimpleGeofence selectedGeofence = (SimpleGeofence) selected.getObject();
				mWatchdog.setGeoRegionId(selectedGeofence.getId());
				newParams.add(selectedGeofence.getId());
				break;

			case HEADER:
				Toast.makeText(this, getString(R.string.watchdog_no_valid_item), Toast.LENGTH_LONG).show();
				return;

			default:
				Toast.makeText(this, getString(R.string.toast_not_implemented), Toast.LENGTH_LONG).show();
				return;
		}

		// operator
		newParams.add(mWatchdogOperator.getCode());
		// treshold
		newParams.add(tresholdValue);
		// action type
		newParams.add(mWatchdogAction);
		switch (mWatchdogAction) {
			case Watchdog.ACTION_NOTIFICATION:
				if (!Utils.validateInput(this, notificationText)) return;

				newParams.add(notificationText.getText().toString());
				break;

			case Watchdog.ACTION_ACTOR:
				if (actorSpinner.getSelectedItem() == null) {
					Toast.makeText(this, getString(R.string.actor_required), Toast.LENGTH_LONG).show();
					return;
				}

				Module selectedActor = getModulesArray(MODULES_ACTORS).get(actorSpinner.getSelectedItemPosition());
				newParams.add(selectedActor.getId());
				break;
		}

		mWatchdog.setOperatorType(mWatchdogOperator);
		mWatchdog.setParams(newParams);
		mWatchdog.setModules(devsIds);

		mWatchdog.setName(ruleName.getText().toString());
		mWatchdog.setEnabled(ruleEnabled.isChecked());

		// TODO check if data were changed ???
		SaveWatchdogTask saveWatchdogTask = new SaveWatchdogTask(this);

		saveWatchdogTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				Toast.makeText(WatchdogEditRuleActivity.this, getResources().getString(success ? R.string.toast_success_save_data : R.string.toast_fail_save_data), Toast.LENGTH_LONG).show();

				// when new rule, close after done
				if (success) finish();
			}
		});

		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(saveWatchdogTask, mWatchdog, CallbackTaskManager.ProgressIndicator.PROGRESS_DIALOG);
	}

	/**
	 * Async task for deleting watchdog
	 */
	private void doRemoveWatchdogTask() {
		RemoveWatchdogTask removeWatchdogTask = new RemoveWatchdogTask(this);

		removeWatchdogTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				Toast.makeText(WatchdogEditRuleActivity.this, getResources().getString(success ? R.string.toast_delete_success : R.string.toast_delete_fail), Toast.LENGTH_SHORT).show();
				finish();
			}
		});

		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(removeWatchdogTask, mWatchdog);
	}

	// ---------- HELPER FUNCTIONS ---------- //

	/**
	 * Helper function for getting type of modules from list of devices
	 *
	 * @param type MODULES_ALL | MODULES_ACTORS | MODULES_SENSORS
	 * @return
	 */
	private List<Module> getModulesArray(int type) {
		if (type == MODULES_ACTORS && _actors != null) {
			return _actors;
		} else if (type == MODULES_SENSORS && _sensors != null) {
			return _sensors;
		}

		List<Module> modules = new ArrayList<Module>();

		for (Device device : mDevices)
			for (Module module : device.getModules()) {
				if (type == MODULES_ACTORS && !module.getType().isActor()) {
					continue;
				} else if (type == MODULES_SENSORS && module.getType().isActor()) {
					continue;
				}

				modules.add(module);
			}

		if (type == MODULES_ACTORS) {
			_actors = modules;
		} else if (type == MODULES_SENSORS) {
			_sensors = modules;
		}

		return modules;
	}

	/**
	 * Helper function for validating EditText
	 *
	 * @param eText
	 * @param additional Array of additional rules to validate
	 * @return
	 */
	private boolean validateInput(EditText eText, String... additional) {
		String inputText = eText.getText().toString().trim();
		if (inputText.length() == 0) {
			eText.requestFocus();
			eText.setError(getString(R.string.toast_field_must_be_filled));
			return false;
		}

		for (String type : additional) {
			switch (type) {
				case "parseInt":
					try {
						int num = Integer.parseInt(inputText);
					} catch (NumberFormatException e) {
						eText.requestFocus();
						eText.setError(getString(R.string.toast_field_must_be_number));
						return false;
					}
					break;
			}
		}

		return true;
	}

	@Override
	public void onConfirm(int confirmType, String dataId) {
		// FIXME: Rewrite with using dataId
		if (confirmType == ConfirmDialog.TYPE_DELETE_WATCHDOG) {
			doRemoveWatchdogTask();
		}
	}
}


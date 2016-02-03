package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.avast.android.dialogs.core.BaseDialogFragment;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.AddDeviceActivity;
import com.rehivetech.beeeon.gui.adapter.DeviceRecycleAdapter;
import com.rehivetech.beeeon.gui.dialog.EditTextDialog;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.DeviceType;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.PairDeviceTask;
import com.rehivetech.beeeon.threading.task.SendParameterTask;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Martin Matejcik
 * @author Tomas Mlynaric
 */
public class SearchDeviceFragment extends BaseApplicationFragment implements DeviceRecycleAdapter.IItemClickListener, EditTextDialog.IPositiveButtonDialogListener {
	private static final String TAG = SearchDeviceFragment.class.getSimpleName();

	private static final long COUNTDOWN_INTERVAL = DateUtils.MINUTE_IN_MILLIS * 2;
	private static final int PAIR_REQUEST_REPEAT_INTERVAL = (int) (DateUtils.SECOND_IN_MILLIS * 3);
	public static final String KEY_GATE_ID = "gate_id";

	private static final String STATE_COUNTDOWN_TIME_ELAPSED = "countdown_time_elapsed";
	private static final String STATE_SELECTED_ITEM_ID = "selected_item_id";

	private static final int DIALOG_CODE_MANUAL = 1;
	private static final int DIALOG_CODE_PASSWORD = 2;

	private String mGateId;

	private CoordinatorLayout mRootView;
	private TextView mCountDownText;
	private RecyclerView mRecyclerView;
	private TextView mSearchingText;

	private DeviceRecycleAdapter mAdapter;
	@Nullable
	private Handler mHandler;
	private long mCountDownTimeElapsed;
	private String mSelectedItemId;

	public static SearchDeviceFragment newInstance(String gateId) {

		Bundle args = new Bundle();
		args.putString(KEY_GATE_ID, gateId);
		SearchDeviceFragment fragment = new SearchDeviceFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();

		if (args != null) {
			mGateId = args.getString(KEY_GATE_ID);
		}
		setHasOptionsMenu(true);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = (CoordinatorLayout) inflater.inflate(R.layout.fragment_search_devices, container, false);

		Toolbar toolbar = (Toolbar) mRootView.findViewById(R.id.beeeon_toolbar);
		AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
		layoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
		toolbar.setLayoutParams(layoutParams);

		mCountDownText = (TextView) mRootView.findViewById(R.id.search_countdown_text);
		mSearchingText = (TextView) mRootView.findViewById(R.id.search_device_searching_text);
		mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.search_device_recycler_view);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

		mAdapter = new DeviceRecycleAdapter(mActivity, this, true);
		mRecyclerView.setAdapter(mAdapter);
		return mRootView;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
			mCountDownTimeElapsed = savedInstanceState.getLong(STATE_COUNTDOWN_TIME_ELAPSED);
			mSelectedItemId = savedInstanceState.getString(STATE_SELECTED_ITEM_ID);
			List<Device> devices = Controller.getInstance(mActivity).getUninitializedDevicesModel().getUninitializedDevicesByGate(mGateId);
			updateAdapter(devices);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_device_search, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.device_search_manual_button:
				EditTextDialog
						.createBuilder(mActivity, mActivity.getSupportFragmentManager())
						.setTitle(R.string.device_search_manual_button)
						.setLayoutRes(R.layout.dialog_device_manual_search)
//						.setEditTextValue(mWeatherCity != null ? mWeatherCity.name : mWidgetData.weather.cityName)
						.setHint(R.string.device_search_manual_search_hint)
						.setPositiveButtonText(mActivity.getString(R.string.device_search_manual_search_dialog_button))
//						.setNegativeButtonText(mActivity.getString(R.string.activity_fragment_btn_cancel))
						.showKeyboard()
						.setTargetFragment(SearchDeviceFragment.this, DIALOG_CODE_MANUAL)
						.show();
				return true;
		}

		return false;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mCountDownTimeElapsed != COUNTDOWN_INTERVAL) {
			startAutomaticPairing();
		} else {
			mCountDownText.setText(convertMillisToText(0));
			Snackbar.make(mRootView, R.string.device_search_snack_bar_title_search_end, Snackbar.LENGTH_INDEFINITE)
					.setAction(R.string.device_search_snack_bar_retry, new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mAdapter.clearData();
							mCountDownTimeElapsed = 0;
							startAutomaticPairing();
						}
					})
					.show();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(STATE_SELECTED_ITEM_ID, mSelectedItemId);
		outState.putLong(STATE_COUNTDOWN_TIME_ELAPSED, mCountDownTimeElapsed);
		mActivity.callbackTaskManager.cancelAndRemoveAll();
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			mActivity.setResult(Activity.RESULT_OK);
			mActivity.finish();
		}
	}

	private void startAutomaticPairing() {
		startPairing(null);
	}

	private void startPairing(@Nullable final String deviceIpAddress) {
		mHandler = new Handler();
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mCountDownTimeElapsed += DateUtils.SECOND_IN_MILLIS;
				mCountDownText.setText(convertMillisToText(COUNTDOWN_INTERVAL - mCountDownTimeElapsed));

				if (mCountDownTimeElapsed == COUNTDOWN_INTERVAL) {
					Snackbar.make(mRootView, R.string.device_search_snack_bar_title_search_end, Snackbar.LENGTH_INDEFINITE)
							.setAction(R.string.device_search_snack_bar_retry, new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									mAdapter.clearData();
									mCountDownTimeElapsed = 0;
									startPairing(deviceIpAddress);
								}
							})
							.show();
					return;
				}

				if (mCountDownTimeElapsed < COUNTDOWN_INTERVAL && mCountDownTimeElapsed % PAIR_REQUEST_REPEAT_INTERVAL == 0) {
					doPairRequestTask(mCountDownTimeElapsed < (DateUtils.SECOND_IN_MILLIS * 4), deviceIpAddress);
				}
				mHandler.postDelayed(this, DateUtils.SECOND_IN_MILLIS);
			}
		});

		Snackbar.make(mRootView, R.string.device_search_snack_bar_title_searching, Snackbar.LENGTH_INDEFINITE)
				.show();
	}

	private static String convertMillisToText(long millisUntilFinished) {
		int minutes = (int) (millisUntilFinished / DateUtils.MINUTE_IN_MILLIS);
		millisUntilFinished -= minutes * DateUtils.MINUTE_IN_MILLIS;
		int seconds = (int) (millisUntilFinished / DateUtils.SECOND_IN_MILLIS);

		return String.format("%2d:%02d", minutes, seconds);
	}

	private void doPairRequestTask(boolean sendPairRequest, String deviceIpAddress) {
		// function creates and starts Task that handles pairing between the gate and the account
		final PairDeviceTask pairDeviceTask = new PairDeviceTask(mActivity, mGateId, sendPairRequest, deviceIpAddress);
		pairDeviceTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {

				if (success) {
					List<Device> devices = Controller.getInstance(getActivity()).getUninitializedDevicesModel().getUninitializedDevicesByGate(mGateId);
					updateAdapter(devices);
				} else {
					if (pairDeviceTask.getException() != null) {
						mActivity.callbackTaskManager.cancelAndRemoveAll();
						if (mHandler != null) {
							mHandler.removeCallbacksAndMessages(null);
						}
						mActivity.finish();
					}
				}
			}
		});

		mActivity.callbackTaskManager.executeTask(pairDeviceTask, mGateId, CallbackTaskManager.ProgressIndicator.PROGRESS_NONE);
	}

	private void updateAdapter(List<Device> adapterData) {

		List<Integer> manufacturers = new ArrayList<>();
		for (Device device : adapterData) {
			if (!manufacturers.contains(device.getType().getManufacturerRes())) {
				manufacturers.add(device.getType().getManufacturerRes());
			}
		}

		ArrayList<Object> adapterList = new ArrayList<>();
		for (int manufacturer : manufacturers) {
			adapterList.add(getString(manufacturer));
			for (Device device : adapterData) {
				if (manufacturer == device.getType().getManufacturerRes()) {
					adapterList.add(device);
				}
			}
		}

		mAdapter.updateData(adapterList);

		if (mAdapter.getItemCount() > 0) {
			mRecyclerView.setVisibility(View.VISIBLE);
			mSearchingText.setVisibility(View.GONE);
		}
	}

	/**
	 * When clicked on list item
	 *
	 * @param position position where clicked
	 * @param viewType view type which was clicked
	 */
	@Override
	public void onRecyclerViewItemClick(int position, int viewType) {
		if (viewType == DeviceRecycleAdapter.TYPE_UNPAIRED_DEVICE) {
			Device device = (Device) mAdapter.getItem(position);

			mSelectedItemId = device.getId();

			// TODO should be in DeviceType as parameter "password_protected"
			if (device.getType().equals(DeviceType.TYPE_6) || device.getType().equals(DeviceType.TYPE_1)) {
				EditTextDialog
						.createBuilder(mActivity, mActivity.getSupportFragmentManager())
						.setTitle(R.string.device_search_enter_password)
						.setLayoutRes(R.layout.dialog_device_enter_password)
						.setHint(R.string.device_search_enter_password_hint)
						.setPositiveButtonText(R.string.device_search_enter_password_ok)
						.setCancelableOnTouchOutside(false)
						.showKeyboard()
						.setTargetFragment(SearchDeviceFragment.this, DIALOG_CODE_PASSWORD)
						.show();
			} else {
				startDeviceSetupActivity(device);
			}
		}
	}

	@Override
	public boolean onRecyclerViewItemLongClick(int position, int viewType) {
		return false;
	}

	/**
	 * Opens device setup activity
	 *
	 * @param device specified device
	 */
	private void startDeviceSetupActivity(Device device) {
		Intent intent = AddDeviceActivity.prepareAddDeviceActivityIntent(mActivity, mGateId, AddDeviceActivity.ACTION_SETUP, device.getId());
		startActivityForResult(intent, 50);
	}

	/**
	 * Handling dialogs buttons clicks
	 *
	 * @param requestCode determines which dialog was clicked
	 * @param view        base view of the dialog
	 * @param dialog      dialog itself (so that it can be dismissed)
	 */
	@Override
	public void onPositiveButtonClicked(int requestCode, View view, BaseDialogFragment dialog) {
		switch (requestCode) {
			case DIALOG_CODE_PASSWORD:
				dialogEnterPasswordSubmitted(view, dialog);
				break;

			case DIALOG_CODE_MANUAL:
				dialogManualSearchSubmitted(view, dialog);
				break;
		}
	}

	private static final String IP_ADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	private static Pattern sPattern = Pattern.compile(IP_ADDRESS_PATTERN);

	/**
	 * Handling when manual search dialog submitted (clicked ok)
	 *
	 * @param view
	 * @param dialog
	 */
	private void dialogManualSearchSubmitted(View view, BaseDialogFragment dialog) {
		final TextInputLayout textInputLayout = (TextInputLayout) view.findViewById(R.id.dialog_edit_text_input_layout);
		EditText editText = textInputLayout.getEditText();

		if (editText == null || !sPattern.matcher(editText.getText().toString()).matches()) {
			textInputLayout.setError(getString(R.string.device_search_manual_error));
			return;
		}

		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
		}
		mAdapter.clearData();
		mCountDownTimeElapsed = 0;
		startPairing(editText.getText().toString());
		dialog.dismiss();
	}

	/**
	 * Handling when password dialog was submited (clicked ok).
	 * Checks user input and asynchronously sends request to the server if password was ok.
	 *
	 * @param view   dialog basic layout view
	 * @param dialog dialog which is closed when success
	 */
	private void dialogEnterPasswordSubmitted(View view, final BaseDialogFragment dialog) {
		// check device selected before we shown dialog
		List<Device> devices = Controller.getInstance(getActivity()).getUninitializedDevicesModel().getUninitializedDevicesByGate(mGateId);
		final Device selectedDevice = Utils.getFromList(mSelectedItemId, devices);

		if (selectedDevice == null) {
			Toast.makeText(mActivity, R.string.device_add_activity_opening_error, Toast.LENGTH_LONG).show();
			dialog.dismiss();
			return;
		}

		final TextInputLayout textInputLayout = (TextInputLayout) view.findViewById(R.id.dialog_edit_text_input_layout);
		EditText editText = textInputLayout.getEditText();
		final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.dialog_enter_password_progressbar);
		CheckBox checkBox = (CheckBox) view.findViewById(R.id.dialog_enter_password_checkbox);

		// check whether any text was entered
		if (editText == null || editText.getText().toString().isEmpty()) {
			textInputLayout.setError(getString(R.string.activity_utils_toast_field_must_be_filled));
			return;
		}

		// async task to send password
		SendParameterTask parameterTask = new SendParameterTask(mActivity, selectedDevice);
		parameterTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (!success) {
					textInputLayout.setError(getString(R.string.device_search_enter_password_wrong));
					return;
				}

				progressBar.setVisibility(View.INVISIBLE);
				dialog.dismiss();
				startDeviceSetupActivity(selectedDevice);
			}
		});

		progressBar.setVisibility(View.VISIBLE);
		mActivity.callbackTaskManager.executeTask(parameterTask, Pair.create("password", editText.getText().toString()), CallbackTaskManager.ProgressIndicator.PROGRESS_NONE);
	}
}

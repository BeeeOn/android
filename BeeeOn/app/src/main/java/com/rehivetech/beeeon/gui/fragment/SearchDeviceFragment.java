package com.rehivetech.beeeon.gui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
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
import com.rehivetech.beeeon.gcm.analytics.GoogleAnalyticsManager;
import com.rehivetech.beeeon.gui.activity.AddDeviceActivity;
import com.rehivetech.beeeon.gui.adapter.DeviceRecycleAdapter;
import com.rehivetech.beeeon.gui.dialog.EditTextDialog;
import com.rehivetech.beeeon.gui.dialog.EnterPasswordDialog;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.PairDeviceTask;
import com.rehivetech.beeeon.threading.task.SendParameterTask;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.util.Validator;

import org.joda.time.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.State;

/**
 * @author Martin Matejcik
 * @author Tomas Mlynaric
 */
public class SearchDeviceFragment extends BaseApplicationFragment implements DeviceRecycleAdapter.IItemClickListener, EditTextDialog.IPositiveButtonDialogListener, EnterPasswordDialog.PasswordDialogListener {
	private static final String TAG = SearchDeviceFragment.class.getSimpleName();

	private static final long COUNTDOWN_INTERVAL = DateUtils.MINUTE_IN_MILLIS * 2;
	private static final int PAIR_REQUEST_REPEAT_INTERVAL = (int) (DateUtils.SECOND_IN_MILLIS * 3);
	public static final String KEY_GATE_ID = "gate_id";

	private static final int DIALOG_CODE_MANUAL = 1;
	private static final int DIALOG_CODE_PASSWORD = 2;
	private static final int REQUEST_SETUP_DEVICE = 50;

	private String mGateId;

	CoordinatorLayout mRootView;
	@Bind(R.id.search_countdown_text) TextView mCountDownText;
	@Bind(R.id.search_device_recycler_view) RecyclerView mRecyclerView;
	@Bind(R.id.search_device_searching_text) TextView mSearchingText;

	private DeviceRecycleAdapter mAdapter;
	@Nullable private CountDownTimer mCountDownTimer;
	@State long mCountDownTimeElapsed;
	@State @Nullable String mSelectedItemId;
	@State long mLastKnownTime;
	@State String mDeviceIpAddress;

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
		ButterKnife.bind(this, mRootView);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
		mAdapter = new DeviceRecycleAdapter(mActivity, this, true);
		mRecyclerView.setAdapter(mAdapter);
		return mRootView;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
			List<Device> devices = Controller.getInstance(mActivity).getUninitializedDevicesModel().getUninitializedDevicesByGate(mGateId);
			updateAdapter(devices);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_device_search, menu);
	}

	/**
	 * Manual search clicking handler
	 *
	 * @param item clicked
	 * @return if consumed
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.device_search_manual_button:
				dialogManualSearchShow();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		GoogleAnalyticsManager.getInstance().logScreen(GoogleAnalyticsManager.SEARCH_DEVICE_SCREEN);
	}

	/**
	 * Shows finished snackbar with retry button
	 */
	public void showFinishedSnackbar() {
		Snackbar.make(mRootView, R.string.device_search_snack_bar_title_search_end, Snackbar.LENGTH_INDEFINITE)
				.setAction(R.string.device_search_snack_bar_retry, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mAdapter.clearData();
						mCountDownTimeElapsed = 0;
						startPairing();
					}
				})
				.show();
	}

	@Override
	public void onStart() {
		super.onStart();

		// calculation of difference between last known time before close and actual
		if (mLastKnownTime > 0) {
			long timeLeft = (DateTimeUtils.currentTimeMillis() - mLastKnownTime);
			if (timeLeft > COUNTDOWN_INTERVAL) {
				mCountDownTimeElapsed = COUNTDOWN_INTERVAL;
			} else if (timeLeft > 0) {
				mCountDownTimeElapsed += timeLeft;
			}

			mLastKnownTime = 0;
		}

		if (mCountDownTimeElapsed < COUNTDOWN_INTERVAL) {
			startPairing();
		} else {
			doPairRequestTask(false); // last time tries to get unitialized devices
			mCountDownText.setText(convertMillisToText(0));
			showFinishedSnackbar();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		mLastKnownTime = DateTimeUtils.currentTimeMillis();

		if (mCountDownTimer != null) {
			mCountDownTimer.cancel();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_SETUP_DEVICE && resultCode == Activity.RESULT_OK) {
			mActivity.setResult(Activity.RESULT_OK);
			mActivity.finish();
		}
	}

	/**
	 * Starts pairing with updating time
	 */
	private void startPairing() {
		if (mCountDownTimer != null) {
			mCountDownTimer.cancel();
		}

		// sends request to the server
		doPairRequestTask(mCountDownTimeElapsed == 0);

		mCountDownTimer = new CountDownTimer(COUNTDOWN_INTERVAL - mCountDownTimeElapsed, DateUtils.SECOND_IN_MILLIS) {
			@Override
			public void onTick(long millisUntilFinished) {
				mCountDownTimeElapsed += DateUtils.SECOND_IN_MILLIS;
				// show how much left
				mCountDownText.setText(convertMillisToText(millisUntilFinished));
				// every repeat interval check new devices
				if (mCountDownTimeElapsed % PAIR_REQUEST_REPEAT_INTERVAL == 0) {
					doPairRequestTask(false);
				}
			}

			@Override
			public void onFinish() {
				mCountDownText.setText(convertMillisToText(0));
				showFinishedSnackbar();
			}
		}.start();

		// show search snackbar
		Snackbar.make(mRootView, R.string.device_search_snack_bar_title_searching, Snackbar.LENGTH_INDEFINITE).show();
	}

	/**
	 * Converts milliseconds to minutes:seconds string
	 *
	 * @param millisUntilFinished milliseconds to convert
	 * @return formatted string
	 */
	@SuppressLint("DefaultLocale")
	private static String convertMillisToText(long millisUntilFinished) {
		int minutes = (int) (millisUntilFinished / DateUtils.MINUTE_IN_MILLIS);
		millisUntilFinished -= minutes * DateUtils.MINUTE_IN_MILLIS;
		int seconds = (int) (millisUntilFinished / DateUtils.SECOND_IN_MILLIS);

		return String.format("%02d:%02d", minutes, seconds);
	}

	/**
	 * Creates pair request that handles pairing between the gate and the account
	 *
	 * @param sendPairRequest should be true only for the first time to send it to server, otherwise only collect data
	 */
	private void doPairRequestTask(boolean sendPairRequest) {
		final PairDeviceTask pairDeviceTask = new PairDeviceTask(mActivity, mGateId, sendPairRequest, mDeviceIpAddress);
		pairDeviceTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {

				if (success) {
					List<Device> devices = Controller.getInstance(mActivity).getUninitializedDevicesModel().getUninitializedDevicesByGate(mGateId);
					updateAdapter(devices);
				} else {
					if (pairDeviceTask.getException() != null) {
						if (mCountDownTimer != null) {
							mCountDownTimer.cancel();
						}
						mActivity.callbackTaskManager.cancelAndRemoveAll();
						mActivity.finish();
					}
				}
			}
		});

		mActivity.callbackTaskManager.executeTask(pairDeviceTask, mGateId, CallbackTaskManager.ProgressIndicator.PROGRESS_NONE);
	}

	/**
	 * Refreshes adapter with new data
	 *
	 * @param adapterData list od devices
	 */
	private void updateAdapter(List<Device> adapterData) {
		// updates manufacturers
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
		if (viewType != DeviceRecycleAdapter.TYPE_UNPAIRED_DEVICE) {
			Log.e(TAG, "Clicked on other type then unpaired device!");
			return;
		}

		Device device = (Device) mAdapter.getItem(position);
		mSelectedItemId = device.getId();

		// TODO should be in DeviceType as parameter "password_protected"
//			if (device.getType().equals(DeviceType.TYPE_6) || device.getType().equals(DeviceType.TYPE_1)) {
//				dialogEnterPasswordShow();
//			} else {
//				startDeviceSetupActivity(device);
//			}
		startDeviceSetupActivity(device);
	}

	@Override
	public boolean onRecyclerViewItemLongClick(int position, int viewType) {
		return false;
	}

	/**
	 * Opens device setup activity and clears selected device id
	 *
	 * @param device specified device
	 */
	private void startDeviceSetupActivity(Device device) {
		Intent intent = AddDeviceActivity.prepareAddDeviceActivityIntent(mActivity, mGateId, AddDeviceActivity.ACTION_SETUP, device.getId());
		mSelectedItemId = null;
		startActivityForResult(intent, REQUEST_SETUP_DEVICE);
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
		if (requestCode == DIALOG_CODE_MANUAL) {
			dialogManualSearchSubmitted(view, dialog);
		}
	}

	/**
	 * Shows dialog for manual searching
	 */
	private void dialogManualSearchShow() {
		EditTextDialog
				.createBuilder(mActivity, mActivity.getSupportFragmentManager())
				.setTitle(R.string.device_search_manual_button)
				.setLayoutRes(R.layout.dialog_device_manual_search)
				.setHint(R.string.device_search_manual_search_hint)
				.setPositiveButtonText(mActivity.getString(R.string.device_search_manual_search_dialog_button))
				.showKeyboard()
				.setTargetFragment(SearchDeviceFragment.this, DIALOG_CODE_MANUAL)
				.show();
	}

	/**
	 * Handling when manual search dialog submitted (clicked ok)
	 *
	 * @param view   dialog basic layout view
	 * @param dialog dialog which is closed when success
	 */
	private void dialogManualSearchSubmitted(View view, BaseDialogFragment dialog) {
		final TextInputLayout textInputLayout = (TextInputLayout) view.findViewById(R.id.dialog_edit_text_input_layout);
		EditText editText = textInputLayout.getEditText();

		// validate input if is in specified format
		if (editText == null || !Validator.validate(textInputLayout, Validator.IP_ADDRESS))
			return;

		mAdapter.clearData();
		mCountDownTimeElapsed = 0;
		mDeviceIpAddress = editText.getText().toString();
		startPairing();
		dialog.dismiss();
	}

	/**
	 * Shows dialog for entering password
	 */
	private void dialogEnterPasswordShow() {
		EnterPasswordDialog.show(mActivity, this, DIALOG_CODE_PASSWORD);
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
		final CheckBox checkBox = (CheckBox) view.findViewById(R.id.dialog_enter_password_checkbox);
		final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.dialog_enter_password_progressbar);

		// check whether any text was entered
		if (editText == null)
			return;

		String password = editText.getText().toString().trim();
		if (password.length() == 0 && !checkBox.isChecked()) {
			textInputLayout.requestFocus();
			textInputLayout.setError(mActivity.getString(R.string.activity_utils_toast_field_must_be_filled));
			return;

		} else if (checkBox.isChecked()) {
			dialog.dismiss();
			startDeviceSetupActivity(selectedDevice);
		}

		// async task to send password
		SendParameterTask parameterTask = new SendParameterTask(mActivity, selectedDevice);
		parameterTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				progressBar.setVisibility(View.INVISIBLE);

				if (!success) {
					textInputLayout.setError(getString(R.string.device_search_enter_password_wrong));
					return;
				}

				dialog.dismiss();
				startDeviceSetupActivity(selectedDevice);
			}
		});

		// run async task
		progressBar.setVisibility(View.VISIBLE);
		mActivity.callbackTaskManager.executeTask(parameterTask, Pair.create("password", editText.getText().toString()), CallbackTaskManager.ProgressIndicator.PROGRESS_NONE);
	}

	@Override
	public void onPositiveButtonClicked(int requestCode, View view, EnterPasswordDialog dialog) {
		dialogEnterPasswordSubmitted(view, dialog);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.unbind(this);
	}
}

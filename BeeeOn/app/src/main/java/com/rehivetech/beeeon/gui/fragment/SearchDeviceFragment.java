package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.AddDeviceActivity;
import com.rehivetech.beeeon.gui.adapter.DeviceRecycleAdapter;
import com.rehivetech.beeeon.gui.dialog.ManualSearchDialog;
import com.rehivetech.beeeon.gui.dialog.PasswordDialog;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.DeviceType;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.PairDeviceTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by martin on 28.1.16.
 */
public class SearchDeviceFragment extends BaseApplicationFragment implements DeviceRecycleAdapter.IItemClickListener, ManualSearchDialog.ManualSearchDialogListener{
	private static final String TAG = SearchDeviceFragment.class.getSimpleName();

	private static final long COUNTDOWN_INTERVAL = DateUtils.MINUTE_IN_MILLIS * 2;
	private static final int PAIR_REQUEST_REPEAT_INTERVAL = (int) (DateUtils.SECOND_IN_MILLIS * 3);
	public static final String KEY_GATE_ID = "gate_id";

	private static final String STATE_COUNTDOWN_TIME_ELAPSED = "countdown_time_elapsed";

	private String mGateId;

	private CoordinatorLayout mRootView;
	private TextView mCountDownText;
	private Button mManualSearchButton;
	private RecyclerView mRecyclerView;
	private TextView mSearchingText;

	private DeviceRecycleAdapter mAdapter;
	private Handler mHandler;
	private long mCountDownTimeElapsed;

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

	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = (CoordinatorLayout) inflater.inflate(R.layout.fragment_search_devices, container, false);

		Toolbar toolbar = (Toolbar) mRootView.findViewById(R.id.beeeon_toolbar);
		AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
		layoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
		toolbar.setLayoutParams(layoutParams);

		mManualSearchButton = (Button) mRootView.findViewById(R.id.search_manual_button);
		mCountDownText = (TextView) mRootView.findViewById(R.id.search_countdown_text);
		mSearchingText = (TextView) mRootView.findViewById(R.id.search_device_searching_text);
		mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.search_device_recycler_view);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

		mAdapter = new DeviceRecycleAdapter(mActivity, this, true);
		mRecyclerView.setAdapter(mAdapter);
		return mRootView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mManualSearchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ManualSearchDialog.show(mActivity, SearchDeviceFragment.this);
			}
		});
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
			mCountDownTimeElapsed = savedInstanceState.getLong(STATE_COUNTDOWN_TIME_ELAPSED);
			List<Device> devices = Controller.getInstance(mActivity).getUninitializedDevicesModel().getUninitializedDevicesByGate(mGateId);
			updateAdapter(devices);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mCountDownTimeElapsed != COUNTDOWN_INTERVAL) {
			startPairing();
		} else {
			mCountDownText.setText(convertMillisToText(0));
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
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(STATE_COUNTDOWN_TIME_ELAPSED, mCountDownTimeElapsed);
		mActivity.callbackTaskManager.cancelAndRemoveAll();
		mHandler.removeCallbacksAndMessages(null);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			mActivity.setResult(Activity.RESULT_OK);
			mActivity.finish();
		}
	}

	private void startPairing() {
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
									startPairing();
								}
							})
							.show();
					return;
				}

				if (mCountDownTimeElapsed < COUNTDOWN_INTERVAL && mCountDownTimeElapsed % PAIR_REQUEST_REPEAT_INTERVAL == 0) {
					doPairRequestTask(mCountDownTimeElapsed < (DateUtils.SECOND_IN_MILLIS * 4));
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

	private void doPairRequestTask(boolean sendPairRequest) {
		// function creates and starts Task that handles pairing between the gate and the account
		final PairDeviceTask pairDeviceTask = new PairDeviceTask(mActivity, mGateId, sendPairRequest);
		pairDeviceTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {

				if (success) {
					List devices = Controller.getInstance(getActivity()).getUninitializedDevicesModel().getUninitializedDevicesByGate(mGateId);
					updateAdapter(devices);
				} else {
					if (pairDeviceTask.getException() != null) {
						mActivity.callbackTaskManager.cancelAndRemoveAll();
						mHandler.removeCallbacksAndMessages(null);
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

	@Override
	public void onRecyclerViewItemClick(int position, int viewType) {
		if (viewType == DeviceRecycleAdapter.TYPE_UNPAIRED_DEVICE) {
			Device device = (Device) mAdapter.getItem(position);

			if (device.getType().equals(DeviceType.TYPE_6) || device.getType().equals(DeviceType.TYPE_1)) {
				PasswordDialog.show(mActivity, SearchDeviceFragment.this);
			} else {
				Intent intent = AddDeviceActivity.prepareAddDeviceActivityIntent(mActivity, mGateId, AddDeviceActivity.ACTION_SETUP, device.getId());
				startActivityForResult(intent, 50);
			}

		}
	}

	@Override
	public boolean onRecyclerViewItemLongClick(int position, int viewType) {
		return false;
	}

	@Override
	public void onPositiveButtonClicked(String ipAddress) {
		//TODO send request for manual search
	}
}

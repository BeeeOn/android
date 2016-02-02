package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.GraphItem;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.util.ChartHelper;

import java.util.Arrays;
import java.util.List;

/**
 * Created by martin on 13.1.16.
 */
public class AddDashboardGraphItemFragment extends BaseApplicationFragment {

	private static final String ARG_GATE_ID = "gate_id";

	private String mGateId;

	private EditText mGraphNameEditText;
	private Spinner mLeftAxisSpinner;
	private Spinner mRightAxisSpinner;
	private Spinner mGraphRangeSpinner;

	private Button mButtonDone;

	public static AddDashboardGraphItemFragment newInstance(String gateId) {

		Bundle args = new Bundle();
		args.putString(ARG_GATE_ID, gateId);
		AddDashboardGraphItemFragment fragment = new AddDashboardGraphItemFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		Bundle args = getArguments();

		if (args != null) {
			mGateId = args.getString(ARG_GATE_ID);
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_add_dashboard_graph_item, container, false);

		mGraphNameEditText = (EditText) view.findViewById(R.id.fragment_add_dashboard_item_graph_name_edit_text);
		mLeftAxisSpinner = (Spinner) view.findViewById(R.id.fragment_add_dashboard_item_left_axis_spinner);
		mRightAxisSpinner = (Spinner) view.findViewById(R.id.fragment_add_dashboard_item_right_axis_spinner);
		mGraphRangeSpinner = (Spinner) view.findViewById(R.id.fragment_add_dashboard_item_graph_range_spinner);
		mButtonDone = (Button) view.findViewById(R.id.fragment_add_dashboard_item_button_done);

		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ArrayAdapter<SpinnerHolder> leftAxisAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_dropdown_item);
		ArrayAdapter<SpinnerHolder> rightAxisAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_dropdown_item);
		ArrayAdapter<String> graphRangeAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_dropdown_item);

		final Controller controller = Controller.getInstance(mActivity);

		List<Device> devices = controller.getDevicesModel().getDevicesByGate(mGateId);


		rightAxisAdapter.add(new SpinnerHolder(null, null));
		for (Device device : devices) {
			for (Module module : device.getVisibleModules()) {
				leftAxisAdapter.add(new SpinnerHolder(device, module));
				rightAxisAdapter.add(new SpinnerHolder(device, module));
			}
		}

		for (int range : ChartHelper.ALL_RANGES) {
			graphRangeAdapter.add(getString(ChartHelper.getIntervalString(range)));
		}

		mLeftAxisSpinner.setAdapter(leftAxisAdapter);
		mRightAxisSpinner.setAdapter(rightAxisAdapter);
		mGraphRangeSpinner.setAdapter(graphRangeAdapter);

		mButtonDone.setOnClickListener(new View.OnClickListener() {

			@Override
			@SuppressWarnings("ResourceType")
			public void onClick(View v) {
				SpinnerHolder leftItem = ((SpinnerHolder) mLeftAxisSpinner.getSelectedItem());
				SpinnerHolder rightItem = ((SpinnerHolder) mRightAxisSpinner.getSelectedItem());

				List<String> deviceIds = Arrays.asList(leftItem.getDevice().getId(), rightItem.getDevice().getId());
				List<String> moduleIds = Arrays.asList(leftItem.getModule().getId(), rightItem.getModule().getId());
				GraphItem graphItem = new GraphItem(mGraphNameEditText.getText().toString(), mGateId, deviceIds, moduleIds, ChartHelper.ALL_RANGES[mGraphRangeSpinner.getSelectedItemPosition()]);

				Intent data = new Intent();
				data.putExtra(DashboardFragment.EXTRA_ADD_ITEM, graphItem);
				mActivity.setResult(10, data);
				mActivity.finish();
			}
		});

	}


	private final class SpinnerHolder {

		private Device mDevice;
		private Module mModule;

		public SpinnerHolder(@Nullable Device device, @Nullable Module module) {
			mDevice = device;
			mModule = module;
		}

		public Device getDevice() {
			return mDevice;
		}

		public Module getModule() {
			return mModule;
		}

		@Override
		public String toString() {
			if (mDevice == null || mModule == null) {
				return "";
			}
			return String.format("%s - %s", mDevice.getName(mActivity), mModule.getName(mActivity));
		}
	}
}

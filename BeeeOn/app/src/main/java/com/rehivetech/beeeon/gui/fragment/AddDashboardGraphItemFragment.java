package com.rehivetech.beeeon.gui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.GraphItem;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.util.ChartHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by martin on 13.1.16.
 */
public class AddDashboardGraphItemFragment extends BaseAddDashBoardItemFragment {

	private Spinner mLeftAxisSpinner;
	private Spinner mRightAxisSpinner;
	private Spinner mGraphRangeSpinner;

	public static AddDashboardGraphItemFragment newInstance(String gateId) {

		Bundle args = new Bundle();
		args.putString(ARG_GATE_ID, gateId);
		AddDashboardGraphItemFragment fragment = new AddDashboardGraphItemFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.fragment_add_dashboard_graph_item, container, false);

		mLeftAxisSpinner = (Spinner) view.findViewById(R.id.fragment_add_dashboard_item_left_axis_spinner);
		mRightAxisSpinner = (Spinner) view.findViewById(R.id.fragment_add_dashboard_item_right_axis_spinner);
		mGraphRangeSpinner = (Spinner) view.findViewById(R.id.fragment_add_dashboard_item_graph_range_spinner);

		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final Controller controller = Controller.getInstance(mActivity);
		List<Device> devices = controller.getDevicesModel().getDevicesByGate(mGateId);

		ArrayAdapter<SpinnerHolder> leftAxisAdapter = createModulesAdapter(mActivity, android.R.layout.simple_spinner_dropdown_item, devices, false);
		ArrayAdapter<SpinnerHolder> rightAxisAdapter = createModulesAdapter(mActivity, android.R.layout.simple_spinner_dropdown_item, devices, true);
		ArrayAdapter<String> graphRangeAdapter = createGraphRangeAdapter(mActivity, android.R.layout.simple_spinner_dropdown_item);

		mLeftAxisSpinner.setAdapter(leftAxisAdapter);
		mRightAxisSpinner.setAdapter(rightAxisAdapter);
		mGraphRangeSpinner.setAdapter(graphRangeAdapter);

		mButtonDone.setOnClickListener(new View.OnClickListener() {

			@Override
			@SuppressWarnings("ResourceType")
			public void onClick(View v) {

				if (mItemNameEditText.getText().length() == 0) {
					mTextInputLayout.setError(getString(R.string.dashboard_add_graph_name_error));
					return;
				}

				SpinnerHolder leftItem = ((SpinnerHolder) mLeftAxisSpinner.getSelectedItem());
				SpinnerHolder rightItem = ((SpinnerHolder) mRightAxisSpinner.getSelectedItem());

				List<String> deviceIds;
				List<String> moduleIds;

				if (rightItem.getDevice() == null) {
					deviceIds = Collections.singletonList(leftItem.getDevice().getId());
					moduleIds = Collections.singletonList(leftItem.getModule().getId());
				} else {
					deviceIds = Arrays.asList(leftItem.getDevice().getId(), rightItem.getDevice().getId());
					moduleIds = Arrays.asList(leftItem.getModule().getId(), rightItem.getModule().getId());
				}

				GraphItem graphItem = new GraphItem(mItemNameEditText.getText().toString(), mGateId, deviceIds, moduleIds, ChartHelper.ALL_RANGES[mGraphRangeSpinner.getSelectedItemPosition()]);

				Intent data = new Intent();
				data.putExtra(DashboardFragment.EXTRA_ADD_ITEM, graphItem);
				mActivity.setResult(10, data);
				mActivity.finish();
			}
		});

	}


}

package com.rehivetech.beeeon.gui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.dashboard.AddDashboardCardAdapter;
import com.rehivetech.beeeon.gui.adapter.dashboard.DashboardModuleSelectAdapter;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.GraphItem;
import com.rehivetech.beeeon.gui.view.Slider;
import com.rehivetech.beeeon.util.ChartHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by martin on 13.1.16.
 */
public class AddDashboardGraphItemFragment extends BaseAddDashBoardItemFragment implements AddDashboardCardAdapter.ItemClickListener {

	private static final String ARG_LEFT_AXIS_MODULE = "left_axis_module";

	private DashboardModuleSelectAdapter.ModuleItem mLeftAxisModule;
	private Slider mSlider;
	private TextView mTitle;

	public static AddDashboardGraphItemFragment newInstance(String gateId, DashboardModuleSelectAdapter.ModuleItem item) {

		Bundle args = new Bundle();
		args.putString(ARG_GATE_ID, gateId);
		args.putParcelable(ARG_LEFT_AXIS_MODULE, item);
		AddDashboardGraphItemFragment fragment = new AddDashboardGraphItemFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();

		mLeftAxisModule = args.getParcelable(ARG_LEFT_AXIS_MODULE);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		return inflater.inflate(R.layout.fragment_add_dashboard_graph_item, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mTitle = (TextView) view.findViewById(R.id.fragment_add_dashboard_item_title);

		fillAdapter(false);

		if (mLeftAxisModule == null) {
			mAdapter.selectFirstModuleItem();
			mButtonDone.setImageResource(R.drawable.arrow_right_bold);
			view.findViewById(R.id.fragment_add_dashboard_item_slider_card).setVisibility(View.GONE);
			mTitle.setText(R.string.dashboard_add_graph_left_axis_label);
		} else {
			mTitle.setText(R.string.dashboard_add_graph_right_axis_label);
			mSlider = (Slider) view.findViewById(R.id.fragment_add_dashboard_item_graph_range);

			List<String> values =getGraphRangeStrings();
			mSlider.setValues(values);
			mSlider.setMaxValue(values.size() - 1);
		}

		mButtonDone.setOnClickListener(new View.OnClickListener() {

			@Override
			@SuppressWarnings("ResourceType")
			public void onClick(View v) {

				DashboardModuleSelectAdapter.ModuleItem moduleItem = null;
				int selectedItem = mAdapter.getFirstSelectedItem();

				if (selectedItem != 0) {
					moduleItem = (DashboardModuleSelectAdapter.ModuleItem) mAdapter.getItem(selectedItem);
				}

				if (mLeftAxisModule == null && moduleItem != null) {
					AddDashboardGraphItemFragment fragment = AddDashboardGraphItemFragment.newInstance(mGateId, moduleItem);
					mActivity.replaceFragment(getTag(), fragment);

				} else {
					Controller controller = Controller.getInstance(mActivity);

					String leftModuleName = controller.getDevicesModel().getModule(mGateId, mLeftAxisModule.getAbsoluteId()).getName(mActivity, true);

					String cardName;
					List<String> moduleIds;
					if (moduleItem != null) {
						String rightModuleName = controller.getDevicesModel().getModule(mGateId, moduleItem.getAbsoluteId()).getName(mActivity, true);
						cardName = String.format("%s + %s", leftModuleName, rightModuleName);
						moduleIds = Arrays.asList(mLeftAxisModule.getAbsoluteId(), moduleItem.getAbsoluteId());

					} else {
						cardName = leftModuleName;
						moduleIds = Collections.singletonList(mLeftAxisModule.getAbsoluteId());
					}

					GraphItem item = new GraphItem(cardName, mGateId, moduleIds, ChartHelper.ALL_RANGES[mSlider.getProgress()]);

					Intent data = new Intent();
					data.putExtra(DashboardFragment.EXTRA_ADD_ITEM, item);
					mActivity.setResult(10, data);
					mActivity.finish();
				}
			}
		});

	}

	private List<String> getGraphRangeStrings() {
		List<String> ranges = new ArrayList<>();

		for (int range : ChartHelper.ALL_RANGES) {
			ranges.add(getString(ChartHelper.getIntervalString(range)));
		}

		return ranges;
	}

	@Override
	public void onItemClick(@AddDashboardCardAdapter.CardItem.CardType int type) {

	}
}

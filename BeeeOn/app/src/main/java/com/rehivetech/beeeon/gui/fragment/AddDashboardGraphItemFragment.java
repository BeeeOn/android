package com.rehivetech.beeeon.gui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gcm.analytics.GoogleAnalyticsManager;
import com.rehivetech.beeeon.gui.adapter.dashboard.AddDashboardCardAdapter;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.GraphItem;
import com.rehivetech.beeeon.gui.view.Slider;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.util.ChartHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.rehivetech.beeeon.gui.adapter.dashboard.DashboardModuleSelectAdapter.ModuleItem;

/**
 * Created by martin on 13.1.16.
 */
public class AddDashboardGraphItemFragment extends BaseAddDashBoardItemFragment implements AddDashboardCardAdapter.ItemClickListener {

	private static final String ARG_LEFT_AXIS_MODULE = "left_axis_module";
	private static final String ARG_RIGHT_AXIS_MODULE = "right_axis_module";

	private ModuleItem mLeftAxisModule;
	private ModuleItem mRightAxisModule;
	@Bind(R.id.fragment_add_dashboard_item_title)
	TextView mTitle;

	public static AddDashboardGraphItemFragment newInstance(int index, String gateId, ModuleItem leftAxisModule, ModuleItem rightAxisModule) {

		Bundle args = new Bundle();
		fillBaseArgs(args, index, gateId);
		args.putParcelable(ARG_LEFT_AXIS_MODULE, leftAxisModule);
		args.putParcelable(ARG_RIGHT_AXIS_MODULE, rightAxisModule);
		AddDashboardGraphItemFragment fragment = new AddDashboardGraphItemFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();

		mLeftAxisModule = args.getParcelable(ARG_LEFT_AXIS_MODULE);
		mRightAxisModule = args.getParcelable(ARG_RIGHT_AXIS_MODULE);
	}

	@SuppressLint("InflateParams")
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		FrameLayout rootView = (FrameLayout) inflater.inflate(R.layout.fragment_add_dashboard_graph_item, container, false);

		View view;

		if (mLeftAxisModule == null || mRightAxisModule == null) {
			view = LayoutInflater.from(mActivity).inflate(R.layout.add_dashboard_recyclerview_item_layout1, null);
		} else {
			view = LayoutInflater.from(mActivity).inflate(R.layout.add_dashboard_graph_item_layout2, null);
		}

		rootView.addView(view, 0);
		ButterKnife.bind(rootView);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

		if (mLeftAxisModule == null || mRightAxisModule == null) {
			super.onViewCreated(view, savedInstanceState);
			fillAdapter(false, null);
			mButtonDone.setImageResource(R.drawable.arrow_right_bold);
		}

		if (mLeftAxisModule == null) {
			mAdapter.selectFirstModuleItem();
			mTitle.setText(R.string.dashboard_add_graph_left_axis_label);

			mButtonDone.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int selectedItem = mAdapter.getFirstSelectedItem();
					ModuleItem moduleItem = (ModuleItem) mAdapter.getItem(selectedItem);

					AddDashboardGraphItemFragment fragment = AddDashboardGraphItemFragment.newInstance(mIndex, mGateId, moduleItem, null);
					mActivity.replaceFragment(getTag(), fragment);
				}
			});
		} else if (mRightAxisModule == null) {
			mTitle.setText(R.string.dashboard_add_graph_right_axis_label);

			mButtonDone.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int selectedItem = mAdapter.getFirstSelectedItem();

					ModuleItem moduleItem = ModuleItem.getEmpty();
					if (selectedItem != 0) {
						moduleItem = (ModuleItem) mAdapter.getItem(selectedItem);
					}

					AddDashboardGraphItemFragment fragment = AddDashboardGraphItemFragment.newInstance(mIndex, mGateId, mLeftAxisModule, moduleItem);
					mActivity.replaceFragment(getTag(), fragment);
				}
			});

		} else {

			final Slider slider = ButterKnife.findById(view, R.id.fragment_add_dashboard_item_graph_range);
			final EditText editText = ButterKnife.findById(view, R.id.fragment_add_dashboard_item_name_edit);
			mButtonDone = ButterKnife.findById(view, R.id.fragment_add_dashboard_item_button_done);

			List<String> values = getGraphRangeStrings();
			slider.setValues(values);

			Controller controller = Controller.getInstance(mActivity);
			Module leftModule = controller.getDevicesModel().getModule(mGateId, mLeftAxisModule.getAbsoluteId());
			String leftModuleName = "";
			String rightModuleName = "";
			if (leftModule != null) {
				leftModuleName = leftModule.getName(mActivity, true);
			}

			Module rightModule = null;
			if (!mRightAxisModule.isEmpty()) {
				rightModule = controller.getDevicesModel().getModule(mGateId, mRightAxisModule.getAbsoluteId());

			}
			if (rightModule != null) {
				rightModuleName = rightModule.getName(mActivity, true);
			}

			String cardName;
			if (!rightModuleName.isEmpty()) {
				cardName = String.format("%s + %s", leftModuleName, rightModuleName);
			} else {
				cardName = leftModuleName;
			}

			editText.setText(cardName);

			mButtonDone.setOnClickListener(new View.OnClickListener() {

				@Override
				@SuppressWarnings("ResourceType")
				public void onClick(View v) {
					List<String> moduleIds;
					if (mRightAxisModule != null && !mRightAxisModule.isEmpty()) {
						moduleIds = Arrays.asList(mLeftAxisModule.getAbsoluteId(), mRightAxisModule.getAbsoluteId());

					} else {
						moduleIds = Collections.singletonList(mLeftAxisModule.getAbsoluteId());
					}

					GraphItem item = new GraphItem(editText.getText().toString(), mGateId, moduleIds, ChartHelper.ALL_RANGES[slider.getProgress()]);
					finishActivity(item);
				}
			});
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		GoogleAnalyticsManager.getInstance().logScreen(GoogleAnalyticsManager.ADD_DASHBOARD_GRAPH_ITEM_SCREEN);
	}

	private List<String> getGraphRangeStrings() {
		List<String> ranges = new ArrayList<>();

		for (int range : ChartHelper.ALL_RANGES) {
			ranges.add(getString(ChartHelper.getIntervalString(range)));
		}

		return ranges;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.unbind(this);
	}

	@Override
	public void onItemClick(@AddDashboardCardAdapter.CardItem.CardType int type) {

	}
}
